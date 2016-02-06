/*
 * Copyright (c) 2014, 2015, Oracle and/or its affiliates. All rights reserved.
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

package oracle.iot.message;

import com.oracle.iot.message.Base64;
import com.oracle.json.Json;
import com.oracle.json.JsonArray;
import com.oracle.json.JsonArrayBuilder;
import com.oracle.json.JsonObject;
import com.oracle.json.JsonObjectBuilder;
import com.oracle.json.JsonValue;

import java.lang.IllegalArgumentException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/**
 * HttpResponseMessage extends Message class. It stores HTTP headers, status code
 * and URL and body. The body is in byte array. Default encoding for string body is UTF-8.
 * This class is immutable.
 */
public final class HttpResponseMessage extends Message {

    /**
     * Builder extends {@link Message.MessageBuilder} class. {@link HttpResponseMessage} class is immutable.
     * A builder is required when creating {@link HttpResponseMessage}. {@link HttpResponseMessage}
     * uses Builder design pattern.
     */
    public final static class Builder extends MessageBuilder<Builder> {

        /** HTTP response message header. Header is a name and list of values pair. */
        private Map<String, List<String>> headers = new HashMap<String, List<String>>();
        /** Status code of the HTTP response message */
        private StatusCode statusCode;
        /** URL of the HTTP response message */
        private String url;
        /** ID of the request. Can be used for pairing request-response */
        private String requestId;
        /** Body of the HTTP response message */
        private byte[] body;

        public Builder() {

        }

        /**
         * Add message header. The name cannot be {@code null}. The values can be {@code null}. The List of values
         * cannot contain {@code null}. Name and values must contain ASCII-printable characters only. Note that
         * the name of the header is always converted to lower case.
         *
         * @param name header name
         * @param values header values
         * @return Builder with updated header field.
         * @throws NullPointerException when the header name is {@code null} or if values contains {@code null} string.
         * @throws IllegalArgumentException when name or values contain non-ASCII-printable characters.
         */
        public final Builder header(String name, List<String> values) {
            Utils.checkNullValueThrowsNPE(name, "ResponseMessage: Header name");
            Utils.checkNullValuesThrowsNPE(values, "ResponseMessage: Header values");
            if (!Utils.isHttpHeaderAsciiPrintable(name, values)){
                throw new IllegalArgumentException("ResponseMessage: Header contains non-ASCII printable characters");
            }

            if (values == null) {
                values = new ArrayList<String>();
            }
            String lowerName = name.toLowerCase(Locale.ROOT);

            if (headers.containsKey(lowerName)) {
                List<String> newValues = new ArrayList<String>(this.headers.get(lowerName));
                newValues.addAll(values);
                this.headers.put(lowerName, Collections.unmodifiableList(new ArrayList<String>(newValues)));

            } else {
                this.headers.put(lowerName, Collections.unmodifiableList(new ArrayList<String>(values)));
            }
            return self();
        }

        /**
         * A convenience method for setting the "Content-Type" header to the specified value.
         *
         * @param contentType The content type to use, cannot be {@code null}. This is used literally.
         * @return Builder with updated contentType field.
         * @throws NullPointerException when contentType is null.
         */
        public final Builder contentType(String contentType) {
            Utils.checkNullValueThrowsNPE(contentType, "ResponseMessage: Content type");
            header("Content-Type", Arrays.asList(contentType));
            return self();
        }

        /**
         * Set message URL. The URL is not checked for validity.
         * 
         * @param url URL of the HTTP request.
         * @return Builder with updated URL field.
         */
        public final Builder url(String url) {
            this.url = url;
            return self();
        }

        /**
         * Set message body.
         * 
         * @param body Body of the HTTP response message, should not be {@code null}.
         * @return Builder with updated body field.
         */
        public final Builder body(byte[] body) {
            this.body = Arrays.copyOf(body, body.length);
            return self();
        }

