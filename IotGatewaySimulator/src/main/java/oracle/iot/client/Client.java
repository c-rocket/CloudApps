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

package oracle.iot.client;

import com.oracle.iot.client.TrustManager;

/**
 * Client of the Oracle IoT Cloud Service. A client is a directly-enrolled
 * device, gateway, or application that has an endpoint identifier and shared
 * secret from previously registering the device with the cloud service.
 * This class is provides API to inherited classes and is not meant to be
 * instantiated.
 */
public class Client {

    private final String server;
    private final int port;
    private final String endpointId;
    private boolean closed;
    /**
     * Create a Client instance with the given endpoint id.
     * @param server The server on which the endpoint is registered
     * @param port The port number, which must be in the range 1 to 65535
     * @param endpointId The endpoint id from registering the device on the server
     * @throws IllegalArgumentException If server is null or an empty string.
     * @throws IllegalArgumentException If port is out of range 1 to 65535.
     * @throws IllegalArgumentException If endpointId is null.
     */
    protected Client(String server, int port, String endpointId) {
        this.server = server;
        this.port = port;
        this.endpointId = endpointId;
        this.closed = true;
        if (this.server == null || this.server.isEmpty()) {
            throw new IllegalArgumentException("server cannot be null");
        }
        if (this.port <= 0 || this.port > 0xFFFF) {
            throw new IllegalArgumentException("port out of range:" + port);
        }
        if (this.endpointId == null) {
            throw new IllegalArgumentException("endpointId cannot be null");
        }
        this.closed = false;
    }

    /**
     * The server on which the endpoint is registered.
     * @return The server on which the endpoint is registered.
     */
    public final String getServer() {
        return server;
    }

    /**
     * Get the server's port
     * @return The server's port
     */
    public int getPort() {
        return port;
    }

    /**
     * The client is assigned an endpoint id during the registration process.
     * @return The endpoint id from registering the device on the server
     */
    public final String getEndpointId() {
        return endpointId;
    }

    /**
     * Authenticate with the server using the client-assertion flow.
     * This method is used if the private key is known from a previous
     * call to {@link #activate(String) activate}.
     * If the private key is not known and the client has
     * been activated, then the device must be registered and
     * {@link Client#activate(String) activated} again.
     *
     * @param privateKey The private key obtained from previously calling
     * {@link #activate(String)}
     * @throws ClientException if the underlying implementation throws an
     * error during the authentication process.
     * @see Client#activate(String)
     */
    public final void authenticate(byte[] privateKey) throws ClientException {
        if (closed) throw new IllegalStateException("closed");
        TrustManager trustManager = TrustManager.getInstance(this);
        trustManager.authenticate(privateKey);
    }

    /**
     * Activate the endpoint on the server using the client-credentials flow.
     * A client that has been registered with the server must be activated.
     * Persisting the private key is the responsibility of the caller.
     * This method will not return null.
     * <p>
     * Currently, the only way to know if this method should be called is
     * by whether or not there is a persisted byte array for the endpoint.
     *
     * @param sharedSecret The shared secret obtained from registering the device
     * @return The Client's private key.
     * @throws SecurityException if there is a SecurityManager and the caller
     * does not have {@link ClientPermission#ACTIVATE} permission
     * @throws ClientException if the underlying implementation throws an
     * error during the activation process.
     */
    public final byte[] activate(String sharedSecret) throws ClientException {
        if (closed) throw new IllegalStateException("closed");
        TrustManager trustManager = TrustManager.getInstance(this);
        return trustManager.activate(sharedSecret);
    }

    /**
     * Release resources for this Client.
     * After calling this method, a call to any API will result in an
     * IllegalStateException.
     */
    public void close() {
        if (!closed) {
            closed = true;
            TrustManager trustManager = TrustManager.getInstance(this);
            trustManager.close();
        }
    }

    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        final Client other = (Client)obj;
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
}
