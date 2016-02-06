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

import com.oracle.json.*;
import java.io.StringWriter;

import java.util.Arrays;
import com.oracle.iot.message.Base64;
import java.util.Date;
import java.util.logging.Logger;

/**
 * DirectActivationResponse
 */
public class DirectActivationResponse {

    private static final String FIELD_ENDPOINT_ID       = "endpointId";
    private static final String FIELD_ACTIVATION_TIME   = "activationTime";
    private static final String FIELD_ENDPOINT_STATE    = "endpointState";
    private static final String FIELD_CERTIFICATE       = "certificate";

    private String endpointId;
    private Date activationTime;
    private String endpointState;

    private static final Logger logger = Logger.getAnonymousLogger();

    private byte[] certificate;

    public byte[] getCertificate() {
        return certificate;
    }

    public void setCertificate(byte[] certificate) {
        this.certificate = certificate;
    }

    public DirectActivationResponse() {}

    public Date getActivationTime() {
        return activationTime;
    }

    public void setActivationTime(Date activationTime) {
        this.activationTime = activationTime;
    }

    public String getEndpointState() {
        return endpointState;
    }

    public void setEndpointState(String endpointState) {
        this.endpointState = endpointState;
    }

    public String getEndpointId() {
        return endpointId;
    }

    public void setEndpointId(String endpointId) {
        this.endpointId = endpointId;
    }

    @Override
    public String toString() {
        return "ActivationResponse{" +
                "endpointId='" + endpointId + '\'' +
                ", activationTime=" + activationTime +
                ", endpointState='" + endpointState + '\'' +
                ", certificate=" + Arrays.toString(certificate) +
                '}';
    }

    public static DirectActivationResponse fromJson(final JsonObject jsonObject) {
        final DirectActivationResponse response = new DirectActivationResponse();
        response.endpointId = jsonObject.getString(FIELD_ENDPOINT_ID);

        // TODO: implement a Date converter
        //activationTime = getJsonDate(jsonObject.get(FIELD_ACTIVATION_TIME));
        response.endpointState = jsonObject.getString(FIELD_ENDPOINT_STATE);

        // TODO: we're manually replacing EOLs here, need to figure out a better solution
        JsonString fieldCertificate = jsonObject.getJsonString(FIELD_CERTIFICATE);
        if (fieldCertificate != null) {
            final String certificateString = fieldCertificate.toString().replace("\r", "").replace("\n", "");
            response.certificate = Base64.getDecoder().decode(certificateString);
        } else {
            response.certificate = new byte[0];
        }

        return response;
    }
    
    public String toJson() {
        final StringWriter stringWriter = new StringWriter();
        final JsonWriter jsonWriter = Json.createWriter(stringWriter);
        final JsonObjectBuilder objectBuilder = Json.createObjectBuilder();

        objectBuilder.add(FIELD_ENDPOINT_ID, endpointId);
        objectBuilder.add(FIELD_ENDPOINT_STATE, endpointState);
        objectBuilder.add(FIELD_ACTIVATION_TIME, activationTime.toString());

        jsonWriter.writeObject(objectBuilder.build());
        jsonWriter.close();

        return stringWriter.toString();
    }
}
