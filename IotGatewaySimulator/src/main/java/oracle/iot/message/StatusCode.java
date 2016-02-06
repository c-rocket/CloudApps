/*
 * Copyright (c) 2014, 2015, Oracle and/or its affiliates. All rights reserved.
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

package oracle.iot.message;

/**
 * Enum for handling HTTP status codes and tests.
 */
public enum StatusCode {
    OK(200,"OK"), 
    CREATED(201, "Created"), 
    ACCEPTED(202, "Accepted"), 
    NON_AUTHORITATIVE_INFORMATION(203,"Non Authoritative Information"), 
    NO_CONTENT(204, "No Content"), 
    BAD_REQUEST(400, "Bad Request"), 
    UNAUTHORIZED(401, "Unauthorized"), 
    PAYMENT_REQUIRED(402, "Payment Required"), 
    FORBIDDEN(403, "Forbidden"), 
    NOT_FOUND(404, "Not Found"), 
    METHOD_NOT_ALLOWED(405, "Method Not Allowed"), 
    NOT_ACCEPTABLE(406, "Not Acceptable"), 
    REQUEST_TIMEOUT(408, "Request Timeout"),
    CONFLICT(409, "Conflict"),
    INTERNAL_SERVER_ERROR(500, "Internal Server Error"), 
    BAD_GATEWAY(502, "Bad Gateway"), 
    SERVICE_UNAVAILABLE(503, "Service Unavailable");

    /** Status code */
    private int code;
    /** Status text */
    private String description;

    /**
     * Construct new instance of StatusCode
     * @param code Status code
     * @param description Status text
     */
    private StatusCode(int code, String description) {
        this.code = code;
        this.description = description;
    }

    /**
     * Returns HTTP status code.
     * @return HTTP status code
     */
    public int getCode() {
        return code;
    }

    /**
     * Returns text description of HTTP status.
     * @return text with description
     */
    public String getDescription() {
        return description;
    }

    /**
     * Return information about StatusCode in format: Http (code) (description).
     * @return Text of StatusCode
     */
    public String toString() {
        return "HTTP " + code + ": " + description;
    }

    /**
     * Returns instance of StatusCode for given code
     * @param code Status code
     * @return instance of StatusCode or null if code was not found.
     */
    public static StatusCode valueOf(int code) {
        for (StatusCode sc : StatusCode.values()) {
            if (sc.code == code) {
                return sc;
            }
        }
        return null;
    }

}
