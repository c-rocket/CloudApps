package com.oracle.iot.model;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.joda.time.DateTime;

import com.fasterxml.jackson.annotation.JsonProperty;

import oracle.iot.client.device.VirtualDevice;

public abstract class IOTDevice {

	private static final int MAX_CHART_SIZE = 10;
	@JsonProperty("authToken")
	private IOTAuthToken authToken;
	@JsonProperty("createDate")
	protected Date createDate;
	@JsonProperty("chartSeries")
	protected List<String> chartSeries = new ArrayList<String>();
	@JsonProperty("chartValues")
	protected List<List<Double>> chartValues = new ArrayList<List<Double>>();
	@JsonProperty("chartLabels")
	protected List<String> chartLabels = new ArrayList<String>();
	@JsonProperty("display")
	private String display;

	public IOTDevice(String id, String secret, String display) {
		this.authToken = new IOTAuthToken(id, secret);
		this.createDate = new DateTime().toDate();
		for (int i = 0; i <= MAX_CHART_SIZE; i++) {
			chartLabels.add(" ");
		}
		this.display = display;
	}

	public String getId() {
		return this.authToken.getId();
	}

	public DateTime getCreateDate() {
		return new DateTime(createDate.getTime());
	}

	protected void addToChart(DateTime date, String series, Double value) {
		int index = chartSeries.indexOf(series);
		List<Double> seriesValues = null;
		if (index == -1) {
			chartSeries.add(series);
			seriesValues = new ArrayList<Double>();
			for (int i = 0; i <= MAX_CHART_SIZE; i++) {
				seriesValues.add(value);
			}
			chartValues.add(seriesValues);
		} else {
			seriesValues = chartValues.get(index);
		}
		seriesValues.add(value);
		// remove the oldest record
		seriesValues.remove(0);
	}

	public abstract Map<String, String> getAlerts();

	public abstract Map<String, Object> getEvents();

	public abstract Map<String, Object> getMetrics();

	public abstract Boolean eventHandler(String event);

	public abstract String getPicture();

	public String getSecret() {
		return this.authToken.getSecret();
	}

	public abstract String getResource();

	public void clearChart() {
		chartSeries = new ArrayList<String>();
		chartValues = new ArrayList<List<Double>>();
		chartLabels = new ArrayList<String>();
	}

	public abstract IOTDevice copy();

	public abstract String getModelURN();

	public abstract void animateMetrics();

	public abstract void update(VirtualDevice virtualDevice);

	public abstract void alert(VirtualDevice virtualDevice, String alert);

	public abstract void addCallbacks(VirtualDevice virtualDevice);

	public List<String> getChartSeries() {
		return this.chartSeries;
	}

	public List<List<Double>> getChartValues() {
		return chartValues;
	}

	public List<String> getChartLabels() {
		return chartLabels;
	}

	public String getDisplay() {
		return display;
	}
	
	

}
