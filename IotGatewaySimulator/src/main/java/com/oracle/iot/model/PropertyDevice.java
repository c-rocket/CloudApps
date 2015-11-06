package com.oracle.iot.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.joda.time.DateTime;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.oracle.iot.util.Constants;

import oracle.iot.client.device.Resource;
import oracle.iot.client.device.Resource.Builder;
import oracle.iot.client.device.Resource.Method;
import oracle.iot.message.AlertMessage;
import oracle.iot.message.DataMessage;
import oracle.iot.message.HttpRequestMessage;
import oracle.iot.message.HttpResponseMessage;
import oracle.iot.message.Message;
import oracle.iot.message.RequestMessageHandler;
import oracle.iot.message.StatusCode;

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
	@JsonIgnore
	List<DeviceResource> resources = new ArrayList<DeviceResource>();

	public PropertyDevice(PropertyDeviceDetails details, String id, String secret) {
		super(id, secret);
		this.details = details;
		for (PropertyMetric metric : details.getMetrics()) {
			if (metric.getBoolSet() != null) {
				currentMetrics.put(metric.getDisplayName(), (boolean) metric.getBoolSet());
			} else {
				currentMetrics.put(metric.getDisplayName(), metric.getDefaultValue());
			}
			final PropertyDevice me = this;
			Builder resourceBuilder = getResourceBuilder(id, metric);
			DeviceResource resource = new DeviceResource(resourceBuilder.build(), new RequestMessageHandler() {

				@Override
				public HttpResponseMessage handleRequest(HttpRequestMessage request) throws Exception {
					String metricName = request.getURL();
					try {
						if (request.getMethod().equalsIgnoreCase("get")) {
							PropertyMetric metric = me.details.getMetricByName(metricName);
							if (metric != null) {
								String defaultValue;
								if (metric.getBoolSet() == null) {
									defaultValue = metric.getDefaultValue().toString();
								} else {
									defaultValue = metric.getBoolSet().toString();
								}
								return new HttpResponseMessage.Builder().header(metricName, Arrays.asList(defaultValue))
										.contentType("text/xml").url(metricName).body(defaultValue)
										.statusCode(StatusCode.OK).source(me.getId()).clientId(request.getClientId())
										.sender(request.getDestination()).destination(request.getSender())
										.requestId(request.getId()).build();
							}
						} else if (request.getMethod().equalsIgnoreCase("put")) {
							String value = request.getBodyString();
							PropertyMetric metric = me.details.getMetricByName(metricName);
							if (metric != null) {
								if (metric.getBoolSet() == null) {
									metric.setDefaultValue(Double.valueOf(value));
								} else {
									metric.setBoolSet(Boolean.valueOf(value));
								}
								return new HttpResponseMessage.Builder().header(metricName, Arrays.asList(value))
										.contentType("text/xml").url(metricName).body(value)
										.statusCode(StatusCode.ACCEPTED).source(me.getId())
										.clientId(request.getClientId()).sender(request.getDestination())
										.destination(request.getSender()).requestId(request.getId()).build();
							}
						}
					} catch (Exception e) {
						log.error("Problem with Endpoint manipulation", e);
					}
					return new HttpResponseMessage.Builder().header(metricName, null).contentType("text/xml")
							.url(metricName).body("invalid request").statusCode(StatusCode.BAD_REQUEST)
							.source(me.getId()).clientId(request.getClientId()).sender(request.getDestination())
							.destination(request.getSender()).requestId(request.getId()).build();
				}
			});
			resources.add(resource);
		}
		log.info("Creating Events: " + details.getEvents().size());
		for (

		PropertyEvent event : details.getEvents())

		{
			eventTriggers.put(event, false);
		}

	}

	private Builder getResourceBuilder(String id, PropertyMetric metric) {
		List<Method> methods = new ArrayList<Method>();
		methods.add(Method.PUT); // allow values to be updated
		methods.add(Method.GET); // allow user to request the value
		Builder resourceBuilder = new Resource.Builder();
		resourceBuilder.endpointName(id).name(metric.getName()).path(metric.getName()).methods(methods);
		return resourceBuilder;
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
		Map<PropertyMetric, Object> calcs = new LinkedHashMap<PropertyMetric, Object>();
		// place default values
		for (PropertyMetric metric : details.getMetrics()) {
			if (metric.getBoolSet() != null) {
				calcs.put(metric, (boolean) metric.getBoolSet());
			} else {
				Double value = calculateAnimatedValue(metric, metric.getDefaultValue());
				calcs.put(metric, Constants.randomDoubleWithinVariation(value));
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
		for (PropertyMetric metric : calcs.keySet()) {
			if (calcs.get(metric) instanceof Double) {
				Double newValue = Constants.scale((Double) calcs.get(metric), 2);
				currentMetrics.put(metric.getDisplayName(), newValue);
			} else if (calcs.get(metric) instanceof Boolean) {
				currentMetrics.put(metric.getDisplayName(), (Boolean) calcs.get(metric));
			}
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
			if (!Constants.isWithinVariation((Double) currentMetrics.get(metric.getDisplayName()),
					eventMetric.getAlternate())) {
				value = eventMetric.getAlternate();
			} else {
				value = eventMetric.getEventValue();
			}
		}
		return value;
	}

	private Double calculateAnimatedValue(PropertyMetric metric, Double value) {
		// if we have no current metric (first time)
		// then start using the default value
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
			if (!Constants.isWithinVariation((Double) currentMetrics.get(metric.getDisplayName()),
					metric.getAlternate())) {
				value = metric.getAlternate();
			} else {
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
			String id = getMetricNameByDisplayName(key);
			if (id != null) {
				if (currentMetrics.get(key) instanceof Double) {
					Double metricValue = (Double) currentMetrics.get(key);
					alertBuilder.dataItem(id, metricValue);
				} else if (currentMetrics.get(key) instanceof Boolean) {
					Boolean metricValue = (Boolean) currentMetrics.get(key);
					alertBuilder.dataItem(id, metricValue);
				}
			}
		}

		alertBuilder.severity(AlertMessage.Severity.CRITICAL);
		log.info("Created Alert: " + alertBuilder.build().toString());
		return alertBuilder.build();

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
	public DataMessage createMessage() {
		animateMetrics();

		DateTime messageDate = new DateTime();
		DataMessage.Builder msgBuilder = new DataMessage.Builder();
		msgBuilder.format(details.getDataFormat());
		msgBuilder.source(getId());

		for (String key : currentMetrics.keySet()) {
			String id = getMetricNameByDisplayName(key);
			if (id != null) {
				if (currentMetrics.get(key) instanceof Double) {
					Double metric = (Double) currentMetrics.get(key);
					msgBuilder.dataItem(id, metric);
					addToChart(messageDate, key, metric);
				} else if (currentMetrics.get(key) instanceof Boolean) {
					Boolean metric = (Boolean) currentMetrics.get(key);
					msgBuilder.dataItem(id, metric);
					addToChart(messageDate, key, metric ? 1d : 0d);
				}
			}
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

	@Override
	public List<DeviceResource> getResources() {
		return resources;
	}

}
