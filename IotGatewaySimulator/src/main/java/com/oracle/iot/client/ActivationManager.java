/*
 * Copyright (c) 2015, Oracle and/or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the LICENSE file that accompanied this code.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Please contact Oracle, 500 Oracle Parkway, Redwood Shores, CA 94065 USA
 * or visit www.oracle.com if you need additional information or have any
 * questions.
 */

package com.oracle.iot.client;

import com.oracle.iot.message.Base64;
import com.oracle.json.Json;
import com.oracle.json.JsonObject;
import com.oracle.json.JsonReader;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.security.GeneralSecurityException;
import java.security.InvalidKeyException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;
import java.security.SignatureException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.security.spec.InvalidKeySpecException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

/**
 * ActivationManager handles client activation
 */
class ActivationManager {

    private static final Charset UTF_8 = Charset.forName("UTF-8");
    private static final String DEFAULT_MESSAGE_DIGEST_ALGORITHM ="HmacSHA256";


    static byte[] directActivation(ConnectionManager connectionManager, String endpointId, String sharedSecret)
            throws IOException, GeneralSecurityException {

        final ActivationPolicyResponse activationPolicyResponse =
                getActivationPolicy(connectionManager, endpointId);

        Logger.getAnonymousLogger().info("activationPolicyResponse: " + String.valueOf(activationPolicyResponse));

        final String algorithm = activationPolicyResponse.getKeyType();
        final int keySize = activationPolicyResponse.getKeySize();
        final String signatureAlgorithm = activationPolicyResponse.getHashAlgorithm();

        final KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance(algorithm);
        keyPairGenerator.initialize(keySize);
        final KeyPair keyPair = keyPairGenerator.genKeyPair();

        final DirectActivationRequest directActivationRequest =
                createDirectActivationRequest(endpointId, sharedSecret, keyPair, signatureAlgorithm);
        Logger.getAnonymousLogger().info("directActivationRequest: " + directActivationRequest.toString());

        final DirectActivationResponse directActivationResponse =
                postDirectActivationRequest(connectionManager, directActivationRequest, endpointId);

        Logger.getAnonymousLogger().info("directActivationResponse: Endpoint state is: " + directActivationResponse.getEndpointState());

        return keyPair.getPrivate().getEncoded();
    }



    ///////////////////////////////////////////////////////////////////////////
    //
    // Routines for direct activation via /iot/api/v1//activation/policy
    // and /iot/api/v1//activation/direct
    //
    ///////////////////////////////////////////////////////////////////////////

    private static ActivationPolicyResponse getActivationPolicy(ConnectionManager connectionManager, String endpointId)
            throws IOException, GeneralSecurityException {

        final Map<String, String> headers = new HashMap<String, String>();
        headers.put("X-ActivationId", endpointId);
        headers.put("Content-Type", "application/json");
        headers.put("Accept", "application/json");

        final ActivationPolicyRequest policyRequest = createActivationPolicyRequest();
        final String payloadString = policyRequest.toJson();
        final byte[] payload = payloadString.getBytes(UTF_8);

        final HttpClient.HttpResponse response = connectionManager.post("/iot/api/v1/activation/policy", payload, headers);
        int status = response.getStatus();
        if (status == 401) {
            // Assume the endpoint is already activated.
            throw new IllegalStateException(endpointId);
        }
        if (status != 200) {
            throw new IOException("HTTP " + status);
        }

        JsonReader reader = null;
        try {
            ByteArrayInputStream is = new ByteArrayInputStream(response.getData());
            reader = Json.createReader(is);
            JsonObject json = reader.readObject();
            ActivationPolicyResponse activationPolicyResponse = ActivationPolicyResponse.fromJson(json);
            Logger.getAnonymousLogger().info(activationPolicyResponse.toString());
            return activationPolicyResponse;
        } finally {
            if (reader != null) {
                reader.close();
            }
        }
    }

    private static ActivationPolicyRequest createActivationPolicyRequest() {
        final ActivationPolicyRequest policyRequest = new ActivationPolicyRequest();
        policyRequest.setDeviceAttributes(new ActivationPolicyRequest.DeviceAttributes());

        final String osName = System.getProperty("os.name");
        policyRequest.getDeviceAttributes().setOsName(osName);

        final String osVersion = System.getProperty("os.version");
        policyRequest.getDeviceAttributes().setOsVersion(osVersion);

        return policyRequest;
    }

    private static DirectActivationRequest createDirectActivationRequest(final String endpointId,
                                                                         final String sharedSecret,
                                                                         final KeyPair keyPair,
                                                                         final String signatureAlgorithm) {

        final DirectActivationRequest.SubjectPublicKeyInfo subjectPublicKeyInfo =
                new DirectActivationRequest.SubjectPublicKeyInfo();

        final DirectActivationRequest.CertificationRequestInfo certificationRequestInfo =
                new DirectActivationRequest.CertificationRequestInfo();
        certificationRequestInfo.setSubjectPublicKeyInfo(subjectPublicKeyInfo);
        certificationRequestInfo.setSubject(endpointId);

        final DirectActivationRequest request = new DirectActivationRequest();
        request.setCertificationRequestInfo(certificationRequestInfo);

        try {
            signRequest(request, sharedSecret, keyPair, signatureAlgorithm);
        } catch (NoSuchAlgorithmException e) {
            Logger.getAnonymousLogger().info(e.toString());
        } catch (InvalidKeyException e) {
            Logger.getAnonymousLogger().info(e.toString());
        } catch (InvalidKeySpecException e) {
            Logger.getAnonymousLogger().info(e.toString());
        } catch (SignatureException e) {
            Logger.getAnonymousLogger().info(e.toString());
        }

        return request;
    }

