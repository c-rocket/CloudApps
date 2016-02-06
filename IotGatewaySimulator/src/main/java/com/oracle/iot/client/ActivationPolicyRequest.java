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

import com.oracle.json.Json;
import com.oracle.json.JsonObjectBuilder;
import com.oracle.json.JsonWriter;

import java.io.StringWriter;

/**
 * ActivationPolicyRequest
 */
public class ActivationPolicyRequest {

    public static class DeviceAttributes {

        private static final String FIELD_OS_NAME = "OSName";
        private static final String FIELD_OS_VERSION = "OSVersion";
        private String osVersion;
        private String osName;

        @Override
        public String toString() {
            return "DeviceAttributes {" +
                    "osVersion='" + osVersion + '\'' +
                    ", osName='" + osName + '\'' +
                    '}';
        }

        public String getOsName() {
            return osName;
        }

        public void setOsName(String osName) {
            this.osName = osName;
        }

        public String getOsVersion() {
            return osVersion;
        }

        public void setOsVersion(String osVersion) {
            this.osVersion = osVersion;
        }

        public JsonObjectBuilder toJson() {
            final JsonObjectBuilder objectBuilder = Json.createObjectBuilder();
            objectBuilder.add(FIELD_OS_NAME, osName);
            objectBuilder.add(FIELD_OS_VERSION, osVersion);
            return objectBuilder;
        }
    }

    private static final String FIELD_DEVICE_ATTRIBUTES = "deviceAttributes";

    private DeviceAttributes  deviceAttributes;

    public DeviceAttributes getDeviceAttributes() {
        return deviceAttributes;
    }

    public void setDeviceAttributes(DeviceAttributes deviceAttributes) {
        this.deviceAttributes = deviceAttributes;
    }

    @Override
    public String toString() {
        return "ActivationPolicyRequest{" +
                "deviceAttributes=" + deviceAttributes +
                '}';
    }

    public String toJson() {

        final StringWriter stringWriter = new StringWriter();
        final JsonWriter jsonWriter = Json.createWriter(stringWriter);
        final JsonObjectBuilder objectBuilder = Json.createObjectBuilder();

        if (deviceAttributes != null)
            objectBuilder.add(FIELD_DEVICE_ATTRIBUTES, deviceAttributes.toJson().build());

        jsonWriter.writeObject(objectBuilder.build());
        jsonWriter.close();

        return stringWriter.toString();
    }

}
