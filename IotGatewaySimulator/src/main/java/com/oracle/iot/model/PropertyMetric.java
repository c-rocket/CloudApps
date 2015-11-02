package com.oracle.iot.model;

public class PropertyMetric {

	private String name = null;
	private String displayName = null;
	private Double defaultValue = null;
	private Double increment = null;
	private Double alternate = null;
	private Double loop = null;
	private Double max = null;
	private Double min = null;
	private Boolean boolSet = null;

	public PropertyMetric(String name, String displayName, Double defaultValue, Double increment, Double alternate,
			Double loop, Double max, Double min) {
		super();
		this.name = name;
		this.displayName = displayName;
		this.defaultValue = defaultValue;
		this.increment = increment;
		this.alternate = alternate;
		this.loop = loop;
		this.max = max;
		this.min = min;
	}

	public PropertyMetric(String name, String displayName, Boolean boolSet) {
		super();
		this.name = name;
		this.displayName = displayName;
		this.boolSet = boolSet;
	}

	public String getName() {
		return name;
	}

	public String getDisplayName() {
		return displayName;
	}

	public Double getDefaultValue() {
		return defaultValue;
	}

	public Double getIncrement() {
		return increment;
	}

	public Double getAlternate() {
		return alternate;
	}

	public Double getLoop() {
		return loop;
	}

	public Double getMax() {
		return max;
	}

	public Double getMin() {
		return min;
	}

	public Boolean getBoolSet() {
		return boolSet;
	}

	public void setDefaultValue(Double defaultValue) {
		this.defaultValue = defaultValue;
	}

	public void setBoolSet(Boolean boolSet) {
		this.boolSet = boolSet;
	}

}
