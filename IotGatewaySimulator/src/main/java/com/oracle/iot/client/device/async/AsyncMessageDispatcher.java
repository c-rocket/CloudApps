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

import com.oracle.iot.client.ConnectionManager;
import com.oracle.iot.client.HttpClient;
import com.oracle.iot.client.TrustManager;
import com.oracle.json.Json;
import com.oracle.json.JsonArray;
import com.oracle.json.JsonArrayBuilder;
import com.oracle.json.JsonReader;
import com.oracle.json.JsonStructure;
import oracle.iot.client.device.DeviceClient;
import oracle.iot.client.device.async.AsyncDeviceClient;
import oracle.iot.message.DataMessage;
import oracle.iot.message.HttpRequestMessage;
import oracle.iot.message.HttpResponseMessage;
import oracle.iot.message.Message;
import oracle.iot.client.device.async.MessageReceipt;
import oracle.iot.message.MessageParsingException;
import oracle.iot.message.RequestMessageHandler;
import oracle.iot.client.device.Resource;
import oracle.iot.message.StatusCode;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * AsyncMessageDispatcher
 */
public final class AsyncMessageDispatcher {

    /*
     * ConnectionManager for sending messages to, and receiving messages
     * from, the server.
     */
    private final ConnectionManager connectionManager;
    
    /*
     * Thread for calling request handlers.
     */
    private final RequestHandlerCallbackThread requestHandlerCallbackThread;

    /*
     * Queue of outgoing messages to be dispatched to server
     * (with thread for servicing the queue)
     */
    private final MessageQueue<Message> outgoingMessageQueue;

    /*
     * Queue of messages from server to be handled
     * (with thread for servicing the queue)
     */
    private final MessageQueue<HttpRequestMessage> incomingMessageQueue;

    /*
     * Needed for "X-EndpointId" header
     */
    private final DeviceClient deviceClient;

    /*
     * Set to true if close method has been called.
     */
    private boolean closed;

    private static final String KEY = "key";
    private static final String VALUE = "value";

    private static final Thread.UncaughtExceptionHandler UNCAUGHT_EXCEPTION_HANDLER =
            new Thread.UncaughtExceptionHandler() {
                public void uncaughtException(Thread t, Throwable e) {
                    Logger.getAnonymousLogger().log(Level.INFO, t.getName() + " caught " + e.toString(), e);
                }
            };
    private static final Map<String, AsyncMessageDispatcher> mappings = new HashMap<>() ;
    public static AsyncMessageDispatcher getAsyncMessageDispatcher(AsyncDeviceClient asyncDeviceClient) {
        if (asyncDeviceClient == null) throw new IllegalArgumentException("asyncDeviceClient cannot be null");
        AsyncMessageDispatcher amd = mappings.get(asyncDeviceClient.getEndpointId()) ;
        if (amd == null) {
            amd = new AsyncMessageDispatcher(asyncDeviceClient) ;
            mappings.put(asyncDeviceClient.getEndpointId(), amd) ;
        }
        return amd ;
    }
    /* pre-tim updates
    public static AsyncMessageDispatcher getAsyncMessageDispatcher(AsyncDeviceClient asyncDeviceClient) {
        if (asyncDeviceClient == null) throw new IllegalArgumentException("asyncDeviceClient cannot be null");
        return Holder.INSTANCE.getMessageDispatcher(asyncDeviceClient);
    }

    private enum Holder {

        INSTANCE;

        private AsyncMessageDispatcher messageDispatcher;

        private synchronized AsyncMessageDispatcher getMessageDispatcher(AsyncDeviceClient deviceClient) {
            if (messageDispatcher == null) {
                messageDispatcher = new AsyncMessageDispatcher(deviceClient);
            } else if (!messageDispatcher.deviceClient.equals(deviceClient)) {
                throw new IllegalArgumentException("deviceClient does not match");
            }
            return messageDispatcher;
        }

        private synchronized void close() {
            messageDispatcher = null;
        }
    } */

