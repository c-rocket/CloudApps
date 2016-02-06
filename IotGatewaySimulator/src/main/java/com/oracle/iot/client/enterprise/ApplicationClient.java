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

package com.oracle.iot.client.enterprise;

import oracle.iot.client.Client;
import oracle.iot.client.ClientException;

/**
 * An Application is a...
 */
public class ApplicationClient extends Client {
    // TODO: define API.
    // Low-level API is HTTP put, get, etc.
    // High-level API is "get the resources for a device registered on the server"

    /**
     * Create a AsyncGatewayClient instance with the given endpoint id.
     * @param server The server on which the endpoint is registered
     * @param port The port number,  which must be in the range 1 to 65535
     * @param endpointId The endpoint id from registering the device on the server
     * @throws IllegalArgumentException If server is null.
     * @throws IllegalArgumentException If port is not -1, or within the range
     * 1 to 65535.
     * @throws IllegalArgumentException If endpointId is null.
     */
    public ApplicationClient(String server, int port, String endpointId) {
        super(server, port, endpointId);
        throw new UnsupportedOperationException("ApplicationClient not supported yet");
    }

}
