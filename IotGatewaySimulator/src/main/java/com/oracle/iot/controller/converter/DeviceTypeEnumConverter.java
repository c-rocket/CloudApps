package com.oracle.iot.controller.converter;

import java.beans.PropertyEditorSupport;

import com.oracle.iot.model.DeviceType;;

public class DeviceTypeEnumConverter extends PropertyEditorSupport {

	@Override
	public void setAsText(final String text) throws IllegalArgumentException {
		setValue(DeviceType.findByName(text.trim()));
	}

}
