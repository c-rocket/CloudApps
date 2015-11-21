package com.oracle.iot.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class PropertyDeviceDetails {
	private String name;
	private String picture;
	private String displayName;
	private String dataFormat;
	private String alertFormat;
	private Boolean enabled = true;

	private List<PropertyMetric> metrics = new ArrayList<PropertyMetric>();
	private List<PropertyAlert> alerts = new ArrayList<PropertyAlert>();
	private Map<String, PropertyEvent> events = new LinkedHashMap<String, PropertyEvent>();

	public PropertyDeviceDetails(String name, String displayName, String dataFormat, String alertFormat,
			String picture) {
		super();
		this.name = name;
		this.displayName = displayName;
		this.dataFormat = dataFormat;
		this.alertFormat = alertFormat;
		this.picture = picture;
	}

	public void addMetric(String name, String display, Double defaultValue, Double increment, Double alternate,
			Double loop, Double max, Double min, Double variation, Boolean hold) {
		metrics.add(
				new PropertyMetric(name, display, defaultValue, increment, alternate, loop, max, min, variation, hold));
	}

	public List<PropertyMetric> getMetrics() {
		return metrics;
	}

	public String getName() {
		return name;
	}

	public String getDisplayName() {
		return displayName;
	}

	public void addEvent(String name, String displayName, Integer priority, String metricName, Double value,
			Double increment, Double alternate, Double loop, Double max, Double min, Boolean hold, Double variation) {
		PropertyEvent propertyEvent = events.get(name);

		if (propertyEvent == null) {
			propertyEvent = new PropertyEvent(name, displayName, priority);
			events.put(name, propertyEvent);
		}
		propertyEvent.addEventMetric(metricName, value, increment, alternate, loop, max, min, hold, variation);
	}

	public void addAlert(String alert, String displayName) {
		alerts.add(new PropertyAlert(alert, displayName));

	}

	public List<PropertyAlert> getAlerts() {
		return alerts;
	}

	public List<PropertyEvent> getEvents() {
		List<PropertyEvent> list = new ArrayList<PropertyEvent>(events.values());
		Collections.sort(list, new Comparator<PropertyEvent>() {
			@Override
			public int compare(PropertyEvent o1, PropertyEvent o2) {
				return o2.getPriority().compareTo(o1.getPriority());
			}
		});
		return list;
	}

	public String getDataFormat() {
		return dataFormat;
	}

	public String getAlertFormat() {
		return alertFormat;
	}

	public String getPicture() {
		return picture;
	}

	public void addMetric(String name, String display, Boolean boolSet) {
		metrics.add(new PropertyMetric(name, display, boolSet));
	}

	public void addEvent(String name, String displayName, Integer priority, String metricName, Boolean boolSet) {
		PropertyEvent propertyEvent = events.get(name);
		if (propertyEvent == null) {
			propertyEvent = new PropertyEvent(name, displayName, priority);
			events.put(name, propertyEvent);
		}
		propertyEvent.addEventMetric(metricName, boolSet);
	}

	public Boolean getEnabled() {
		return enabled;
	}

	public void enable() {
		this.enabled = true;
	}

	public void disable() {
		this.enabled = false;
	}

	public PropertyMetric getMetricByName(String metricName) {
		for (PropertyMetric metric : this.metrics) {
			if (metric.getName().equalsIgnoreCase(metricName)) {
				return metric;
			}
		}
		return null;
	}

	public void setPicture(String picture) {
		this.picture = picture;
	}
}