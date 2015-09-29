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

public class DrillSite extends IOTDevice {
	private static final String DATA_FORMAT = "urn:oracle:iot:device:modem";
	private static final String ALERT_FORMAT = "urn:oracle:iot:alert:hvac";

	@JsonIgnore
	protected double drillRpm = 200;
	@JsonIgnore
	protected double temperature = 140;
	@JsonIgnore
	protected double depth = 0;
	@JsonIgnore
	protected double vibration = 1.0;
	@JsonIgnore
	protected double maxDepth = 5900;

	@JsonIgnore
	protected boolean eventDrillSlowDown = false;
	@JsonIgnore
	protected boolean eventDrillOverheat = false;
	@JsonIgnore
	protected boolean eventDrillFailure = false;

	public DrillSite(String id, String secret) {
		super(id, secret);
	}

	@Override
	public Map<String, String> getAlerts() {
		Map<String, String> alerts = new LinkedHashMap<String, String>();
		alerts.put("alertSensorDisconnect", "Sensor Disconnect");
		return alerts;
	}

	@Override
	public Map<String, Object> getEvents() {
		Map<String, Object> events = new LinkedHashMap<String, Object>();
		Map<String, Object> obj = new LinkedHashMap<String, Object>();
		obj.put("display", "Drill Slow Down");
		obj.put("value", eventDrillSlowDown);
		events.put("eventDrillSlowDown", obj);

		obj = new LinkedHashMap<String, Object>();
		obj.put("display", "Drill Overheat");
		obj.put("value", eventDrillOverheat);
		events.put("eventDrillOverheat", obj);

		obj = new LinkedHashMap<String, Object>();
		obj.put("display", "Drill Failure");
		obj.put("value", eventDrillFailure);
		events.put("eventDrillFailure", obj);
		return events;
	}

	@Override
	public Map<String, Object> getMetrics() {
		Map<String, Object> map = new LinkedHashMap<String, Object>();
		map.put("Drill RPM", drillRpm);
		map.put("Temperature", temperature);
		map.put("Depth", depth);
		map.put("Vibration", vibration);
		return map;
	}

	@Override
	public AlertMessage createAlertMessage(String alertMessage) {
		String description = "Bad Alert";
		if (alertMessage.equalsIgnoreCase("alertSensorDisconnect")) {
			description = "Sensor Disconnect";
		}

		AlertMessage.Builder alertBuilder = new AlertMessage.Builder();
		alertBuilder.format(ALERT_FORMAT).source(getId()).description(description).dataItem("drillSpeed", drillRpm)
				.dataItem("temperature", temperature).dataItem("depth", depth).dataItem("vibration", vibration)
				.eventTime(new DateTime().toDate()).severity(AlertMessage.Severity.CRITICAL);

		return alertBuilder.build();
	}

	@Override
	public Boolean eventHandler(String eventMessage) {
		if (eventMessage.equalsIgnoreCase("eventDrillSlowDown")) {
			eventDrillSlowDown = !eventDrillSlowDown;
			return true;
		}
		if (eventMessage.equalsIgnoreCase("eventDrillOverheat")) {
			eventDrillOverheat = !eventDrillOverheat;
			return true;
		}
		if (eventMessage.equalsIgnoreCase("eventDrillFailure")) {
			eventDrillFailure = !eventDrillFailure;
			return true;
		}
		return false;
	}

	@Override
	public DataMessage createMessage() {
		animateMetrics();
		DateTime messageDate = new DateTime();
		DataMessage.Builder msgBuilder = new DataMessage.Builder();
		msgBuilder.format(DATA_FORMAT).source(getId()).dataItem("drillSpeed", drillRpm)
				.dataItem("temperature", temperature).dataItem("depth", depth).dataItem("vibration", vibration)
				.eventTime(messageDate.toDate()).reliability(Message.Reliability.BEST_EFFORT)
				.priority(Message.Priority.MEDIUM);
		DataMessage message = msgBuilder.build();
		addToChart(messageDate, "Drill RPM", drillRpm);
		addToChart(messageDate, "Temperature", temperature);
		addToChart(messageDate, "Depth", depth);
		addToChart(messageDate, "Vibration", vibration);

		return message;
	}

	private void animateMetrics() {
		if (eventDrillFailure) {
			drillRpm = 0;
			temperature = 0;
		} else {
			drillRpm = Constants.randomDouble(180, 200, 2);
			vibration = Constants.randomDouble(0.5, 3, 2);

			if (eventDrillOverheat) {
				temperature = temperature + 5;
			} else {
				temperature = Constants.randomDouble(140, 160, 2);
			}
			if (eventDrillSlowDown) {
				drillRpm = Constants.randomDouble(100, 120, 2);
				temperature = Constants.randomDouble(180, 200, 2);
				depth = depth + 2;
				vibration = Constants.randomDouble(3, 4.5, 2);
			} else {
				depth = depth + 10;
			}
			if (depth > maxDepth) {
				depth = 0;
			}
		}
	}

	@Override
	public String getPicture() {
		return "drill.png";
	}

	@Override
	public String getThumbnail() {
		return "drill-thumb.png";
	}

	@Override
	public String getResource() {
		return "drill";
	}

	@Override
	public IOTDevice copy() {
		DrillSite copy = new DrillSite(getId(), getSecret());
		copy.drillRpm = this.drillRpm;
		copy.temperature = this.temperature;
		copy.depth = this.depth;
		copy.vibration = this.vibration;
		copy.eventDrillFailure = this.eventDrillFailure;
		copy.eventDrillOverheat = this.eventDrillOverheat;
		copy.eventDrillSlowDown = this.eventDrillSlowDown;
		copy.createDate = this.createDate;
		copy.chartSeries = Constants.copyStringList(this.chartSeries);
		copy.chartValues = Constants.copyListofLists(this.chartValues);
		copy.chartLabels = Constants.copyStringList(this.chartLabels);

		return copy;
	}

}
