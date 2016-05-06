package com.oracle.iot.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.InputStream;
import java.util.Map;

import javax.annotation.Resource;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.web.multipart.MultipartFile;

import com.oracle.iot.client.message.AlertMessage;
import com.oracle.iot.dao.DevicePropertiesLoaderDao;
import com.oracle.iot.util.Constants;

import oracle.iot.client.device.Alert;
import oracle.iot.client.device.VirtualDevice;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "file:src/main/webapp/WEB-INF/spring/appServlet/servlet-context.xml" })
@DirtiesContext(classMode = ClassMode.BEFORE_EACH_TEST_METHOD)
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
		device.animateMetrics();

		// assert
		Map<String, Object> metrics = device.getMetrics();
		assertMetric((Double) metrics.get("Output Temp (C)"), 30.0);
		assertMetric((Double) metrics.get("Vibration (G)"), 1.0);
		assertMetric((Double) metrics.get("Oil Viscosity (cP)"), 0.25);
		assertMetric((Double) metrics.get("Motor Amperage (A)"), 50.0);
	}

	@Test
	// @Ignore
	public void hvacPropertyDeviceTest_eventMotorOverheat_increment() throws Exception {
		// setup
		String id = "testId";
		String secret = "testPassword";
		PropertyDeviceDetails deviceDetails = dao.getDevice("hvac");

		PropertyDevice device = new PropertyDevice(deviceDetails, id, secret);

		// execute
		device.animateMetrics();
		Map<String, Object> metrics = device.getMetrics();
		Double originalTemp = (Double) metrics.get("Output Temp (C)");
		device.eventHandler("eventMotorOverheat");
		device.animateMetrics();
		Double originalTemp2 = (Double) metrics.get("Output Temp (C)");
		device.animateMetrics();
		Double originalTemp3 = (Double) metrics.get("Output Temp (C)");

		// assert
		metrics = device.getMetrics();
		assertEquals(((Double) metrics.get("Output Temp (C)")), Constants.scale(originalTemp + 10.0, 2), Double.NaN);
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
		device.animateMetrics();
		device.eventHandler("eventMotorOverheat");
		device.animateMetrics();
		device.animateMetrics();
		device.animateMetrics();
		device.animateMetrics();
		device.animateMetrics();
		device.animateMetrics();
		device.animateMetrics();
		device.animateMetrics();
		device.animateMetrics();
		device.animateMetrics();
		device.animateMetrics();
		device.animateMetrics();
		device.animateMetrics();
		device.animateMetrics();
		device.animateMetrics();
		device.animateMetrics();
		device.animateMetrics();
		device.animateMetrics();
		device.animateMetrics();

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
		device.animateMetrics();
		device.eventHandler("eventMotorOverheat");
		device.eventHandler("eventMotorFailure");
		device.animateMetrics();
		device.animateMetrics();
		device.animateMetrics();
		device.animateMetrics();
		device.animateMetrics();
		device.animateMetrics();
		device.animateMetrics();
		device.animateMetrics();
		device.animateMetrics();
		device.animateMetrics();
		device.animateMetrics();
		device.animateMetrics();
		device.animateMetrics();
		device.animateMetrics();
		device.animateMetrics();
		device.animateMetrics();
		device.animateMetrics();
		device.animateMetrics();
		device.animateMetrics();

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
		device.animateMetrics();

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
		device.animateMetrics();
		device.animateMetrics();

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
		device.animateMetrics();

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
		device.animateMetrics();
		device.eventHandler("eventMotorOverheat");
		device.eventHandler("eventMotorFailure");
		device.eventHandler("eventHvacNotWorking");
		device.animateMetrics();

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
		VirtualDevice vd = Mockito.mock(VirtualDevice.class);
		Alert alert = Mockito.mock(Alert.class);
		Mockito.when(vd.createAlert(Mockito.any(String.class))).thenReturn(alert);

		PropertyDevice device = new PropertyDevice(deviceDetails, id, secret);

		// execute
		device.alert(vd, "alertDoorOpen");

		// assert
		Mockito.verify(alert, Mockito.times(1)).raise();
	}

	@Test
	public void drillSitePropertyDeviceTest_normalLoop() throws Exception {
		// setup
		String id = "testId";
		String secret = "testPassword";
		InputStream inputStream = this.getClass().getClassLoader()
				.getResourceAsStream("deviceLoad/drill_site.properties");
		MultipartFile multipartFile = Mockito.mock(MultipartFile.class);
		Mockito.when(multipartFile.getInputStream()).thenReturn(inputStream);
		dao.loadNewDevice(multipartFile, null);

		// execute
		dao.loadNewDevice(multipartFile, null);

		PropertyDeviceDetails deviceDetails = dao.getDevice("drillsite");

		PropertyDevice device = new PropertyDevice(deviceDetails, id, secret);

		// execute
		device.animateMetrics();
		device.animateMetrics();
		device.animateMetrics();

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

		InputStream inputStream = this.getClass().getClassLoader()
				.getResourceAsStream("deviceLoad/drill_site.properties");
		MultipartFile multipartFile = Mockito.mock(MultipartFile.class);
		Mockito.when(multipartFile.getInputStream()).thenReturn(inputStream);
		dao.loadNewDevice(multipartFile, null);

		PropertyDeviceDetails deviceDetails = dao.getDevice("drillsite");

		// execute
		PropertyDevice device = new PropertyDevice(deviceDetails, id, secret);
		for (int i = 0; i < 1500; i++) {
			device.animateMetrics();
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
		InputStream inputStream = this.getClass().getClassLoader()
				.getResourceAsStream("deviceLoad/drill_site.properties");
		MultipartFile multipartFile = Mockito.mock(MultipartFile.class);
		Mockito.when(multipartFile.getInputStream()).thenReturn(inputStream);
		dao.loadNewDevice(multipartFile, null);
		PropertyDeviceDetails deviceDetails = dao.getDevice("drillsite");

		PropertyDevice device = new PropertyDevice(deviceDetails, id, secret);

		// execute
		device.animateMetrics();
		device.eventHandler("eventDrillSlowDown");
		device.animateMetrics();
		device.animateMetrics();

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
		InputStream inputStream = this.getClass().getClassLoader()
				.getResourceAsStream("deviceLoad/drill_site.properties");
		MultipartFile multipartFile = Mockito.mock(MultipartFile.class);
		Mockito.when(multipartFile.getInputStream()).thenReturn(inputStream);
		dao.loadNewDevice(multipartFile, null);
		PropertyDeviceDetails deviceDetails = dao.getDevice("drillsite");

		PropertyDevice device = new PropertyDevice(deviceDetails, id, secret);

		// execute
		device.animateMetrics();
		device.eventHandler("eventDrillSlowDown");
		for (int i = 0; i < 5902; i++) {
			device.animateMetrics();
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
		InputStream inputStream = this.getClass().getClassLoader()
				.getResourceAsStream("deviceLoad/drill_site.properties");
		MultipartFile multipartFile = Mockito.mock(MultipartFile.class);
		Mockito.when(multipartFile.getInputStream()).thenReturn(inputStream);
		dao.loadNewDevice(multipartFile, null);
		PropertyDeviceDetails deviceDetails = dao.getDevice("drillsite");

		PropertyDevice device = new PropertyDevice(deviceDetails, id, secret);

		// execute
		device.animateMetrics();
		device.animateMetrics();
		device.eventHandler("eventDrillFailure");
		device.animateMetrics();
		device.animateMetrics();
		device.animateMetrics();

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
