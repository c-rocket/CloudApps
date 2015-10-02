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

public class HVAC extends IOTDevice {
	private static final String DATA_FORMAT = "com:oracle:iot:model:devices:hvac";
	private static final String ALERT_FORMAT = "com:oracle:iot:model:devices:alert:myhvac";

	@JsonIgnore
	protected double outputTemp = 30.0;
	@JsonIgnore
	protected double vibration = 1.0;
	@JsonIgnore
	protected double oilViscosity = 0.25;
	@JsonIgnore
	protected double motorAmperage = 50.0;

	@JsonIgnore
	protected boolean eventMotorFailure = false;
	@JsonIgnore
	protected boolean eventHvacNotWorking = false;
	@JsonIgnore
	protected boolean eventMotorOverheat = false;

	public HVAC(String id, String secret) {
		super(id, secret);
	}

	@Override
	public Map<String, String> getAlerts() {
		Map<String, String> alerts = new LinkedHashMap<String, String>();
		alerts.put("alertDoorOpen", "Door Open");
		alerts.put("alertDoorClosed", "Door Closed");
		return alerts;
	}

	@Override
	public Map<String, Object> getEvents() {
		Map<String, Object> events = new LinkedHashMap<String, Object>();
		Map<String, Object> obj = new LinkedHashMap<String, Object>();
		obj.put("display", "Motor Failure");
		obj.put("value", eventMotorFailure);
		events.put("eventMotorFailure", obj);

		obj = new LinkedHashMap<String, Object>();
		obj.put("display", "HVAC Failure");
		obj.put("value", eventHvacNotWorking);
		events.put("eventHvacNotWorking", obj);

		obj = new LinkedHashMap<String, Object>();
		obj.put("display", "Overheat Motor");
		obj.put("value", eventMotorOverheat);
		events.put("eventMotorOverheat", obj);
		return events;
	}

	@Override
	public Map<String, Object> getMetrics() {
		Map<String, Object> map = new LinkedHashMap<String, Object>();
		map.put("Output Temp (C)", outputTemp);
		map.put("Vibration (G)", vibration);
		map.put("Oil Viscosity (cP)", oilViscosity);
		map.put("Motor Amperage (A)", motorAmperage);
		return map;
	}

	@Override
	public AlertMessage createAlertMessage(String alertMessage) {
		String description = "Bad Alert";
		if (alertMessage.equalsIgnoreCase("alertDoorOpen")) {
			description = "Unit Door is Open";
		} else if (alertMessage.equalsIgnoreCase("alertDoorClosed")) {
			description = "Unit Door is Closed";
		}

		AlertMessage.Builder alertBuilder = new AlertMessage.Builder();
		alertBuilder.format(ALERT_FORMAT).source(getId()).description(description).dataItem("outputTemp", outputTemp)
				.dataItem("vibration", vibration).dataItem("oilViscosity", oilViscosity)
				.dataItem("motorAmperage", motorAmperage).severity(AlertMessage.Severity.CRITICAL);

		return alertBuilder.build();
	}

	@Override
	public Boolean eventHandler(String eventMessage) {
		if (eventMessage.equalsIgnoreCase("eventMotorFailure")) {
			eventMotorFailure = !eventMotorFailure;
			if (!eventMotorFailure)
				motorAmperage = 50.0;
			return true;
		} else if (eventMessage.equalsIgnoreCase("eventHvacNotWorking")) {
			eventHvacNotWorking = !eventHvacNotWorking;
			if (!eventHvacNotWorking) {
				outputTemp = 30.0;
				vibration = 1.0;
				oilViscosity = 0.25;
				motorAmperage = 50.0;
			}
			return true;
		} else if (eventMessage.equalsIgnoreCase("eventMotorOverheat")) {
			eventMotorOverheat = !eventMotorOverheat;
			if (!eventMotorOverheat)
				outputTemp = 30.0;
			return true;
		}
		return false;
	}

	@Override
	public DataMessage createMessage() {
		animateMetrics();
		DateTime messageDate = new DateTime();
		DataMessage.Builder msgBuilder = new DataMessage.Builder();
		msgBuilder.format(DATA_FORMAT).source(getId()).dataItem("outputTemp", outputTemp)
				.dataItem("vibration", vibration).dataItem("oilViscosity", oilViscosity)
				.dataItem("motorAmperage", motorAmperage).reliability(Message.Reliability.BEST_EFFORT)
				.priority(Message.Priority.MEDIUM);
		DataMessage message = msgBuilder.build();
		addToChart(messageDate, "Output Temp", outputTemp);
		addToChart(messageDate, "Vibration", vibration);
		addToChart(messageDate, "Oil Viscosity", oilViscosity);
		addToChart(messageDate, "Motor Amperage", motorAmperage);

		return message;
	}

	private void animateMetrics() {
		if (eventHvacNotWorking) {
			outputTemp = 0;
			vibration = 0;
			oilViscosity = 0;
			motorAmperage = 0;
		} else {
			vibration = Constants.randomDouble(0.5, 3, 2);
			oilViscosity = Constants.randomDouble(0.1, 0.4, 2);

			if (eventMotorFailure) {
				if (motorAmperage != 75)
					motorAmperage = 75;
				else
					motorAmperage = 0;
			} else {
				motorAmperage = Constants.randomDouble(42, 53, 2);
			}
			if (eventMotorOverheat) {
				if (outputTemp < 120) {
					outputTemp = outputTemp + 5;
				}
			} else {
				outputTemp = Constants.randomDouble(28.0, 32, 2);
			}
		}
	}

	@Override
	public String getPicture() {
		return "hvac.png";
	}

	@Override
	public String getThumbnail() {
		return "hvac-thumb.png";
	}

	@Override
	public String getResource() {
		return "hvac";
	}

	@Override
	public IOTDevice copy() {
		HVAC copy = new HVAC(getId(), getSecret());
		copy.outputTemp = this.outputTemp;
		copy.vibration = this.vibration;
		copy.oilViscosity = this.oilViscosity;
		copy.motorAmperage = this.motorAmperage;
		copy.eventMotorFailure = this.eventMotorFailure;
		copy.eventHvacNotWorking = this.eventHvacNotWorking;
		copy.eventMotorOverheat = this.eventMotorOverheat;
		copy.createDate = this.createDate;
		copy.chartSeries = Constants.copyStringList(this.chartSeries);
		copy.chartValues = Constants.copyListofLists(this.chartValues);
		copy.chartLabels = Constants.copyStringList(this.chartLabels);

		return copy;
	}

}