    private AsyncMessageDispatcher(DeviceClient deviceClient) {

        this.closed = false;

        this.deviceClient = deviceClient;
        ;
        final TrustManager trustManager = TrustManager.getInstance(deviceClient);
        this.connectionManager = ConnectionManager.getInstance(trustManager);

        this.requestHandlerCallbackThread = new RequestHandlerCallbackThread(this);
        this.requestHandlerCallbackThread.start();

        final Message pollMessage = new DataMessage.Builder().source(deviceClient.getEndpointId()).format("urn:ping:message")
                .priority(Message.Priority.HIGH).dataItem(KEY, VALUE).build();

        final int pollingInterval =
                AsyncMessagingConfiguration.getInstance().getPollingInterval();
        this.outgoingMessageQueue =
                new MessageQueue<Message>(pollingInterval, pollMessage) {
            @Override
            protected void dispatch(Collection<MessageQueueEntry<Message>> entries) {
                AsyncMessageDispatcher.this.post(entries);
            }
        };
        this.outgoingMessageQueue.setName("OutgoingMessageDispatcher");
        this.outgoingMessageQueue.setUncaughtExceptionHandler(UNCAUGHT_EXCEPTION_HANDLER);
        this.outgoingMessageQueue.start();

        this.incomingMessageQueue =
                new MessageQueue<HttpRequestMessage>() {
                    @Override
                    protected void dispatch(Collection<MessageQueueEntry<HttpRequestMessage>> entries) {
                        AsyncMessageDispatcher.this.handle(entries);
                    }
                };
        this.incomingMessageQueue.setName("IncomingMessageDispatcher");
        this.incomingMessageQueue.setUncaughtExceptionHandler(UNCAUGHT_EXCEPTION_HANDLER);
        this.incomingMessageQueue.start();

    }

    final Map<String, MessageReceiptImpl> messageTrackingReceiptMap =
            Collections.synchronizedMap(new HashMap<String, MessageReceiptImpl>());

    /*
     * Called from DeviceClientImpl - this is where a call to DeviceClient#sendMessages
     * ends up.
     */
    Collection<MessageReceipt> sendMessage(Collection<Message> messages) {

        List<MessageReceipt> receipts =
                new ArrayList<MessageReceipt>(messages.size());
        // TODO: enqueue in bulk, ensure all messages in the collection are posted together
        for (Message message : messages) {

            MessageReceiptImpl messageTrackingReceipt =
                    new MessageReceiptImpl(message);
            if(outgoingMessageQueue.enqueue(message)) {
                final String key = message.getClientId();
                messageTrackingReceiptMap.put(key, messageTrackingReceipt);
            } else {
                messageTrackingReceipt.setStatus(MessageReceipt.Status.FAILURE);
            };
            receipts.add(messageTrackingReceipt);
        }

        return receipts;
    }

    /*
     * Called from DeviceClientImpl - this is where a call to DeviceClient#sendMessage
     * ends up. All this does is enqueue the message in the outgoingMessageQueue
     */
    public MessageReceipt sendMessage(Message message) {

        if (message.getReliability() == Message.Reliability.GUARANTEED_DELIVERY) {
            // TODO: Do something to persist the message until it is delivered...
            // Message is un-persisted in the MessageSender run() method
            throw new UnsupportedOperationException("Support for GUARANTEED_DELIVERY not implemented yet");
        }

        MessageReceiptImpl messageTrackingReceipt =
                new MessageReceiptImpl(message);
        if(outgoingMessageQueue.enqueue(message)) {
            final String key = message.getClientId();
            messageTrackingReceiptMap.put(key, messageTrackingReceipt);
        } else {
            messageTrackingReceipt.setStatus(MessageReceipt.Status.FAILURE);
        };
        return messageTrackingReceipt;
    }

    /** @see oracle.iot.client.Client#close() */
    public synchronized void close() {
        // This is called from DeviceClientImpl and the assumption is made
        // that the DeviceClientImpl method is synchronized.
        if (!closed) {
            closed = true;
            // pre tim g change
            // Holder.INSTANCE.close();
            // remove from the mapping
            mappings.remove(deviceClient.getEndpointId()) ;
            requestHandlerCallbackThread.close();
            outgoingMessageQueue.close();
            incomingMessageQueue.close();
        }
    }
    /*************************************************************************
     *
     * Methods and classes for handling outgoing message dispatch
     * Implementation and API in this section should be private
     *
     *************************************************************************/

