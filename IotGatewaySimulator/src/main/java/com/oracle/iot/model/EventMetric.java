package com.oracle.iot.model;

public class EventMetric {
	private String metricName = null;
	private Double eventValue = null;
	private Double increment = null;
	private Double alternate = null;
	private Double loop = null;
	private Double max = null;
	private Double min = null;
	private Boolean hold = Boolean.FALSE;
	private Boolean boolSet = null;
	private Double variation = null;

	public EventMetric(String metricName, Double eventValue, Double increment, Double alternate, Double loop,
			Double max, Double min, Boolean hold, Double variation) {
		super();
		this.metricName = metricName;
		this.eventValue = eventValue;
		this.increment = increment;
		this.alternate = alternate;
		this.loop = loop;
		this.max = max;
		this.min = min;
		this.hold = hold;
		this.variation = variation;
	}

	public EventMetric(String metricName, Boolean boolSet) {
		super();
		this.metricName = metricName;
		this.boolSet = boolSet;
	}

	public String getMetricName() {
		return metricName;
	}

	public Double getEventValue() {
		return eventValue;
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

	public Boolean getHold() {
		return hold;
	}

	public Boolean getBoolSet() {
		return boolSet;
	}

	public Double getVariation() {
		return variation;
	}

}