        /**
         * Set message body using a {@link String}. The encoding for the body is UTF-8.
         *
         * @param body body of the HTTP message, should not be {@code null}.
         * @return Builder with updated body field.
         */
        public final Builder body(String body) {
            return body(body.getBytes(StandardCharsets.UTF_8));
        }

        /**
         * Set http response status code.
         * 
         * @param statusCode Status code for HTTP response.
         * @return Builder with updated statusCode field.
         */
        public final Builder statusCode(StatusCode statusCode) {
            this.statusCode = statusCode;
            return self();
        }

        /**
         * Sets the Id of {@link HttpRequestMessage} for that this response was created.
         *
         * @param requestId Http request message id.
         * @return Builder with updated requestId field.
         */
        public final Builder requestId(String requestId) {
            this.requestId = requestId;
            return self();
        }

        /**
         * Copy content of another {@link HttpResponseMessage} to this.
         *
         * @param message {@link Message} to copy
         * @return Builder with updated fields from other instance of {@link HttpResponseMessage}.
         * @throws IllegalArgumentException when instance of other message than {@link HttpResponseMessage} is passed.
         */
        @Override
        public final Builder copy(Message message) {
            if (message instanceof HttpResponseMessage) {
                HttpResponseMessage responseMsg = (HttpResponseMessage) message;
                super.copy(responseMsg);
                Map<String, List<String>> oldHeader = responseMsg.getHeaders();
                Set<Map.Entry<String,List<String>>> headerEntries = oldHeader.entrySet();
                for(Map.Entry<String,List<String>> entry : headerEntries) {
                    header(entry.getKey(), entry.getValue());
                }
                this.body = Arrays.copyOf(responseMsg.getBody(), responseMsg.getBody().length);
                this.statusCode = responseMsg.getStatusCode();
                this.url = responseMsg.getURL();
                this.requestId = responseMsg.getRequestId();
            } else {
                throw new IllegalArgumentException("Can not copy a different type of message");
            }
            return self();
        }

        /**
         * Method to convert a {@link JsonObject} to a {@link HttpResponseMessage.Builder}.
         *
         * @param jsonObject The jsonObject to convert.
         * @return A new {@link oracle.iot.message.HttpResponseMessage.Builder} constructed from the JSON string.
         * @throws MessageParsingException when json object is wrong or does not contain mandatory fields.
         */
        @Override
        public final Builder fromJSON(JsonObject jsonObject) {
            super.fromJSON(jsonObject);

            final JsonObject payload = jsonObject.getJsonObject("payload");
            Utils.checkNullValueAndThrowMPE(payload, "response.message.payload.null");

            final StatusCode statusCode;
            try {
                statusCode = StatusCode.valueOf(payload.getInt("statusCode"));
            }
            catch (NullPointerException e) {
                throw new MessageParsingException("response.message.status.null", e);
            }
            catch (ClassCastException e) {
                throw new MessageParsingException("response.message.status.notNumber", e);
            }
            catch (IllegalArgumentException e) {
                throw new MessageParsingException("response.message.status.wrong", e);
            }

            final String url = Utils.jsonToString(payload.getJsonString("url"));

            final String requestId = Utils.jsonToString(payload.getJsonString("requestId"));
            Utils.checkNullOrEmptyStringThrowMPE(requestId, "response.message.requestId.null");

            final byte[] body;
            try {
                body = Base64.getDecoder().decode(payload.getString("body"));
            }
            catch (IllegalArgumentException e) {
                throw new MessageParsingException("response.message.body.wrong");
            }
            catch (NullPointerException e) {
                throw new MessageParsingException("response.message.body.null");
            }

            this.statusCode(statusCode);
            this.url(url);
            this.body(body);
            this.requestId(requestId);

            final JsonObject headers = payload.getJsonObject("headers");

            if (headers != null) {
                final List<String> headerList = new ArrayList();
                final Set<Map.Entry<String,JsonValue>> entries = headers.entrySet();
                for (Map.Entry<String,JsonValue> entry : entries) {
                    final String key = entry.getKey();
                    final JsonValue jsonValue = entry.getValue();
                    if (jsonValue.getValueType() != JsonValue.ValueType.ARRAY) {
                        continue;
                    }
                    final JsonArray headerValues = (JsonArray)jsonValue;
                    if (headerValues != null) {
                        for (int i = 0; i < headerValues.size(); i++) {
                            String headerValue = headerValues.getString(i);
                            headerList.add(headerValue);
                        }
                    }
                    this.header(key, headerList);
                    headerList.clear();
                }
            }

            return self();
        }

