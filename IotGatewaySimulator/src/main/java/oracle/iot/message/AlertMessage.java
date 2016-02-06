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
import com.oracle.json.JsonObject;
import com.oracle.json.JsonObjectBuilder;
import com.oracle.json.JsonValue;

import java.lang.IllegalArgumentException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * AlertMessage extends Message class. It can be used for alerts to IoT Server. This class
 * is immutable
 */
public final class AlertMessage extends Message {

    /** optional description for this alert */
    private String description;

    /** severity of this alert */
    private Severity severity;

    /**
     * The Severity of this Alert
     */
    public enum Severity {
        LOW(4), NORMAL(3), SIGNIFICANT(2), CRITICAL(1);

        /** int value of the severity */
        private final int value;

        /**
         * Constructs enum from appropriate value. The value should be in the range [1-4] (CRITICAL=1,
         * LOW=4)
         */
        private Severity(int value) {
            this.value = value;
        }

        /**
         * Returns severity as int value.
         * @return value of the severity
         */
        public int getValue() {
            return value;
        }
    }

    /** format for the alert message */
    private final String format;

    /** Actual data for the alert message */
    private final List<DataItem<?>> items;

    /**
     * AlertMessage constructor takes AlertMessage.Builder and set values to each
     * field. If the value is {@code null},this method will set a default value.
     * format cannot be {@code null} or empty. description cannot be {@code null}
     * or empty.
     *
     * @param builder AlertMessage.Builder
     *
     * @throws IllegalArgumentException if the format is not specified or is empty
     * @throws IllegalArgumentException if the description is not specified or is empty
     */
    private AlertMessage(Builder builder) {
        super(builder);

        if ((builder.format == null || builder.format.isEmpty())) {
            throw new IllegalArgumentException("format cannot be null or empty.");
        } else {
            this.format = builder.format;
        }

        if ((builder.description == null || builder.description.isEmpty())) {
            throw new IllegalArgumentException("description cannot be null or empty.");
        } else {
            this.description = builder.description;
        }

        items = Collections.unmodifiableList(new ArrayList<DataItem<?>>(builder.items));

        if(builder.severity == null) {
            this.severity = Severity.SIGNIFICANT;
        }
        else {
            this.severity = builder.severity;
        }
    }


    /**
     * Gets the severity of this alert
     *
     * @return the severity of this alert
     */
    public Severity getSeverity() {
        return severity;
    }

    /**
     * Gets the description for this alert.
     *
     * @return the description for this alert, never {@code null}.
     */
    public String getDescription() {
        return description;
    }
    /**
     * Get the name of the format for data.
     *
     * @return data format, never {@code null}.
     */
    public final String getFormat() {
        return this.format;
    }

    /**
     * Get message payload as {@link List} of {@link DataItem}s.
     *
     * @return Payload that is carried by {@link AlertMessage}, never {@code null}.
     */
    public final List<DataItem<?>> getDataItems() {
        return Collections.unmodifiableList(this.items);
    }

    /**
     * {@link Builder} extends {@link Message.MessageBuilder} class. {@link AlertMessage} class is immutable. A
     * {@link Builder} is required when creating {@link AlertMessage}. {@link AlertMessage} uses Builder
     * design pattern.
     */
    public final static class Builder extends MessageBuilder<Builder> {


        /** message for the alert */
        private String description;

        /** severity for the alert */
        private Severity severity;

        public Builder() {
            // Alert messages are highest priority
            priority = Priority.HIGHEST;

            // default severity is SIGNIFICANT
            severity = Severity.SIGNIFICANT;
        }

        /** format for the alert message */
        private String format;

        /** List of data items */
        private List<DataItem<?>> items = new ArrayList<DataItem<?>>();

        /**
         * Set alert severity
         *
         * @param severity the severity for the (@link AlertMessage}
         * @return Builder with update severity field
         */
        public final Builder severity(Severity severity) {
            this.severity = severity;
            return self();
        }

        /**
         * Set the description for the alert
         *
         * @param description description for the {@link AlertMessage}
         * @return Builder with updated format field.
         */
        public final Builder description(String description) {
            this.description = description;
            return self();
        }

