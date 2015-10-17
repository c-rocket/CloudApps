package com.oracle.iot.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.Map;

import javax.annotation.Resource;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.oracle.iot.dao.DevicePropertiesLoaderDao;

import oracle.iot.message.AlertMessage;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "file:src/main/webapp/WEB-INF/spring/appServlet/servlet-context.xml" })
public class PropertyDeviceTest {
	@Resource
	private DevicePropertiesLoaderDao dao;

	@Test
	public void hvacPropertyDeviceTest_normal() throws Exception {
		// setup
		String id = "testId";
		String secret = "testPassword";
		PropertyDeviceDetails deviceDetails = dao.getDevice("hvac");

		PropertyDevice device = new PropertyDevice(deviceDetails, id, secret);

		// execute
		device.createMessage();

		// assert
		Map<String, Object> metrics = device.getMetrics();
		assertMetric((Double) metrics.get("Output Temp (C)"), 30.0);
		assertMetric((Double) metrics.get("Vibration (G)"), 1.0);
		assertMetric((Double) metrics.get("Oil Viscosity (cP)"), 0.25);
		assertMetric((Double) metrics.get("Motor Amperage (A)"), 50.0);
	}

	@Test
	public void hvacPropertyDeviceTest_eventMotorOverheat_increment() throws Exception {
		// setup
		String id = "testId";
		String secret = "testPassword";
		PropertyDeviceDetails deviceDetails = dao.getDevice("hvac");

		PropertyDevice device = new PropertyDevice(deviceDetails, id, secret);

		// execute
		device.createMessage();
		Map<String, Object> metrics = device.getMetrics();
		Double originalTemp = (Double) metrics.get("Output Temp (C)");
		device.eventHandler("eventMotorOverheat");
		device.createMessage();
		device.createMessage();

		// assert
		metrics = device.getMetrics();
		assertTrue(((Double) metrics.get("Output Temp (C)")).compareTo(originalTemp + 10.0) == 0);
		assertMetric((Double) metrics.get("Vibration (G)"), 1.0);
		assertMetric((Double) metrics.get("Oil Viscosity (cP)"), 0.25);
		assertMetric((Double) metrics.get("Motor Amperage (A)"), 50.0);
	}

	@Test
	public void hvacPropertyDeviceTest_eventMotorOverheat_max() throws Exception {
		// setup
		String id = "testId";
		String secret = "testPassword";
		PropertyDeviceDetails deviceDetails = dao.getDevice("hvac");

		PropertyDevice device = new PropertyDevice(deviceDetails, id, secret);

		// execute
		device.createMessage();
		device.eventHandler("eventMotorOverheat");
		device.createMessage();
		device.createMessage();
		device.createMessage();
		device.createMessage();
		device.createMessage();
		device.createMessage();
		device.createMessage();
		device.createMessage();
		device.createMessage();
		device.createMessage();
		device.createMessage();
		device.createMessage();
		device.createMessage();
		device.createMessage();
		device.createMessage();
		device.createMessage();
		device.createMessage();
		device.createMessage();
		device.createMessage();

		// assert
		Map<String, Object> metrics = device.getMetrics();
		assertEquals((Double) metrics.get("Output Temp (C)"), 120, Double.NaN);
		assertMetric((Double) metrics.get("Vibration (G)"), 1.0);
		assertMetric((Double) metrics.get("Oil Viscosity (cP)"), 0.25);
		assertMetric((Double) metrics.get("Motor Amperage (A)"), 50.0);
	}

	@Test
	public void hvacPropertyDeviceTest_eventMotorOverheat_combined() throws Exception {
		// setup
		String id = "testId";
		String secret = "testPassword";
		PropertyDeviceDetails deviceDetails = dao.getDevice("hvac");

		PropertyDevice device = new PropertyDevice(deviceDetails, id, secret);

		// execute
		device.createMessage();
		device.eventHandler("eventMotorOverheat");
		device.eventHandler("eventMotorFailure");
		device.createMessage();
		device.createMessage();
		device.createMessage();
		device.createMessage();
		device.createMessage();
		device.createMessage();
		device.createMessage();
		device.createMessage();
		device.createMessage();
		device.createMessage();
		device.createMessage();
		device.createMessage();
		device.createMessage();
		device.createMessage();
		device.createMessage();
		device.createMessage();
		device.createMessage();
		device.createMessage();
		device.createMessage();

		// assert
		Map<String, Object> metrics = device.getMetrics();
		assertEquals((Double) metrics.get("Output Temp (C)"), 120, Double.NaN);
		assertMetric((Double) metrics.get("Vibration (G)"), 1.0);
		assertMetric((Double) metrics.get("Oil Viscosity (cP)"), 0.25);
		assertEquals((Double) metrics.get("Motor Amperage (A)"), 75, Double.NaN);
	}

