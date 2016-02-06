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

import com.oracle.json.Json;
import com.oracle.json.JsonArray;
import com.oracle.json.JsonException;
import com.oracle.json.JsonNumber;
import com.oracle.json.JsonObject;
import com.oracle.json.JsonObjectBuilder;
import com.oracle.json.JsonReader;
import com.oracle.json.JsonString;
import com.oracle.json.JsonValue;
import com.oracle.json.JsonWriter;

import java.io.StringReader;
import java.io.StringWriter;
import java.util.Arrays;
import com.oracle.iot.message.Base64;
import java.util.HashMap;
import java.util.Map;

/**
 * DirectActivationRequest
 */
public class DirectActivationRequest {

    public static final String FIELD_CERTIFICATION_REQUEST_INFO = "certificationRequestInfo";
    public static final String FIELD_SIGNATURE_ALGORITHM = "signatureAlgorithm";
    public static final String FIELD_SIGNATURE = "signature";

    public static final String PUBLIC_KEY_ENCODING_FORMAT_X509 = "X.509";

    public static class SubjectPublicKeyInfo {
        public static final String FIELD_ALGORITHM = "algorithm";
        public static final String FIELD_PUBLIC_KEY = "publicKey";
        public static final String FIELD_FORMAT = "format";
        public static final String FIELD_SECRET_HASH_ALGORITHM = "secretHashAlgorithm";

        private String algorithm;
        private byte[] publicKey;
        private String format = PUBLIC_KEY_ENCODING_FORMAT_X509;
        private String secretHashAlgorithm;

        public String getAlgorithm() {
            return algorithm;
        }

        public void setAlgorithm(String algorithm) {
            this.algorithm = algorithm;
        }

        public byte[] getPublicKey() {
            return publicKey;
        }

        public void setPublicKey(byte[] subjectPublicKey) {
            this.publicKey = subjectPublicKey;
        }

        public String getFormat() {
            return format;
        }

        public void setFormat(String format) {
            this.format = format;
        }

        public String getSecretHashAlgorithm() {
            return secretHashAlgorithm;
        }

        public void setSecretHashAlgorithm(String secretHashAlgorithm) {
            this.secretHashAlgorithm = secretHashAlgorithm;
        }

        public JsonObjectBuilder toJson() {
            final JsonObjectBuilder objectBuilder = Json.createObjectBuilder();

            objectBuilder.add(FIELD_ALGORITHM, algorithm);
            objectBuilder.add(FIELD_PUBLIC_KEY,
                    Base64.getEncoder().encodeToString(publicKey));
            objectBuilder.add(FIELD_FORMAT, format.toString());
            objectBuilder.add(FIELD_SECRET_HASH_ALGORITHM,
                    secretHashAlgorithm.toString());

            return objectBuilder;
        }

        public void fromJson(final JsonObject jsonObject) {
            if (jsonObject != null) {
                setFormat(jsonObject.get(FIELD_FORMAT).toString());
                setAlgorithm(jsonObject.get(FIELD_ALGORITHM).toString());

                final String encodedPublicKey =
                        ((JsonString) jsonObject.get(FIELD_PUBLIC_KEY)).getString();
                setPublicKey(Base64.getDecoder().decode(encodedPublicKey));
                setSecretHashAlgorithm(
                        jsonObject.get(FIELD_SECRET_HASH_ALGORITHM).toString());
            }
        }

        @Override
        public String toString() {
            return "SubjectPublicKeyInfo{" +
                    "algorithm='" + algorithm + '\'' +
                    ", publicKey=" + Arrays.toString(publicKey) +
                    ", format='" + format + '\'' +
                    ", secretHashAlgorithm='" + secretHashAlgorithm + '\'' +
                    '}';
        }

        public boolean isValid() {
            return this.algorithm != null && !this.algorithm.isEmpty() &&
                    this.format != null && !this.format.isEmpty() &&
                    this.secretHashAlgorithm != null &&
                    !this.secretHashAlgorithm.isEmpty() &&
                    this.publicKey != null && this.publicKey.length > 0;
        }
    }

