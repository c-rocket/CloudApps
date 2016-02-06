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
import oracle.iot.client.Client;
import oracle.iot.client.ClientException;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URL;
import java.nio.charset.Charset;
import java.security.GeneralSecurityException;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.Signature;
import java.security.SignatureException;
import java.security.spec.KeySpec;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

/**
 * TrustManager is a singleton that handles OAuth and access token
 */
public class TrustManager {
    private final static Map<String, TrustManager> mappings = new HashMap<>() ;
    public static TrustManager getInstance(Client client) {
        if (client == null) throw new IllegalArgumentException("client cannot be null");
        TrustManager tm = mappings.get(client.getEndpointId()) ;
        if (tm == null) {
            tm = new TrustManager(client) ;
            mappings.put(client.getEndpointId(), tm) ;
        }
        return tm ;
    }
    // this is a new function, to enable the connection manager / trust manager mappings
    public String getEndpointId() {
        return endpointId ;
    }
    /**
     * Get an instance of TrustManager for the given Client. Only one
     * TrustManager instance is allowed. This may or may not change in the
     * future.
     *
     * @return an instance of TrustManager for the client
     * @throws IllegalArgumentException if the client does not match the instance
     */
    /*public static TrustManager getInstance(Client client) {
        if (client == null) throw new IllegalArgumentException("client cannot be null");
        return Holder.INSTANCE.getTrustManager(client);
    }

    private enum Holder {
        INSTANCE;

        private TrustManager trustManager;
        private synchronized TrustManager getTrustManager(Client client) {

            if (trustManager == null) {
                trustManager = new TrustManager(client);
            } else if (!trustManager.server.equals(client.getServer()) ||
                        trustManager.port != client.getPort() ||
                       !trustManager.endpointId.equals(client.getEndpointId())) {
                System.err.println(System.identityHashCode(trustManager));
                System.err.println("\t" + trustManager.server + "\t" + client.getServer());
                System.err.println("\t" + trustManager.port + "\t" + client.getPort());
                System.err.println("\t" + trustManager.endpointId + "\t" + client.getEndpointId());
                    throw new IllegalArgumentException("client does not match");
            }

            return trustManager;
        }

        private void close() {
            if (trustManager != null) {
                trustManager = null;
            }

        }
    }*/

    // JWT expiration claim adjustment value, in milliseconds. This is added
    // to the current millisecond time since January 1, 1970 in order to get
    // some time "not to far" in the future for the JWT "exp" claim.
    private final static long EXP_CLAIM_DELTA = 15L * 1000L * 60L; // 15 minutes

    public void authenticate(byte[] privateKey) throws ClientException {

        try {
            setClientCredentials(privateKey);
            this.accessToken = renewAccessToken();

        } catch (GeneralSecurityException e) {
            throw new ClientException(e.getMessage(), e);
        } catch (IOException e) {
            throw new ClientException(e.getMessage(), e);
        }

    }

    public byte[] activate(String sharedSecret) throws ClientException {
        try {
            setClientCredentials(sharedSecret);
            ConnectionManager connectionManager = ConnectionManager.getInstance(this);
            byte[] privateKey = ActivationManager.directActivation(connectionManager, endpointId, sharedSecret);
            authenticate(privateKey);
            return privateKey;
        } catch (GeneralSecurityException e) {
            throw new ClientException(e.getMessage(), e);
        } catch (IOException e) {
            throw new ClientException(e.getMessage(), e);
        }
    }

    public synchronized void close() {
        // remove us from the map
        mappings.remove(endpointId) ;
        // Holder.INSTANCE.close();
//        connectionManager.close();
    }

    private static final Charset UTF_8                            = Charset.forName("UTF-8");
    private static final String  DEFAULT_MESSAGE_DIGEST_ALGORITHM = "HmacSHA256";

    private final String server;
    private final int port;
    private final String endpointId;

    private AccessToken accessToken;
    private byte[] credentialsPostData;

    private TrustManager(Client client) {

        this.server = client.getServer();
        this.port = client.getPort();
        this.endpointId = client.getEndpointId();

    }
    
    String getServer() { return server; }
    int getPort() { return port; }

    void setClientCredentials(String sharedSecret) {

        if (sharedSecret != null) {
            final String symmetricKey = createSymmetricKey(endpointId, sharedSecret);
            credentialsPostData = getCredentialsPostData(endpointId, symmetricKey);
            accessToken = null;
        }

    }

