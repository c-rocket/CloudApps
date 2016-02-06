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

package com.oracle.iot.client.device.sync;

import oracle.iot.client.ClientPermission;
import oracle.iot.client.device.DeviceClient;
import oracle.iot.message.HttpRequestMessage;
import oracle.iot.message.HttpResponseMessage;
import oracle.iot.message.Message;
import oracle.iot.message.StatusCode;

import java.util.Collection;
import java.util.Collections;

/**
 * A DeviceClient that is able to send messages to the IoT server synchronously.
 */
public class SyncDeviceClient extends DeviceClient {

    /**
     * Create a SyncDeviceClient instance with the given endpoint id.
     * @param server The server on which the endpoint is registered
     * @param port The port number, which must be in the range 1 to 65535
     * @param endpointId The endpoint id from registering the device on the server
     * @throws IllegalArgumentException If server is null.
     * @throws IllegalArgumentException If port is not -1, or within the range
     * 1 to 65535.
     * @throws IllegalArgumentException If endpointId is null.
     */
    public SyncDeviceClient(String server, int port, String endpointId) {
        super(server, port, endpointId);
    }

    /**
     * Send a message to the server, and receive messages from the server for
     * processing.
     * @param message The message to be sent
     * @return A Collection of messages, possibly empty, from the server.
     * @throws SecurityException if there is a SecurityManager and the caller
     * does not have {@link ClientPermission#SEND_MESSAGE} permission
     * @throws IllegalStateException if there is no connection to the server
     */
    public final Collection<HttpRequestMessage> sendMessage(Message message) {
        return Collections.EMPTY_LIST;
    }

    /**
     * Process a request from the server by calling the registered handler,
     * if any. If there is not a registered handler, a default handler will
     * be used. The default handler will return a response indicating that the
     * resource was not found. The caller is responsible for sending the
     * response message back to the server by calling
     * {@link #sendMessage(Message)}. This method will
     * not return null.
     * @param message The message to be processed
     * @return A response message to be sent to the server.
     */
    public final HttpResponseMessage processMessage(HttpRequestMessage message) {
        return new HttpResponseMessage.Builder()
                        .requestId(message.getId())
                        .source(message.getDestination())
                        .statusCode(StatusCode.NOT_FOUND)
        .build();
    }

}
