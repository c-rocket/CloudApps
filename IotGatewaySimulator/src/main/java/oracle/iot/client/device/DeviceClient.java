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

package oracle.iot.client.device;

import com.oracle.iot.message.ResourcesReportMessage;
import com.oracle.json.JsonObject;
import oracle.iot.client.Client;
import oracle.iot.client.ClientPermission;
import oracle.iot.message.HttpRequestMessage;
import oracle.iot.message.HttpResponseMessage;
import oracle.iot.message.RequestMessageHandler;
import oracle.iot.message.StatusCode;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

/**
 * A DeviceClient is able to to register handlers for messages from the IoT server.
 * This class provides common API to inherited classes and is not meant to be
 * instantiated.
 */
public class DeviceClient extends Client {


    /**
     * Registered RequestMessageHandlers
     */
    private final List<RequestHandlerRegistration> requestHandlerRegistrations;

    /**
     * Create a DeviceClient instance with the given endpoint id.
     *
     * @param server     The server on which the endpoint is registered
     * @param port       The port number, which must be in the range 1 to 65535
     * @param endpointId The endpoint id from registering the device on the server
     * @throws IllegalArgumentException If server is null.
     * @throws IllegalArgumentException If port is not -1, or within the range
     *                                  1 to 65535.
     * @throws IllegalArgumentException If endpointId is null.
     */
    protected DeviceClient(String server, int port, String endpointId) {
        super(server, port, endpointId);

        this.requestHandlerRegistrations = new ArrayList<RequestHandlerRegistration>();

        final Resource reconciliationResource = new Resource.Builder()
                .endpointName(endpointId)
                .method(Resource.Method.POST)
                .name(RECONCILIATION_URL)
                .path(RECONCILIATION_URL)
                .status(Resource.Status.ADDED)
                .build();

        // Add the resource reconciliation handler
        this.requestHandlerRegistrations.add(
                new RequestHandlerRegistration(reconciliationResource, RECONCILIATION_HANDLER)
        );


        // Add the default reconciliation handler. This handler should always
        // be the last handler in the list.
        this.requestHandlerRegistrations.add(
                new RequestHandlerRegistration(null, DEFAULT_HANDLER)
        );


    }

    /**
     * Registers a {@link RequestMessageHandler} for the given resource. The first, most
     * specifically registered handler for a given path will be allowed to handle the request.
     * The {@link RequestMessageHandler#handleRequest(HttpRequestMessage) handleRequest}
     * method is called on a separate thread.
     * @param resource If null, registers a catch-all handler.
     * @param handler  The handler to call, must not be null.
     * @return true if the server should be notified of the resource update
     * @throws SecurityException        if there is a SecurityManager and the caller
     *                                  does not have {@link ClientPermission#REGISTER_HANDLER} permission
     * @throws IllegalStateException    if there is no connection to the server
     * @throws IllegalArgumentException if the same resource path is already registered
     */
    public boolean registerRequestHandler(Resource resource, RequestMessageHandler handler) {

        final SecurityManager securityManager = System.getSecurityManager();
        if (securityManager != null) {
            securityManager.checkPermission(ClientPermission.REGISTER_HANDLER);
        }

        if (handler == null) {
            return unregisterRequestHandler(resource);
        }

        synchronized (requestHandlerRegistrations) {

            if (resource == null) {

                // take out handler with null
                for (int n = requestHandlerRegistrations.size() - 1; 0 <= n; --n) {
                    final RequestHandlerRegistration reg = requestHandlerRegistrations.get(n);
                    final Resource regResource = reg.resource;
                    if (regResource == null) {
                        requestHandlerRegistrations.remove(n);
                    }
                }

                // add the default handler back in
                final RequestHandlerRegistration registration =
                        new RequestHandlerRegistration(null, handler);

                requestHandlerRegistrations.add(registration);

                // No reporting on default handler since it is not a resource
                return false;
            }

            // RequestHandlerRegistration is immutable, so make a new one
            final RequestHandlerRegistration registration =
                    new RequestHandlerRegistration(resource, handler);

            final String resourcePath = resource.getPath();

            for (int n = 0, nMax = requestHandlerRegistrations.size(); n < nMax; n++) {

                final RequestHandlerRegistration reg = requestHandlerRegistrations.get(n);
                final Resource regResource = reg.resource;

                // Null resource is already handled, so there is no need
                // to look at registrations with null resource
                if (regResource == null) continue;

                final String regResourcePath = regResource.getPath();
                if (resourcePath.equals(regResourcePath)) {
                    throw new IllegalArgumentException("resource already registered");
                }
            }

            requestHandlerRegistrations.add(registration);
            Collections.sort(requestHandlerRegistrations);

            return true;
        }
    }

