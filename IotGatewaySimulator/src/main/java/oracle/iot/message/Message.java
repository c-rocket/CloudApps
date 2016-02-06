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

import com.oracle.json.Json;
import com.oracle.json.JsonArray;
import com.oracle.json.JsonArrayBuilder;
import com.oracle.json.JsonNumber;
import com.oracle.json.JsonObject;
import com.oracle.json.JsonObjectBuilder;
import com.oracle.json.JsonReader;
import com.oracle.json.JsonString;
import com.oracle.json.JsonStructure;
import com.oracle.json.JsonValue;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * An abstract class for different types of messages in messaging server.
 * This class is immutable.
 */
public abstract class Message {

    /**
     * The priority of the message.
     */
    public enum Priority {
        LOWEST(0), LOW(1), MEDIUM(2), HIGH(3), HIGHEST(4);

        /** int value of the priority*/
        private final int value;

        /** Constructs enum from appropriate value. The value should be from interval [0-4] (0=LOWEST, 1=LOW,
         * 2=MEDIUM, 3=HIGH, 4=HIGHEST).
         */
        private Priority(int value) {
            this.value = value;
        }

        /**
         * Returns priority as int value.
         * @return priority ranging from 0 (LOWEST) to 4 (HIGHEST)
         */
        public int getValue() {
            return value;
        }
    }

    /**
     * Reliability of the message
     */
    public enum Reliability {
        /** on GW: Message won't be persisted, number of of retry count if sending was unsuccessful
         is default value */
        NO_GUARANTEE(0),

        /** on GW: Message will be persisted, number of retry count if sending was unsuccessful
         is (default value) * 2 */
        BEST_EFFORT(1),

        /** on GW: Message will be persisted, number of retry count will be set to Integer.MAX_VALUE.*/
        GUARANTEED_DELIVERY(2);

        /** int value of the reliability*/
        private final int value;

        /** Constructs enum from appropriate value The value should be from interval [0-2] (0=NO_GUARANTEE,
         * 1=BEST_EFFORT, 2=GUARANTEED_DELIVERY.
         */
        private Reliability(int value) {
            this.value = value;
        }

        /**
         * Returns reliability as int value.
         * @return reliability ranging from 0 (NO_GUARANTEE) to 2 (GUARANTEED_DELIVERY)
         */
        public int getValue() {
            return value;
        }
    }

    /**
     * The message type. Each type corresponds to one type of message.
     */
    protected enum Type {
        DATA,
        LOG,
        MESSAGE,
        REQUEST,
        RESPONSE,
        RESOURCES_REPORT,
        UPDATE_BUNDLE,
        UPDATE_BUNDLE_STATE,
        BUNDLE_FAILURE,
        WAKEUP,
        ALERT
    }

    /**
     * The message direction with respect to the device.
     */
    public enum Direction {
        FROM_DEVICE,
        TO_DEVICE
    }

    /**
     * Created time of the message diagnostic value. The value of the created time as a {@link java.lang.Long} that
     * contains the epoch value in milliseconds.
     */
    public static final String DIAG_CREATED_TIME = "createdTime";
    /**
     * Client address diagnostic value. The address client as a {@link String}
     */
    public static final String DIAG_CLIENT_ADDRESS = "clientAddress";

    /**
     * This is an abstract class for different message builders. Message classes
     * are immutable. A builder is required when creating messages.
     */
    public static abstract class MessageBuilder<T extends MessageBuilder<T>> {

        /**
         * A unique ID for each message that is assigned by the server (on a receipt of a message from a client or
         * when the server generates a message)
         */
        protected String id;
        /**
         * A unique ID for each message assigned by the client for tracking purposes.
         */
        protected String clientId;
        /**
         * Source of the message (should correspond to endpoint id)
         */
        protected String source;
        /**
         * Destination of the message.
         */
        protected String destination;
        /**
         * Message priority.
         */
        protected Priority priority;
        /**
         * Reliability for message delivering.
         */
        protected Reliability reliability;
        /**
         * Message event time.
         */
        protected Date eventTime;
        /**
         * Extra information for the message.
         */
        protected MessageProperties properties;
        /**
         * The message sender (should correspond to endpoint id).
         */
        protected String sender;
        /**
         * The message diagnostics
         */
        protected Map<String, Object> diagnostics;
        /**
         * The message direction (with respect to the server)
         */
        protected Direction direction;
        /**
         * The time when the message was received by the server (epoch value in nanoseconds)
         */
        protected Long receivedTime;
        /**
         * The time when the message was sent by the server (epoch value in nanoseconds)
         */
        protected Long sentTime;

        /**
         * Return current instance of the {@link Message.MessageBuilder}.
         * @return Current instance of {@link Message.MessageBuilder}.
         */
        protected abstract T self();

        public MessageBuilder() {

        }

        /**
         * Sets message ID. Message id is instance of {@link UUID}. If no id is provided during creation of the
         * {@link Message}, random one is created.
         *
         * @param id {@link UUID} of the message assigned to this message.
         * @return generic type of message builder
         */
        public final T id(String id) {
            this.id = id;
            return self();
        }

        /**
         * Sets message client ID. The client message id should be unique within the client. If no id is provided
         * during creation of the {@link Message}, random one is created.
         *
         * @param id {@link UUID} of the message assigned to this message.
         * @return generic type of message builder
         */
        public final T clientId(String id) {
            this.clientId = id;
            return self();
        }

