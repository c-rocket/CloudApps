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

public class FleetTruck extends IOTDevice {
	private static final String DATA_FORMAT = "urn:oracle:iot:device:fleet";
	private static final String ALERT_FORMAT = "urn:oracle:iot:alert:fleet";

	@JsonIgnore
	protected double currentSpeed = 80.0;
	@JsonIgnore
	protected double latitude = 51.03;
	@JsonIgnore
	protected double longitude = 114.05;
	@JsonIgnore
	protected double fuelLevel = 100;
	@JsonIgnore
	protected double engineTemperature = 93;
	@JsonIgnore
	private Double startLong = 114.05;
	@JsonIgnore
	private Double endLong = 113.33;
	@JsonIgnore
	private Double startLat = 51.03;
	@JsonIgnore
	private Double endLat = 53.32;
	@JsonIgnore
	private Boolean up = true;

	@JsonIgnore
	protected boolean eventEngineOverheat = false;
	@JsonIgnore
	protected boolean eventSpeedIncrease = false;
	@JsonIgnore
	protected boolean eventOffRoute = false;
	@JsonIgnore
	protected boolean eventEngineStall = false;

	public FleetTruck(String id, String secret) {
		super(id, secret);
	}

	@Override
	public Map<String, String> getAlerts() {
		Map<String, String> alerts = new LinkedHashMap<String, String>();
		alerts.put("alertDriverDoorOpen", "Driver Door Open");
		alerts.put("alertPassengerDoorOpen", "Passenger Door Open");
		alerts.put("alertCargoDoorOpen", "Cargo Door Open");
		return alerts;
	}

	@Override
	public Map<String, Object> getEvents() {
		Map<String, Object> events = new LinkedHashMap<String, Object>();
		Map<String, Object> obj = new LinkedHashMap<String, Object>();
		obj.put("display", "Engine Overheat");
		obj.put("value", eventEngineOverheat);
		events.put("eventEngineOverheat", obj);

		obj = new LinkedHashMap<String, Object>();
		obj.put("display", "Speed Increase");
		obj.put("value", eventSpeedIncrease);
		events.put("eventSpeedIncrease", obj);

		obj = new LinkedHashMap<String, Object>();
		obj.put("display", "Off Route");
		obj.put("value", eventOffRoute);
		events.put("eventOffRoute", obj);

		obj = new LinkedHashMap<String, Object>();
		obj.put("display", "Engine Stall");
		obj.put("value", eventEngineStall);
		events.put("eventEngineStall", obj);
		return events;
	}

	@Override
	public Map<String, Object> getMetrics() {
		Map<String, Object> map = new LinkedHashMap<String, Object>();
		map.put("Current Speed", currentSpeed);
		map.put("Latitude", latitude);
		map.put("Longitude", longitude);
		map.put("Fuel Level", fuelLevel);
		map.put("Engine Temp.", engineTemperature);
		return map;
	}

	@Override
	public AlertMessage createAlertMessage(String alertMessage) {
		String description = "Bad Alert";
		if (alertMessage.equalsIgnoreCase("alertDriverDoorOpen")) {
			description = "Driver Door Opened";
		} else if (alertMessage.equalsIgnoreCase("alertPassengerDoorOpen")) {
			description = "Passender Door Opened";
		} else if (alertMessage.equalsIgnoreCase("alertCargoDoorOpen")) {
			description = "Cargo Door Opened";
		}

		AlertMessage.Builder alertBuilder = new AlertMessage.Builder();
		alertBuilder.format(ALERT_FORMAT).source(getId()).description(description)
				.dataItem("currentSpeed", currentSpeed).dataItem("latitude", latitude).dataItem("longitude", longitude)
				.dataItem("fuelLevel", fuelLevel).dataItem("engineTemperature", engineTemperature)
				.eventTime(new DateTime().toDate()).severity(AlertMessage.Severity.CRITICAL);

		AlertMessage alert = alertBuilder.build();
		return alert;
	}