	@Test
	public void hvacPropertyDeviceTest_eventMotorOverheat_shortCiruit_up() throws Exception {
		// setup
		String id = "testId";
		String secret = "testPassword";
		PropertyDeviceDetails deviceDetails = dao.getDevice("hvac");

		PropertyDevice device = new PropertyDevice(deviceDetails, id, secret);

		// execute
		device.eventHandler("eventMotorFailure");
		device.createMessage();

		// assert
		Map<String, Object> metrics = device.getMetrics();
		assertMetric((Double) metrics.get("Output Temp (C)"), 30.0);
		assertMetric((Double) metrics.get("Vibration (G)"), 1.0);
		assertMetric((Double) metrics.get("Oil Viscosity (cP)"), 0.25);
		assertEquals((Double) metrics.get("Motor Amperage (A)"), 75.0, Double.NaN);
	}

	@Test
	public void hvacPropertyDeviceTest_eventMotorOverheat_shortCiruit_down() throws Exception {
		// setup
		String id = "testId";
		String secret = "testPassword";
		PropertyDeviceDetails deviceDetails = dao.getDevice("hvac");

		PropertyDevice device = new PropertyDevice(deviceDetails, id, secret);

		// execute
		device.eventHandler("eventMotorFailure");
		device.createMessage();
		device.createMessage();

		// assert
		Map<String, Object> metrics = device.getMetrics();
		assertMetric((Double) metrics.get("Output Temp (C)"), 30.0);
		assertMetric((Double) metrics.get("Vibration (G)"), 1.0);
		assertMetric((Double) metrics.get("Oil Viscosity (cP)"), 0.25);
		assertEquals((Double) metrics.get("Motor Amperage (A)"), 0.0, Double.NaN);
	}

	@Test
	public void hvacPropertyDeviceTest_eventFailure() throws Exception {
		// setup
		String id = "testId";
		String secret = "testPassword";
		PropertyDeviceDetails deviceDetails = dao.getDevice("hvac");

		PropertyDevice device = new PropertyDevice(deviceDetails, id, secret);

		// execute
		device.eventHandler("eventHvacNotWorking");
		device.createMessage();

		// assert
		Map<String, Object> metrics = device.getMetrics();
		assertEquals((Double) metrics.get("Output Temp (C)"), 0, Double.NaN);
		assertEquals((Double) metrics.get("Vibration (G)"), 0, Double.NaN);
		assertEquals((Double) metrics.get("Oil Viscosity (cP)"), 0, Double.NaN);
		assertEquals((Double) metrics.get("Motor Amperage (A)"), 0.0, Double.NaN);
	}

	@Test
	public void hvacPropertyDeviceTest_eventFailure_priority() throws Exception {
		// setup
		String id = "testId";
		String secret = "testPassword";
		PropertyDeviceDetails deviceDetails = dao.getDevice("hvac");

		PropertyDevice device = new PropertyDevice(deviceDetails, id, secret);

		// execute
		device.createMessage();
		device.eventHandler("eventMotorOverheat");
		device.eventHandler("eventMotorFailure");
		device.eventHandler("eventHvacNotWorking");
		device.createMessage();

		// assert
		Map<String, Object> metrics = device.getMetrics();
		assertEquals((Double) metrics.get("Output Temp (C)"), 0, Double.NaN);
		assertEquals((Double) metrics.get("Vibration (G)"), 0, Double.NaN);
		assertEquals((Double) metrics.get("Oil Viscosity (cP)"), 0, Double.NaN);
		assertEquals((Double) metrics.get("Motor Amperage (A)"), 0.0, Double.NaN);
	}

	@Test
	public void hvacPropertyDeviceTest_alert() throws Exception {
		// setup
		String id = "testId";
		String secret = "testPassword";
		PropertyDeviceDetails deviceDetails = dao.getDevice("hvac");

		PropertyDevice device = new PropertyDevice(deviceDetails, id, secret);

		// execute
		AlertMessage message = device.createAlertMessage("alertDoorOpen");

		// assert
		assertNotNull(message);
	}

