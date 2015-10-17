package com.oracle.iot.dao;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Repository;

import com.oracle.iot.model.Constants;
import com.oracle.iot.model.PropertyDeviceDetails;
import com.oracle.iot.model.PropertyMetric;

@Repository
public class DevicePropertiesLoaderDao {

	private static final Logger logger = Logger.getLogger(DevicePropertiesLoaderDao.class);

	Map<String, PropertyDeviceDetails> devices = new LinkedHashMap<String, PropertyDeviceDetails>();

	public DevicePropertiesLoaderDao() {
		super();
		loadDevices();
	}

	private void loadDevices() {
		ClassLoader classLoader = getClass().getClassLoader();
		File directory = new File(classLoader.getResource("devices").getFile());
		logger.info("Loading " + directory.listFiles().length + " files from: " + directory.getAbsolutePath());
		for (File device : directory.listFiles()) {
			Properties prop = new Properties();
			try {
				prop.load(new FileInputStream(device));
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
			// load device specific details
			String name = prop.getProperty("name");
			PropertyDeviceDetails newDevice = new PropertyDeviceDetails(name, prop.getProperty("display.name"),
					prop.getProperty("data.format"), prop.getProperty("alert.format"), prop.getProperty("picture"));

			// load metrics
			List<String> metrics = Arrays.asList(prop.getProperty("metrics").split(","));
			for (String metric : metrics) {
				String displayName = prop.getProperty("metrics." + metric + ".display");
				Double defaultValue = Double.valueOf(prop.getProperty("metrics." + metric + ".default"));
				Double increment = Constants.doubleOrNull(prop.getProperty("metrics." + metric + ".increment"));
				Double alternate = Constants.doubleOrNull(prop.getProperty("metrics." + metric + ".alternate"));
				Double loop = Constants.doubleOrNull(prop.getProperty("metrics." + metric + ".loop"));
				Double max = Constants.doubleOrNull(prop.getProperty("metrics." + metric + ".max"));
				Double min = Constants.doubleOrNull(prop.getProperty("metrics." + metric + ".min"));
				newDevice.addMetric(metric, displayName, defaultValue, increment, alternate, loop, max, min);
			}

			// load alerts
			List<String> alerts = Arrays.asList(prop.getProperty("alerts").split(","));
			for (String alert : alerts) {
				String displayName = prop.getProperty("alerts." + alert + ".display");
				newDevice.addAlert(alert, displayName);
			}

			// load events
			List<String> events = Arrays.asList(prop.getProperty("events").split(","));
			for (String event : events) {
				String displayName = prop.getProperty("events." + event + ".display");
				Integer priority = Integer.valueOf(prop.getProperty("events." + event + ".priority", "1"));
				for (PropertyMetric eventMetric : newDevice.getMetrics()) {
					String metricName = eventMetric.getName();
					String eventMetricBase = "events." + event + "." + metricName;
					Double value = Constants.doubleOrNull(prop.getProperty(eventMetricBase + ".value"));
					Double increment = Constants.doubleOrNull(prop.getProperty(eventMetricBase + ".increment"));
					Double loop = Constants.doubleOrNull(prop.getProperty(eventMetricBase + ".loop"));
					if (value != null || increment != null || loop != null) {
						Double alternate = Constants.doubleOrNull(prop.getProperty(eventMetricBase + ".alternate"));
						Double max = Constants.doubleOrNull(prop.getProperty(eventMetricBase + ".max"));
						Double min = Constants.doubleOrNull(prop.getProperty(eventMetricBase + ".min"));
						newDevice.addEvent(event, displayName, priority, metricName, value, increment, alternate, loop,
								max, min);
					}
				}
			}

			devices.put(name, newDevice);
		}

	}

	public List<String> getDeviceNames() {
		return new ArrayList<String>(devices.keySet());
	}

	public PropertyDeviceDetails getDevice(String name) {
		return devices.get(name);
	}

	public List<Map<String, String>> getTypes() {
		List<Map<String, String>> list = new ArrayList<Map<String, String>>();
		for (String name : devices.keySet()) {
			Map<String, String> map = new LinkedHashMap<String, String>();
			map.put("name", name);
			map.put("display", devices.get(name).getDisplayName());
			list.add(map);
		}
		logger.info("Returning Types count: " + list.size());
		return list;
	}

}