    /**
     * Unregisters whatever {@link RequestMessageHandler} was registered for
     * the given resource.
     * @param resource The resource for which handlers will be un-registered.
     * @return true if the server should be notified of the resource update
     * @throws SecurityException     if there is a SecurityManager and the caller
     *                               does not have {@link ClientPermission#REGISTER_HANDLER} permission
     * @throws IllegalStateException if there is no connection to the server
     */
    public boolean unregisterRequestHandler(Resource resource) {

        final SecurityManager securityManager = System.getSecurityManager();
        if (securityManager != null) {
            securityManager.checkPermission(ClientPermission.REGISTER_HANDLER);
        }

        synchronized (requestHandlerRegistrations) {

            if (resource == null) {

                // take out handler with null
                for (int n = requestHandlerRegistrations.size() - 1; 0 <= n; --n) {
                    final RequestHandlerRegistration reg = requestHandlerRegistrations.get(n);
                    final Resource regResource = reg.resource;
                    if (regResource == null) {
                        requestHandlerRegistrations.remove(n);
                    }
                }

                // Add the default handler
                final RequestHandlerRegistration registration =
                        new RequestHandlerRegistration(null, DEFAULT_HANDLER);
                requestHandlerRegistrations.add(registration);

                // No reporting on default handler since it is not a resource
                return false;
            }

            final String resourcePath = resource.getPath();

            // don't remove reconciliation handler
            if (RECONCILIATION_URL.equals(resourcePath)) {
                return false;
            }

            for (int n = requestHandlerRegistrations.size() - 1; 0 <= n; --n) {

                final RequestHandlerRegistration reg = requestHandlerRegistrations.get(n);
                final Resource regResource = reg.resource;

                // Null resource is already handled, so there is no need
                // to look at registrations with null resource
                if (regResource == null) continue;

                final String regResourcePath = regResource.getPath();

                if (resourcePath.equals(regResourcePath)) {

                    requestHandlerRegistrations.remove(n);

                    return true;
                }

            }
            return false;
        }
    }

    /**
     * Unregisters the specified handler, if registered,
     * for all paths for which it is registered. If there are no
     * paths registered for the handler, an empty list is returned.
     * This method will not return null.
     * @param handler The handler. If null, this call has no effect.
     * @return The resources that were unregistered, or an empty list
     * @throws SecurityException     if there is a SecurityManager and the caller
     *                               does not have {@link ClientPermission#REGISTER_HANDLER} permission
     * @throws IllegalStateException if there is no connection to the server
     */
    public List<Resource> unregisterRequestHandler(RequestMessageHandler handler) {

        final SecurityManager securityManager = System.getSecurityManager();
        if (securityManager != null) {
            securityManager.checkPermission(ClientPermission.REGISTER_HANDLER);
        }

        if (handler == DEFAULT_HANDLER) {
            return Collections.EMPTY_LIST;
        }

        synchronized (requestHandlerRegistrations) {

            if (handler == null) {

                // take out handler with null
                for (int n = requestHandlerRegistrations.size() - 1; 0 <= n; --n) {
                    final RequestHandlerRegistration reg = requestHandlerRegistrations.get(n);
                    final Resource regResource = reg.resource;
                    if (regResource == null) {
                        requestHandlerRegistrations.remove(n);
                    }
                }

                // Add the default handler
                final RequestHandlerRegistration registration =
                        new RequestHandlerRegistration(null, DEFAULT_HANDLER);
                requestHandlerRegistrations.add(registration);

                // No reporting on default handler since it is not a resource
                return Collections.EMPTY_LIST;
            }

            List<Resource> removedResources = new ArrayList<Resource>();

            for (int n = requestHandlerRegistrations.size() - 1; 0 <= n; --n) {

                final RequestHandlerRegistration reg = requestHandlerRegistrations.get(n);
                final RequestMessageHandler reqisteredHandler = reg.handler;

                final Resource registeredResource = reg.resource;

                // don't remove reconciliation handler
                if (registeredResource != null &&
                        RECONCILIATION_URL.equals(registeredResource.getPath())) {
                    continue;
                }

                if (handler == reqisteredHandler) {

                    if (registeredResource != null) {

                        requestHandlerRegistrations.remove(n);
                        removedResources.add(registeredResource);

                    } else {

                        // registeredResource was null, so this is the
                        // default handler. Reset the default handler, but
                        // don't remove or report.
                        final RequestHandlerRegistration registration =
                                new RequestHandlerRegistration(null, DEFAULT_HANDLER);
                        requestHandlerRegistrations.set(n, registration);
                    }
                }

            }

            return removedResources;
        }
    }

