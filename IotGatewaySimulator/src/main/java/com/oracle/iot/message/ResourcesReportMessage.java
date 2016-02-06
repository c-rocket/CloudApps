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

package com.oracle.iot.message;

import com.oracle.json.Json;
import com.oracle.json.JsonArray;
import com.oracle.json.JsonArrayBuilder;
import com.oracle.json.JsonObject;
import com.oracle.json.JsonObjectBuilder;
import oracle.iot.message.Message;
import oracle.iot.client.device.Resource;

import java.io.ByteArrayInputStream;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.Collator;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.TreeSet;
import java.util.logging.Logger;

/**
 * ResourcesReportMessage extends Message class. It is used for sending sync information between
 * Resource Directory on Gateway and Resource Directory on server. This class is immutable.
 * {
 *   reportType: 'typeOfTheReport',
 *   reconciliationMark: 'hash for resource available  on client at moment of message generation'
 *   resources: [{
 *       path: 'pathToResource',
 *       resourceStatus: 'STATUS_OF_RESOURCE',
 *       description: 'resourceDescription',
 *       (other resource properties. TBD)
 *   },
 *   {
 *       path: 'pathToResource',
 *       resourceStatus: 'STATUS_OF_RESOURCE',
 *       description: 'resourceDescription',
 *       (other resource properties. TBD)
 *   },
 *   ...
 *   ]
 *  }
 */
public final class ResourcesReportMessage extends Message {

    /**
     * Enumeration for choosing type of the resource directory message. Allowed
     * statuses are UPDATE, DELETE, RECONCILIATION.
     */
    public enum Type {
        UPDATE,
        DELETE,
        RECONCILIATION 
    }

    /**
     * List of resources described by the message
     */
    private ArrayList<Resource> resources;

    /**
     * Type of the message
     */
    private Type type;
    
    /**
     * hash for resource available  on client at moment of message generation
     */
    private String reconciliationMark;

    /**
     * Endpoint ID, for that the resources shall be updated
     */
    private String endpointName;

    /**
     * Message constructor takes message builder and set values to each field.
     * If other values are {@code null}, sets a default value.
     *
     * @param builder {@link ResourcesReportMessage.Builder} containing all information for Resource Directory message.
     * @throws IllegalArgumentException if the operation is not set.
     */
    private ResourcesReportMessage(Builder builder) {
        super(builder);
        
        if (builder.endpointName == null || builder.endpointName.isEmpty() ||
           (builder.resources.isEmpty() && (builder.type == Type.UPDATE))) {
            throw new IllegalArgumentException("ResourcesReportMessage resources cannot be empty.");
        } else {
            this.endpointName = builder.endpointName;
            this.resources = new ArrayList(builder.resources);
            this.type = builder.type;
            this.reconciliationMark = builder.reconciliationMark == null ? "" 
                                      : builder.reconciliationMark;
        }
    }

    /**
     * Calculates the MD5 hash value for a list of Strings.
     * The String list is being alphabetical ordered before the calculation
     * @param resStrings List os Strings
     * @return hash value
     */
    public static String getMD5ofList(List<String> resStrings) {

        // NOTE: This implementation must match the implementation on the server!
        Collection<String> resCol
                = new TreeSet<String>(Collator.getInstance());
        resCol.addAll(resStrings);

        String[] ordered = new String[resCol.size()];
        resCol.toArray(ordered);
        MessageDigest dg;
        try {
            dg = MessageDigest.getInstance("MD5");
            for (String s : ordered) {
                dg.update(s.getBytes("UTF-8"));
            }
            byte[] hashedBytes = dg.digest();
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < hashedBytes.length; ++i) {
                sb.append(Integer.toHexString((hashedBytes[i] & 0xFF) | 0x100).substring(1, 3));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            Logger.getAnonymousLogger().severe(e.getMessage());
        } catch (UnsupportedEncodingException e) {
            Logger.getAnonymousLogger().severe(e.getMessage());
        }
        return null;
    }
//    
//    @Override
//    public MessageBuilder getMessageBuilder() {
//        return new ResourcesReportMessage.Builder().copy(this);
//    }
//    
    /**
     * Builder extends {@link MessageBuilder} class. {@link ResourcesReportMessage} class is immutable.
     * A builder is required when creating new {@link ResourcesReportMessage}.
     */
    public static final class Builder extends MessageBuilder<Builder> {

        /**
         * {@link List} of resources. Each Resource object is represented by a path which is the path to the resource.
         */
        private ArrayList<Resource> resources = new ArrayList<Resource>();
        private Type type = Type.UPDATE;
        private String endpointName = null;
        private String reconciliationMark = "";

        public Builder() {

        }