	@Test
	public void drillSitePropertyDeviceTest_normalLoop() throws Exception {
		// setup
		String id = "testId";
		String secret = "testPassword";
		PropertyDeviceDetails deviceDetails = dao.getDevice("drillsite");

		PropertyDevice device = new PropertyDevice(deviceDetails, id, secret);

		// execute
		device.createMessage();
		device.createMessage();
		device.createMessage();

		// assert
		Map<String, Object> metrics = device.getMetrics();
		assertMetric((Double) metrics.get("Drill Speed (rpm)"), 200.0);
		assertMetric((Double) metrics.get("Temperature (C)"), 145.0);
		assertEquals((Double) metrics.get("Depth (x100 ft)"), 0.12, Double.NaN);
		assertMetric((Double) metrics.get("Vibration (G)"), 1.0);

	}

	@Test
	public void drillSitePropertyDeviceTest_normalLoopWrap() throws Exception {
		// setup
		String id = "testId";
		String secret = "testPassword";
		PropertyDeviceDetails deviceDetails = dao.getDevice("drillsite");

		PropertyDevice device = new PropertyDevice(deviceDetails, id, secret);

		// execute
		for (int i = 0; i < 1500; i++) {
			device.createMessage();
		}

		// assert
		Map<String, Object> metrics = device.getMetrics();
		assertMetric((Double) metrics.get("Drill Speed (rpm)"), 200.0);
		assertMetric((Double) metrics.get("Temperature (C)"), 145.0);
		assertEquals((Double) metrics.get("Depth (x100 ft)"), 0.96, Double.NaN);
		assertMetric((Double) metrics.get("Vibration (G)"), 1.0);

	}

	@Test
	public void drillSitePropertyDeviceTest_eventLoopValue() throws Exception {
		// setup
		String id = "testId";
		String secret = "testPassword";
		PropertyDeviceDetails deviceDetails = dao.getDevice("drillsite");

		PropertyDevice device = new PropertyDevice(deviceDetails, id, secret);

		// execute
		device.createMessage();
		device.eventHandler("eventDrillSlowDown");
		device.createMessage();
		device.createMessage();

		// assert
		Map<String, Object> metrics = device.getMetrics();
		assertMetric((Double) metrics.get("Drill Speed (rpm)"), 110.0);
		assertMetric((Double) metrics.get("Temperature (C)"), 185.0);
		assertEquals((Double) metrics.get("Depth (x100 ft)"), 0.06, Double.NaN);
		assertMetric((Double) metrics.get("Vibration (G)"), 4.0);

	}

	@Test
	public void drillSitePropertyDeviceTest_eventLoopWrapValue() throws Exception {
		// setup
		String id = "testId";
		String secret = "testPassword";
		PropertyDeviceDetails deviceDetails = dao.getDevice("drillsite");

		PropertyDevice device = new PropertyDevice(deviceDetails, id, secret);

		// execute
		device.createMessage();
		device.eventHandler("eventDrillSlowDown");
		for (int i = 0; i < 5902; i++) {
			device.createMessage();
		}

		// assert
		Map<String, Object> metrics = device.getMetrics();
		assertMetric((Double) metrics.get("Drill Speed (rpm)"), 110.0);
		assertMetric((Double) metrics.get("Temperature (C)"), 185.0);
		assertEquals((Double) metrics.get("Depth (x100 ft)"), 0.05, Double.NaN);
		assertMetric((Double) metrics.get("Vibration (G)"), 4.0);

	}

	@Test
	public void drillSitePropertyDeviceTest_eventHold() throws Exception {
		// setup
		String id = "testId";
		String secret = "testPassword";
		PropertyDeviceDetails deviceDetails = dao.getDevice("drillsite");

		PropertyDevice device = new PropertyDevice(deviceDetails, id, secret);

		// execute
		device.createMessage();
		device.createMessage();
		device.eventHandler("eventDrillFailure");
		device.createMessage();
		device.createMessage();
		device.createMessage();

		// assert
		Map<String, Object> metrics = device.getMetrics();
		assertMetric((Double) metrics.get("Drill Speed (rpm)"), 0.0);
		assertMetric((Double) metrics.get("Temperature (C)"), 25.0);
		assertEquals((Double) metrics.get("Depth (x100 ft)"), 0.08, Double.NaN);
		assertMetric((Double) metrics.get("Vibration (G)"), 0.0);

	}

	private void assertMetric(Double metric, Double defaultValue) {
		Double min = defaultValue * 0.9;
		Double max = defaultValue * 1.1;

		assertTrue(metric >= min);
		assertTrue(metric <= max);
	}
}
