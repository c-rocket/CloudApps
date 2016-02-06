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

package com.oracle.iot.client.device.async;

import oracle.iot.message.Message;
import oracle.iot.client.device.async.MessageReceipt;

import java.util.logging.Logger;

/**
 * MessageReceiptImpl
 */
final class MessageReceiptImpl implements MessageReceipt {

    private final Message message;
    private Status status;
    private boolean abortMessage;
    private NotificationHandler notificationHandler;
    private static MessageTrackingNotifiler notifier;

    public MessageReceiptImpl(Message message) {
        super();
        this.message = message;
        this.status = Status.QUEUED;
        this.abortMessage = false;
    }

    @Override
    public synchronized void abortMessage() {
        this.abortMessage = true;
    }

    public synchronized boolean isAbortMessage() {
        return this.abortMessage;
    }

    @Override
    public synchronized void setNotificationHandler(NotificationHandler notificationHandler) {
        this.notificationHandler = notificationHandler;
        if (this.notificationHandler != null && notifier == null) {
            notifier = new MessageTrackingNotifiler();
            notifier.start();
        }
    }

    @Override
    public synchronized Status getStatus() {
        return status;
    }

    synchronized void setStatus(Status status) {

        if (this.status == status) return;

        final Status oldStatus = this.status;
        this.status = status;
        if (notificationHandler != null) {
            notifier.notify(notificationHandler, this, oldStatus, this.status);
        }
    }

    @Override
    public Message getMessage() {
        return message;
    }

    private static class MessageTrackingNotifiler extends Thread {

        private final Object LOCK = new Object();
        // busy should really be atomic
        private boolean busy = false;

        private NotificationHandler notificationHandler;
        private MessageReceipt receipt;
        private Status oldValue;
        private Status newValue;

        private void notify(final NotificationHandler notificationHandler, final MessageReceipt receipt, final Status oldValue, final Status newValue) {
            synchronized (LOCK) {
                try {
                    while (busy) {
                        LOCK.wait();
                    }
                    busy = true;

                    this.notificationHandler = notificationHandler;
                    this.receipt = receipt;
                    this.oldValue = oldValue;
                    this.newValue = newValue;
                    LOCK.notifyAll();

                } catch (InterruptedException e) {
                    // Restore the interrupted status
                    Thread.currentThread().interrupt();
                }
            }
        }

        @Override public void run() {

            synchronized (LOCK) {
                try {
                    while (!busy) {
                        LOCK.wait();

                        try {
                            notificationHandler.changed(receipt, oldValue, newValue);
                        } catch (Exception e) {
                            Logger.getAnonymousLogger().severe(e.getMessage());
                        }
                        busy = false;
                        LOCK.notifyAll();
                    }
                } catch (InterruptedException e) {
                    // Restore the interrupted status
                    Thread.currentThread().interrupt();
                }
            }

        }
    }

}