        /**
         * Set the type of the {@link ResourcesReportMessage} to register.
         *
         * @return Builder with already set operation field
         */
        public final Builder delete() {
            this.type = Type.DELETE;
            if (!resources.isEmpty()) {
                resources.clear();
            }
            return self();
        }

        /**
         * Sets the type of the {@link Resource} to register and
         * adds the resource in the list
         *
         * @param ri the resource to register.
         * @return Builder with already set operation field
         */
        public final Builder register(Resource ri) {
            this.type = Type.UPDATE;
//            ri.setStatus(oracle.iot.client.device.Resource.Status.ADDED);
            this.resources.add(ri);
            return self();
        }

        /**
         * Set the type of the {@link Resource} to remove and
         * adds the resource in the list
         *
         * @param ri the resource to remove.
         * @return Builder with already set operation field
         */
        public final Builder remove(Resource ri) {
            this.type = Type.DELETE;
//            ri.setStatus(oracle.iot.client.device.Resource.Status.REMOVED);
            this.resources.add(ri);
            return self();
        }

        /**
         * Add a resource in the list
         *
         * @param ri the resource to add.
         * @return Builder with already set operation field
         */
        public final Builder add(Resource ri) {
            this.type = Type.UPDATE;
            this.resources.add(ri);
            return self();
        }

        /**
         * Adds all resources.
         *
         * @param resources Resources to be added.
         * @return Builder with already set resources field.
         */
        public final Builder resources(List<Resource> resources) {
            this.type = Type.UPDATE;
            this.resources.addAll(resources);
            return self();
        }

        /**
         * Set the reconciliation mark
         * @param rM - reconciliation mark
         * @return Builder with already set reconciliationMark field.
         */
        public final Builder reconciliationMark(String rM) {
            this.reconciliationMark = rM;
            return self();
        }

        /**
         * Set the endpointName this message is for.
         *
         * @param endpointName the endpoint Name for this message.
         * @return Builder with endpointName set.
         */
        public final Builder endpointName(String endpointName) {
            this.endpointName = endpointName;
            return self();
        }

        /**
         * Sets the message type to RECONCILIATION.
         *
         * @return  Builder with message type set to RDMessageType.RECONCILIATION.
         */
        public final Builder reconcile() {
            this.type = Type.RECONCILIATION;
            return self();
        }

        /**
         * Returns current instance of {@link ResourcesReportMessage.Builder}.
         *
         * @return Instance of {@link ResourcesReportMessage.Builder}
         */
        @Override
        protected Builder self() {
            return this;
        }

        /**
         * Creates new instance of {@link ResourcesReportMessage} using values from {@link ResourcesReportMessage.Builder}.
         *
         * @return Instance of {@link ResourcesReportMessage}
         */
        @Override
        public ResourcesReportMessage build() {
            return new ResourcesReportMessage(this);
        }

        /**
         * Copy another {@link ResourcesReportMessage} to this.
         *
         * @param message message to copy
         * @return ResourceDirectory.Builder
         * @throws IllegalArgumentException when instance of other message than {@link ResourcesReportMessage} is passed.
         */
        @Override
        public final Builder copy(Message message) {
            if (message instanceof ResourcesReportMessage) {
                ResourcesReportMessage msg = (ResourcesReportMessage) message;
                super.copy(msg);
                this.endpointName = msg.endpointName;
                this.resources = new ArrayList(msg.resources);
                this.type = msg.type;
                this.reconciliationMark = msg.reconciliationMark;
                this.endpointName = msg.endpointName;

            } else {
                throw new IllegalArgumentException("Can not copy a different type of message");
            }
            return self();
        }

        /**
         * Method to convert a JSON format string to a {@link ResourcesReportMessage}.
         *
         * @param jsonObject the jsonObject to convert
         * @return A new instance of {@link ResourcesReportMessage.Builder} constructed from the JSON string.
         */
        @Override
        public final Builder fromJSON(JsonObject jsonObject) {
            super.fromJSON(jsonObject);
            final JsonObject payload = jsonObject.getJsonObject("payload");
            final JsonObject payloadValue = payload.getJsonObject("value");

            final String rt = Utils.jsonToString(payloadValue.getJsonString("reportType"));
            this.type = Type.valueOf(Type.class, rt);
            this.reconciliationMark = Utils.jsonToString(payloadValue.getJsonString("reconciliationMark"));
            this.endpointName = Utils.jsonToString(payloadValue.getJsonString("endpointName"));
            final JsonArray resArray = payloadValue.getJsonArray("resources");
            if (resArray != null) {
                for (int i = 0; i < resArray.size(); i++) {
                    resources.add(new Resource.Builder().fromJSON(resArray.getJsonObject(i)).build());
                }
            }

            return self();
        }
    }