        /**
         * Set message source. The value should correspond with endpoint ID where the message was originated.
         *
         * @param source source (source endpoint ID) of the message
         * @return generic type of message builder
         */
        public final T source(String source) {
            this.source = source;
            return self();
        }

        /**
         * Set message destination. The value should correspond with endpoint ID where the message should be delivered.
         *
         * @param destination destination (destination endpoint ID) of the message
         * @return generic type of message builder
         */
        public final T destination(String destination) {
            this.destination = destination;
            return self();
        }

        /**
         * Set message priority.
         *
         * @param priority priority of the message
         * @return generic type of message builder
         */
        public final T priority(Priority priority) {
            this.priority = priority;
            return self();
        }

        /**
         * Sets reliability for message delivering.
         *
         * @param reliability reliability for message delivering.
         * @return generic type of the message builder
         */
        public final T reliability(Reliability reliability){
            this.reliability = reliability;
            return self();
        }

        /**
         * Set message creation time.
         *
         * @param eventTime event time of the message
         * @return generic type of message builder
         */
        public final T eventTime(Date eventTime) {
            if (eventTime != null) {
                this.eventTime = new Date(eventTime.getTime());
            }
            return self();
        }

        /**
         * Set message creation time.
         *
         * @param eventTime event time of the message
         * @return generic type of message builder
         */
        public final T eventTime(long eventTime) {
            this.eventTime = new Date(eventTime);
            return self();
        }

        /**
         * Set message properties.
         *
         * @param properties extra information for the message
         * @return generic type of message builder
         */
        public final T properties(MessageProperties properties) {
            this.properties = properties;
            return self();
        }

        /**
         * Set message sender. The value should correspond with endpoint ID where the message was sent from.
         *
         * @param sender sender (sender endpoint ID) of the message
         * @return generic type of message builder
         */
        public final T sender(String sender) {
            this.sender = sender;
            return self();
        }

        /**
         * Set message direction.
         *
         * @param direction message direction
         * @return generic type of message builder
         */
        public final T direction(Direction direction) {
            this.direction = direction;
            return self();
        }

        /**
         * Set the time when the message was received by the server.
         *
         * @param time epoch value in nano seconds
         * @return generic type of message builder
         */
        public final T receivedTime(Long time) {
            this.receivedTime = time;
            return self();
        }

        /**
         * Set the time when the message was sent by the server.
         *
         * @param time epoch value in nano seconds
         * @return generic type of message builder
         */
        public final T sentTime(Long time) {
            this.sentTime = time;
            return self();
        }

        /**
         *  Set a diagnostic {@link String} value
         *
         * @param name diagnostic name
         * @param value disgnostic value
         * @return generic type of message builder
         */
        public final T diagnostic(String name, String value) {
            if ( diagnostics == null ){
                diagnostics = new HashMap<String,Object>();
            }
            this.diagnostics.put(name, value);
            return self();
        }

        /**
         *  Set a diagnostic {@link Number} value
         *
         * @param name diagnostic name
         * @param value disgnostic value
         * @return generic type of message builder
         */
        public final T diagnostic(String name, Number value) {
            if ( diagnostics == null ){
                diagnostics = new HashMap<String,Object>();
            }
            this.diagnostics.put(name, value);
            return self();
        }

        /**
         *  Set a diagnostic {@link Boolean} value
         *
         * @param name diagnostic name
         * @param value disgnostic value
         * @return generic type of message builder
         */
        public final T diagnostic(String name, boolean value) {
            if ( diagnostics == null ){
                diagnostics = new HashMap<String,Object>();
            }
            this.diagnostics.put(name, value);
            return self();
        }

        /**
         * Copy common fields from another {@link Message} to this.
         *
         * @param message {@link Message} to copy common fields from.
         * @return generic type of message builder
         */
        public T copy(Message message) {
            this.id = message.id;
            this.clientId = message.clientId;
            this.source = message.source;
            this.destination = message.destination;
            this.priority = message.priority;
            this.properties = message.getProperties();
            this.eventTime = new Date(message.getEventTime().getTime());
            this.reliability = message.reliability;
            this.sender = message.sender;
            this.diagnostics = copy(message.diagnostics); // message has readonly copy, so have to clone
            this.direction = message.direction;
            this.receivedTime = message.receivedTime;
            this.sentTime = message.sentTime;
            return self();
        }

        private Map<String,Object> copy(Map<String,Object> map){
            if ( map == null )
                return null;
            Map<String, Object> copied = new HashMap<String, Object>(map.size());
            copied.putAll(map);
            return copied;
        }