    private static DirectActivationResponse postDirectActivationRequest(
            ConnectionManager connectionManager,
            DirectActivationRequest directActivationRequest,
            String endpointId)
            throws IOException, GeneralSecurityException {

        final String payloadString = directActivationRequest.toJson();
        final byte[] payload = payloadString.getBytes(UTF_8);

        // Send public key here...
        final Map<String, String> headers = new HashMap<String, String>();
        headers.put("X-ActivationId", endpointId);
        headers.put("Content-Type", "application/json");
        headers.put("Accept", "application/json");

        final HttpClient.HttpResponse response =
                connectionManager.post("/iot/api/v1/activation/direct", payload, headers);
        int status = response.getStatus();
        if (status == 401) {
            throw new IllegalStateException("endpoint already activated");
        }
        if (status != 200) {
            throw new IOException("HTTP " + status);
        }

        JsonReader reader = null;
        try {
            ByteArrayInputStream is = new ByteArrayInputStream(response.getData());
            reader = Json.createReader(is);
            JsonObject json = reader.readObject();
            DirectActivationResponse directActivationResponse = DirectActivationResponse.fromJson(json);
            Logger.getAnonymousLogger().info(directActivationResponse.toString());
            return directActivationResponse;
        } finally {
            if (reader != null) {
                reader.close();
            }
        }

    }

    static void signRequest(
            final DirectActivationRequest directActivationRequest,
            final String sharedSecret, final KeyPair keyPair,
            final String signatureAlgorithm)
            throws NoSuchAlgorithmException, InvalidKeyException,
            SignatureException, InvalidKeySpecException {

        DirectActivationRequest.CertificationRequestInfo
                certificationRequestInfo =
                directActivationRequest.getCertificationRequestInfo();
        DirectActivationRequest.SubjectPublicKeyInfo
                subjectPublicKeyInfo =
                certificationRequestInfo.getSubjectPublicKeyInfo();

        PublicKey publicKey = keyPair.getPublic();
        subjectPublicKeyInfo.setAlgorithm(publicKey.getAlgorithm());
        subjectPublicKeyInfo.setPublicKey(publicKey.getEncoded());
        subjectPublicKeyInfo.setFormat(publicKey.getFormat());
        subjectPublicKeyInfo.setSecretHashAlgorithm(
                DEFAULT_MESSAGE_DIGEST_ALGORITHM);

        byte[] secretHash;
        try {
            secretHash = createDigest(subjectPublicKeyInfo.getSecretHashAlgorithm(),
                    certificationRequestInfo.getSubject(), sharedSecret);
        } catch (UnsupportedEncodingException uee) {
            throw new SignatureException(uee);
        }

        byte[] signature = signSignature(
                getSignaturePayload(certificationRequestInfo, secretHash),
                signatureAlgorithm,
                keyPair.getPrivate());

        directActivationRequest.setSignatureAlgorithm(signatureAlgorithm);
        directActivationRequest.setSignature(signature);
    }

    static byte[] getSignaturePayload(
            DirectActivationRequest.CertificationRequestInfo requestInfo,
            byte[] secretHash) {
        DirectActivationRequest.SubjectPublicKeyInfo subjectPublicKeyInfo =
                requestInfo.getSubjectPublicKeyInfo();
        String payload = requestInfo.getSubject() + "\n" +
                subjectPublicKeyInfo.getAlgorithm() + "\n" +
                subjectPublicKeyInfo.getFormat() + "\n" +
                subjectPublicKeyInfo.getSecretHashAlgorithm() + "\n";
        Map<String,Object> attributes = requestInfo.getAttributes();
        if ( attributes != null ){
            for ( String attributeKey : attributes.keySet()) {
                String attributeValue = attributes.get(attributeKey).toString();
                if ( attributeValue != null )
                    attributeValue = "\'"+attributeValue+"\'";
                else
                    attributeValue = "null";
                payload += (attributeKey + "=" + attributeValue+"\n");
            }
        }

        byte[] payloadBytes = payload.getBytes(UTF_8);
        byte[] signatureBytes = new byte[payloadBytes.length+
                secretHash.length+
                subjectPublicKeyInfo.getPublicKey().length];
        System.arraycopy(payloadBytes, 0,
                signatureBytes, 0,
                payloadBytes.length);
        System.arraycopy(secretHash, 0,
                signatureBytes, payloadBytes.length,
                secretHash.length);
        System.arraycopy(subjectPublicKeyInfo.getPublicKey(), 0,
                signatureBytes, payloadBytes.length+secretHash.length,
                subjectPublicKeyInfo.getPublicKey().length);

        return signatureBytes;
    }

    private static byte[] signSignature(final byte[] signaturePayload,
                                        final String signatureAlgorithm,
                                        final PrivateKey privateKey)
            throws NoSuchAlgorithmException, InvalidKeyException, SignatureException {

        final Signature signature = Signature.getInstance(signatureAlgorithm);
        signature.initSign(privateKey);
        signature.update(signaturePayload);
        return signature.sign();
    }

    private static byte[] createDigest(String algorithm, String clientId, String sharedSecret) throws
            NoSuchAlgorithmException, InvalidKeyException, UnsupportedEncodingException {

        final byte[] content = (clientId + "\n" + sharedSecret).getBytes(UTF_8);
        byte[] digest;
        if (algorithm.startsWith("Hmac")) {
            // computes RFC 2104-compliant HMAC signature.
            final SecretKeySpec key = new SecretKeySpec(sharedSecret.getBytes(UTF_8), algorithm);
            final Mac md = Mac.getInstance(algorithm);
            md.init(key);
            md.update(content);
            digest = md.doFinal();
        }
        else {
            final MessageDigest md = MessageDigest.getInstance(algorithm);
            md.update(content);
            digest = md.digest();
        }
        return digest;
    }

}
