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

import com.oracle.json.Json;
import com.oracle.json.JsonObject;
import com.oracle.json.JsonObjectBuilder;
import com.oracle.json.JsonString;
import oracle.iot.message.Message;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

/**
 * This class represents a resource for an endpoint. 
 */
public class Resource {
    /**
     * Method supported by a Resource
     */
    public enum Method {
        GET(1),
        POST(2),
        PUT(4),
        DELETE(8);

        private int value;

        private Method(int value) {
            this.value = value;
        }

        public int value() {
            return value;
        }

        /**
         * Returns the Method value for the String
         *
         * @param name String shall be "GET" or "POST"
         *
         * @return Method
         */
        public static Method createValue(String name) {
            try {
                return valueOf(name);
            } catch (IllegalArgumentException e) {
                return null;
            }
        }

        /**
         * Returns a list of the Method values for the bit string
         *
         * @param bitMask bit string
         *
         * @return list of the Method values
         */
        public static List<Method> createValue(int bitMask) {
            List<Method> methods = new ArrayList<Method>();

            for (Method m : Method.values()) {
                if ((bitMask & m.value()) != 0) {
                    methods.add(m);
                }
            }

            return methods;
        }

        /**
         * Returns a bit mask for a list of the Method values
         *
         * @param methods list of the Method values
         *
         * @return int bit mask
         */
        public static int getBitMask(List<Method> methods) {
            int result = 0;

            for (Method m : methods) {
                result = result + m.value();
            }

            return result;
        }

        /**
         * Returns String for a list of the Method values
         *
         * @param methods list of the Method values.
         * @return String a comma-delimited string of method names or an empty string if methods is empty or
         *                {@code null}.
         */
        public static String listToString(List<Method> methods) {
            StringBuilder result = new StringBuilder();

            if ((methods != null) && !methods.isEmpty()) {
                for (Method method : methods) {
                    result.append(result.length() == 0 ? "" : ",").append(method.name());
                }
            }

            return result.toString();
        }

        /**
         * Converts string of the method names into a list of the Method values.  Returns an empty list methods is
         * {@code null} or empty.
         *
         * @param methods string of methods
         *
         * @return list of the Method values
         */
        public static List<Method> stringToList(String methods) {
            List<Method> result = new ArrayList<Method>();

            if ((methods != null) && !methods.isEmpty()) {
                final StringTokenizer methodsParser = new StringTokenizer(methods, ",");

                while (methodsParser.hasMoreTokens()) {
                    Method method = Method.createValue(methodsParser.nextToken());

                    if (method != null) {
                        result.add(method);
                    }
                }
            }

            return result;
        }
    };

    /**
     * Enumeration for choosing status of the resource directory item.  Allowed statuses are ADDED, REMOVED.
     */
    public enum Status {
        ADDED,
        REMOVED
    }

    /**
     * The endpoint name or ID this resource is for.
     */
    private String endpointName;

    /**
     * Resource name. This name should describe the resource in free form.
     */
    private String name;

    /**
     * Resource status.
     */
    private Status status;

    /**
     * Path of the resource
     */
    private String path;

    /**
     * Methods supported by the resource
     */
    private List<Method> methods;

    private Resource(Builder builder) {

        if ((builder.name == null) || builder.name.isEmpty() ||
                (builder.path == null) || builder.path.isEmpty()) {
            throw new IllegalArgumentException("Resource name, path cannot be null or empty.");
        }

        this.endpointName = builder.endpointName;
        this.name = builder.name;
        this.path = builder.path;

        if (builder.status != null) {
            this.status = builder.status;
        } else {
            this.status = Status.ADDED;
        }

        if (this.status != Status.REMOVED) {
            if ((builder.methods == null) || builder.methods.isEmpty()) {
                throw new IllegalArgumentException("Resource methods cannot be null or empty.");
            } else {
                this.methods = builder.methods;
            }
        }
    }

    /**
     * {@link Resource} class is immutable. A builder is required when
     * creating new {@link Resource}.
     */
    public static final class Builder {
        /**
         * Resource's endpoint name or ID.
         * RD TODO: Is endpointName needed?
         */
        private String endpointName;

        /**
         * URI at which the resource can be found.  This must be unique for the endpoint.
         */
        private String path;

        /**
         * Resource name or description in free form.
         * It could be used for resource lookup.
         */
        private String name;

        /**
         * Resource status. Defines what action shall be done with it.
         * (ADDED, REMOVED)
         */
        private Resource.Status status;

        private List<Method> methods;

        /**
         * Constructor - does nothing.
         */
        public Builder() {
        }