        /**
         * Set message format.
         *
         * @param format format for the {@link AlertMessage}
         * @return Builder with updated format field.
         */
        public final Builder format(String format) {
            this.format = format;
            return self();
        }

        /**
         * Add {@code double} {@link DataItem}. Key cannot be {@code null}, empty or long {@link String}.
         *
         * @param key {@link String} item key.
         * @param value {@code double} item value.
         * @param <T> builder
         * @return Builder with added new dataItem field.
         *
         * @throws IllegalArgumentException when value is {@link Double#NEGATIVE_INFINITY}, {@link Double#POSITIVE_INFINITY},
         *                                  {@link Double#NaN} or the key is empty or long string.
         * @throws NullPointerException when the key is {@code null}.
         */
        public final <T> Builder dataItem(String key, double value) {
            this.items.add(new DataItem<Double>(key, value));
            return self();
        }

        /**
         * Add {@code boolean} {@link DataItem}. Key cannot be {@code null}, empty or long {@link String}.
         *
         * @param key {@link String} item key
         * @param value {@code boolean} item value
         * @param <T> builder
         * @return Builder with added new dataItem field.
         *
         * @throws IllegalArgumentException when the key is empty or long string.
         * @throws NullPointerException when the key is {@code null}.
         */
        public final <T> Builder dataItem(String key, boolean value) {
            this.items.add(new DataItem<Boolean>(key, value));
            return self();
        }

        /**
         * Add {@link String} {@link DataItem}. Key cannot be {@code null}, empty or long {@link String}.
         * Value cannot be long {@link String}.
         *
         * @param key {@link String} item key.
         * @param value {@link String} item value.
         * @param <T> builder
         * @return Builder with added new dataItem field.
         *
         * @throws IllegalArgumentException when the key is empty, key or value are long strings.
         * @throws NullPointerException when the key or value are {@code null}.
         */
        public final <T> Builder dataItem(String key, String value) {
            this.items.add(new DataItem<String>(key, value));
            return self();
        }

        /**
         * Add all {@link DataItem}s to existing ones.
         *
         * @param dataItems {@link Collection} of {@link DataItem}.
         * @param <T> builder
         * @return Builder with added new dataItem field.
         */
        public final <T> Builder dataItems(Collection<DataItem<?>> dataItems) {
            this.items.addAll(dataItems);
            return self();
        }

        /**
         * Creates new instance of {@link AlertMessage} using values from {@link AlertMessage.Builder}.
         * @return Instance of {@link AlertMessage}
         */
        @Override
        public final AlertMessage build() {
            return new AlertMessage(this);

        }

        /**
         * Returns current instance of {@link AlertMessage.Builder}.
         * @return Instance of {@link AlertMessage.Builder}
         */
        @Override
        protected final Builder self() {
            return this;
        }

        /**
         * Copy data from another {@link AlertMessage} to this {@link AlertMessage}.
         *
         * @param message Message to copy data from.
         * @return Builder with updated fields from other instance of {@link AlertMessage}.
         * @throws IllegalArgumentException when instance of other message than {@link AlertMessage} is passed.
         */
        @Override
        public final Builder copy(Message message) {
            if (message instanceof AlertMessage) {
                AlertMessage alertMsg = (AlertMessage) message;
                super.copy(alertMsg);
                this.items.addAll(alertMsg.getDataItems());
                this.format = alertMsg.format;
                this.description= alertMsg.description;
                this.severity = alertMsg.severity;
            } else {
                throw new IllegalArgumentException("Can not copy a different type of message");
            }
            return self();
        }

