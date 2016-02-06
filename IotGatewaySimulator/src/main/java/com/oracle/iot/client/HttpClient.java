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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.zip.GZIPInputStream;
import java.util.zip.InflaterInputStream;
import javax.net.ssl.HttpsURLConnection;

import com.oracle.iot.util.IoTHostnameVerifier;

/**
 */
public final class HttpClient {
    
    private final URL url;
    
    private static final int DEFAULT_RESPONSE_TIMEOUT = 15000; // milliseconds
    private static final int responseTimeout;
    static {
        int value = DEFAULT_RESPONSE_TIMEOUT;
        try {
            value = Integer.getInteger("oracle.iot.client.responseTimeout", DEFAULT_RESPONSE_TIMEOUT);
        } catch (SecurityException e) {
            // use default value
            value = DEFAULT_RESPONSE_TIMEOUT;
        } finally {
            responseTimeout = value;
        }
    }

    // Static variable for injection transport implemention in unit tests without real server
    private static Transport transport = new Transport();

    static void setTransport(Transport tr) {
        transport = tr;
    }

    static class Transport {
        Transport() {}

        HttpResponse invokeMethod(String method, byte[] data, Map<String, String> headers,
                                              URL url) throws IOException {
            // TODO: Assign the hostname verifier in
            // an initializer when the trust interface is refactored
            // since it only needs to be set once.
            // The use of server.cn property is documented in Client.java

            // Set the hostname verifier.
            String serverCN = System.getProperty(
                "com.oracle.iot.client.server.cn");
            if (serverCN == null || serverCN.isEmpty()) {
                throw new IllegalArgumentException("server cn must be set");
            }
            IoTHostnameVerifier iothostnameverifier = 
                new IoTHostnameVerifier(url.getHost(), serverCN);

            // we know that the connection is https
            HttpsURLConnection.setDefaultHostnameVerifier(iothostnameverifier);

            // Create the connection object
            final HttpURLConnection con = (HttpURLConnection) url.openConnection();

            con.setInstanceFollowRedirects(true);
            con.setConnectTimeout(responseTimeout > 0 ? responseTimeout : 0);
            con.setReadTimeout(responseTimeout > 0 ? responseTimeout : 0);

            // Add request headers (caller-supplied first, so we overwrite any collisions)
            con.setRequestMethod(method);
            if (headers != null) {
                for (Map.Entry<String, String> entry : headers.entrySet()) {
                    con.setRequestProperty(entry.getKey(), entry.getValue());
                }
            }

            if (data != null) {
                // Send post request
                con.setDoOutput(true);

                con.setFixedLengthStreamingMode(data.length);
                pipe(new ByteArrayInputStream(data), con.getOutputStream());
            }
            con.connect();

            // Validate the connection
            final int responseCode = con.getResponseCode();
            Map<String, List<String>> responseHeaders = con.getHeaderFields();

            // Read the response
            byte[] responseData = null;
            try {
                responseData = getResponseBody(con,
                        (responseCode >= 200 && responseCode < 300) ? con.getInputStream():con.getErrorStream());
            } catch (IOException ioe){
                ioe.printStackTrace();
            }

            con.disconnect();

            return new HttpResponse(responseCode, responseData, responseHeaders);
        }
    }

    public HttpClient(String url) throws MalformedURLException {
        this(new URL(url));
    }

    public HttpClient(URL url) {
        if (url == null) {
            throw new IllegalArgumentException("url cannot be null");
        }
        this.url = url;
    }

    public HttpResponse post(byte[] data, Map<String, String> headers) throws IOException {
        return transport.invokeMethod("POST", data, headers, url);
    }

    public HttpResponse get(Map<String, String> headers) throws IOException {
        return transport.invokeMethod("GET", null, headers, url);
    }

    public HttpResponse delete(Map<String, String> headers) throws IOException {
        return transport.invokeMethod("DELETE", null, headers, url);
    }

    static void pipe(InputStream in, OutputStream out) throws IOException {
        // Setup so that if in or out is null but not both, then the one that
        // isn't null will end up being closed.
        // Otherwise any failure ends up as an IOException but the streams are still closed
        try {
            if (in != null && out != null) {
                final byte[] buffer = new byte[8096];
                int length;
                while ((length = in.read(buffer)) != -1) {
                    out.write(buffer, 0, length);
                }
            }
        } finally {
            if (in != null) {
                try { in.close(); } catch (IOException ioe) {}
            }
            if (out != null) {
                try { out.close(); } catch (IOException ioe) {}
            }
        }
    }

    static byte[] getResponseBody(HttpURLConnection http, InputStream responseStream) throws IOException {
        // If this is GZIP encoded, then wrap the input stream
        final String contentEncoding = http.getContentEncoding();
        if ("gzip".equals(contentEncoding)) {
            responseStream = new GZIPInputStream(responseStream);
        } else if ( "deflate".equals(contentEncoding)) {
            responseStream = new InflaterInputStream(responseStream);
        }
        ByteArrayOutputStream body = new ByteArrayOutputStream();
        pipe(responseStream, body);
        return body.toByteArray();
    }

    public static class HttpResponse {
        private int status;
        private byte[] data;
        private Map<String, List<String>> headers;

        public HttpResponse(int responseCode, byte[] data, Map<String, List<String>> headers) {
            this.status = responseCode;
            this.data = data;
            this.headers = headers;
        }

        public int getStatus() {
            return status;
        }

        public byte[] getData() {
            return data;
        }

        public Map<String, List<String>> getHeaders() {
            return headers;
        }
    }
}
