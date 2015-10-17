package com.oracle.iot.model;

public class PropertyMetric {

	private String name;
	private String displayName;
	private Double defaultValue;
	private Double increment;
	private Double alternate;
	private Double loop;
	private Double max;
	private Double min;

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

}