        /**
         * Method to deserialization of the Message from a JsonObject.
         *
         * @param jsonObject the jsonObject to convert
         * @return generic type of message builder
         * @throws MessageParsingException when json object is wrong or does not contain mandatory fields.
         */
        public T fromJSON(JsonObject jsonObject) {
            final String id = Utils.jsonToString(jsonObject.getJsonString("id"));
            this.id(id);

            this.clientId = Utils.jsonToString(jsonObject.getJsonString("clientId"));
            final String source = Utils.jsonToString(jsonObject.getJsonString("source"));
            final String destination = Utils.jsonToString(jsonObject.getJsonString("destination"));
            final String sender = Utils.jsonToString(jsonObject.getJsonString("sender"));
            // TODO: remove second condition after HttpRequestMessage from Server has source
            if ((source == null || source.isEmpty()) && (destination == null || destination.isEmpty()))
                throw new MessageParsingException("message.source.destination.null");
            this.source(source);
            this.destination(destination);
            this.sender(sender);

            final String priority = Utils.jsonToString(jsonObject.getJsonString("priority"));
            Utils.checkNullValueAndThrowMPE(priority, "message.priority.null");
            try {
                this.priority(Message.Priority.valueOf(priority));
            } catch (IllegalArgumentException e) {
                throw new MessageParsingException("message.priority.illegal", e);
            }

            final String reliability = Utils.jsonToString(jsonObject.getJsonString("reliability"));
            Utils.checkNullValueAndThrowMPE(reliability, "message.reliability.null");
            try {
                this.reliability(Message.Reliability.valueOf(reliability));
            } catch (IllegalArgumentException e) {
                throw new MessageParsingException("message.reliability.illegal", e);
            }

            if ( jsonObject.get("eventTime") != null &&
                    jsonObject.get("eventTime").getValueType() == JsonValue.ValueType.NUMBER) {
                final JsonNumber eventTimeJSON = jsonObject.getJsonNumber("eventTime");
                final Date eventTime = new Date(eventTimeJSON.longValue());
                this.eventTime(eventTime);
            } else {
                throw new MessageParsingException("message.eventTime.wrong");
            }


            final MessageProperties.Builder properties = new MessageProperties.Builder();
            final JsonObject propertiesObject = jsonObject.getJsonObject("properties");
            if (propertiesObject != null) {
                final Set<String> keySet = propertiesObject.keySet();
                for (String key: keySet) {
                    Utils.checkKeyLengthAndThrowMPE(key, "message.property.key.long");
                    final JsonArray keyValues = propertiesObject.getJsonArray(key);
                    if (keyValues.size() == 0) {
                        List<String> values = Collections.emptyList();
                        properties.addValues(key, values);
                    }
                    else {
                        for (int j = 0; j < keyValues.size(); j++) {
                            String value = keyValues.getString(j);
                            Utils.checkValueLengthAndThrowMPE(value, "message.property.value.long");
                            properties.addValue(key, value);
                        }
                    }
                }
            }
            this.properties(properties.build());

            final JsonObject diagnosticsObject = jsonObject.getJsonObject("diagnostics");
            if ( diagnosticsObject != null ){
                Set<Map.Entry<String,JsonValue>> entries = diagnosticsObject.entrySet();
                for (Map.Entry<String,JsonValue> entry : entries) {
                    String key = entry.getKey();
                    JsonValue jsonValue = entry.getValue();
                    final JsonValue.ValueType valueType = jsonValue.getValueType();
                    if ( valueType == JsonValue.ValueType.NUMBER ) {
                        this.diagnostic(key, ((JsonNumber) jsonValue).numberValue());
                    } else if (valueType == JsonValue.ValueType.TRUE || valueType == JsonValue.ValueType.FALSE) {
                        this.diagnostic(key, valueType == JsonValue.ValueType.TRUE);
                    } else { // treat everything else as String
                        this.diagnostic(key, jsonValue.toString());
                    }
                }
            }

            final String direction = Utils.jsonToString(jsonObject.getJsonString("direction"));
            if ( direction != null ) {
                try {
                    this.direction(Message.Direction.valueOf(direction));
                } catch (IllegalArgumentException e) {
                    throw new MessageParsingException("message.direction.wrong", e);
                }
            }
            if ( jsonObject.get("receivedTime") != null &&
                    jsonObject.get("receivedTime").getValueType() == JsonValue.ValueType.NUMBER) {
                final JsonNumber timeJSON = jsonObject.getJsonNumber("receivedTime");
                this.receivedTime(timeJSON.longValue());
            }
            if ( jsonObject.get("sentTime") != null &&
                    jsonObject.get("sentTime").getValueType() == JsonValue.ValueType.NUMBER) {
                final JsonNumber timeJSON = jsonObject.getJsonNumber("sentTime");
                this.sentTime(timeJSON.longValue());
            }

            return self();
        }

        /**
         * Abstract build method
         *
         * @return message
         */
        public abstract Message build();
    }

    /**
     * A unique ID for each message generated by the server (on receipt of a message from the client or when
     * a message is generated by the server)
     */
    protected final String id;
    /**
     * A unique ID for each message. It is generated when the message is first
     * created or can be passed in by the user.
     */
    protected final String clientId;
    /**
     * Source of the message.
     */
    protected final String source;
    /**
     * Destination of the message.
     */
    protected final String destination;
    /**
     * Message priority.
     */
    protected final Priority priority;
    /**
     * Reliability for message delivering.
     */
    protected final Reliability reliability;
    /**
     * Message event time.
     */
    protected final Date eventTime;
    /**
     * Extra information for the message.
     */
    protected final MessageProperties properties;
    /**
     * The message sender (should correspond to endpoint id).
     */
    protected String sender;
    /**
     * The message diagnostics
     */
    protected Map<String,Object> diagnostics;
    /**
     * The message direction (with respect to the server)
     */
    protected Direction direction;
    /**
     * The time when the message was received by the server (epoch value in milliseconds)
     */
    protected Long receivedTime;
    /**
     * The time when the message was sent by the server (epoch value in milliseconds)
     */
    protected Long sentTime;

