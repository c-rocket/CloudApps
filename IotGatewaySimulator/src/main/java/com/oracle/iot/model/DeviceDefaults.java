package com.oracle.iot.model;

public enum DeviceDefaults {
	MESSAGE_FREQUENCY(2);

	private Object value;

	DeviceDefaults(Object value) {
		this.value = value;
	}

	public Object getValue() {
		return this.value;
	}

}
