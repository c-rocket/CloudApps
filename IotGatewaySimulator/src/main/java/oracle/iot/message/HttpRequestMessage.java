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

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * HttpRequestMessage extends Message class. It stores HTTP headers, parameters,
 * method, URL and body. The body is in byte array. Default encoding for string body is UTF-8.
 * This class is immutable.
 */
public final class HttpRequestMessage extends Message {

    /**
     * Builder extends {@link Message.MessageBuilder} class. {@link HttpRequestMessage} class is immutable.
     * A builder is required when creating {@link HttpRequestMessage}. {@link HttpRequestMessage} uses Builder
     * design pattern.
     */
    public final static class Builder extends MessageBuilder<Builder> {

        /** HTTP request message header. Header is pair of name and list of values. */
        private Map<String, List<String>> headers = new HashMap<String, List<String>>();
        /** HTTP request message parameter */
        private Map<String, String> params = new HashMap<String, String>();
        /** Method of the HTTP request message */
        private String method;
        /** URL of the HTTP request message */
        private String url;
        /** Body of the HTTP request message */
        private byte[] body;

        public Builder() {

        }

        /**
         * Add Http message header. The name cannot be {@code null}. The values can be {@code null}. The {@link List} of values
         * cannot contain {@code null}s. Name and values must contain ASCII-printable characters only. Note that
         * the name of the header is always converted to lower case.
         * 
         * @param name Name of the http header.
         * @param values Http header values.
         * @return Builder with updated header field.
         * @throws NullPointerException when the header name is {@code null} or if values contains {@code null} string.
         * @throws IllegalArgumentException when name or values contain non-ASCII-printable characters.
         */
        public final Builder header(String name, List<String> values) {
            Utils.checkNullValueThrowsNPE(name, "RequestMessage: Header name");
            Utils.checkNullValuesThrowsNPE(values, "RequestMessage: Header values");
            if (!Utils.isHttpHeaderAsciiPrintable(name, values)){
                throw new IllegalArgumentException("RequestMessage: Header contains non-ASCII printable characters!");
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
         * Add Http parameter. Name of the param cannot be {@code null}. Parameter value can be {@code null}.
         * 
         * @param name Http parameter name.
         * @param value Http parameter value.
         * @return Builder with updated param field.
         * @throws NullPointerException when the name is {@code null}.
         */
        public final Builder param(String name, String value) {
            Utils.checkNullValueThrowsNPE(name, "RequestMessage: Param name");
            this.params.put(name, value);
            return self();
        }

        /**
         * Set message URL. The URL is not checked for validity.
         *
         * @param url URL of the HTTP request.
         * @return Builder with updated url field.
         */
        public final Builder url(String url) {
            this.url = url;
            return self();
        }

        /**
         * Set message body.
         * 
         * @param body Body of the HTTP message, should not be {@code null}.
         * @return Builder with updated body field.
         */
        public final Builder body(byte[] body) {
            this.body = Arrays.copyOf(body, body.length);
            return self();
        }

        /**
         * Set message body using a {@link String}. The encoding for the body is UTF-8.
         *
         * @param body Body of the HTTP message, should not be {@code null}.
         * @return Builder with updated body field.
         */
        public final Builder body(String body) {
            return body(body.getBytes(StandardCharsets.UTF_8));
        }

        /**
         * Set Http method. Tha name of the HTTP method is always converted to lower case.
         * 
         * @param method Method of the HTTP.
         * @return Builder with updated method field.
         */
        public final Builder method(String method) {
            this.method = (method != null) ? method.toLowerCase(Locale.ROOT) : null;
            return self();
        }

        /**
         * Copy content of another {@link HttpRequestMessage} to this.
         *
         * @param message {@link Message} to copy.
         * @return Builder with updated fields from other instance of {@link HttpRequestMessage}.
         * @throws IllegalArgumentException when instance of other message than {@link HttpRequestMessage} is passed.
         */
        @Override
        public final Builder copy(Message message) {
            if (message instanceof HttpRequestMessage) {
                HttpRequestMessage requestMsg = (HttpRequestMessage) message;
                super.copy(requestMsg);
                Map<String, List<String>> oldHeader = requestMsg.getHeaders();
                Set<Map.Entry<String,List<String>>> headerEntries = oldHeader.entrySet();
                for(Map.Entry<String,List<String>> entry : headerEntries) {
                    header(entry.getKey(), entry.getValue());
                }
                Map<String, String> oldParams = requestMsg.getParams();
                Set<Map.Entry<String,String>> paramEntries = oldParams.entrySet();
                for (Map.Entry<String,String> entry : paramEntries) {
                    param(entry.getKey(), entry.getValue());
                }
                this.body = Arrays.copyOf(requestMsg.getBody(), requestMsg.getBody().length);
                this.method = requestMsg.getMethod();
                this.url = requestMsg.getURL();
            } else {
                throw new IllegalArgumentException("Can not copy a different type of message");
            }
            return self();
        }

        /**
         * Method to convert a {@link JsonObject} to a {@link HttpRequestMessage.Builder}.
         *
         * @param jsonObject The jsonObject to convert.
         * @return A new {@link oracle.iot.message.HttpRequestMessage.Builder} constructed from the JSON string.
         * @throws MessageParsingException when json object is wrong or does not contain mandatory fields.
         */
        @Override
        public final Builder fromJSON(JsonObject jsonObject) {
            super.fromJSON(jsonObject);
            if ( this.clientId == null || this.clientId.isEmpty() )
                throw new MessageParsingException("clientId should be set for HttpRequestMessage");

            final JsonObject payload = jsonObject.getJsonObject("payload");
            Utils.checkNullValueAndThrowMPE(payload, "request.message.payload.null");

            final String method = Utils.jsonToString(payload.getJsonString("method"));
            Utils.checkNullOrEmptyStringThrowMPE(method, "request.message.method.null");

            final String url = Utils.jsonToString(payload.getJsonString("url"));
            Utils.checkNullOrEmptyStringThrowMPE(url, "request.message.url.null");

            final byte[] body;
            try {
                body = Base64.getDecoder().decode(payload.getString("body"));
            }
            catch (IllegalArgumentException e) {
                throw new MessageParsingException("request.message.body.wrong");
            }
            catch (NullPointerException e) {
                throw new MessageParsingException("request.message.body.null");
            }
            final JsonObject headers = payload.getJsonObject("headers");
            final JsonObject params = payload.getJsonObject("params");

            this.method(method);
            this.url(url);
            this.body(body);

            if (headers != null) {
                final List<String> headerList = new ArrayList();
                for (String key : headers.keySet()) {
                    final JsonArray headerValues = headers.getJsonArray(key);
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

            if (params != null) {
                for (String paramName : params.keySet()) {
                    final String paramValue = params.getString(paramName);
                    this.param(paramName, paramValue);
                }
            }

            return self();
        }

        /**
         * Returns current instance of {@link HttpRequestMessage.Builder}.
         * @return Instance of {@link HttpRequestMessage.Builder}
         */
        @Override
        protected final Builder self() {
            return this;
        }

        /**
         * Creates new instance of {@link HttpRequestMessage} using values from {@link HttpRequestMessage.Builder}.
         * @return Instance of {@link HttpRequestMessage}
         */
        @Override
        public HttpRequestMessage build() {
            return new HttpRequestMessage(this);
        }

    }

    /** HTTP request message header. Header is pair of name and list of values. */
    private final Map<String, List<String>> headers;

    /** HTTP request message parameter */
    private final Map<String, String> params;

    /** Method of the HTTP request message */
    private final String method;

    /** URL of the HTTP request message */
    private final String url;

    /** Body of the HTTP request message */
    private final byte[] body;

    /**
     * {@link HttpRequestMessage} constructor takes {@link HttpRequestMessage.Builder} and set values to
     * each field. If the value is {@code null}, set a default value. Url cannot be {@code null} or empty.
     * 
     * @param builder generic type of message builder
     * @throws IllegalArgumentException when the url is not specified
     */
    private HttpRequestMessage(Builder builder) {
        super(builder);
        if (builder.headers == null) {
            // unreachable code, see initialization of headers in Builder
            this.headers = Collections.emptyMap();
        } else {
            this.headers = Collections.unmodifiableMap(new HashMap<String,List<String>>(builder.headers));
        }
        if (builder.params == null) {
            // unreachable code, see initialization of params in Builder
            this.params = Collections.emptyMap();
        } else {
            this.params = Collections.unmodifiableMap(new HashMap<String,String>(builder.params));
        }
        if (builder.url == null || builder.url.isEmpty()) {
          throw new IllegalArgumentException("Requested url cannot be null or empty.");
        } else {
            this.url = builder.url;
        }
        if (builder.body == null) {
            this.body = new byte[0];
        } else {
            this.body = Arrays.copyOf(builder.body, builder.body.length);
        }
        if (builder.method == null) {
            this.method = "get";
        } else {
            this.method = builder.method;
        }
    }

    /**
     * Get Http request message headers.
     * @return {@link Map} of pair {@link String} and {@link List} of {@link String} representing headers, never {@code null}
     */
    public final Map<String, List<String>> getHeaders() {
        return Collections.unmodifiableMap(this.headers);
    }

    /**
     * Get Http request message header values for given header name.
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
     * Get request message header value at given index.
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
     * Get first value from request message header.
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
     * Get Http request message parameters.
     * 
     * @return {@link Map} of pair {@link String} and {@link String} representing parameters, never {@code null}
     */
    public final Map<String, String> getParams() {
        return this.params;
    }

    /**
     * Get parameter value for given parameter name.
     * 
     * @param name parameter name, should not be {@code null}.
     * @return parameter value, may return {@code null} if the parameter name does not exist.
     */
    public final String getParam(String name) {
        return this.params.get(name);
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
     * Get HTTP request message body. If message body contains string value, default encoding is UTF-8.
     * 
     * @return body, never {@code null}
     */
    public final byte[] getBody() {
        return Arrays.copyOf(body, body.length);
    }

    /**
     * Get HTTP request message body in {@link String}. It convert and return the inner representation of message
     * body to {@link String}.
     *
     * @return Http message body as {@link String}, never {@code null}
     */
    public final String getBodyString(){
        return new String(getBody(), StandardCharsets.UTF_8);
    }

    /**
     * Get request message method. Note that the returned {@link java.lang.String} is always in lower case.
     * 
     * @return Http method, never {@code null}
     */
    public final String getMethod() {
        if (this.method != null) {
            return this.method.toLowerCase(Locale.ROOT);
        }
        // unreachable code, see constructor
        return null;
    }

    /**
     * Get message type.
     *
     * @return type, never {@code null}.
     */
    @Override
    public Type getType() {
        return Type.REQUEST;
    }

    /**
     * Method to return MessageBuilder that is populated with content of this instance.
     *
     * @return returns {@link DataMessage.Builder}.
     */
    @Override
    public MessageBuilder getMessageBuilder() {
        return new HttpRequestMessage.Builder().copy(this);
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
     * Exports Http request message to {@link JsonObject} format.
     * 
     * @return request message in JSON format
     */
    @Override
    public final JsonObject toJSON() {
        final JsonObjectBuilder builder = Utils.commonFieldsToJSON(this);
        final JsonObjectBuilder payload = Json.createObjectBuilder();
        final JsonObjectBuilder headerObject = Json.createObjectBuilder();
        final JsonObjectBuilder paramsObject = Json.createObjectBuilder();

        payload.add("method", this.method);
        payload.add("url", this.url);

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

        for (Map.Entry<String, String> entry : params.entrySet()) {
            paramsObject.add(entry.getKey(), entry.getValue());
        }
        payload.add("params", paramsObject);
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

        HttpRequestMessage that = (HttpRequestMessage) o;

        if (!Arrays.equals(body, that.body)) return false;
        if (!headers.equals(that.headers)) return false;
        if (!method.equals(that.method)) return false;
        if (!params.equals(that.params)) return false;
        if (!url.equals(that.url)) return false;

        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + headers.hashCode();
        result = 31 * result + params.hashCode();
        result = 31 * result + method.hashCode();
        result = 31 * result + url.hashCode();
        result = 31 * result + Utils.hashCodeByteArray(body);
        return result;
    }
}