        /**
         * Returns current instance of {@link HttpResponseMessage.Builder}.
         * @return Instance of {@link HttpResponseMessage.Builder}
         */
        @Override
        protected final Builder self() {
            return this;
        }

        /**
         * Creates new instance of {@link HttpResponseMessage} using values from {@link HttpResponseMessage.Builder}.
         * @return Instance of {@link HttpResponseMessage}
         */
        @Override
        public HttpResponseMessage build() {
            return new HttpResponseMessage(this);
        }

    }

    /** HTTP response message header. Header is a name and list of values pair. */
    private final Map<String, List<String>> headers;
    /** Status code of the HTTP response message */
    private final StatusCode statusCode;
    /** URL of the HTTP response message */
    private final String url;
    /** Body of the HTTP response message */
    private final byte[] body;
    /** Id of the request message. Can be used for pairing request-response */
    private final String requestId;

    /**
     * {@link HttpResponseMessage} constructor takes {@link HttpResponseMessage.Builder} and set values
     * to each field. If the value is {@code null}, set a default value.
     * 
     * @param builder generic type of message builder
     */
    private HttpResponseMessage(Builder builder) {
        super(builder);
        if (builder.requestId != null) {
            this.requestId = builder.requestId;
        } else {
            this.requestId = null;
        }
        if (builder.headers == null) {
            // unreachable code, see initialization of params in Builder
            this.headers = Collections.emptyMap();
        } else {
            this.headers = Collections.unmodifiableMap(new HashMap<String,List<String>>(builder.headers));
        }
        if (builder.url == null) {
            this.url = "";
        } else {
            this.url = builder.url;
        }
        if (builder.body == null) {
            this.body = new byte[0];
        } else {
            this.body = Arrays.copyOf(builder.body, builder.body.length);
        }
        if (builder.statusCode == null) {
            this.statusCode = StatusCode.OK;
        } else {
            this.statusCode = builder.statusCode;
        }
    }

    /**
     * Get Http response message headers.
     * 
     * @return {@link Map} of pair {@link String} and {@link List} of {@link String} representing headers, never {@code null}
     */
    public final Map<String, List<String>> getHeaders() {
        return Collections.unmodifiableMap(this.headers);
    }

    /**
     * Get Http response message header values for given header name.
     *
     * @param name header name, should not be {@code null}.
     * @return {@link List} of header values, never {@code null}
     */
    public final List<String> getHeaderValues(String name) {
        if (name != null) {
            name = name.toLowerCase(Locale.ROOT);
            return Collections.unmodifiableList(this.headers.get(name));
        }
        return Collections.emptyList();
    }

    /**
     * Get response message header value at given index.
     *
     * @param name header name, should not be {@code null}.
     * @param index index in {@link List} of header values.
     * @return header value, may return {@code null} if header name does not exist or index is out of range.
     */
    public final String getHeaderValue(String name, int index) {
        if (name != null) {
            name = name.toLowerCase(Locale.ROOT);
        }
        
        List<String> values = this.headers.get(name);
        
        if (values != null && values.size() > index) {
            return this.headers.get(name).get(index);
        } else {
            return null;
        }
    }

