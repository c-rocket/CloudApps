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

package oracle.iot.client.device.async;

import com.oracle.iot.client.device.async.AsyncMessageDispatcher;
import com.oracle.iot.message.ResourcesReportMessage;
import oracle.iot.client.ClientPermission;
import oracle.iot.client.device.DeviceClient;
import oracle.iot.message.Message;
import oracle.iot.message.RequestMessageHandler;
import oracle.iot.client.device.Resource;

import java.util.List;
import java.util.UUID;

/**
 * A DeviceClient that is able to send messages to the IoT server asynchronously.
 */
public class AsyncDeviceClient extends DeviceClient {

    /**
     * Create an AsyncDeviceClient instance with the given endpoint id.
     * @param server The server on which the endpoint is registered
     * @param port The port number, which must be in the range 1 to 65535
     * @param endpointId The endpoint id from registering the device on the server
     * @throws IllegalArgumentException If server is null.
     * @throws IllegalArgumentException If port is not -1, or within the range
     * 1 to 65535.
     * @throws IllegalArgumentException If endpointId is null.
     */
    public AsyncDeviceClient(String server, int port, String endpointId) {
        super(server, port, endpointId);
        this.closed = false;
    }

    /**
     * Send a message to the server. This method does not block.
     * @param message The message to be sent
     * @return A {@link MessageReceipt} that can
     * be used to obtain the status of the message within the dispatcher.
     * @throws SecurityException if there is a SecurityManager and the caller
     * does not have {@link ClientPermission#SEND_MESSAGE} permission
     * @throws IllegalStateException if there is no connection to the server
     */
    public final MessageReceipt sendMessage(Message message) {
        final SecurityManager securityManager = System.getSecurityManager();
        if (securityManager != null) {
            securityManager.checkPermission(ClientPermission.SEND_MESSAGE);
        }
        final AsyncMessageDispatcher messageDispatcher =
                AsyncMessageDispatcher.getAsyncMessageDispatcher(this);
        return messageDispatcher.sendMessage(message);
    }

    @Override
    public boolean registerRequestHandler(Resource resource, RequestMessageHandler handler) {
        boolean registered = false;
        // Note: intentional assignment in if-condition
        if (registered = super.registerRequestHandler(resource, handler)) {
            ResourcesReportMessage rrm = new ResourcesReportMessage.Builder()
                    .endpointName(getEndpointId())
                    .source(getEndpointId())
                    .id(UUID.randomUUID().toString())
                    .reconciliationMark(getReconciliationMark())
                    .add(copy(resource, Resource.Status.ADDED))
                    .build();
            sendMessage(rrm);
        }
        return registered;
    }

    @Override
    public boolean unregisterRequestHandler(Resource resource) {
        boolean unregistered = false;
        // Note: intentional assignment in if-condition
        if (unregistered = super.unregisterRequestHandler(resource)) {
            ResourcesReportMessage rrm = new ResourcesReportMessage.Builder()
                    .endpointName(getEndpointId())
                    .source(getEndpointId())
                    .id(UUID.randomUUID().toString())
                    .reconciliationMark(getReconciliationMark())
                    .remove(copy(resource, Resource.Status.REMOVED))
                    .build();
            sendMessage(rrm);
        }
        return unregistered;
    }

    @Override
    public List<Resource> unregisterRequestHandler(RequestMessageHandler handler) {
        final List<Resource> resources = super.unregisterRequestHandler(handler);
        if (!resources.isEmpty()) {
            final ResourcesReportMessage.Builder builder =
                    new ResourcesReportMessage.Builder()
                            .endpointName(getEndpointId())
                            .source(getEndpointId())
                            .id(UUID.randomUUID().toString())
                            .reconciliationMark(getReconciliationMark());

            for (int n=0, nMax=resources.size(); n<nMax; n++) {
                final Resource resource = resources.get(n);
                Resource removed = copy(resource, Resource.Status.REMOVED);
                builder.remove(removed);
            }

            final ResourcesReportMessage rrm = builder.build();
            sendMessage(rrm);
        }
        return resources;
    }

    /**
     * {@inheritDoc}
     * Queued messages will not be dispatched and will return a failure status.
     * Messages in sending status will be processed, but any that fail to be
     * delivered will not be retried.
     * Any request from the server will be sent a response indicating that the
     * resource is no longer available (e.g, HTTP 410).
     * Requests already in the queue to be handled will, likewise, be given a
     * response indicating that the resource is no longer available.
     */
    @Override
    public void close() {
        if (!closed) {
            closed = true;
            final AsyncMessageDispatcher messageDispatcher =
                    AsyncMessageDispatcher.getAsyncMessageDispatcher(this);
            messageDispatcher.close();
        }
        super.close();
    }

    private volatile boolean closed;

    private static Resource copy(Resource orig, Resource.Status status) {
        return new Resource.Builder()
                .methods(orig.getMethods())
                .endpointName(orig.getEndpointName())
                .name(orig.getName())
                .path(orig.getPath())
                .status(status)
                .build();
    }
}
