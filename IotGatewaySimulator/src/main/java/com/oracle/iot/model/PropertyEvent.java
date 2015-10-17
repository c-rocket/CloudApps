package com.oracle.iot.model;

import java.util.ArrayList;
import java.util.List;

public class PropertyEvent {

	private String name;
	private String displayName;
	private Integer priority;
	private List<EventMetric> eventMetrics = new ArrayList<EventMetric>();

	public PropertyEvent(String name, String displayName, Integer priority) {
		super();
		this.name = name;
		this.displayName = displayName;
		this.priority = priority;
	}

	public String getName() {
		return name;
	}

	public String getDisplayName() {
		return displayName;
	}

	public List<EventMetric> getEventMetrics() {
		return eventMetrics;
	}

	public void addEventMetric(String metricName, Double value, Double increment, Double alternate, Double loop,
			Double max, Double min) {
		eventMetrics.add(new EventMetric(metricName, value, increment, alternate, loop, max, min));
	}

	public Integer getPriority() {
		return priority;
	}

}
