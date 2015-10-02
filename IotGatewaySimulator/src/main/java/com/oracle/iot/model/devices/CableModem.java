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

public class CableModem extends IOTDevice {

	private static final String DATA_FORMAT = "com:oracle:iot:model:devices:modem";
	private static final String ALERT_FORMAT = "com:oracle:iot:model:devices:alert:modem";

	@JsonIgnore
	protected double downloadRate = 60.0;
	@JsonIgnore
	protected double uploadRate = 3.0;
	@JsonIgnore
	protected double powerOutput = 12;
	@JsonIgnore
	protected double ping = 10;

	@JsonIgnore
	protected boolean eventThrottledDownload = false;
	@JsonIgnore
	protected boolean eventThrottledUpload = false;
	@JsonIgnore
	protected boolean eventDeviceFailure = false;

	public CableModem(String id, String secret) {
		super(id, secret);
	}

	@Override
	public Map<String, String> getAlerts() {
		Map<String, String> alerts = new LinkedHashMap<String, String>();
		alerts.put("alertWifiFailure", "Wifi Failure");
		alerts.put("alertModemReset", "Modem Reset");
		alerts.put("alertUnableToConnect", "Unable to Connect");
		return alerts;
	}

	@Override
	public Map<String, Object> getEvents() {
		Map<String, Object> events = new LinkedHashMap<String, Object>();
		Map<String, Object> obj = new LinkedHashMap<String, Object>();
		obj.put("display", "Throttle Download");
		obj.put("value", eventThrottledDownload);
		events.put("eventThrottledDownload", obj);

		obj = new LinkedHashMap<String, Object>();
		obj.put("display", "Throttle Upload");
		obj.put("value", eventThrottledUpload);
		events.put("eventThrottledUpload", obj);

		obj = new LinkedHashMap<String, Object>();
		obj.put("display", "Device Failure");
		obj.put("value", eventDeviceFailure);
		events.put("eventDeviceFailure", obj);
		return events;
	}

	@Override
	public Map<String, Object> getMetrics() {
		Map<String, Object> map = new LinkedHashMap<String, Object>();
		map.put("Download Rate (Mbps)", downloadRate);
		map.put("Upload Rate (Mbps)", uploadRate);
		map.put("Ping (ms)", ping);
		map.put("Power Output (V-DC)", powerOutput);
		return map;
	}

	@Override
	public AlertMessage createAlertMessage(String alertMessage) {
		String description = "Bad Alert";
		if (alertMessage.equalsIgnoreCase("alertWifiFailure")) {
			description = "Wifi failure occured";
		} else if (alertMessage.equalsIgnoreCase("alertModemReset")) {
			description = "Modem was reset";
		} else if (alertMessage.equalsIgnoreCase("eventDeviceFailure")) {
			description = "Device failure occured";
		}

		AlertMessage.Builder alertBuilder = new AlertMessage.Builder();
		alertBuilder.format(ALERT_FORMAT).source(getId()).description(description)
				.dataItem("downloadRate", downloadRate).dataItem("uploadRate", uploadRate)
				.dataItem("powerOutput", powerOutput).eventTime(new DateTime().toDate())
				.severity(AlertMessage.Severity.CRITICAL);

		AlertMessage alert = alertBuilder.build();
		return alert;
	}

	@Override
	public Boolean eventHandler(String eventMessage) {
		// TODO Auto-generated method stub
		if (eventMessage.equalsIgnoreCase("eventThrottledDownload")) {
			eventThrottledDownload = !eventThrottledDownload;
			if (!eventThrottledDownload)
				downloadRate = 60.0;
			return true;
		}
		if (eventMessage.equalsIgnoreCase("eventThrottledUpload")) {
			eventThrottledUpload = !eventThrottledUpload;
			if (!eventThrottledUpload) {
				uploadRate = 3.0;
			}
			return true;
		}
		if (eventMessage.equalsIgnoreCase("eventDeviceFailure")) {
			eventDeviceFailure = !eventDeviceFailure;
			if (!eventDeviceFailure) {
				downloadRate = 60.0;
				uploadRate = 3.0;
				powerOutput = 12.0;
				ping = 10.0;
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
		msgBuilder.format(DATA_FORMAT).source(getId()).dataItem("downloadRate", downloadRate)
				.dataItem("uploadRate", uploadRate).dataItem("powerOutput", powerOutput).eventTime(messageDate.toDate())
				.reliability(Message.Reliability.BEST_EFFORT).priority(Message.Priority.MEDIUM);
		DataMessage message = msgBuilder.build();
		addToChart(messageDate, "Download Rate", downloadRate);
		addToChart(messageDate, "Upload Rate", uploadRate);
		addToChart(messageDate, "Power Output", powerOutput);

		return message;
	}

	public void animateMetrics() {
		if (eventDeviceFailure) {
			downloadRate = 0;
			uploadRate = 0;
			powerOutput = 0;
			ping = 0;
		} else {
			downloadRate = Constants.randomDouble(55, 65, 2);
			uploadRate = Constants.randomDouble(2.5, 3.5, 2);
			powerOutput = Constants.randomDouble(11.9, 12.1, 2);
			ping = Constants.randomDouble(8, 14, 2);

			if (eventThrottledDownload) {
				downloadRate = Constants.randomDouble(15, 25, 2);
			}
			if (eventThrottledUpload) {
				uploadRate = Constants.randomDouble(0.1, 0.5, 2);
			}
		}
	}

	@Override
	public String getPicture() {
		return "modem.png";
	}

	@Override
	public String getThumbnail() {
		return "modem-thumb.png";
	}

	@Override
	public String getResource() {
		return "modem";
	}

	@Override
	public IOTDevice copy() {
		CableModem copy = new CableModem(getId(), getSecret());
		copy.downloadRate = this.downloadRate;
		copy.uploadRate = this.uploadRate;
		copy.powerOutput = this.powerOutput;
		copy.ping = this.ping;
		copy.eventDeviceFailure = this.eventDeviceFailure;
		copy.eventThrottledDownload = this.eventThrottledDownload;
		copy.eventThrottledUpload = this.eventThrottledUpload;
		copy.createDate = this.createDate;
		copy.chartSeries = Constants.copyStringList(this.chartSeries);
		copy.chartValues = Constants.copyListofLists(this.chartValues);
		copy.chartLabels = Constants.copyStringList(this.chartLabels);

		return copy;
	}

}