    /*
     * Send a collection of messages to the server
     */
    private void post(final Collection<MessageQueueEntry<Message>> entries) {

        JsonArrayBuilder jsonArrayBuilder = Json.createArrayBuilder();
        for (MessageQueueEntry entry : entries) {

            final Message message = entry.getMessage();
            final String key = message.getClientId();
            final MessageReceiptImpl receipt = messageTrackingReceiptMap.get(key);

            final boolean abort = receipt != null ? receipt.isAbortMessage() : false;

            if (!abort) {
                jsonArrayBuilder.add(message.toJSON());
            }

            if (receipt != null) {
                if (!abort) {
                    receipt.setStatus(MessageReceipt.Status.SENDING);
                } else {
                    receipt.setStatus(MessageReceipt.Status.FAILURE);
                    messageTrackingReceiptMap.remove(key);
                }
            }

        }

        JsonArray jsonArray = jsonArrayBuilder.build();
        if (jsonArray.isEmpty()) return;

        try {
            Logger.getAnonymousLogger().info("sending " + entries.size() + " messages");
            post(jsonArray);

            for (MessageQueueEntry entry : entries) {
                final Message message = entry.getMessage();
                final String key = message.getClientId();

                final MessageReceiptImpl receipt = messageTrackingReceiptMap.get(key);

                if (receipt != null) {
                    receipt.setStatus(MessageReceipt.Status.SUCCESS);
                    messageTrackingReceiptMap.remove(key);
                }
            }

            return;

        } catch(GeneralSecurityException e) {
            Logger.getAnonymousLogger().info(e.toString());
        } catch (IOException e) {
            Logger.getAnonymousLogger().info(e.toString());
        }

        // Note the 'return' above. If we get here, then there was an
        // exception thrown.
        Logger.getAnonymousLogger().info("could not send messages " + entries.size());
        for (MessageQueueEntry entry : entries) {
            // TODO: should this retry be delayed?
            // TODO: do not retry if the HTTP response indicates other than a temporary failure
            // TODO: this might not be the best place to handle this
            final Message message = entry.getMessage();
            final String key = message.getClientId();
            final MessageReceiptImpl receipt = messageTrackingReceiptMap.get(key);

            final boolean abort = receipt != null ? receipt.isAbortMessage() : false;
            final boolean queued = !abort && entry.retry(AsyncMessageDispatcher.this);

            if (receipt != null) {
                if (!queued) {
                    receipt.setStatus(MessageReceipt.Status.FAILURE);
                    messageTrackingReceiptMap.remove(key);
                }
            }
        }
    }

    /*
     * Called from MessageQueueEntry if the message needs retry
     */
    private void retry(MessageQueueEntry entry) {

        final Message message = entry.getMessage();
        final String key = message.getClientId();
        final MessageReceiptImpl receipt = messageTrackingReceiptMap.get(key);

        final boolean abort = receipt != null ? receipt.isAbortMessage() : false;
        final boolean queued = !abort && outgoingMessageQueue.retry(entry);

        if (receipt != null) {
            if (queued) {
                receipt.setStatus(MessageReceipt.Status.RETRYING);
            } else {
                receipt.setStatus(MessageReceipt.Status.FAILURE);
                messageTrackingReceiptMap.remove(key);
            }
        }
    }