    /**
     * Message constructor takes message builder and set values to each field.
     * If the value is {@code null}, set a default value. The default value for priority
     * is {@link Priority#LOW}. The default value for reliability is {@link Reliability#BEST_EFFORT}
     *
     * @param builder MessageBuilder
     * @throws IllegalArgumentException when the message has no source and no destination set.
     */
    protected Message(MessageBuilder<?> builder) {
        if (builder.clientId != null) {
            this.clientId = builder.clientId;
        } else {
            // Don't require clientId to be set, auto-generate one if it isn't.
            this.clientId = UUID.randomUUID().toString();
        }

        this.id = builder.id;

        if ((builder.source == null || builder.source.isEmpty())) {
            throw new IllegalArgumentException("Source cannot be null or empty.");
        }
            this.source = builder.source;

        if (builder.destination == null) {
            this.destination = "";
        } else {
            this.destination = builder.destination;
        }

        if (builder.sender == null) {
            this.sender = "";
        } else {
            this.sender = builder.sender;
        }

        if (builder.eventTime == null) {
            this.eventTime = new Date();
        } else {
            this.eventTime = builder.eventTime;
        }

        if (builder.priority == null) {
            this.priority = Priority.LOW;
        } else {
            this.priority = builder.priority;
        }
        if (builder.reliability == null){
            this.reliability = Message.Reliability.BEST_EFFORT;
        } else {
            this.reliability = builder.reliability;
        }

        if (builder.properties == null) {
            MessageProperties.Builder propertyBuilder = new MessageProperties.Builder();
            this.properties = propertyBuilder.build();

        } else {
            this.properties = builder.properties;
        }

        this.diagnostics = builder.diagnostics;
        this.diagnostics = (builder.diagnostics != null ) ? Collections.unmodifiableMap(builder.diagnostics) : null;
        this.direction = builder.direction;
        this.receivedTime = builder.receivedTime;
        this.sentTime = builder.sentTime;
    }

    /**
     * Get unique ID for a message.
     *
     * @return Message id ({@link String}), never {@code null}
     */
    public final String getId() {
        return id;
    }

    /**
     * Abstract method to return message type.
     *
     * @return type, never {@code null}.
     */
    public abstract Type getType();

    /**
     * Get client's unique ID for a message.
     *
     * @return Message id ({@link String}), never {@code null}
     */
    public final String getClientId() {
        return clientId;
    }

    /**
     * Get message source (Endpoint Id from which the message is originated).
     *
     * @return source, may be {@code null} if the message was sent from server.
     */
    public final String getSource() {
        return source;
    }

    /**
     * Get message destination (Endpoint Id to which the message is originated).
     *
     * @return destination, may be {@code null} if the message was sent to server.
     */
    public final String getDestination() {
        return destination;
    }

    /**
     * Get message priority.
     *
     * @return priority, never {@code null}.
     */
    public final Priority getPriority() {
        return priority;
    }

    /**
     * Get message reliability
     *
     * @return reliability, never {@code null}.
     */
    public final Reliability getReliability() { return reliability;}

    /**
     * Get event time of the message
     *
     * @return eventTime, never {@code null}.
     */
    public final Date getEventTime() {
        return new Date(eventTime.getTime());
    }

    /**
     * Get message properties.
     *
     * @return properties, never {@code null}
     */
    public final MessageProperties getProperties() {
        return this.properties;
    }

    /**
     * Get message sender (Endpoint Id from which the message is sent from).
     *
     * @return properties, never {@code null}
     */
    public final String getSender() {
        return this.sender;
    }

    /**
     * Get message diagnostics
     *
     * @return message diagnostics, can be {@code null}
     */
    public final Map<String,Object> getDiagnostics() {
        return this.diagnostics;
    }

    public final String getDiagnosticStringValue(String diagName) {
        if ( diagName != null && this.diagnostics != null ){
            Object value = this.diagnostics.get(diagName);
            return (value instanceof  String) ? (String)value : null;
        }
        return null;
    }

    public final Number getDiagnosticNumberValue(String diagName) {
        if ( diagName != null && this.diagnostics != null ){
            Object value = this.diagnostics.get(diagName);
            return (value instanceof  Number) ? (Number)value : null;
        }
        return null;
    }

    public final Boolean getDiagnosticBooleanValue(String diagName) {
        if ( diagName != null && this.diagnostics != null ){
            Object value = this.diagnostics.get(diagName);
            return (value instanceof  Boolean) ? (Boolean)value : Boolean.FALSE;
        }
        return Boolean.FALSE;
    }

    /**
     * Get message direction.
     *
     * @return message direction, can be {@code null}
     */
    public final Direction getDirection() {
        return this.direction;
    }

    /**
     * Get message received time.
     *
     * @return message received time, can be {@code null}
     */
    public final Long getReceivedTime() {
        return this.receivedTime;
    }

    /**
     * Get message sent time.
     *
     * @return message sent time, can be {@code null}
     */
    public final Long getSentTime() {
        return this.sentTime;
    }

    /**
     * Abstract method to return MessageBuilder. This MessageBuilder should be populated with the content
     * of the message object that invokes it.
     *
     * @return returns {@link DataMessage.Builder}.
     */
    public abstract MessageBuilder getMessageBuilder();

    /**
     * Exports common data from {@link Message} to {@link String} using JSON interpretation of the message.
     *
     * @return JSON interpretation of the message as {@link String}.
     */
    public String toString() {
        return toJSON().toString();
    }

    /**
     * Export the basic properties of the message to {@link JsonObject}.
     *
     * @return message fields in JsonObject format, never {@code null}
     */
    public JsonObject toJSON() {
        return Utils.commonFieldsToJSON(this).build();
    }

    /**
     * Export the basic properties of the message to {@link JsonObject}.
     *
     * @param expand {@code boolean} flag indicating if properties to be included or not. Default is True.
     * @return message fields in {@link JsonObject} format, never {@code null}.
     */
    public JsonObject toJSON(boolean expand) {
        return Utils.commonFieldsToJSON(this, expand).build();
    }