	@Override
	public Boolean eventHandler(String eventMessage) {
		// TODO Auto-generated method stub
		if (eventMessage.equalsIgnoreCase("eventEngineOverheat")) {
			eventEngineOverheat = !eventEngineOverheat;
			return true;
		}
		if (eventMessage.equalsIgnoreCase("eventSpeedIncrease")) {
			eventSpeedIncrease = !eventSpeedIncrease;
			return true;
		}
		if (eventMessage.equalsIgnoreCase("eventOffRoute")) {
			eventOffRoute = !eventOffRoute;
			if (!eventOffRoute) {
				longitude = startLong;
				latitude = startLat;
			}
			return true;
		}
		if (eventMessage.equalsIgnoreCase("eventEngineStall")) {
			eventEngineStall = !eventEngineStall;
			return true;
		}
		return false;
	}

	@Override
	public DataMessage createMessage() {
		animateMetrics();
		DateTime messageDate = new DateTime();
		DataMessage.Builder msgBuilder = new DataMessage.Builder();
		msgBuilder.format(DATA_FORMAT).source(getId()).dataItem("currentSpeed", currentSpeed)
				.dataItem("latitude", latitude).dataItem("longitude", longitude).dataItem("fuelLevel", fuelLevel)
				.dataItem("engineTemperature", engineTemperature).eventTime(messageDate.toDate())
				.reliability(Message.Reliability.BEST_EFFORT).priority(Message.Priority.MEDIUM);
		DataMessage message = msgBuilder.build();
		addToChart(messageDate, "Current Speed", currentSpeed);
		addToChart(messageDate, "Latitude", latitude);
		addToChart(messageDate, "Longitude", longitude);
		addToChart(messageDate, "Fuel Level", fuelLevel);
		addToChart(messageDate, "Engine Temp.", engineTemperature);

		return message;
	}

	private void animateMetrics() {
		if (eventEngineStall) {
			currentSpeed = 0;
			engineTemperature = 0;
		} else {

			if (eventOffRoute) {

			}
			if (eventSpeedIncrease) {
				if (currentSpeed < 130)
					currentSpeed = currentSpeed + 2;
			} else {
				currentSpeed = Constants.randomDouble(105, 112, 2);
			}
			if (eventEngineOverheat) {
				if (currentSpeed < 180)
					engineTemperature = engineTemperature + 5;
			} else {
				engineTemperature = Constants.randomDouble(90, 95, 2);
			}
			if (eventOffRoute) {
				longitude = 112.49;
				latitude = 49.42;
			} else {
				longitude = Constants.randomDouble(startLong, endLong, 2);
				if (up) {
					latitude = latitude + 0.1;
					if (latitude >= endLat)
						up = false;
				} else {
					latitude = latitude - 0.1;
					if (latitude <= startLat) {
						up = true;
					}
				}
			}
			fuelLevel = fuelLevel - 3;
			if (fuelLevel < 5) {
				fuelLevel = 100;
			}
		}
	}

	@Override
	public String getPicture() {
		return "truck.png";
	}

	@Override
	public String getThumbnail() {
		return "truck-thumb.png";
	}

	@Override
	public String getResource() {
		return "truck";
	}

	@Override
	public IOTDevice copy() {
		FleetTruck copy = new FleetTruck(getId(), getSecret());
		copy.currentSpeed = this.currentSpeed;
		copy.latitude = this.latitude;
		copy.longitude = this.longitude;
		copy.fuelLevel = this.fuelLevel;
		copy.engineTemperature = this.engineTemperature;
		copy.eventEngineOverheat = this.eventEngineOverheat;
		copy.eventSpeedIncrease = this.eventSpeedIncrease;
		copy.eventOffRoute = this.eventOffRoute;
		copy.eventEngineStall = this.eventEngineStall;
		copy.createDate = this.createDate;
		copy.chartSeries = Constants.copyStringList(this.chartSeries);
		copy.chartValues = Constants.copyListofLists(this.chartValues);
		copy.chartLabels = Constants.copyStringList(this.chartLabels);

		return copy;
	}

}