    /*
     * Called from post(Collection<MessageQueueEntry>). This simply makes
     * the other code easier to read.
     */
    private synchronized boolean post(JsonArray jsonArray) throws IOException, GeneralSecurityException {

        final byte[] payload = jsonArray.toString().getBytes(Charset.forName("UTF-8"));

        final Map<String, String> headers = new HashMap<String, String>();
        headers.put("Content-Type", "application/json");
        headers.put("Accept", "application/json");
        headers.put("X-EndpointId", deviceClient.getEndpointId());

        final HttpClient.HttpResponse response =
                connectionManager.post("/iot/api/v1/messages",payload, headers);

        final int status = response.getStatus();

//        if (message.getReliability() == Message.Reliability.GUARANTEED_DELIVERY) {
//            // TODO: something to remove message from persistence
//        }
        if (status == 202) {
            JsonReader reader = null;
            byte[] data = response.getData();
            // if data.length == 2, then it is an empty json array and there are
            // no values in the message.
            if (data != null && data.length > 2) {
                try {
                    ByteArrayInputStream is = new ByteArrayInputStream(data);
                    reader = Json.createReader(is);
                    JsonStructure jsonStructure = reader.read();
                    List<Message> incomingMessages = Message.fromJSON(jsonStructure);
                    for (Message msg : incomingMessages) {
                        if (msg instanceof HttpRequestMessage) {
                            // Don't care about tracking message status here
                            Logger.getAnonymousLogger().fine(String.valueOf(msg.toJSON()));
                            incomingMessageQueue.enqueue((HttpRequestMessage) msg);
                        }
                    }
                } catch (MessageParsingException e) {
                    Logger.getAnonymousLogger().info(e.getMessage());
                } finally {
                    if (reader != null) {
                        reader.close();
                    }
                }
            }
            return true;

        } else {
            Logger.getAnonymousLogger().info("POST /iot/api/v1/messages: received 'HTTP " + status + "'");
            Logger.getAnonymousLogger().fine(jsonArray.toString());
            return false;
        }

    }

    /**
     * Entry in our outgoing message queue. It can be sorted and it has a
     * retry count.
     */
    private static class MessageQueueEntry<E extends Message> implements Comparable<MessageQueueEntry<E>> {

        public int compareTo(MessageQueueEntry<E> other) {

            // Note this implementation is not consistent with equals. It is possible
            // that a.compareTo(b) == 0 is not that same boolean value as a.equals(b)

            // The natural order of enum is the enum's ordinal, i.e.,
            // x.getPriority().compareTo(y.getPriority() will give {x,y}
            // if x is a lower priority. What we want is to sort by the
            // higher priority.
            int c = other.getPriority().compareTo(getPriority());

            // If they are the same priority, take the one that was created first
            if (c == 0) {
                c = getEventTime().compareTo(other.getEventTime());
            }

            // If they are still the same, take the one with higher reliability.
            if (c == 0) {
                c = getReliability().compareTo(other.getReliability());
            }

            // Finally, take the one with the greater number of retries
            if (c == 0) {
                c = getRetryCount() - other.getRetryCount();
            }

            return c;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) return true;
            if (obj == null || obj.getClass() != getClass()) return false;
            return this.getMessage().equals(((MessageQueueEntry) obj).getMessage());
        }

        @Override
        public int hashCode() {
            return getMessage().hashCode();
        }

        @Override
        public String toString() {
            return "MessageQueueEntry{" + getMessage().toString() + "}";
        }

        E getMessage() {
            return message;
        }

        Message.Priority getPriority() {
            return message.getPriority();
        }

        Date getEventTime() {
            return message.getEventTime();
        }

        Message.Reliability getReliability() {
            return message.getReliability();
        }

        int getRetryCount() {
            return retryCount;
        }

        boolean retry(AsyncMessageDispatcher messageDispatcher) {
            boolean retry = (--retryCount > 0);
            if (retry) {
                messageDispatcher.retry(this);
            }
            return retry;
        }

         MessageQueueEntry(E message) {
            this.message = message;

            switch (message.getReliability()) {
                case NO_GUARANTEE:
                    this.retryCount = MAX_RETRY_COUNT;
                    break;
                case BEST_EFFORT:
                    this.retryCount = MAX_RETRY_COUNT * 2;
                    break;
                case GUARANTEED_DELIVERY:
                    this.retryCount = Integer.MAX_VALUE;
                    break;
                default:
                    this.retryCount = 0;
                    break;
            }
        }