    /**
     * Lookup a RequestMessageHandler for the given resource.
     * Will not return null.
     * @param resource The resource to look up.
     * @return The RequestMessageHandler, which may be the default handler.
     */
    public RequestMessageHandler getRequestHandler(Resource resource) {

        if (resource == null) return DEFAULT_HANDLER;

        synchronized (requestHandlerRegistrations) {

            final String path = resource.getPath();
            final int methodBitMask = Resource.Method.getBitMask(resource.getMethods());
            return getRequestHandler(path, methodBitMask);

        }
    }

    /*
     * @param path The resource path to look up.
     * @param methodBitMask A bit mask of the methods to lookup
     * @return The RequestMessageHandler for the given resource.
     */
    private RequestMessageHandler getRequestHandler(String path, int methodBitMask) {

        //
        // Note that this loop depends on requestHandlerRegistrations being
        // sorted such that longer contexts come first in the list.
        // Secondary sort is methods, with fewer methods coming first.
        // That is, [GET] comes before [GET, PUT]. If there are
        // registered handlers [{'/foo/bar', [GET]}, {'/foo/bar', [GET,PUT]}]
        // and we're looking for {'/foo/bar', [GET]}, we should get the
        // first one. But if we're looking for {'/foo/bar', [PUT]}, we
        // should get {'/foo/bar', [GET,PUT]}
        //
        for (int n = 0, nMax = requestHandlerRegistrations.size(); n < nMax; n++) {

            final RequestHandlerRegistration registration
                    = requestHandlerRegistrations.get(n);

            final String pathThat =
                    registration.resource != null
                            ? registration.resource.getPath() : null;

            if (path != null && pathThat != null) {
                int diff = pathThat.length() - path.length();
                if (diff == 0) {
                    if (path.equals(pathThat)) {
                        // Check that resource methods are a subset of
                        // registration.resource methods.
                        final int methodsThat = Resource.Method.getBitMask(registration.resource.getMethods());
                        if ((methodBitMask & methodsThat) == methodBitMask) {
                            return registration.handler;
                        }
                    }

                } else if (diff < 0) {
                    if (path.startsWith(pathThat)) {
                        // Check that resource methods are a subset of
                        // registration.resource methods.
                        final int methodsThat = Resource.Method.getBitMask(registration.resource.getMethods());
                        if ((methodBitMask & methodsThat) == methodBitMask) {
                            return registration.handler;
                        }
                    }
                }
                // else {
                //     diff > 0, which means that registration.resource
                //     is more specific than resource
                // }
            } else if (registration.resource == null) {
                // if we get to the null resource handler, return it.
                return registration.handler;
            }
        }


        // We should never get here.
        return DEFAULT_HANDLER;
    }

    // Only for unit test usage. Note that this returns a new List
    // every time, so it is best to not call this method from production
    // code!
    List<RequestHandlerRegistration> getRequestHandlerRegistrations() {
        return Collections.unmodifiableList(requestHandlerRegistrations);
    }

    //
    // This is the default behavior for a resource that hasn't registered a handler.
    //
    static final RequestMessageHandler DEFAULT_HANDLER =
            new RequestMessageHandler() {

                public HttpResponseMessage handleRequest(HttpRequestMessage request) throws Exception {
                    HttpResponseMessage.Builder builder =
                            new HttpResponseMessage.Builder()
                                    .requestId(request.getId())
                                    .source(request.getDestination())
                                    .statusCode(StatusCode.NOT_FOUND);
                    return builder.build();
                }
            };

    static final class RequestHandlerRegistration implements Comparable<RequestHandlerRegistration> {
        final Resource resource;
        final RequestMessageHandler handler;