        /**
         * Sets the name or ID of the resource's endpoint.
         *
         * @param endpointName name or ID of the resource's endpoint.
         * @return Builder with the endpointName added.
         */
        public Builder endpointName(String endpointName) {
            this.endpointName = endpointName;
            return this;
        }

        /**
         * Sets the path of the resource.  path must be unique for all resources within an endpoint.
         *
         * @param path Uri of the resource.  Must be unique and must not be {@code null}.
         * @return Builder with already set path field.
         */
        public Builder path(String path) {
            this.path = path;
            return this;
        }

        /**
         * Sets the name of the resource.
         *
         * @param name name of the resource.
         * @return Builder with added name
         */
        public Builder name(String name) {
            this.name = name;
            return this;
        }

        /**
         * Sets the status value for the resource.  The default is Resource.Status.ADDED if no status or a
         * {@code null} status is specified.
         *
         * @param status the status of the resource.
         * @return a Builder with the specified status added.
         */
        public Builder status(Resource.Status status) {
            this.status = status;
            return this;
        }

        /**
         * Sets the method value for the resource.  The default is an empty list if no method or a {@code null}
         * method is specified.
         *
         * @param method status of the resource
         * @return a Builder with the specified method added.
         */
        public Builder method(Resource.Method method) {
            if (this.methods == null) {
                this.methods = new ArrayList();
            }

            this.methods.add(method);
            return this;
        }

        /**
         * Sets the method values for the resource.  The default is an empty list if no method or a {@code null}
         * methods is specified.
         *
         * @param methods a {@link List} of methods supported by the resource.
         * @return a Builder with the specified methods added.
         */
        public Builder methods(List<Method> methods) {
            if (this.methods == null) {
                this.methods = new ArrayList();
            }

            if (methods != null) {
                for (Resource.Method method : methods) {
                    this.methods.add(method);
                }
            }

            return this;
        }

        /**
         * Method to convert a JSON format string to a {@link Resource}.
         *
         * @param resObject the jsonObject to convert
         * @return A new instance of {@link Resource.Builder} constructed
         * from the JSON string.
         */
        public final Builder fromJSON(JsonObject resObject) {
            final String st = jsonToString(resObject.getJsonString("status"));

            if (st != null) {
                this.status = st.equals("ADDED") ? Resource.Status.ADDED : Resource.Status.REMOVED;
            }

            this.methods =
                    Resource.Method.stringToList(jsonToString(resObject.getJsonString("methods")));
            this.endpointName = jsonToString(resObject.getJsonString("endpointName"));
            this.name = jsonToString(resObject.getJsonString("name"));
            this.path = jsonToString(resObject.getJsonString("path"));

            return this;
        }

        /**
         * Builds (creates) new instance of {@link Resource}.
         *
         * @return new instance of {@link Resource}
         */
        public Resource build() {
            return new Resource(this);
        }

    }

    public JsonObject toJSON() {
        final JsonObjectBuilder res = Json.createObjectBuilder();
        if ((this.endpointName != null) && !this.endpointName.isEmpty()) {
            res.add("endpointName", this.endpointName);
        }
        res.add("name", this.name);
        res.add("path", this.path);

        if (this.status != null) {
            res.add("status", this.status.name());
        } else {
            res.add("status", Status.ADDED.name());
        }

        if (this.methods != null) {
            res.add("methods", Method.listToString(this.methods));
        }

        return res.build();
    }

    public final void setStatus(Resource.Status st) {
        this.status = st;
    }

    public final String getEndpointName() {
        return this.endpointName;
    }

    public final String getName() {
        return this.name;
    }

    public final String getPath() {
        return this.path;
    }

    public final Resource.Status getStatus() {
        return this.status;
    }

    public final List<Method> getMethods() {
        return this.methods;
    }

    /**
     * {@inheritDoc}
     */
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        Resource that = (Resource) o;

        if (!this.name.equals(that.name)) {
            return false;
        }
        if (!this.path.equals(that.path)) {
            return false;
        }
        if (!this.status.equals(that.status)) {
            return false;
        }

        if ((this.methods != null) && !this.methods.equals(that.methods)) {
            return false;
        }

        return true;
    }

    /**
     * {@inheritDoc}
     */
    public int hashCode() {
        int result = name.hashCode();
        result = 31 * result + path.hashCode();

        if (this.status != null) {
            result = 31 * result + status.hashCode();
        }

        if (this.methods != null) {
            result = 31 * result + methods.hashCode();
        }

        return result;
    }

    /**
     * Method converts Json string to java String
     * @param jsonString Json interpretation of string
     * @return String value
     */
    private static String jsonToString(JsonString jsonString) {
        if (jsonString != null) {
            return jsonString.getString();
        }
        return null;
    }    
}