    public static class CertificationRequestInfo {
        public static final String FIELD_SUBJECT = "subject";
        public static final String FIELD_SUBJECT_PUBLIC_KEY_INFO = "subjectPublicKeyInfo";
        public static final String FIELD_ATTRIBUTES = "attributes";
        private String subject;
        private SubjectPublicKeyInfo subjectPublicKeyInfo;
        private Map<String, Object> attributes;

        public String getSubject() {
            return subject;
        }

        public void setSubject(String subject) {
            this.subject = subject;
        }

        public Map<String, Object> getAttributes() {
            return attributes;
        }

        public void setAttributes(Map<String, Object> attributes) {
            this.attributes = attributes;
        }


        public SubjectPublicKeyInfo getSubjectPublicKeyInfo() {
            return subjectPublicKeyInfo;
        }

        public void setSubjectPublicKeyInfo(SubjectPublicKeyInfo subjectPublicKeyInfo) {
            this.subjectPublicKeyInfo = subjectPublicKeyInfo;
        }

        @Override
        public String toString() {
            return "CertificationRequestInfo{" +
                    "subject='" + subject + '\'' +
                    ", subjectPublicKeyInfo=" + subjectPublicKeyInfo +
                    ", attributes=" + attributes +
                    '}';
        }

        public JsonObjectBuilder toJson() {

            final JsonObjectBuilder objectBuilder = Json.createObjectBuilder();

            objectBuilder.add(FIELD_SUBJECT, subject);
            if (subjectPublicKeyInfo != null) {
                objectBuilder.add(FIELD_SUBJECT_PUBLIC_KEY_INFO, subjectPublicKeyInfo.toJson());
            }

            JsonObjectBuilder items = Json.createObjectBuilder();
            if (attributes != null) {
                addEntries(attributes, items);
            }

            objectBuilder.add(FIELD_ATTRIBUTES, items);

            return objectBuilder;
        }


        // TODO: create a JsonUtils class and move all the repeated Json actions there

        /**
         * Adds each entry from the <i>map</i> to the <i>items</i> object.
         * @param map
         * @param items
         */
        private static void addEntries(Map<String, ? extends Object> map, JsonObjectBuilder items) {
            for (Map.Entry<String, ? extends Object> entry : map.entrySet()) {
                String key = entry.getKey();
                Object value = entry.getValue();
                if (value instanceof Number)
                    items.add(key, ((Number) value).longValue());
                else if (value instanceof Boolean)
                    items.add(key, (Boolean) value);
                else if (value instanceof JsonArray)
                    items.add(key, (JsonArray) value);
                else if (value instanceof JsonObject)
                    items.add(key, (JsonObject) value);
                else
                    items.add(key, value.toString());
            }
        }

        public void fromJson(JsonObject jsonObject) {
            if (jsonObject != null) {
                setSubject(jsonObject.get(FIELD_SUBJECT).toString());
                subjectPublicKeyInfo = new SubjectPublicKeyInfo();
                final JsonObject subjectPublicKeyInfoJsonObj =
                        jsonObject.getJsonObject(FIELD_SUBJECT_PUBLIC_KEY_INFO);
                if (subjectPublicKeyInfoJsonObj != null) {
                    subjectPublicKeyInfo.fromJson(subjectPublicKeyInfoJsonObj);
                }
                setAttributes(getJsonMap(jsonObject.get(FIELD_ATTRIBUTES), FIELD_ATTRIBUTES));
            }
        }

