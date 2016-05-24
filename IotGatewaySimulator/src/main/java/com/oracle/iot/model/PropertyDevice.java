package com.oracle.iot.model;

import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.log4j.Logger;
import org.joda.time.DateTime;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.oracle.iot.util.Constants;

import oracle.iot.client.AbstractVirtualDevice.ChangeEvent;
import oracle.iot.client.AbstractVirtualDevice.NamedValue;
import oracle.iot.client.device.Alert;
import oracle.iot.client.device.VirtualDevice;

public class PropertyDevice extends IOTDevice {
	@JsonIgnore
	private static final Logger log = Logger.getLogger(IOTDevice.class);
	@JsonIgnore
	private static final String DISPLAY = "display";
	@JsonIgnore
	private static final String VALUE = "value";
	@JsonIgnore
	private PropertyDeviceDetails details;
	@JsonIgnore
	Map<String, Object> currentMetrics = new LinkedHashMap<String, Object>();
	@JsonIgnore
	Map<PropertyEvent, Boolean> eventTriggers = new LinkedHashMap<PropertyEvent, Boolean>();

	public PropertyDevice(PropertyDeviceDetails details, String id, String secret) {
		super(id, secret);
		this.details = details;
		// define all the default values for the existing metrics
		for (PropertyMetric metric : details.getMetrics()) {
			if (metric.getBoolSet() != null) {
				currentMetrics.put(metric.getDisplayName(), (boolean) metric.getBoolSet());
			} else {
				currentMetrics.put(metric.getDisplayName(), metric.getDefaultValue());
			}
		}
		for (PropertyEvent event : details.getEvents()) {
			eventTriggers.put(event, false);
		}
	}

	@Override
	public Map<String, String> getAlerts() {
		Map<String, String> alerts = new LinkedHashMap<String, String>();
		for (PropertyAlert alert : details.getAlerts()) {
			alerts.put(alert.getName(), alert.getDisplayName());
		}
		return alerts;
	}

	@Override
	public Map<String, Object> getEvents() {
		Map<String, Object> map = new LinkedHashMap<String, Object>();
		for (PropertyEvent event : eventTriggers.keySet()) {
			Map<String, Object> obj = new LinkedHashMap<String, Object>();
			obj.put(DISPLAY, event.getDisplayName());
			obj.put(VALUE, eventTriggers.get(event));
			map.put(event.getName(), obj);
		}
		return map;
	}

	@Override
	public Map<String, Object> getMetrics() {
		return currentMetrics;
	}

	@Override
	public void animateMetrics() {
		Map<PropertyMetric, Object> calcs = new LinkedHashMap<PropertyMetric, Object>();
		// place default values

		for (PropertyMetric metric : details.getMetrics()) {
			if (metric.getBoolSet() != null) {
				calcs.put(metric, (boolean) metric.getBoolSet());
			} else {
				Double value = calculateAnimatedValue(metric, metric.getDefaultValue());
				calcs.put(metric, value);
			}
		}
		// loop through events and change values if needed
		for (PropertyEvent event : eventTriggers.keySet()) {
			// loop through metrics in event that is active
			if (eventTriggers.get(event)) {
				for (EventMetric eventMetric : event.getEventMetrics()) {
					PropertyMetric metric = getPropertyMetric(calcs, eventMetric);
					if (calcs.get(metric) instanceof Double) {
						Double value = calculateAnimatedEventValue(eventMetric, metric, (Double) calcs.get(metric));
						calcs.put(metric, value);
					} else if (calcs.get(metric) instanceof Boolean) {
						Boolean value = eventMetric.getBoolSet();
						calcs.put(metric, value);
					}

				}
			}
		}

		// write out updated values as current values
		DateTime newMessageDateTime = new DateTime();
		for (PropertyMetric key : calcs.keySet()) {
			if (calcs.get(key) instanceof Double) {
				Double newValue = Constants.scale((Double) calcs.get(key), 2);
				currentMetrics.put(key.getDisplayName(), newValue);
				addToChart(newMessageDateTime, key.getDisplayName(), newValue);
			} else if (calcs.get(key) instanceof Boolean) {
				Boolean newValue = (Boolean) calcs.get(key);
				currentMetrics.put(key.getDisplayName(), newValue);
				addToChart(newMessageDateTime, key.getDisplayName(), newValue ? 1d : 0d);
			}
		}
	}

	private Double calculateAnimatedEventValue(EventMetric eventMetric, PropertyMetric metric, Double value) {
		// hold number at current value
		if (eventMetric.getHold()) {
			return (Double) currentMetrics.get(metric.getDisplayName());
		}
		// set straight up value
		if (eventMetric.getEventValue() != null) {
			value = Constants.randomDoubleWithinVariation(eventMetric.getEventValue(), eventMetric.getVariation());
		}
		// increment
		if (eventMetric.getIncrement() != null) {
			value = (Double) currentMetrics.get(metric.getDisplayName()) + eventMetric.getIncrement();
		}
		// loop
		if (eventMetric.getLoop() != null) {
			value = (Double) currentMetrics.get(metric.getDisplayName()) + eventMetric.getLoop();
		}
		// alternate
		if (eventMetric.getAlternate() != null) {
			if (!Constants.isWithinVariation((Double) currentMetrics.get(metric.getDisplayName()),
					eventMetric.getAlternate(), eventMetric.getVariation())) {
				return Constants.randomDoubleWithinVariation(eventMetric.getAlternate(), eventMetric.getVariation());
			} else {
				return Constants.randomDoubleWithinVariation(eventMetric.getEventValue(), eventMetric.getVariation());
			}
		}
		// max
		if (eventMetric.getMax() != null && value > eventMetric.getMax()) {
			if (eventMetric.getIncrement() != null) {
				value = eventMetric.getMax();
			} else if (eventMetric.getLoop() != null) {
				value = eventMetric.getEventValue();
			}
		}
		// min
		if (eventMetric.getMin() != null && value < eventMetric.getMin()) {
			if (eventMetric.getIncrement() != null) {
				value = eventMetric.getMin();
			} else if (eventMetric.getLoop() != null) {
				value = eventMetric.getEventValue();
			}
		}
		return value;
	}