    /**
     * Get first value from response message header.
     *
     * @param name header name, should not be {@code null}.
     * @return first header value, may return {@code null} if the header name does not exist or no values were set for given header name.
     */
    public final String getHeaderValue(String name) {
        if (name != null) {
            name = name.toLowerCase(Locale.ROOT);
        }
        
        List<String> values = this.headers.get(name);
        
        if (values != null && values.size() > 0)
            return values.get(0);
        
        return null;
    }

    /**
     * Get request message URL.
     * 
     * @return URL, never {@code null}
     */
    public final String getURL() {
        return this.url;
    }

    /**
     * Get HTTP response message body. If message body contains string value, default encoding is UTF-8.
     * 
     * @return body, never {@code null}
     */
    public final byte[] getBody() {
        return Arrays.copyOf(body, body.length);
    }

    /**
     * Get HTTP response message body in {@link String}. It convert and return the inner representation of message
     * body to {@link String}.
     *
     * @return Http message body as {@link String}, never {@code null}
     */
    public final String getBodyString(){
        return new String(getBody(), StandardCharsets.UTF_8);
    }

    /**
     * Get Http response message status code.
     * 
     * @return statusCode, never {@code null}
     */
    public final StatusCode getStatusCode() {
        return this.statusCode;
    }

    /**
     * Get ID of {@link HttpRequestMessage}. {@link HttpRequestMessage} Id can be used for pairing request-response.
     * @return Request id, never {@code null}
     */
    public String getRequestId() {
        return requestId;
    }

    /**
     * Get message type.
     *
     * @return type, never {@code null}.
     */
    @Override
    public Type getType() {
        return Type.RESPONSE;
    }

    /**
     * Method to return MessageBuilder that is populated with content of this instance.
     *
     * @return returns {@link DataMessage.Builder}.
     */
    @Override
    public MessageBuilder getMessageBuilder() {
        return new HttpResponseMessage.Builder().copy(this);
    }

    /**
     * Exports data from {@link HttpRequestMessage} to {@link String} using JSON interpretation of the message.
     *
     * @return JSON interpretation of the message as {@link String}.
     */
    @Override
    public final String toString() {
        return toJSON().toString();
    }
    
    /**
     * Exports response message to {@link JsonObject}.
     * 
     * @return response message in JSON format
     */
    @Override
    public final JsonObject toJSON() {
        final JsonObjectBuilder builder = Utils.commonFieldsToJSON(this);
        final JsonObjectBuilder payload = Json.createObjectBuilder();
        final JsonObjectBuilder headerObject = Json.createObjectBuilder();

        builder.add("type", Type.RESPONSE.name());
        payload.add("statusCode", this.statusCode.getCode());
        payload.add("url", this.url);
        payload.add("requestId", this.requestId);

        Set<Map.Entry<String,List<String>>> entries = headers.entrySet();
        for (Map.Entry<String,List<String>> entry : entries) {
            final JsonArrayBuilder headerValues = Json.createArrayBuilder();
            String key = entry.getKey();
            List<String> values = entry.getValue();
            for (String v : values) {
                headerValues.add(v);
            }
            headerObject.add(key, headerValues);
        }
        payload.add("headers", headerObject);
        payload.add("body", new String(Base64.getEncoder().encode(this.body), StandardCharsets.UTF_8));

        builder.add("payload", payload);

        return builder.build();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;

        HttpResponseMessage that = (HttpResponseMessage) o;

        if (!Arrays.equals(body, that.body)) return false;
        if (!headers.equals(that.headers)) return false;
        if (statusCode != that.statusCode) return false;
        if (!url.equals(that.url)) return false;
        if (!requestId.equals(that.requestId)) return false;

        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + headers.hashCode();
        result = 31 * result + statusCode.hashCode();
        result = 31 * result + url.hashCode();
        result = 31 * result + requestId.hashCode();
        result = 31 * result + Utils.hashCodeByteArray(body);
        return result;
    }
}
