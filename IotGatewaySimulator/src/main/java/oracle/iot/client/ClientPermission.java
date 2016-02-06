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
package oracle.iot.client;

import java.security.BasicPermission;

/**
 * The <code>ClientPermission</code> class represents access rights for
 * the Client Library API.
 * A <code>ClientPermission</code> contains a target name but no actions
 * list; you either have the named permission or you don't.
 * <br>
 * <br> 
 * The following table lists the possible <code>ClientPermission</code> names,
 * and for each provides a description of what the permission allows and a 
 * discussion of the risks of granting code the permission.
 * <br>
 * <br>
 * <table border=1 cellpadding=5 summary="permission target name, what the
 * permission allows, and affected APIs">
 * <tr>
 * <th>Permission Target Name</th>
 * <th>What the Permission Allows</th>
 * <th>Risks of Allowing this Permission</th>
 * </tr>
 *
 * <tr>
 * <td>activate</td>
 * <td>Allows activating a client with the server</td>
 * <td></td>
 * </tr>
 *
 * <tr>
 * <td>register-handler</td>
 * <td>Allows the application to register a handler for specific resource path.</td>
 * <td></td>
 * </tr>
 *
 * <tr>
 * <td>send-message</td>
 * <td>Allows the application to send message to the server</td>
 * <td></td>
 * </tr>
 *
 * </table>
 */
public class ClientPermission extends BasicPermission {

    private static final long serialVersionUID = 8270774586119583103L;

    /** Permission to activate the endpoint on the server */
    public static final ClientPermission ACTIVATE = new ClientPermission("activate");

    /** Permission to send messages to the server */
    public static final ClientPermission SEND_MESSAGE = new ClientPermission("send-message");

    /**
     * Permission to register {@link oracle.iot.message.RequestMessageHandler message handlers}
     * to handle messages from the server.
     */
    public static final ClientPermission REGISTER_HANDLER = new ClientPermission("register-handler");


    /**
     * Constructs a <code>ClientPermission</code> with the specified name.
     * 
     * @param name the name of the permission.
     * 
     * @throws IllegalArgumentException if name is empty or invalid
     */
    public ClientPermission(String name) {
        super(name);
        if(!isValid(name)) {
            throw new IllegalArgumentException("Invalid permission name: " + name);
        }        
    }
    
    /**
     * Constructs a <code>ClientPermission</code> with the specified name.
     * 
     * @param name the name of the permission.
     * @param actions the actions for the permission. Actions must be the empty string or null.
     * 
     * @throws IllegalArgumentException if name is empty or invalid, or actions is not the empty string or null
     */
    public ClientPermission(String name, String actions) {
        super(name, actions);
        if(!isValid(name)) {
            throw new IllegalArgumentException("Invalid permission name: " + name);
        }        
        if(actions != null && actions.isEmpty() == false){
            throw new IllegalArgumentException("Actions parameter must be null or empty string.");
        }            
    }

    private boolean isValid(String name) {
        return "activate".equals(name)
            || "register-handler".equals(name)
            || "send-message".equals(name);
    }
}
