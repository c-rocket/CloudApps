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

import oracle.iot.message.Message;

/**
 * MessageReceipt is used to track the delivery of a Message that
 * has been queued to send to the server.
 */
public interface MessageReceipt {

    /**
     * The possible status of a Message in the message dispatcher.
     */
    enum Status {

        /** The message is in the queue to be sent to the server. */
        QUEUED,

        /** The message is in the process of being sent to the server. */
        SENDING,

        /**
         * The message was received by the server and the server
         * returned a response indicating success.
         */
        SUCCESS,

        /**
         * The message was failed to be delivered and will not be retried.
         * This could be due to an error response from the server indicating
         * a permanent failure, because the number of retries has been exceeded,
         * or because the DeviceClient close() method was called.
         */
        FAILURE,

        /**
         * The message was failed to be delivered and will not be retried.
         * This could be due to some network error or an error response from
         * the server that indicates a transient failure.
         */
        RETRYING
    }

    /**
     * A method that is called when the status of the Message changes.
     */
    interface NotificationHandler {

        void changed(MessageReceipt observable,
                        MessageReceipt.Status oldValue,
                        MessageReceipt.Status newValue);
    }

    /**
     * Remove the message from the sending queue. If this method is called when
     * the message status is {@link MessageReceipt.Status#QUEUED QUEUED},
     * the message will be removed from the sending queue and the resulting
     * status will be {@link MessageReceipt.Status#FAILURE FAILURE}.
     * If this method is called when the message status is
     * {@link MessageReceipt.Status#SENDING SENDING} and the next
     * state would be {@link MessageReceipt.Status#RETRYING RETRYING},
     * the message will not be retried and the resulting status will be
     * {@link MessageReceipt.Status#FAILURE FAILURE}; otherwise,
     * the resulting state will be
     * {@link MessageReceipt.Status#SUCCESS SUCCESS}, or
     * {@link MessageReceipt.Status#FAILURE FAILURE}, depending on the
     * outcome of the message transaction with the server.
     * If this method is called when the message status is
     * {@link MessageReceipt.Status#SUCCESS SUCCESS} or
     * {@link MessageReceipt.Status#FAILURE FAILURE}, the
     * message is no longer in the queue and this call has no effect.
     */
    void abortMessage();

    /**
     * Add a listener for MessageReceipt status changes. There may
     * only be one listener. Calling this method more than once will overwrite
     * an existing listener registration.
     * <p>Note that it is possible for the message to be processed before
     * the listener is added.</p>
     * @param notificationHandler The listener to call when the status changes
     */
    void setNotificationHandler(NotificationHandler notificationHandler);

    /**
     * Get the current status of the message in the dispatcher.
     * @return {@link MessageReceipt.Status}
     */
    Status getStatus();

    /**
     * Get the message belonging to this MessageReceipt.
     * @return {@link oracle.iot.message.Message}
     */
    Message getMessage();

}