        /**
         * Method to convert from an instance of {@link JsonObject} to a {@link oracle.iot.message.AlertMessage.Builder}.
         *
         * @param jsonObject The jsonObject to convert.
         * @return A new {@link oracle.iot.message.AlertMessage.Builder} constructed from the JSON string.
         * @throws MessageParsingException when json object is wrong or does not contain mandatory fields.
         */
        @Override
        public final Builder fromJSON(JsonObject jsonObject) {
            super.fromJSON(jsonObject);

            final JsonObject payload = jsonObject.getJsonObject("payload");
            Utils.checkNullValueAndThrowMPE(payload, "alert.message.payload.null");

            final String format  = Utils.jsonToString(payload.getJsonString("format"));
            Utils.checkNullOrEmptyStringThrowMPE(format, "alert.message.format.null");
            this.format(format);

            final String severity  = Utils.jsonToString(payload.getJsonString("severity"));
            Utils.checkNullOrEmptyStringThrowMPE(format, "alert.message.severity.null");
            try {
                this.severity(Severity.valueOf(severity));
            } catch(IllegalArgumentException e) {
                throw new MessageParsingException("alert.message.severity.invalid");
            }

            final String description  = Utils.jsonToString(payload.getJsonString("description"));
            Utils.checkNullOrEmptyStringThrowMPE(description, "alert.message.description.null");
            this.description(description);

            final JsonObject data = payload.getJsonObject("data");
            if (data != null) {
                final Set<Map.Entry<String,JsonValue>> entries = data.entrySet();
                for (Map.Entry<String,JsonValue> entry : entries) {
                    String key = entry.getKey();
                    Utils.checkKeyLengthAndThrowMPE(key, "alert.message.item.key.long");
                    JsonValue value = entry.getValue();
                    JsonValue.ValueType valueType = value.getValueType();
                    switch (valueType) {
                        case TRUE:
                        case FALSE:
                            this.dataItem(key, data.getBoolean(key));
                            break;
                        case STRING:
                            String stringValue = data.getString(key);
                            Utils.checkValueLengthAndThrowMPE(stringValue, "alert.message.item.value.long");
                            this.dataItem(key, stringValue);
                            break;
                        case NUMBER:
                            this.dataItem(key, data.getJsonNumber(key).doubleValue());
                            break;
                        default:
                            throw new MessageParsingException("alert.message.item.value.unexpected");
                    }
                }
            }
            else {
                throw new MessageParsingException("alert.message.data.null");
            }


            return self();
        }

    }

    @Override
    public Type getType() {
        return Type.ALERT;
    }

    /**
     * Method to return MessageBuilder that is populated with content of this instance.
     *
     * @return returns {@link DataMessage.Builder}.
     */
    @Override
    public MessageBuilder getMessageBuilder() {
        return new AlertMessage.Builder().copy(this);
    }

    /**
     * Exports data from {@link AlertMessage} to {@link String} using JSON interpretation of the message.
     * @return JSON interpretation of the message as {@link String}.
     */
    @Override
    public final String toString() {
        return toJSON().toString();
    }

    /**
     * Export {@link AlertMessage} to {@link JsonObject}.
     * @return {@link oracle.iot.message.AlertMessage} as {@link JsonObject}.
     */
    @Override
    public JsonObject toJSON() {
        final JsonObjectBuilder builder = Utils.commonFieldsToJSON(this);

        builder.add("type", Type.ALERT.name());

        JsonObjectBuilder payload = Json.createObjectBuilder();
        payload.add("format", this.format);
        payload.add("description", this.description);
        payload.add("severity", this.getSeverity().toString());

        final JsonObjectBuilder dataItems = Json.createObjectBuilder();

        for (DataItem<?> item : items) {
            switch(item.getType()) {
                case STRING  : dataItems.add(item.getKey(), (String) item.getValue()); break;
                case BOOLEAN : dataItems.add(item.getKey(), (Boolean) item.getValue()); break;
                case DOUBLE  : dataItems.add(item.getKey(), (Double) item.getValue()); break;
            }
        }
        payload.add("data", dataItems);

        builder.add("payload", payload);



        return builder.build();
    }

    /**
     * Export {@link AlertMessage} to {@link JsonObject}.
     * @param expand {@link Boolean} flag indicating if the payload and properties to be included or not. Default value is true.
     * @return {@link AlertMessage} as {@link JsonObject}
     */
    @Override
    public JsonObject toJSON(boolean expand) {
        if (expand == true) {
            return toJSON();
        }

        final JsonObjectBuilder builder = Utils.commonFieldsToJSON(this, false);
        builder.add("type", Type.ALERT.name());

        return builder.build();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;

        AlertMessage that = (AlertMessage) o;
        if(!description.equals(that.description)) return false;
        if(!severity.equals(that.severity)) return false;
        if (!items.equals(that.items)) return false;

        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + items.hashCode();
        result = 31 * result + description.hashCode();
        result = 31 * result + severity.hashCode();
        return result;
    }

}
