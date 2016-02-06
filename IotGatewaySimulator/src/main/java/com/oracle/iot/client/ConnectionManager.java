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

import java.io.IOException;
import java.net.URL;
import java.security.GeneralSecurityException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

/**
 * ConnectionManager
 */
public final class ConnectionManager {
    private final static Map<String, ConnectionManager> mappings = new HashMap<>();
    public static ConnectionManager getInstance(TrustManager trustManager) {
        if (trustManager == null) throw new IllegalArgumentException("trustManager cannot be null");
        // If for this trust manager there is a connection manager return it, otherwise create a new one and add it to the mappings before returning it
        ConnectionManager cm = mappings.get(trustManager.getEndpointId()) ;
        if (cm == null) {
            cm = new ConnectionManager(trustManager) ;
            mappings.put(trustManager.getEndpointId(), cm) ;
        }
        return cm ;
    }
    /* Origional code, before Tim G modifications for multiple clients
    public static ConnectionManager getInstance(TrustManager trustManager) {
        if (trustManager == null) throw new IllegalArgumentException("trustManager cannot be null");
        return Holder.INSTANCE.getConnectionManager(trustManager);
    }

    private enum Holder {
        INSTANCE;

        private ConnectionManager connectionManager;
        private synchronized ConnectionManager getConnectionManager(TrustManager trustManager) {
            if (connectionManager == null) {
                connectionManager = new ConnectionManager(trustManager);
            } else if (!connectionManager.trustManager.equals(trustManager)) {
                throw new IllegalArgumentException("trustManager does not match");
            }
            return connectionManager;
        }

        private void close() {
            connectionManager = null;
        }
    }
    */
    private final TrustManager trustManager;

    private ConnectionManager(TrustManager trustManager) {
        this.trustManager = trustManager;
    }

    public HttpClient.HttpResponse post(String restApi, byte[] payload, Map<String, String> headers) throws IOException, GeneralSecurityException {

        final String serverHost = trustManager.getServer();
        final int serverPort = trustManager.getPort();

        final URL url = new URL("https", serverHost, serverPort, restApi);

        AccessToken accessToken = trustManager.getAccessToken();
        // TODO: Adding authorization header could be handled differently/better somehow.
        final Map<String,String> _headers = new HashMap<String,String>(headers);
        _headers.put("Authorization", accessToken.getTokenType() + " " + accessToken.getToken());

        final HttpClient httpClient = new HttpClient(url);
        HttpClient.HttpResponse response = httpClient.post(payload, _headers);
        Logger.getAnonymousLogger().fine("POST " + url.toExternalForm() + " reponse = " + response.getStatus());

        if (response.getStatus() == 401) {
            accessToken = trustManager.renewAccessToken();
            _headers.put("Authorization", accessToken.getTokenType() + " " + accessToken.getToken());
            response = httpClient.post(payload, _headers);
        }

        return response;
    }


    public HttpClient.HttpResponse get(String restApi, Map<String, String> headers) throws IOException, GeneralSecurityException {

        final String serverHost = trustManager.getServer();
        final int serverPort = trustManager.getPort();

        final URL url = new URL("https", serverHost, serverPort, restApi);
        Logger.getAnonymousLogger().info("GET " + url.toExternalForm());

        AccessToken accessToken = trustManager.getAccessToken();

        final Map<String, String> _headers = new HashMap<String, String>(headers);
        _headers.put("Authorization", accessToken.getTokenType() + " " + accessToken.getToken());

        final HttpClient httpClient = new HttpClient(url);
        HttpClient.HttpResponse response = httpClient.get(_headers);

        if (response.getStatus() == 401) {
            accessToken = trustManager.renewAccessToken();
            _headers.put("Authorization", accessToken.getTokenType() + " " + accessToken.getToken());
            response = httpClient.get(_headers);
        }

        return response;
    }
    public synchronized void close() {
        trustManager.close();
        // remove the mapping
        mappings.remove(trustManager.getEndpointId()) ;
    }
    /* old version of the code
    public synchronized void close() {
        Holder.INSTANCE.close();
    }
    */
}
