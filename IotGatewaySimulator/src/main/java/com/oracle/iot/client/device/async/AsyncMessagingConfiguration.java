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

/**
 * Represents configuration information for the AsyncMessagingDispatcher.
 */
final class AsyncMessagingConfiguration {

    static AsyncMessagingConfiguration getInstance() {
        return Holder.INSTANCE;
    }

    // There is currently only one option, but obviously this will grow over time
    public enum OutOfSpaceOptions {
        // This will require we somehow notify the caller of the failure. If we are returning
        // a Future, I can do it that way. Otherwise it is a checked or unchecked exception.
        REJECT
    }

    private static final int DEFAULT_MAXIMUM_MESSAGES_PER_CONNECTION = 100;
    private static final int DEFAULT_MAXIMUM_MESSAGES_TO_QUEUE = 1000;
    private static final int DEFAULT_MAXIMUM_NUMBER_OF_RETRIES = 5;
    private static final int DEFAULT_MAXIMUM_NUMBER_OF_REQUEST_HANDLER_THREADS = 10;
    private static final int DEFAULT_POLLING_INTERVAL = 5000; // milliseconds
    private static final int DEFAULT_RESPONSE_TIMEOUT = 15000; // milliseconds
    private static final int DEFAULT_SERVER_MESSAGE_HISTORY_CAPACITY = 100;
    private static final int DEFAULT_WAIT_TIME_IF_THERE_ARE_NO_CONNECTIONS = 5000; // milliseconds
    private static final OutOfSpaceOptions DEFAULT_OUT_OF_SPACE_OPTION = OutOfSpaceOptions.REJECT;


    private final int maximumMessagesPerConnection;
    private final int maximumMessagesToQueue;
    private final int maximumNumberOfRetries;
    private final int maximumNumberOfRequestHandlerThreads;
    private final int pollingInterval;
    private final int responseTimeout;
    private final int serverMessageHistoryCapacity;
    private final int waitTimeIfThereAreNoConnections;
    private final OutOfSpaceOptions outOfSpaceOptions;

    private static int getIntProperty(String name, int defaultValue) {
        try {
            Integer value = Integer.getInteger(name, defaultValue);
            if (value != null) return value.intValue();
        } catch (SecurityException e) {
            // use default value
        }
        return defaultValue;
    }

    private AsyncMessagingConfiguration() {
        maximumMessagesPerConnection = getIntProperty("oracle.iot.client.maximumMessagesPerConnection", DEFAULT_MAXIMUM_MESSAGES_PER_CONNECTION);
        maximumMessagesToQueue = getIntProperty("oracle.iot.client.maximumMessagesToQueue", DEFAULT_MAXIMUM_MESSAGES_TO_QUEUE);
        maximumNumberOfRetries = getIntProperty("oracle.iot.client.maximumNumberOfRetries", DEFAULT_MAXIMUM_NUMBER_OF_RETRIES);
        maximumNumberOfRequestHandlerThreads = getIntProperty("oracle.iot.client.maximumNumberOfRequestHandlerThreads", DEFAULT_MAXIMUM_NUMBER_OF_REQUEST_HANDLER_THREADS);
        outOfSpaceOptions = DEFAULT_OUT_OF_SPACE_OPTION;
        pollingInterval = getIntProperty("oracle.iot.client.pollingInterval", DEFAULT_POLLING_INTERVAL);
        responseTimeout = getIntProperty("oracle.iot.client.responseTimeout", DEFAULT_RESPONSE_TIMEOUT);
        serverMessageHistoryCapacity = getIntProperty("oracle.iot.client.serverMessageHistoryCapacity", DEFAULT_SERVER_MESSAGE_HISTORY_CAPACITY);
        waitTimeIfThereAreNoConnections = getIntProperty("oracle.iot.client.waitTimeIfThereAreNoConnections ", DEFAULT_WAIT_TIME_IF_THERE_ARE_NO_CONNECTIONS);
    }

    private static class Holder {
        private static final AsyncMessagingConfiguration INSTANCE = new AsyncMessagingConfiguration();
    }

    /**
     * Gets the option to use when we run out of space in the MessagingService queue.
     * @return the option to use. Null is treated as "REJECT".
     */
    public OutOfSpaceOptions getOutOfSpaceOption() {
        return outOfSpaceOptions;
    }

    /**
     * Gets the maximum number of messages that we should send per any one connection. Note
     * that if this value is too large, it is possible that the message sending system will
     * get overwhelmed if the incoming message rate is faster than the processing rate. This
     * value should probably be less then 10,000 or so messages per connection. Testing indicates
     * that a reasonable value is probably around 100.
     * @return The max to send. If negative, then we will pick some default value.
     *         If negative we'll also log (should not ever really happen).
     */
    public int getMaximumMessagesPerConnection() {
        return maximumMessagesPerConnection;
    }

    /**
     * Gets the maximum number of messages that we should queue up per messaging service.
     * @return The max to queue. If negative, then we will pick some default value.
     *         If negative we'll also log (should not ever really happen).
     */
    public int getMaximumMessagesToQueue() {
        return maximumMessagesToQueue;
    }

    /**
     * The amount of time to wait if I find there are no connections.
     * @return The amount of time to wait. A null value will be treated as some default, and logged.
     *         (It should never really be null).
     */
    public int getWaitTimeIfThereAreNoConnections() {
        return waitTimeIfThereAreNoConnections;
    }

    /**
     * The maximum amount of time that can go by without sending some form of communication to the
     * server to check for incoming requests or commands. Setting this value to something very small
     * (such as 10ms) will cause continuous polling of the server, putting a higher load on the server
     * but also potentially decreasing the latency between a command / request being made on the server
     * side and it being handled by the client. On the other hand, it will also decrease battery life
     * significantly on the client.
     *
     * @return The maximum amount of time that can go by without sending some form of communication.
     *         A null value will be treated as some default, and logged (it should never really be null).
     */
    public int getPollingInterval() {
        return pollingInterval;
    }

    /**
     * The maximum amount of time that can Consumer for HttpResponseMessage wait until the response is
     * considered to be lost. Some time after this timeout, the future for sending HttpRequestMessage is
     * cancelled and consumer is removed from the Collection of waiting consumers.
     *
     * @return The maximum amount of time that can consumer wait for HttpResponseMessage
     */
    public int getResponseTimeout() {
        return responseTimeout;
    }

    /**
     * The maximum number of retries to try before discarding the message if I find there are no connections.
     * @return The maximum number of retries to try before discarding the message. If negative,
     * then we will pick some default value. If negative we'll also log (should not ever really happen).
     */
    public int getMaximumNumberOfRetries() {
        return maximumNumberOfRetries;
    }

    /**
     * The number of messages' ids that will be stored for preventing processing same messages from the server.
     */
    public int getServerMessagesHistoryCapacity() {
        return serverMessageHistoryCapacity;
    }

    /**
     * The number of threads that will be created for handling incoming messages.
     * Request handlers are called back on one of these threads. The threads
     * are cached.
     */
    public int getMaximumNumberOfRequestHandlerThreads() {
        return maximumNumberOfRequestHandlerThreads;
    }
}
