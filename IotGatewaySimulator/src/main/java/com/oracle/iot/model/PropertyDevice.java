package com.oracle.iot.model;

import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.log4j.Logger;
import org.joda.time.DateTime;

import com.fasterxml.jackson.annotation.JsonIgnore;

import oracle.iot.message.AlertMessage;
import oracle.iot.message.DataMessage;
import oracle.iot.message.Message;

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
		for (PropertyMetric metric : details.getMetrics()) {
			currentMetrics.put(metric.getDisplayName(), metric.getDefaultValue());
		}
		log.info("Creating Events: " + details.getEvents().size());
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

	public void animateMetrics() {
		Map<PropertyMetric, Double> calcs = new LinkedHashMap<PropertyMetric, Double>();
		// place default values
		for (PropertyMetric metric : details.getMetrics()) {
			Double value = Constants.RandomFourPercent(metric.getDefaultValue());
			value = calculateAnimatedValue(metric, Constants.RandomFourPercent(metric.getDefaultValue()));
			calcs.put(metric, value);
		}
		// loop through events and change values if needed
		for (PropertyEvent event : eventTriggers.keySet()) {
			// loop through metrics in event that is active
			if (eventTriggers.get(event)) {
				for (EventMetric eventMetric : event.getEventMetrics()) {
					PropertyMetric metric = getPropertyMetric(calcs, eventMetric);
					Double value = calcs.get(metric);
					value = calculateAnimatedEventValue(eventMetric, metric, value);

					calcs.put(metric, value);
				}
			}
		}

		// write out updated values as current values
		for (PropertyMetric metric : calcs.keySet()) {
			Double newValue = Constants.scale(calcs.get(metric), 2);
			currentMetrics.put(metric.getDisplayName(), newValue);
		}
	}

	private Double calculateAnimatedEventValue(EventMetric eventMetric, PropertyMetric metric, Double value) {
		// hold number at current value
		if (eventMetric.getHold()) {
			value = (Double) currentMetrics.get(metric.getDisplayName());
		}
		// set straight up value
		if (eventMetric.getEventValue() != null) {
			value = eventMetric.getEventValue();
		}
		// increment
		if (eventMetric.getIncrement() != null) {
			value = (Double) currentMetrics.get(metric.getDisplayName()) + eventMetric.getIncrement();
		}
		// loop
		if (eventMetric.getLoop() != null) {
			value = (Double) currentMetrics.get(metric.getDisplayName()) + eventMetric.getLoop();
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
		// alternate
		if (eventMetric.getAlternate() != null) {
			if (((Double) currentMetrics.get(metric.getDisplayName())).compareTo(eventMetric.getAlternate()) != 0) {
				value = eventMetric.getAlternate();
			} else {
				value = eventMetric.getEventValue();
			}
		}
		return value;
	}

	private Double calculateAnimatedValue(PropertyMetric metric, Double value) {
		if (currentMetrics.get(metric.getDisplayName()) == null) {
			value = metric.getDefaultValue();
		}
		// increment
		if (metric.getIncrement() != null) {
			value = (Double) currentMetrics.get(metric.getDisplayName()) + metric.getIncrement();
		}
		// loop
		if (metric.getLoop() != null) {
			value = (Double) currentMetrics.get(metric.getDisplayName()) + metric.getLoop();
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
		// alternate
		if (metric.getAlternate() != null) {
			if (value.compareTo(metric.getAlternate()) != 0) {
				value = metric.getAlternate();
			} else {
				value = metric.getDefaultValue();
			}
		}
		return value;
	}

	private PropertyMetric getPropertyMetric(Map<PropertyMetric, Double> list, EventMetric eventMetric) {
		for (PropertyMetric metric : list.keySet()) {
			if (metric.getName().equals(eventMetric.getMetricName())) {
				return metric;
			}
		}
		return null;
	}

	@Override
	public AlertMessage createAlertMessage(String alertName) {
		String description = "Invalid Alert";
		for (PropertyAlert alert : details.getAlerts()) {
			if (alert.getName().equalsIgnoreCase(alertName)) {
				description = alert.getDisplayName();
				break;
			}
		}
		AlertMessage.Builder alertBuilder = new AlertMessage.Builder();
		alertBuilder.format(details.getAlertFormat());
		alertBuilder.source(getId());
		alertBuilder.description(description);

		for (String key : currentMetrics.keySet()) {
			Double metric = (Double) currentMetrics.get(key);
			alertBuilder.dataItem(key, metric);
		}

		alertBuilder.severity(AlertMessage.Severity.CRITICAL);
		log.info("Created Alert: " + alertBuilder.build().toString());
		return alertBuilder.build();
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
	public DataMessage createMessage() {
		animateMetrics();

		DateTime messageDate = new DateTime();
		DataMessage.Builder msgBuilder = new DataMessage.Builder();
		msgBuilder.format(details.getDataFormat());
		msgBuilder.source(getId());

		for (String key : currentMetrics.keySet()) {
			Double metric = (Double) currentMetrics.get(key);
			msgBuilder.dataItem(key, metric);
			addToChart(messageDate, key, metric);
		}
		msgBuilder.reliability(Message.Reliability.BEST_EFFORT);
		msgBuilder.priority(Message.Priority.MEDIUM);
		return msgBuilder.build();
	}

	@Override
	public String getPicture() {
		return details.getPicture();
	}

	@Override
	public String getThumbnail() {
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

}