    /**
     * Method to return MessageBuilder that is populated with content of this instance.
     *
     * @return returns {@link Message.MessageBuilder}.
     */
    @Override
    public MessageBuilder getMessageBuilder() {
        return new ResourcesReportMessage.Builder().copy(this);
    }


    /**
     * Exports data from {@link ResourcesReportMessage} to {@link String} using JSON interpretation of the message.
     *
     * @return JSON interpretation of the message as {@link String}.
     */
    @Override
    public String toString() {
        return toJSON().toString();
    }

    /**
     * Method to export the Resource Directory message to {@link JsonObject}.
     *
     * @return JSON interpretation of {@link ResourcesReportMessage}
     */
    @Override
    public JsonObject toJSON() {
        final JsonObjectBuilder builder = Utils.commonFieldsToJSON(this);
        final JsonObjectBuilder payload = Json.createObjectBuilder();
        final JsonObjectBuilder value = Json.createObjectBuilder();
        final JsonArrayBuilder resource = Json.createArrayBuilder();

        builder.add("type", Message.Type.RESOURCES_REPORT.name());
        payload.add("type", "JSON");
        value.add("reportType", this.type.name());
        if ((this.reconciliationMark != null) && !this.reconciliationMark.isEmpty()) {
            value.add("reconciliationMark", this.reconciliationMark);
        }
        value.add("endpointName", this.endpointName);

        for (Resource r: this.resources) {
            resource.add(r.toJSON());
        }

        value.add("resources", resource.build());
        payload.add("value", value.build());
        builder.add("payload", payload.build());
        
        return builder.build();
    }

    @Override
    public Message.Type getType() {
        return Message.Type.RESOURCES_REPORT;
    }

    /**
     * Returns List of resources described in the message. 
     *
     * @return List of resources, never {@code null}
     */
    public List<Resource> getResources() {
        return resources;
    }

    /**
     * Returns type of the message (Could be UPDATE or DELETE)
     * 
     * @return RDMessageType 
     */
    public Type getMessageType() {
        return type;
    }

    public String getReconciliationMark() {
        return reconciliationMark;
    }

    /**
     * Returns endpoint id for the message
     * @return endpoint id
     */
    public String getEndpointName() {
        return endpointName;
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;

        ResourcesReportMessage that = (ResourcesReportMessage) o;

        if (!this.type.equals(that.type)) return false;
        if (!resources.equals(that.resources)) return false;
        if (!this.reconciliationMark.equals(that.reconciliationMark)) return false;

        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + type.hashCode();
        result = 31 * result + resources.hashCode();
        return result;
    }

    public static class ReportResponse {
        public static enum ResponseStatus {
            OK,
            BAD_REPORT,
            RECONCILIATION 
        }
        private String endpointName;
        private ResponseStatus status;

        private ReportResponse(Builder builder) {

            if ((builder.endpointName == null) || builder.endpointName.isEmpty()
                    ) {
                throw new IllegalArgumentException("Resource name, path cannot be null or empty.");
            }

            this.endpointName = builder.endpointName;

            if (builder.status != null) {
                this.status = builder.status;
            } else {
                this.status = ResponseStatus.OK;
            }
        }
        
        public JsonObject toJSON() {
            final JsonObjectBuilder res = Json.createObjectBuilder();
            res.add("endpointName", this.endpointName);

            if (this.status != null) {
                res.add("status", this.status.name());
            } else {
                res.add("status", ResponseStatus.OK.name());
            }

            return res.build();
        }
        
        public String getEndpointName() {
            return endpointName;
        }
        
        public ResponseStatus getStatus() {
            return status;
        }
        
        public String toString() {
            return toJSON().toString();
        }
        
        /**
         * {@link Resource} class is immutable. A builder is required when
         * creating new {@link Resource}.
         */
        public static final class Builder {
            private String endpointName;

            private ResponseStatus status;
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

            public Builder status(ResponseStatus status) {
                this.status = status;
                return this;
            }

            public final Builder fromString(String str) {
                if ((str == null) || str.isEmpty()) {
                    return this;
                }
                return fromJSON(Json.createReader(new ByteArrayInputStream(str.getBytes(StandardCharsets.UTF_8))).readObject());
            }
            
            public final Builder fromJSON(JsonObject resObject) {
                final String st = Utils.jsonToString(resObject.getJsonString("status"));

                if (st != null) {
                    this.status = st.equals("OK") ? ResponseStatus.OK : 
                            st.equals("BAD_REPORT") ? ResponseStatus.BAD_REPORT : 
                                                    ResponseStatus.RECONCILIATION;
                }
                return this;
            }

            public ReportResponse build() {
                return new ReportResponse(this);
            }
        }
    }
}
