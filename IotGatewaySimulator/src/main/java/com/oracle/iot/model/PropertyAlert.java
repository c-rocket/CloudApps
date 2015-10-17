package com.oracle.iot.model;

public class PropertyAlert {
	private String name;
	private String displayName;

	public PropertyAlert(String name, String displayName) {
		super();
		this.name = name;
		this.displayName = displayName;
	}

	public String getName() {
		return name;
	}

	public String getDisplayName() {
		return displayName;
	}

}