    void setClientCredentials(byte[] encodedKey) {

        PrivateKey privateKey = null;
        if (encodedKey != null && (privateKey = generatePrivateKey(encodedKey)) != null) {
            credentialsPostData = getAssertionCredentialsPostData(endpointId, privateKey);
            accessToken = null;
        }

    }

    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        final TrustManager other = (TrustManager)obj;
        if (this.port != other.port) return false;
        if (this.server != null ? !this.server.equals(other.server) : other.server != null) return false;
        if (this.endpointId != null ? !this.endpointId.equals(other.endpointId) : other.endpointId != null) return false;
        return true;
    }

    public int hashCode() {
        int hash = 7;
        hash = 37 * hash + (server != null ? server.hashCode() : 0);
        hash = 37 * hash + (endpointId != null ? endpointId.hashCode() : 0);
        hash = 37 * hash + port;
        return hash;
    }

    ///////////////////////////////////////////////////////////////////////////
    //
    // Hooks for testing
    //
    ///////////////////////////////////////////////////////////////////////////

    /**
     * Hook for unit testing. Call equals method without exposing
     * TrustManager constructor. Return result of
     * new TrustManager(client1).equals(new TrustManager(client2)),
     */
    static boolean equalsTest(Client client1, Client client2) {
        final TrustManager tm1 = new TrustManager(client1);
        final TrustManager tm2 = client2 != null ? new TrustManager(client2) : null;
        return tm1.equals(tm2);
    }

    /**
     * Hook for unit testing. Call hashCode method without exposing
     * TrustManager constructor. Calculate hash of TrustManager for
     * each client and return true if one equals the other.
     */
    static boolean hashTest(Client client1, Client client2) {
        final TrustManager tm1 = new TrustManager(client1);
        final TrustManager tm2 = new TrustManager(client2);
        return tm1.hashCode() == tm2.hashCode();
    }

    ///////////////////////////////////////////////////////////////////////////
    //
    // Routines for creating AccessToken via /iot/api/v1/oauth2/token
    //
    ///////////////////////////////////////////////////////////////////////////

    AccessToken getAccessToken() throws IOException, GeneralSecurityException {
        if (accessToken == null || accessToken.hasExpired()) {
            accessToken = renewAccessToken();
        }
        return accessToken;
    }

    AccessToken renewAccessToken() throws IOException, GeneralSecurityException {

        URL url = new URL("https", server, port, "/iot/api/v1/oauth2/token");

        HashMap headers = new HashMap();
        headers.put("Content-Type", "application/x-www-form-urlencoded");
        headers.put("Accept", "application/json");

        Logger.getAnonymousLogger().info("POST /iot/api/v1/oauth2/token");

        HttpClient httpClient = new HttpClient(url);
        HttpClient.HttpResponse response = httpClient.post(credentialsPostData, headers);
        int status = response.getStatus();
        if (status != 200) {
            throw new IOException("HTTP " + status);
        }

        JsonReader reader = null;
        final byte[] data = response.getData();
        if (data == null || data.length == 0) {
            throw new IOException("empty payload");
        }

        try {
            ByteArrayInputStream is = new ByteArrayInputStream(data);
            reader = Json.createReader(is);
            JsonObject json = reader.readObject();
            AccessToken _accessToken = AccessToken.fromJSON(json);
            //Logger.getAnonymousLogger().info(accessToken.toString());
            return _accessToken;
        } finally {
            if (reader != null) {
                reader.close();
            }
        }
    }

    /*package for testing*/ static byte[] getCredentialsPostData(String endpointId, String symmetricKey) {

        Logger.getAnonymousLogger().info("Using client credentials flow");

        StringBuilder builder = new StringBuilder();
        builder.append("grant_type=client_credentials&client_id=");
        builder.append(endpointId);
        builder.append("&client_secret=");
        builder.append(symmetricKey);
        builder.append("&scope=oracle/iot/activation");
        String dataString = builder.toString();
        Logger.getAnonymousLogger().info("client_credentials: " + dataString);

        byte[] data = dataString.getBytes(UTF_8);
        return data;
    }

    /*package for testing*/ static byte[] getAssertionCredentialsPostData(String endpointId, PrivateKey privateKey) {

        Logger.getAnonymousLogger().info("Using client assertion flow");

        StringBuilder postData = new StringBuilder();
        postData.append("grant_type=client_credentials");
        postData.append("&client_assertion_type=urn%3Aietf%3Aparams%3Aoauth%3Aclient-assertion-type%3Ajwt-bearer"); // already url-encoded
        postData.append("&client_assertion=" + buildClientAssertion(endpointId, privateKey));
        postData.append("&scope=");
        String dataString = postData.toString();
        Logger.getAnonymousLogger().info("client_credentials: " + dataString);
        return dataString.getBytes(UTF_8);
    }

    /*package for testing*/ static String buildClientAssertion(String endpointId, PrivateKey privateKey) {

        // Expiration claim is in units of seconds since January 1, 1970 UTC.
        // Note that EXP_CLAIM_DELTA is in units of milliseconds.
        final long exp = (System.currentTimeMillis() + EXP_CLAIM_DELTA) / 1000L;

        final String header = "{\"typ\":\"JWT\",\"alg\":\"RS256\"}";
        final String claims = "{\"iss\":\"" + endpointId + "\"" +
                                ", \"sub\":\"" + endpointId + "\"" +
                                ", \"aud\":\"oracle/iot/oauth2/token\"" +
                                ", \"exp\":" + exp  +
                               "}";

        try {
            StringBuilder inputToSign = new StringBuilder();

            inputToSign.append(Base64.getUrlEncoder().encodeToString(header.getBytes(UTF_8)));
            inputToSign.append(".");
            inputToSign.append(Base64.getUrlEncoder().encodeToString(claims.getBytes(UTF_8)));

            byte[] bytesToSign = inputToSign.toString().getBytes(UTF_8);
            byte[] signedBytes = signSignature(bytesToSign, "SHA256withRSA", privateKey);
            String signature = Base64.getUrlEncoder().encodeToString(signedBytes);

            inputToSign.append(".");
            inputToSign.append(signature);
            return inputToSign.toString();

        } catch (NoSuchAlgorithmException e) {
            Logger.getAnonymousLogger().info(e.toString());
        } catch (InvalidKeyException e) {
            Logger.getAnonymousLogger().info(e.toString());
        } catch (SignatureException e) {
            Logger.getAnonymousLogger().info(e.toString());
        }
        return null;
    }

    /*package for testing*/ static byte[] signSignature(final byte[] signaturePayload,
                                        final String signatureAlgorithm,
                                        final PrivateKey privateKey)
            throws NoSuchAlgorithmException, InvalidKeyException, SignatureException {

        final Signature signature = Signature.getInstance(signatureAlgorithm);
        signature.initSign(privateKey);
        signature.update(signaturePayload);
        return signature.sign();
    }

    ///////////////////////////////////////////////////////////////////////////
    //
    // Routines for creating keys
    //
    ///////////////////////////////////////////////////////////////////////////

    /*package for testing*/ static String createSymmetricKey(String endpointId, String sharedSecret) {

        String key = null;
        try {
            final Charset charset = UTF_8;
            final byte[] content = (endpointId + "\n" + sharedSecret).getBytes(charset);
            final SecretKeySpec keySpec = new SecretKeySpec(sharedSecret.getBytes(charset), DEFAULT_MESSAGE_DIGEST_ALGORITHM);
            final Mac mac = Mac.getInstance(DEFAULT_MESSAGE_DIGEST_ALGORITHM); // throws NoSuchAlgorithmException
            mac.init(keySpec); // throws InvalidKeyException
            mac.update(content);
            final byte[] digest = mac.doFinal();
            final String urlEncodedDigest = Base64.getUrlEncoder().encodeToString(digest);
            key = DEFAULT_MESSAGE_DIGEST_ALGORITHM + ":" + urlEncodedDigest;
        } catch (NoSuchAlgorithmException e) {
            Logger.getAnonymousLogger().info(e.toString());
        } catch (InvalidKeyException e) {
            Logger.getAnonymousLogger().info(e.toString());
        } finally {
            return key;
        }

    }

    /*package for testing*/ static PrivateKey generatePrivateKey(byte[] encoded) {
        PrivateKey privateKey = null;
        try {
            KeySpec keySpec = new PKCS8EncodedKeySpec(encoded);
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            privateKey = keyFactory.generatePrivate(keySpec);
        } catch (Exception e) {
            Logger.getAnonymousLogger().severe(e.toString());
        }
        return privateKey;
    }

}