	private Double calculateAnimatedValue(PropertyMetric metric, Double value) {
		// if we have no current metric (first time)
		// then start using the default value
		if (currentMetrics.get(metric.getDisplayName()) == null) {
			value = Constants.randomDoubleWithinVariation(metric.getDefaultValue(), metric.getVariation());
		}
		if (metric.getHold()) {
			return (Double) currentMetrics.get(metric.getDisplayName());
		} else {
			value = Constants.randomDoubleWithinVariation(metric.getDefaultValue(), metric.getVariation());
		}
		// increment
		if (metric.getIncrement() != null) {
			value = (Double) currentMetrics.get(metric.getDisplayName()) + metric.getIncrement();
		}
		// loop
		else if (metric.getLoop() != null) {
			value = (Double) currentMetrics.get(metric.getDisplayName()) + metric.getLoop();
		}
		// alternate
		else if (metric.getAlternate() != null) {
			if (!Constants.isWithinVariation((Double) currentMetrics.get(metric.getDisplayName()),
					metric.getAlternate(), metric.getVariation())) {
				return Constants.randomDoubleWithinVariation(metric.getAlternate(), metric.getVariation());
			} else {
				return Constants.randomDoubleWithinVariation(metric.getDefaultValue(), metric.getVariation());
			}
		}
		// max
		if (metric.getMax() != null && value > metric.getMax()) {
			if (metric.getIncrement() != null) {
				value = metric.getMax();
			} else if (metric.getLoop() != null) {
				value = metric.getDefaultValue();
			}
		}
		// min
		if (metric.getMin() != null && value < metric.getMin()) {
			if (metric.getIncrement() != null) {
				value = metric.getMin();
			} else if (metric.getLoop() != null) {
				value = metric.getDefaultValue();
			}
		}

		return value;
	}

	private PropertyMetric getPropertyMetric(Map<PropertyMetric, Object> list, EventMetric eventMetric) {
		for (PropertyMetric metric : list.keySet()) {
			if (metric.getName().equals(eventMetric.getMetricName())) {
				return metric;
			}
		}
		return null;
	}

	@Override
	public void alert(VirtualDevice virtualDevice, String alertName) {
		Alert alert = virtualDevice.createAlert(alertName);
		for (String key : currentMetrics.keySet()) {
			String id = getMetricNameByDisplayName(key);
			if (id != null) {
				if (currentMetrics.get(key) instanceof Double) {
					Double metricValue = (Double) currentMetrics.get(key);
					alert.set(id, metricValue);
				} else if (currentMetrics.get(key) instanceof Boolean) {
					Boolean metricValue = (Boolean) currentMetrics.get(key);
					alert.set(id, metricValue);
				}
			}
		}
		alert.raise();
	}

	private String getMetricNameByDisplayName(String displayName) {
		for (PropertyMetric metric : details.getMetrics()) {
			if (metric.getDisplayName().equals(displayName)) {
				return metric.getName();
			}
		}
		return null;
	}

	@Override
	public Boolean eventHandler(String eventName) {
		for (PropertyEvent event : eventTriggers.keySet()) {
			if (event.getName().equals(eventName)) {
				eventTriggers.put(event, !eventTriggers.get(event));
				return true;
			}
		}
		return false;
	}

	@Override
	public void update(VirtualDevice virtualDevice) {
		VirtualDevice update = virtualDevice.update();

		for (String key : currentMetrics.keySet()) {
			String id = getMetricNameByDisplayName(key);
			if (id != null) {
				update.set(id, currentMetrics.get(key));
			}
		}
		update.finish();
	}

	@Override
	public String getPicture() {
		return details.getPicture();
	}

	@Override
	public String getResource() {
		return details.getName();
	}

	@Override
	public IOTDevice copy() {
		return this;
	}

	@Override
	public void addCallbacks(VirtualDevice virtualDevice) {
		for (PropertyMetric metric : details.getMetrics()) {
			final String metricName = metric.getName();
			final String metricDisplay = metric.getDisplayName();
			if (metric.getBoolSet() != null) {
				virtualDevice.setOnChange(metricName, new VirtualDevice.ChangeCallback<VirtualDevice>() {
					@Override
					public void onChange(ChangeEvent<VirtualDevice> event) {
						NamedValue<?> namedValue = event.getNamedValue();
						currentMetrics.put(metricDisplay, Boolean.valueOf(namedValue.getValue().toString()));
					}
				});
			} else {
				virtualDevice.setOnChange(metricName, new VirtualDevice.ChangeCallback<VirtualDevice>() {
					@Override
					public void onChange(ChangeEvent<VirtualDevice> event) {
						NamedValue<?> namedValue = event.getNamedValue();
						currentMetrics.put(metricDisplay, namedValue.getValue());
					}
				});
			}
		}
	}
}