        RequestHandlerRegistration(Resource context, RequestMessageHandler handler) {
            this.resource = context;
            this.handler = handler;
        }

        // Note that other code depends on RequestHandlerRegistrations being
        // sorted such that longer contexts come first.
        public int compareTo(RequestHandlerRegistration o) {

            if (resource == o.resource) return 0;
            if (resource == null) return 1;
            if (o.resource == null) return -1;

            final String firstPath = resource.getPath();
            final String secondPath = o.resource.getPath();

            if (firstPath == null && secondPath == null) return 0;
            if (firstPath == null && secondPath != null) return 1;
            if (firstPath != null && secondPath == null) return -1;

            int diff = secondPath.length() - firstPath.length();
            if (diff != 0) return diff;

            return secondPath.compareTo(firstPath);
        }

        @Override
        public boolean equals(Object obj) {

            // equals and compareTo have to agree.

            if (obj == null || obj.getClass() != this.getClass()) return false;
            final RequestHandlerRegistration other = (RequestHandlerRegistration)obj;

            if (resource != null && other.resource != null) {
                final String firstPath = resource.getPath();
                final String secondPath = other.resource.getPath();

                if (firstPath != null) {
                    return firstPath.equals(secondPath);
                }
            }
            return false;
        }

        @Override
        public String toString() {
            return "RequestHandlerRegistration for resource '" + resource + "'";
        }

    }

    // Resource Directory
    private static final String RECONCILIATION_URL = "/manage/resources/";

    private final RequestMessageHandler RECONCILIATION_HANDLER =
            new RequestMessageHandler() {
                @Override
                public HttpResponseMessage handleRequest(HttpRequestMessage request) throws Exception {

                    if (!"POST".equalsIgnoreCase(request.getMethod())) {
                        HttpResponseMessage.Builder builder =
                                new HttpResponseMessage.Builder();

                        builder.source(request.getDestination())
                                .destination(request.getSource())
                                .requestId(request.getId())
                                .statusCode(StatusCode.METHOD_NOT_ALLOWED);

                        final HttpResponseMessage responseMessage = builder.build();
                        return responseMessage;
                    }

                    ResourcesReportMessage.Builder builder = new ResourcesReportMessage.Builder();
                    builder.endpointName(getEndpointId())
                            .source(getEndpointId())
                            .id(UUID.randomUUID().toString())
                            .reconcile()
                            .reconciliationMark(getReconciliationMark());


                    for (int n = 0, nMax = requestHandlerRegistrations.size(); n < nMax; n++) {

                        RequestHandlerRegistration registration = requestHandlerRegistrations.get(n);

                        if (registration.resource == null) continue;
                        if (registration.resource.getPath() == null) continue;
                        if (RECONCILIATION_URL.equals(registration.resource.getPath()))
                            continue;

                        builder.add(registration.resource);

                    }

                    ResourcesReportMessage resourcesReportMessage = builder.build();
                    JsonObject json = resourcesReportMessage.toJSON();
                    byte[] body = null;
                    try {
                        body = json.toString().getBytes("UTF-8");
                    } catch (UnsupportedEncodingException e) {
                        // UTF-8 is required so this will never happen.
                        // But findbugs complains that body might be null,
                        // so initialize it anyway.
                        body = new byte[0];
                    }

                    HttpResponseMessage responseMessage =
                            new HttpResponseMessage.Builder()
                                    .source(request.getDestination())
                                    .destination(request.getSource())
                                    .requestId(request.getId())
                                    .body(body)
                                    .statusCode(StatusCode.ACCEPTED)
                                    .build();

                    return responseMessage;
                }
            };

    /**
     * Calculate the reconciliation mark value for the registered resources
     * @return The calculated valiue of the reconciliation mark
     */
    protected String getReconciliationMark() {

        final List<String> paths = new ArrayList<String>();
        for (int n = 0, nMax = requestHandlerRegistrations.size(); n < nMax; n++) {

            final RequestHandlerRegistration registration = requestHandlerRegistrations.get(n);

            if (registration.resource == null) continue;
            if (registration.resource.getPath() == null) continue;
            if (RECONCILIATION_URL.equals(registration.resource.getPath()))
                continue;

            final String path = registration.resource.getPath();
            paths.add(path);
        }
        return ResourcesReportMessage.getMD5ofList(paths);
    }


}