    /**
     * Convert a {@link List} of messages into a {@link JsonArray}.
     *
     * @param messages The {@link List} of {@link Message}s
     * @return A {@link JsonArray} representing the list of {@link Message}s, never {@code null}.
     */
    public static JsonArray toJSON(List<? extends Message> messages) {
        final JsonArrayBuilder builder = Json.createArrayBuilder();

        for (Message message : messages) {
            if (message != null) {
                builder.add(message.toJSON());
            }
        }
        return builder.build();
    }

    /**
     * Convert a {@link List} of {@link Message}s into a {@link JsonArray}.
     *
     * @param messages The {@link List} of {@link Message}s.
     * @param expand {@code boolean} flag indicating if properties to be included or not.
     * @return A {@link JsonArray} representing the list of {@link Message}s
     */
    public static JsonArray toJSON(List<Message> messages, boolean expand) {
        final JsonArrayBuilder builder = Json.createArrayBuilder();

        for (Message message : messages) {
            if (message != null) {
                builder.add(message.toJSON(expand));
            }
        }
        return builder.build();
    }

    /**
     * Convert a json string to a {@link List} of {@link Message}s.
     *
     * @param jsonString {@link String} containing json interpretation of {@link Message}s
     * @return The {@link List} of {@link Message}s from the jsonString.
     * @throws MessageParsingException when jsonString is {@code null}.
     */
    public static List<Message> fromJSON(String jsonString) {
        if (jsonString == null) {
            throw new MessageParsingException("message.jsonString.null");
        }

        return fromJSON(jsonString.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * Convert a byte array to a {@link List} of {@link Message}s.
     *
     * @param messagesByteArray {@code byte[]} containing json interpretation of {@link Message}s
     * @return The {@link List} of {@link Message}s from the {@code byte[]}
     * @throws MessageParsingException when messageArray is {@code null}.
     */
    public static List<Message> fromJSON(byte[] messagesByteArray) {
        if (messagesByteArray == null) {
            throw new MessageParsingException("message.byteArray.null");
        }

        final InputStream is = new ByteArrayInputStream(messagesByteArray);
        final JsonReader jsonReader = Json.createReader(is);
        final JsonStructure structure = jsonReader.read();
        jsonReader.close();

        return fromJSON(structure);
    }

    /**
     * Convert a {@link JsonStructure} to a {@link List} of {@link Message}s.
     *
     * @param structure {@link JsonStructure} containing {@code Message}s
     * @return The {@link List} of {@link Message}s from the {@link JsonStructure}
     * @throws MessageParsingException when structure is {@code null} or when other exception occur during parsing
     */
    public static List<Message> fromJSON(JsonStructure structure) {
        if (structure == null) {
            throw new MessageParsingException("message.structure.null");
        }

        final List<Message> messageCollection = new ArrayList<Message>();
        if (JsonValue.ValueType.ARRAY.equals(structure.getValueType())) {
            final JsonArray jsonArray = (JsonArray) structure;
            for (int i = 0; i < jsonArray.size(); i++) {
                final JsonObject jsonObject = jsonArray.getJsonObject(i);
                try {
                    messageCollection.add(getMessage(jsonObject));
                } catch (Exception e) {
                    if (e instanceof MessageParsingException) {
                        throw (MessageParsingException)e;
                    } else {
                        throw new MessageParsingException("message.parsing.unknown", e);
                    }
                }
            }
        } else if (JsonValue.ValueType.OBJECT.equals(structure.getValueType())) {
            final JsonObject jsonObject = (JsonObject) structure;
            messageCollection.add(getMessage(jsonObject));
        }
        return messageCollection;
    }

    /**
     * Convert a byte array to a {@link List} of {@link oracle.iot.message.Message.MessageBuilder}s.
     *
     * @param messagesByteArray {@code byte[]} containing json interpretation of {@link Message}s
     * @return The {@link List} of {@link oracle.iot.message.Message.MessageBuilder}s from the {@code byte[]}
     * @throws MessageParsingException when messageArray is {@code null}.
     */
    public static List<Message.MessageBuilder> createBuilderFromJSON(byte[] messagesByteArray) {
        if (messagesByteArray == null) {
            throw new MessageParsingException("message.byteArray.null");
        }

        final InputStream is = new ByteArrayInputStream(messagesByteArray);
        final JsonReader jsonReader = Json.createReader(is);
        final JsonStructure structure = jsonReader.read();
        jsonReader.close();
        return createBuilderFromJSON(structure);
    }

    private static List<Message.MessageBuilder> createBuilderFromJSON(JsonStructure structure) {
        if (structure == null) {
            throw new MessageParsingException("message.structure.null");
        }

        final List<Message.MessageBuilder> messageBuilderList = new ArrayList<Message.MessageBuilder>();
        if (JsonValue.ValueType.ARRAY.equals(structure.getValueType())) {
            final JsonArray jsonArray = (JsonArray) structure;
            for (int i = 0; i < jsonArray.size(); i++) {
                final JsonObject jsonObject = jsonArray.getJsonObject(i);
                try {
                    messageBuilderList.add(getMessageBuilder(jsonObject));
                } catch (Exception e) {
                    if (e instanceof MessageParsingException) {
                        throw (MessageParsingException)e;
                    } else {
                        throw new MessageParsingException("message.parsing.unknown", e);
                    }
                }
            }
        } else if (JsonValue.ValueType.OBJECT.equals(structure.getValueType())) {
            final JsonObject jsonObject = (JsonObject) structure;
            messageBuilderList.add(getMessageBuilder(jsonObject));
        }
        return messageBuilderList;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Message message = (Message) o;

        if (id == null ? (message.id != null) : (!id.equals(message.id))) return false;
        if (!clientId.equals(message.clientId)) return false;
        if (!eventTime.equals(message.eventTime)) return false;
        if (!destination.equals(message.destination)) return false;
        if (priority != message.priority) return false;
        if (!properties.equals(message.properties)) return false;
        if (reliability != message.reliability) return false;
        if (!source.equals(message.source)) return false;
        if (!sender.equals(message.sender)) return false;
        if (diagnostics == null ? (message.diagnostics != null) : (!diagnostics.equals(message.diagnostics))) return false;
        if (direction == null ? (message.direction != null) : (!direction.equals(message.direction))) return false;
        if (receivedTime == null ? (message.receivedTime != null) : (!receivedTime.equals(message.receivedTime))) return false;
        if (sentTime == null ? (message.sentTime != null) : (!sentTime.equals(message.sentTime))) return false;
        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        int result = (id != null) ? id.hashCode():0;
            result = 31 * result + clientId.hashCode();
        result = 31 * result + source.hashCode();
        result = 31 * result + destination.hashCode();
        result = 31 * result + priority.hashCode();
        result = 31 * result + reliability.hashCode();
        result = 31 * result + eventTime.hashCode();
        result = 31 * result + properties.hashCode();
        result = 31 * result + sender.hashCode();
        if ( diagnostics != null )
            result = 31 * result + diagnostics.hashCode();
        if ( direction != null )
            result = 31 * result + direction.hashCode();
        if ( receivedTime != null )
            result = 31 * result + receivedTime.hashCode();
        if ( sentTime != null )
            result = 31 * result + sentTime.hashCode();
        return result;
    }

    /**
     * Method to deserialize one {@link Message} object from JSON interpretation.
     * @param jsonObject JSON interpretation of a {@link Message}.
     * @return {@link Message} object extracted from jsonObject
     * @throws MessageParsingException when json object or does not contain field "type" or type is not supported.
     */
    private static Message getMessage(JsonObject jsonObject) {
        return getMessageBuilder(jsonObject).build();
    }

    private static Message.MessageBuilder getMessageBuilder(JsonObject jsonObject) {
        if (jsonObject != null && jsonObject.getJsonString("type") != null) {
            final Type type;
            try {
                type = Type.valueOf(jsonObject.getJsonString("type").getString());
            } catch (IllegalArgumentException e) {
                throw new MessageParsingException("message.type.illegal", e);
            }
            if (type != null) {
                switch (type) {
                    case DATA:
                        return new DataMessage.Builder().fromJSON(jsonObject);
                    case REQUEST:
                        return new HttpRequestMessage.Builder().fromJSON(jsonObject);
                    case RESPONSE:
                        return new HttpResponseMessage.Builder().fromJSON(jsonObject);
//                    case UPDATE_BUNDLE:
//                        return new UpdateBundleMessage.Builder().fromJSON(jsonObject);
//                    case UPDATE_BUNDLE_STATE:
//                        return new UpdateBundleStateMessage.Builder().fromJSON(jsonObject);
//                    case BUNDLE_FAILURE:
//                        return new BundleFailureMessage.Builder().fromJSON(jsonObject);
//                    case WAKEUP:
//                        return new WakeUpMessage.Builder().fromJSON(jsonObject);
//                    case RESOURCE_DIRECTORY:
//                        return new ResourceDirectoryMessage.Builder().fromJSON(jsonObject);
//                    case LOG:
//                        return new LogMessage.Builder().fromJSON(jsonObject);
                    case ALERT:
                        return new AlertMessage.Builder().fromJSON(jsonObject);
                }
            }
        }
        throw new MessageParsingException("message.type.null");
    }

    /**
     * A utility class contains methods that can be used in messages.
     * This class is package private.
     */
    protected static final class Utils {

        //no instances here
        private Utils() {}

        /**
         * Constant defines maximum length of string key in Json
         */
        static final int MAX_KEY_LENGTH = 2048;

        /**
         * Constant defines maximum length of string value in Json
         */
        static final int MAX_STRING_VALUE_LENGTH = 4096;

        /**
         * Method checks length of the string parameter. It converts it to {@code byte[]} using UTF-8 encoding at first.
         * Then if it is longer than {@link Utils#MAX_KEY_LENGTH} bytes, method throws
         * {@link MessageParsingException}.
         * @param key String param for checking.
         * @param message Text of error to be thrown.
         * @throws MessageParsingException If the key is longer than {@link Utils#MAX_KEY_LENGTH} bytes.
         */
        public static void checkKeyLengthAndThrowMPE(final String key, final String message) {
            try {
                if (key.getBytes("UTF-8").length >= MAX_KEY_LENGTH) {
                    throw new MessageParsingException(message);
                }
            } catch (UnsupportedEncodingException e) {
                // This should never happen since "UTF-8" is valid encoding name
                e.printStackTrace();
            }
        }

        /**
         * Method checks length of the string parameter. It converts it to {@code byte[]} using UTF-8 encoding at first.
         * Then if it is longer than {@link Utils#MAX_STRING_VALUE_LENGTH} bytes, method throws
         * {@link MessageParsingException}.
         * @param value String param for checking.
         * @param message Text of error to be thrown.
         * @throws MessageParsingException If the value is longer than {@link Utils#MAX_STRING_VALUE_LENGTH} bytes.
         */
        public static void checkValueLengthAndThrowMPE(final String value, final String message) {
            try {
                if (value.getBytes("UTF-8").length >= MAX_STRING_VALUE_LENGTH) {
                    throw new MessageParsingException(message);
                }
            } catch (UnsupportedEncodingException e) {
                // This should never happen since "UTF-8" is valid encoding name
                e.printStackTrace();
            }
        }

        /**
         * Method checks whether the value is {@code null} or not. If it is {@code null}, method throws
         * {@link MessageParsingException}.
         * @param value Value for checking for {@code null}
         * @param message Text of error to be thrown.
         * @throws MessageParsingException If the value is {@code null}.
         */
        public static void checkNullValueAndThrowMPE(final Object value, final String message) {
            if (value == null)
                throw new MessageParsingException(message);
        }

        /**
         * Method checks whether the {@link String} value is {@code null} or empty. If it is, method throws
         * {@link MessageParsingException}.
         * @param value Value for checking for {@code null} or empty
         * @param message Text of error to be thrown.
         * @throws MessageParsingException If the value is {@code null} or empty.
         */
        public static void checkNullOrEmptyStringThrowMPE(final String value, final String message) {
            if (value == null || value.isEmpty()) {
                throw new MessageParsingException(message);
            }
        }

        /**
         * Method checks length of the string parameter. It converts it to {@code byte[]} using UTF-8 encoding at first.
         * Then if it is longer than {@link Utils#MAX_KEY_LENGTH} bytes, method throws
         * {@link IllegalArgumentException}.
         * @param key String param for checking.
         * @param message Text of error to be thrown.
         * @throws IllegalArgumentException If the key is longer than {@link Utils#MAX_KEY_LENGTH} bytes.
         */
        public static void checkKeyLengthAndThrowIAE(final String key, final String message) {
            try {
                if (key.getBytes("UTF-8").length >= MAX_KEY_LENGTH) {
                    throw new IllegalArgumentException(message + " is longer than " + MAX_KEY_LENGTH + " bytes.");
                }
            } catch (UnsupportedEncodingException e) {
                // This should never happen since "UTF-8" is valid encoding name
                e.printStackTrace();
            }
        }

        /**
         * Method checks length of the string parameter. It converts it to {@code byte[]} using UTF-8 encoding at first. Then
         * if it is longer than {@link Utils#MAX_STRING_VALUE_LENGTH} bytes, method throws
         * {@link IllegalArgumentException}.
         * @param value String param for checking.
         * @param message Text of error to be thrown.
         * @throws IllegalArgumentException If the value is longer than {@link Utils#MAX_STRING_VALUE_LENGTH} bytes.
         */
        public static void checkValueLengthAndThrowIAE(final String value, final String message) {
            if (value == null) return;
            try {
                if (value.getBytes("UTF-8").length >= MAX_STRING_VALUE_LENGTH) {
                    throw new IllegalArgumentException(message + " is longer than " + MAX_STRING_VALUE_LENGTH + " bytes.");
                }
            } catch (UnsupportedEncodingException e) {
                // This should never happen since "UTF-8" is valid encoding name
                e.printStackTrace();
            }
        }

        /**
         * Method checks length of items in the {@link Collection}. It converts them to {@code byte[]} using UTF-8 encoding
         * at first. Then if any is longer than {@link Utils#MAX_STRING_VALUE_LENGTH} bytes,
         * {@link IllegalArgumentException} is thrown.
         * @param values {@link Collection} of {@code String} to be checked.
         * @param message Text of error to be thrown.
         * @throws IllegalArgumentException If any of the the value is longer than {@link Utils#MAX_STRING_VALUE_LENGTH}
         *                                  bytes.
         */
        public static void checkValuesLengthAndThrowIAE(final Collection<String> values, final String message) {
            if (values == null) return;
            for (String value: values) {
                try {
                    if (value.getBytes("UTF-8").length >= MAX_STRING_VALUE_LENGTH) {
                        throw new IllegalArgumentException(message + " contains value longer than " + MAX_STRING_VALUE_LENGTH + " bytes.");
                    }
                } catch (UnsupportedEncodingException e) {
                    // This should never happen since "UTF-8" is valid encoding name
                    e.printStackTrace();
                }
            }
        }

        /**
         * Method checks whether the collection contains non-null values. If the collection is {@code null}, no
         * {@link Exception} is thrown.
         *
         * @param values Checked collection.
         * @param message Text for exception message
         * @throws NullPointerException Exception is thrown when {@link Collection} contains null value
         */
        public static void checkNullValuesThrowsNPE(final Collection<String> values, final String message) {
            if (values == null) {
                return;
            }
            for (String value : values){
                if (value == null) {
                    throw new NullPointerException(message + " contains null value");
                }
            }
        }

        /**
         * Method checks whether the first parameter is {@code null} and returns {@link NullPointerException} accordingly.
         * @param checkedValue Checked value.
         * @param message Text for exception message
         * @throws NullPointerException Exception thrown when checkedValue is null.
         */
        public static void checkNullValueThrowsNPE(final Object checkedValue, final String message) {
            if (checkedValue == null) {
                throw new NullPointerException(message + " is null");
            }
        }

        /**
         * Method checks whether the first parameter is empty and returns {@link IllegalArgumentException} accordingly.
         * @param checkedValue Checked value.
         * @param message Text for exception message
         * @throws IllegalArgumentException Exception thrown when checkedValue is empty.
         */
        public static void checkEmptyStringThrowsIAE(final String checkedValue, final String message) {
            if (checkedValue.isEmpty()){
                throw new IllegalArgumentException(message + " is empty");
            }
        }

        /**
         * Method computes hash code for byte array
         * @param a Array of bytes
         * @return hash code
         */
        public static int hashCodeByteArray(final byte a[]) {
            if (a == null)
                return 0;

            int result = 1;
            for (byte element : a)
                result = 31 * result + element;

            return result;
        }

        /**
         * Method converts Json string to java String
         * @param jsonString Json interpretation of string
         * @return String value
         */
        public static String jsonToString(JsonString jsonString) {
            if (jsonString != null) {
                return jsonString.getString();
            }
            return null;
        }

        /**
         * Checks whether characters inside string are US-ASCII printable chars
         * @param text Text for checking
         * @return true, if the text is contain only US-ASCII printable characters or for null strings
         */
        public static boolean isAsciiPrintable(final String text) {
            if (text == null)
                return true;
            for (char ch : text.toCharArray()) {
                if (ch < 32 || ch > 126)
                    return false;
            }
            return true;
        }

        /**
         * Checks whether strings inside collection contain US-ASCII printable chars only
         *
         * @param texts Collection of string to be checked
         * @return true, if the texts contain only US-ASCII printable characters or for null collection
         */
        public static boolean isAsciiPrintable(final Collection<String> texts) {
            if (texts == null) return true;
            for (String text : texts) {
                if (!isAsciiPrintable(text)) {
                    return false;
                }
            }
            return true;
        }

        /**
         * Check whether the http header contains US-ASCII printable characters only
         * @param name name of the header
         * @param values values of the header
         * @return true, if all string contain US-ASCII printable characters
         */
        public static boolean isHttpHeaderAsciiPrintable(final String name, final List<String> values) {
            return isAsciiPrintable(name) && isAsciiPrintable(values);
        }

        /**
         * Returns a {@link JsonObjectBuilder} containing common fields of the message.
         * @param message the message to convert
         * @return {@link JsonObjectBuilder}, never {@code null}
         */
        public static JsonObjectBuilder commonFieldsToJSON(Message message) {
            return commonFieldsToJSON(message, true);
        }

        /**
         * Returns a {@link JsonObjectBuilder} containing common fields of the message.
         * @param message the message to convert
         * @param expand {@code boolean} flag indicating if properties to be included or not.
         * @return {@link JsonObjectBuilder}, never {@code null}
         */
        public static JsonObjectBuilder commonFieldsToJSON(Message message, boolean expand) {
            final JsonObjectBuilder builder = Json.createObjectBuilder();

            if ( message.getId() != null)
                builder.add("id", message.getId());
            if ( message.getClientId() != null)
                builder.add("clientId", message.getClientId());
            builder.add("source", message.getSource());
            builder.add("destination", message.getDestination());
            builder.add("priority", message.getPriority().toString());
            builder.add("reliability", message.getReliability().toString());
            builder.add("eventTime", (message.getEventTime().getTime()));
            builder.add("sender", message.getSender());
            builder.add("type", message.getType().name());

            if (expand == true) {
                builder.add("properties", message.getProperties().toJSON());
            }

            if ( message.getDirection() != null ){
                builder.add("direction", message.getDirection().name());
            }
            if ( message.getReceivedTime() != null ){
                builder.add("receivedTime", message.getReceivedTime().longValue());
            }
            if ( message.getSentTime() != null ){
                builder.add("sentTime", message.getSentTime().longValue());
            }
            if ( message.getDiagnostics() != null ){
                builder.add("diagnostics", toJSON(message.getDiagnostics()));
            }

            return builder;
        }

        public static JsonObject toJSON(Map<String,Object> map) {
            final JsonObjectBuilder builder = Json.createObjectBuilder();
            final Set<Map.Entry<String,Object>> entries = map.entrySet();
            for(Map.Entry<String,Object> entry : entries) {
                final String key = entry.getKey();
                final Object value = entry.getValue();
                if ( value instanceof Boolean ){
                    builder.add(key, (Boolean)value);
                } else if ( value instanceof Number ){
                    if ( value instanceof Double )
                        builder.add(key, ((Double)value).doubleValue());
                    else
                        builder.add(key, ((Long)value).longValue());
                } else if ( value instanceof String ) {
                    builder.add(key, value.toString());
                }
            }
            return builder.build();
        }

        /**
         * This method convert {@link java.util.Date} to {@link String} in format "yyyy-MM-dd'T'HH:mm:ss.SSSXXX".
         * The string representation is then used in JSON.
         *
         * @param date {@link Date} to be converted.
         * @return {@link String} representation of the the date.
         */
        public static String dateToString(final Date date) {
            final SimpleDateFormat dateFormat = DATE_FORMATTER.get();
            return dateFormat.format(date);
        }

        /**
         * This method convert {@link String} representation of the date in format "yyyy-MM-dd'T'HH:mm:ss.SSSXXX"
         * to {@link Date}.
         *
         * @param date String representation of the date.
         * @return {@link Date}.
         */
        public static Date stringToDate(final String date) {
            final SimpleDateFormat dateFormat = DATE_FORMATTER.get();
            return dateFormat.parse(date, new java.text.ParsePosition(0));
        }


        private static final ThreadLocal<SimpleDateFormat> DATE_FORMATTER = new ThreadLocal<SimpleDateFormat>() {
            @Override
            protected SimpleDateFormat initialValue() {
                return new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX");
            }
        };

    }
}