        // TODO: taken from server JsonUtils, refactor!
        private static Map<String, Object> getJsonMap(JsonValue value, String name) {
            if (value == JsonValue.NULL || value == null)
                return null;
            if (!(value instanceof JsonObject))
                throw new IllegalArgumentException("expected '" + name +
                        "' to be a map");
            JsonValue items = ((JsonObject) value).get("items");
            if (items == null) {
                if (((JsonObject) value).get("links") == null)
                    items = value;
                else
                    return new HashMap<String, Object>();
            }
            if (!(items instanceof JsonObject))
                throw new IllegalArgumentException("expected '" + name +
                        ".items' to be a map, but found " + items);
            Map<String, Object> result = new HashMap<String,Object>();
            for (Map.Entry<String, JsonValue> e : ((JsonObject) items).entrySet()) {
                JsonValue raw = e.getValue();
                Object v;
                switch (raw.getValueType()) {
                    case NUMBER:
                        v = ((JsonNumber) raw).longValue();
                        break;
                    case STRING:
                        v = ((JsonString) raw).getString();
                        break;
                    case TRUE:
                        v = true;
                        break;
                    case FALSE:
                        v = false;
                        break;
                    case NULL:
                        v = null;
                        break;
                    default:
                        throw new IllegalArgumentException(
                                "unsupported map value '" + raw + "'");
                }
                result.put(e.getKey(), v);
            }
            return result;
        }


        public boolean isValid() {
            return this.subject != null && !this.subject.isEmpty() &&
                    this.subjectPublicKeyInfo != null && this.subjectPublicKeyInfo.isValid();
        }
    }

    private CertificationRequestInfo certificationRequestInfo;
    private String signatureAlgorithm;
    private byte[] signature;

    public CertificationRequestInfo getCertificationRequestInfo() {
        return certificationRequestInfo;
    }

    public void setCertificationRequestInfo(CertificationRequestInfo certificationRequestInfo) {
        this.certificationRequestInfo = certificationRequestInfo;
    }

    public String getSignatureAlgorithm() {
        return signatureAlgorithm;
    }

    public void setSignatureAlgorithm(String signatureAlgorithm) {
        this.signatureAlgorithm = signatureAlgorithm;
    }

    public byte[] getSignature() {
        return signature;
    }

    public void setSignature(byte[] signature) {
        this.signature = signature;
    }

    public static DirectActivationRequest fromJson(final String jsonString) {
        JsonReader reader = Json.createReader(new StringReader(jsonString));
        DirectActivationRequest request = new DirectActivationRequest();
        try {
            JsonObject jsonObject = reader.readObject();
            if (jsonObject != null)
                request.fromJson(jsonObject);
        } catch (JsonException ex) {
            // TODO
        }
        return request;
    }

    public void fromJson(final JsonObject jsonObject) {
        certificationRequestInfo = new CertificationRequestInfo();

        final JsonObject fieldJsonObject = jsonObject.getJsonObject(FIELD_CERTIFICATION_REQUEST_INFO);
        if (fieldJsonObject != null) {
            certificationRequestInfo.fromJson(fieldJsonObject);
        }
        setSignatureAlgorithm(jsonObject.get(FIELD_SIGNATURE_ALGORITHM).toString());

        final String encodedSignature = ((JsonString) jsonObject.get(FIELD_SIGNATURE)).getString();
        setSignature(Base64.getDecoder().decode(encodedSignature));
    }

    public String toJson() {

        final StringWriter stringWriter = new StringWriter();
        final JsonWriter jsonWriter = Json.createWriter(stringWriter);
        final JsonObjectBuilder objectBuilder = Json.createObjectBuilder();

        if (certificationRequestInfo != null) {
            objectBuilder.add(FIELD_CERTIFICATION_REQUEST_INFO, certificationRequestInfo.toJson());
        }
        objectBuilder.add(FIELD_SIGNATURE_ALGORITHM, signatureAlgorithm);
        objectBuilder.add(FIELD_SIGNATURE, Base64.getEncoder().encodeToString(signature));

        jsonWriter.writeObject(objectBuilder.build());
        jsonWriter.close();

        return stringWriter.toString();
    }

    public boolean isValid() {
        return this.signature != null && this.signature.length > 0 &&
                this.signatureAlgorithm != null && !this.signatureAlgorithm.isEmpty() &&
                this.certificationRequestInfo != null && this.certificationRequestInfo.isValid();
    }

    @Override
    public String toString() {
        return "DirectActivationRequest{" +
                "certificationRequestInfo=" + certificationRequestInfo +
                ", signatureAlgorithm='" + signatureAlgorithm + '\'' +
                ", signature=" + Arrays.toString(signature) +
                '}';
    }
}