        private static final int MAX_RETRY_COUNT =
                AsyncMessagingConfiguration.getInstance().getMaximumNumberOfRetries();
        private final E message;
        private int retryCount;
    }
    
    /*************************************************************************
     *
     * Methods and classes for handling incoming message dispatch
     * Implementation and API in this section should be private
     *
     *************************************************************************/

    /*
     * Handle a collection of HttpRequestMessages from the server by invoking
     * the corresponding handler. This gets called from the incomingMessageQueue's
     * dispatch method.
     */
    private void handle(final Collection<MessageQueueEntry<HttpRequestMessage>> entries) {

        for (MessageQueueEntry entry : entries) {

            final HttpRequestMessage message = (HttpRequestMessage) entry.getMessage();
            requestHandlerCallbackThread.handle(message);
        }
    }


    /*************************************************************************
     *
     * Handling of the blocking queue, servicing the queue,
     * and dispatching dequeued entries.
     *
     *************************************************************************/
    private static abstract class MessageQueue<T extends Message> extends Thread {

        // TODO: extend Thread and get rid of the anonymous Runnable
        private final Object LOCK = new Object();
        private final List<MessageQueueEntry<T>>[] queues;
        private final long timeout;
        private int count;

        private static final int MAXIMUM_MESSAGES_PER_CONNECTION =
                AsyncMessagingConfiguration.getInstance().getMaximumMessagesPerConnection();

        private static final int MAXIMUM_MESSAGES_TO_QUEUE =
                AsyncMessagingConfiguration.getInstance().getMaximumMessagesToQueue();

        private static final String KEY = "key";
        private static final String VALUE = "value";

         // A special message for polling
        private final MessageQueueEntry<T> pollMessageEntry;

        private boolean closed;

        protected MessageQueue() {
            this(0, null);
        }

        protected MessageQueue(int duration, T pollMessageEntry) {
            Message.Priority[] priorities = Message.Priority.values();
            this.queues = new ArrayList[priorities.length];
            this.count = 0;
            this.timeout = duration >= 0 ? duration : 0;
            this.pollMessageEntry = pollMessageEntry != null
                    ? new MessageQueueEntry<T>(pollMessageEntry)
                    : null;
            this.closed = false;
        }

        void close() {
            synchronized (LOCK) {
                closed = true;
                LOCK.notifyAll();
            }
        }

        @Override
        public void run() {
            try {
                while (!closed) {
                    synchronized (LOCK) {
                        while (!closed && count == 0) {
                            LOCK.wait(timeout);
                            // If timeout is zero, then we either got a notify or
                            // we got a spurious wakeup, so loop again to make
                            // sure there is something in the queue.
                            // If timeout is greater than zero, then we want to send a
                            // poll message, so we break out of the loop.
                            if (timeout > 0) break;
                        }
                        Collection<MessageQueueEntry<T>> entries = getEntriesToSend();
                        dispatch(entries);
                        LOCK.notifyAll();
                    }
                }
            } catch (InterruptedException e) {
                // Restore the interrupted status
                Thread.currentThread().interrupt();
            }
        }

        boolean enqueue(T entry) {

            if (entry == null) return false;

            synchronized (LOCK) {
                // TODO: if (queue.size() >= MAXIMUM_MESSAGES_TO_QUEUE) send alert
                final Message.Priority priority = entry.getPriority();
                final int index = priority.ordinal();
                List<MessageQueueEntry<T>> list = queues[index];
                if (list == null) {
                    list = queues[index] = new ArrayList<MessageQueueEntry<T>>();
                }
                list.add(new MessageQueueEntry<T>(entry));
                count += 1;
                LOCK.notifyAll();
            }

            return true;

        }

        boolean retry(MessageQueueEntry<T> entry) {

            if (entry == null) return false;

            synchronized (LOCK) {
                    // TODO: if (queue.size() >= MAXIMUM_MESSAGES_TO_QUEUE) send alert
                final Message message = entry.getMessage();
                final Message.Priority priority = message.getPriority();
                final int index = priority.ordinal();
                List<MessageQueueEntry<T>> list = queues[index];
                if (list == null) {
                    list = queues[index] = new ArrayList<MessageQueueEntry<T>>();
                }
                list.add(entry);
                count += 1;
                LOCK.notifyAll();
            }

            return true;
        }

        private Collection<MessageQueueEntry<T>> getEntriesToSend() {
            Collection<MessageQueueEntry<T>> entriesToSend  = null;

                // Note that the queue might still be empty at this point
                int nMax = Math.min(count, MAXIMUM_MESSAGES_PER_CONNECTION);
                entriesToSend = new ArrayList<MessageQueueEntry<T>>(nMax);

                if (0 < count) {

                    if (nMax == MAXIMUM_MESSAGES_PER_CONNECTION) {

                        //
                        // If we are going to send the maximum number of
                        // messages, then sort each queue before doing the
                        // round-robin selection. If we're going to send
                        // less than the maximum, then don't bother with
                        // the sort since we'll be sending all the messages.
                        //
                        for (int q = 0; q < queues.length; q++) {
                            final List<MessageQueueEntry<T>> list = queues[q];
                            if (list == null || list.isEmpty()) {
                                continue;
                    }
                            Collections.sort(list);
                        }
                    }
                    int index = 0;

                    // round-robin
                    while (entriesToSend.size() < nMax) {
                        index = index % queues.length;
                        final List<MessageQueueEntry<T>> list = queues[index++];
                        if (list == null || list.isEmpty()) {
                            continue;
                        }
                        MessageQueueEntry<T> entry = list.remove(0);
                        entriesToSend.add(entry);
                    }

                    count -= nMax;

                } else if (!closed) {
                    // if there are no entries to send, then send a poll message
                    entriesToSend.add(pollMessageEntry);
                }
            return entriesToSend;
        }

        protected abstract void dispatch(Collection<MessageQueueEntry<T>> entries);

    }

    private static class RequestHandlerCallbackThread extends Thread {

        private final Object LOCK = new Object();
        private final List<HttpRequestMessage> messages;
        private final AsyncMessageDispatcher messageDispatcher;
        private boolean closed;

        private RequestHandlerCallbackThread(AsyncMessageDispatcher messageDispatcher) {
            super("RequestHandlerCallbackThread");
            this.messageDispatcher = messageDispatcher;
            this.messages = new ArrayList<HttpRequestMessage>();
            closed = false;
        }

        private void handle(HttpRequestMessage message) {
            synchronized (LOCK) {
                this.messages.add(message);
                LOCK.notifyAll();
            }
        }

        private void close() {
            synchronized (LOCK) {
                closed = true;
                LOCK.notifyAll();
            }
        }

        @Override
        public void run() {
            try {
                while (!closed) {
                    synchronized (LOCK) {
                        while (!closed && messages.isEmpty()) {
                            LOCK.wait();
                        }
                        processCallbacks();
                        LOCK.notifyAll();
                    }
                }
            } catch (InterruptedException e) {
            }
        }

        private void processCallbacks() {

            while(!messages.isEmpty()) {

                final HttpRequestMessage httpRequestMessage = messages.remove(0);

                final String path = httpRequestMessage.getURL();

                final String methodString = httpRequestMessage.getMethod().toUpperCase(Locale.ROOT);
                final Resource.Method method = Resource.Method.valueOf(methodString);
                final Resource resource = new Resource.Builder()
                        .name(path)
                        .path(path)
                        .method(method)
                        .build();

                final RequestMessageHandler requestMessageHandler =
                        messageDispatcher.deviceClient.getRequestHandler(resource);

                try {

                    final HttpResponseMessage responseMessage =
                            requestMessageHandler.handleRequest(httpRequestMessage);
                    messageDispatcher.sendMessage(responseMessage);

                } catch (Exception e) {
                    Logger.getAnonymousLogger().log(Level.WARNING, e.getMessage(), e);

                    HttpResponseMessage.Builder builder =
                            new HttpResponseMessage.Builder();

                    builder.source(httpRequestMessage.getDestination())
                            .destination(httpRequestMessage.getSource())
                            .requestId(httpRequestMessage.getId())
                            .statusCode(StatusCode.INTERNAL_SERVER_ERROR);

                    final HttpResponseMessage responseMessage = builder.build();
                    messageDispatcher.sendMessage(responseMessage);
                }
            }
        }

    }
}
