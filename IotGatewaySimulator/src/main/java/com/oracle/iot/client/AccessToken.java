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

import com.oracle.json.JsonObject;

/**
* AccessToken
*/
class AccessToken {
    private final long expires;
    private final String tokenType;
    private final String token;
    private final long expirationTime;

    public AccessToken(long expires, String tokenType, String token) {
        this.expires = expires;
        this.tokenType = tokenType;
        this.token = token;
        this.expirationTime = System.currentTimeMillis() + expires;
    }

    public final boolean hasExpired() {
        return (System.currentTimeMillis() >= this.expirationTime);
    }

    public long getExpires() {
        return expires;
    }

    public String getTokenType() {
        return tokenType;
    }

    public String getToken() {
        return token;
    }

    @Override
    public String toString() {
        return "Accessor.Token{" +
                "expires=" + expires +
                ", tokenType='" + tokenType + '\'' +
                ", token='" + token + '\'' +
                '}';
    }

    public static AccessToken fromJSON(final JsonObject jsonObject) {
        AccessToken token = new AccessToken(
                jsonObject.getInt("expires_in"),
                jsonObject.getString("token_type"),
                jsonObject.getString("access_token"));
        return token;
    }
}
