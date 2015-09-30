package com.oracle.iot.model.devices;

import java.util.LinkedHashMap;
import java.util.Map;

import org.joda.time.DateTime;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.oracle.iot.model.Constants;
import com.oracle.iot.model.IOTDevice;

import oracle.iot.message.AlertMessage;
import oracle.iot.message.DataMessage;
import oracle.iot.message.Message;

public class Pipeline extends IOTDevice {

	private static final String DATA_FORMAT = "com:oracle:iot:model:devices:pipeline";
	private static final String ALERT_FORMAT = "com:oracle:iot:model:devices:alert:pipeline";

	@JsonIgnore
	protected double capacityLoad = 60.0;
	@JsonIgnore
	protected double internalPressure = 12.50;
	@JsonIgnore
	protected double temperature = 65;
	@JsonIgnore
	protected double flowSpeed = 5; // meters per second

	@JsonIgnore
	protected boolean eventPressureDrop = false;
	@JsonIgnore
	protected boolean eventFullCapacity = false;
	@JsonIgnore
	protected boolean eventShutdown = false;

	public Pipeline(String id, String secret) {
		super(id, secret);
	}

	@Override
	public Map<String, String> getAlerts() {
		Map<String, String> alerts = new LinkedHashMap<String, String>();
		alerts.put("alertSensorFailure", "Sensor Failure");
		return alerts;
	}

	@Override
	public Map<String, Object> getEvents() {
		Map<String, Object> events = new LinkedHashMap<String, Object>();
		Map<String, Object> obj = new LinkedHashMap<String, Object>();
		obj.put("display", "Pressure Drop");
		obj.put("value", eventPressureDrop);
		events.put("eventPressureDrop", obj);

		obj = new LinkedHashMap<String, Object>();
		obj.put("display", "Full Capacity");
		obj.put("value", eventFullCapacity);
		events.put("eventFullCapacity", obj);

		obj = new LinkedHashMap<String, Object>();
		obj.put("display", "Shut Off");
		obj.put("value", eventShutdown);
		events.put("eventShutdown", obj);
		return events;
	}

	@Override
	public Map<String, Object> getMetrics() {
		Map<String, Object> map = new LinkedHashMap<String, Object>();
		map.put("Curr. Capacity", capacityLoad);
		map.put("Pressure (x100)", internalPressure);
		map.put("Temperature", temperature);
		map.put("Flow Speed", flowSpeed);
		return map;
	}

	@Override
	public AlertMessage createAlertMessage(String alertMessage) {
		String description = "Bad Alert";
		if (alertMessage.equalsIgnoreCase("alertSensorFailure")) {
			description = "Sensor Failure";
		}

		AlertMessage.Builder alertBuilder = new AlertMessage.Builder();
		alertBuilder.format(ALERT_FORMAT).source(getId()).description(description)
				.dataItem("capacityLoad", capacityLoad).dataItem("internalPressure", internalPressure)
				.dataItem("temperature", temperature).dataItem("flowRate", flowSpeed).eventTime(new DateTime().toDate())
				.severity(AlertMessage.Severity.CRITICAL);

		AlertMessage alert = alertBuilder.build();
		return alert;
	}

	@Override
	public Boolean eventHandler(String eventMessage) {
		// TODO Auto-generated method stub
		if (eventMessage.equalsIgnoreCase("eventPressureDrop")) {
			eventPressureDrop = !eventPressureDrop;
			if (!eventPressureDrop)
				internalPressure = 12.50;
			return true;
		}
		if (eventMessage.equalsIgnoreCase("eventFullCapacity")) {
			eventFullCapacity = !eventFullCapacity;
			if (!eventFullCapacity) {
				capacityLoad = 60;
			}
			return true;
		}
		if (eventMessage.equalsIgnoreCase("eventShutdown")) {
			eventShutdown = !eventShutdown;
			if (!eventShutdown) {
				internalPressure = 12.50;
				capacityLoad = 60;
				temperature = 25;
				flowSpeed = 5;
			}
			return true;
		}
		return false;
	}

	@Override
	public DataMessage createMessage() {
		animateMetrics();
		DateTime messageDate = new DateTime();
		DataMessage.Builder msgBuilder = new DataMessage.Builder();
		msgBuilder.format(DATA_FORMAT).source(getId()).dataItem("internalPressure", internalPressure)
				.dataItem("capacityLoad", capacityLoad).dataItem("temperature", temperature)
				.dataItem("flowSpeed", flowSpeed).eventTime(messageDate.toDate())
				.reliability(Message.Reliability.BEST_EFFORT).priority(Message.Priority.MEDIUM);
		DataMessage message = msgBuilder.build();
		addToChart(messageDate, "Curr. Capacity", capacityLoad);
		addToChart(messageDate, "Pressure (x100)", internalPressure);
		addToChart(messageDate, "Temperature", temperature);
		addToChart(messageDate, "Flow Speed", flowSpeed);

		return message;
	}

	private void animateMetrics() {
		if (eventShutdown) {
			capacityLoad = 0;
			internalPressure = 0;
			temperature = 25;
			flowSpeed = 0;
		} else {
			internalPressure = 1250;
			capacityLoad = 60;
			temperature = 25;
			flowSpeed = 5;

			capacityLoad = Constants.randomDouble(55, 65, 2);
			internalPressure = Constants.randomDouble(12.25, 12.75, 2);
			temperature = Constants.randomDouble(58, 62, 2);
			flowSpeed = Constants.randomDouble(4, 6, 2);

			if (eventFullCapacity) {
				capacityLoad = 100;
				flowSpeed = Constants.randomDouble(5, 7, 2);
				internalPressure = Constants.randomDouble(13.00, 13.75, 2);
				temperature = Constants.randomDouble(65, 69, 2);
			}
			if (eventPressureDrop) {
				flowSpeed = Constants.randomDouble(3.5, 4, 2);
				internalPressure = Constants.randomDouble(9.00, 9.50, 2);
				temperature = Constants.randomDouble(55, 58, 2);
			}
		}
	}

	@Override
	public String getPicture() {
		return "pipeline.png";
	}

	@Override
	public String getThumbnail() {
		return "pipeline-thumb.png";
	}

	@Override
	public String getResource() {
		return "pipeline";
	}

	@Override
	public IOTDevice copy() {
		Pipeline copy = new Pipeline(getId(), getSecret());
		copy.capacityLoad = this.capacityLoad;
		copy.internalPressure = this.internalPressure;
		copy.flowSpeed = this.flowSpeed;
		copy.temperature = this.temperature;
		copy.eventPressureDrop = this.eventPressureDrop;
		copy.eventFullCapacity = this.eventFullCapacity;
		copy.eventShutdown = this.eventShutdown;
		copy.createDate = this.createDate;
		copy.chartSeries = Constants.copyStringList(this.chartSeries);
		copy.chartValues = Constants.copyListofLists(this.chartValues);
		copy.chartLabels = Constants.copyStringList(this.chartLabels);

		return copy;
	}

}
