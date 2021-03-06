package com.oracle.iot.dao;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.InputStream;
import java.util.List;
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

import com.oracle.iot.model.EventMetric;
import com.oracle.iot.model.PropertyAlert;
import com.oracle.iot.model.PropertyDeviceDetails;
import com.oracle.iot.model.PropertyEvent;
import com.oracle.iot.model.PropertyMetric;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "file:src/main/webapp/WEB-INF/spring/appServlet/servlet-context.xml" })
@DirtiesContext(classMode = ClassMode.BEFORE_EACH_TEST_METHOD)
public class DevicePropertiesLoaderDaoTest {

	@Resource
	private DevicePropertiesLoaderDao dao;

	@Test
	public void loadHvacDevice_HvacEntirely() throws Exception {
		// execute
		List<String> names = dao.getDeviceNames();

		// assert
		assertTrue(names.size() >= 1);
		assertTrue(names.contains("hvac"));

		PropertyDeviceDetails device = dao.getDevice("hvac");
		assertEquals(device.getDisplayName(), "HVAC");
		assertNotNull(device.getPicture());
		assertEquals(device.getDataFormat(), "urn:com:oracle:iot:model:devicesimulator:hvac");
		assertEquals(device.getAlertFormat(), "urn:com:oracle:iot:model:devicesimulator:alert:hvac");

		List<PropertyMetric> metrics = device.getMetrics();
		assertEquals(metrics.size(), 4);
		assertEquals(metrics.get(0).getName(), "outputTemp");
		assertEquals(metrics.get(0).getDisplayName(), "Output Temp (C)");
		assertEquals(metrics.get(0).getDefaultValue().doubleValue(), 30.0, Double.NaN);

		assertEquals(metrics.get(1).getName(), "vibration");
		assertEquals(metrics.get(1).getDisplayName(), "Vibration (G)");
		assertEquals(metrics.get(1).getDefaultValue().doubleValue(), 1.0, Double.NaN);

		assertEquals(metrics.get(2).getName(), "oilViscosity");
		assertEquals(metrics.get(2).getDisplayName(), "Oil Viscosity (cP)");
		assertEquals(metrics.get(2).getDefaultValue().doubleValue(), 0.25, Double.NaN);

		assertEquals(metrics.get(3).getName(), "motorAmperage");
		assertEquals(metrics.get(3).getDisplayName(), "Motor Amperage (A)");
		assertEquals(metrics.get(3).getDefaultValue().doubleValue(), 50.0, Double.NaN);

		List<PropertyAlert> alerts = device.getAlerts();
		assertEquals(alerts.size(), 2);
		assertEquals(alerts.get(0).getName(), "alertDoorOpen");
		assertEquals(alerts.get(0).getDisplayName(), "Door Open");
		assertEquals(alerts.get(1).getName(), "alertDoorClosed");
		assertEquals(alerts.get(1).getDisplayName(), "Door Closed");

		List<PropertyEvent> events = device.getEvents();
		assertEquals(events.size(), 3);

		assertEquals(events.get(0).getName(), "eventMotorOverheat");
		assertEquals(events.get(0).getDisplayName(), "Motor Overheat");
		assertEquals((int) events.get(0).getPriority(), 3);
		List<EventMetric> eventMetrics = events.get(0).getEventMetrics();
		assertEquals(eventMetrics.size(), 1);
		assertEquals(eventMetrics.get(0).getMetricName(), "outputTemp");
		assertNull(eventMetrics.get(0).getEventValue());
		assertEquals(eventMetrics.get(0).getIncrement(), 5.0, Double.NaN);
		assertNull(eventMetrics.get(0).getLoop());
		assertEquals(eventMetrics.get(0).getMax(), 120.0, Double.NaN);
		assertNull(eventMetrics.get(0).getMin());
		assertNull(eventMetrics.get(0).getAlternate());

		assertEquals(events.get(1).getName(), "eventMotorFailure");
		assertEquals(events.get(1).getDisplayName(), "Motor Short Circuit");
		assertEquals((int) events.get(1).getPriority(), 2);
		eventMetrics = events.get(1).getEventMetrics();
		assertEquals(eventMetrics.size(), 1);
		assertEquals(eventMetrics.get(0).getMetricName(), "motorAmperage");
		assertEquals(eventMetrics.get(0).getEventValue(), 0, Double.NaN);
		assertNull(eventMetrics.get(0).getIncrement());
		assertNull(eventMetrics.get(0).getLoop());
		assertNull(eventMetrics.get(0).getMax());
		assertNull(eventMetrics.get(0).getMin());
		assertEquals(eventMetrics.get(0).getAlternate(), 75, Double.NaN);

		assertEquals(events.get(2).getName(), "eventHvacNotWorking");
		assertEquals(events.get(2).getDisplayName(), "HVAC Failure");
		assertEquals((int) events.get(2).getPriority(), 1);
		eventMetrics = events.get(2).getEventMetrics();
		assertEquals(eventMetrics.size(), 4);
		assertEquals(eventMetrics.get(0).getMetricName(), "outputTemp");
		assertEquals(eventMetrics.get(0).getEventValue(), 0, Double.NaN);
		assertNull(eventMetrics.get(0).getIncrement());
		assertNull(eventMetrics.get(0).getLoop());
		assertNull(eventMetrics.get(0).getMin());
		assertNull(eventMetrics.get(0).getMax());
		assertNull(eventMetrics.get(0).getAlternate());

		assertEquals(eventMetrics.get(1).getMetricName(), "vibration");
		assertEquals(eventMetrics.get(1).getEventValue(), 0, Double.NaN);
		assertNull(eventMetrics.get(1).getIncrement());
		assertNull(eventMetrics.get(1).getLoop());
		assertNull(eventMetrics.get(1).getMax());
		assertNull(eventMetrics.get(1).getMin());
		assertNull(eventMetrics.get(1).getAlternate());

		assertEquals(eventMetrics.get(2).getMetricName(), "oilViscosity");
		assertEquals(eventMetrics.get(2).getEventValue(), 0, Double.NaN);
		assertNull(eventMetrics.get(2).getIncrement());
		assertNull(eventMetrics.get(2).getLoop());
		assertNull(eventMetrics.get(2).getMax());
		assertNull(eventMetrics.get(2).getMin());
		assertNull(eventMetrics.get(2).getAlternate());

		assertEquals(eventMetrics.get(3).getMetricName(), "motorAmperage");
		assertEquals(eventMetrics.get(3).getEventValue(), 0, Double.NaN);
		assertNull(eventMetrics.get(3).getIncrement());
		assertNull(eventMetrics.get(3).getLoop());
		assertNull(eventMetrics.get(3).getMax());
		assertNull(eventMetrics.get(3).getMin());
		assertNull(eventMetrics.get(3).getAlternate());
	}

	@Test
	public void loadHvacDevice_NewBitsDrillSite() throws Exception {
		// setup
		InputStream inputStream = this.getClass().getClassLoader()
				.getResourceAsStream("deviceLoad/drill_site.properties");
		MultipartFile multipartFile = Mockito.mock(MultipartFile.class);
		Mockito.when(multipartFile.getInputStream()).thenReturn(inputStream);

		// execute
		dao.loadNewDevice(multipartFile, null);
		List<String> names = dao.getDeviceNames();

		// assert
		assertTrue(names.size() >= 1);
		assertTrue(names.contains("drillsite"));

		PropertyDeviceDetails device = dao.getDevice("drillsite");
		assertEquals(device.getDisplayName(), "Drill Site");
		assertNotNull(device.getPicture());
		assertEquals(device.getDataFormat(), "urn:com:oracle:iot:model:devicesimulator:drillsite");
		assertEquals(device.getAlertFormat(), "urn:com:oracle:iot:model:devicesimulator:alert:drillsite");

		List<PropertyMetric> metrics = device.getMetrics();
		assertEquals(metrics.size(), 4);
		assertEquals(metrics.get(2).getName(), "depth");
		assertEquals(metrics.get(2).getDisplayName(), "Depth (x100 ft)");
		assertEquals(metrics.get(2).getDefaultValue().doubleValue(), 0, Double.NaN);
		assertEquals(metrics.get(2).getLoop().doubleValue(), 0.04, Double.NaN);
		assertEquals(metrics.get(2).getMax().doubleValue(), 59, Double.NaN);

		List<PropertyEvent> events = device.getEvents();
		assertEquals(events.size(), 3);

		assertEquals(events.get(0).getName(), "eventDrillSlowDown");
		assertEquals(events.get(0).getDisplayName(), "Drill Slow Down");
		assertEquals((int) events.get(0).getPriority(), 3);
		List<EventMetric> eventMetrics = events.get(0).getEventMetrics();
		assertEquals(eventMetrics.size(), 4);
		assertEquals(eventMetrics.get(0).getMetricName(), "drillRpm");
		assertEquals(eventMetrics.get(0).getEventValue(), 110, Double.NaN);
		assertEquals(eventMetrics.get(1).getMetricName(), "temperature");
		assertEquals(eventMetrics.get(1).getEventValue(), 185, Double.NaN);
		assertEquals(eventMetrics.get(2).getMetricName(), "depth");
		assertEquals(eventMetrics.get(2).getEventValue(), 0, Double.NaN);
		assertEquals(eventMetrics.get(2).getLoop(), 0.01, Double.NaN);
		assertEquals(eventMetrics.get(2).getMax(), 59.0, Double.NaN);
		assertEquals(eventMetrics.get(3).getMetricName(), "vibration");
		assertEquals(eventMetrics.get(3).getEventValue(), 4, Double.NaN);

		assertEquals(events.get(2).getName(), "eventDrillFailure");
		assertEquals(events.get(2).getDisplayName(), "Drill Failure");
		assertEquals((int) events.get(2).getPriority(), 1);
		eventMetrics = events.get(2).getEventMetrics();
		assertEquals(eventMetrics.size(), 4);
		assertEquals(eventMetrics.get(0).getMetricName(), "drillRpm");
		assertEquals(eventMetrics.get(0).getEventValue(), 0, Double.NaN);
		assertEquals(eventMetrics.get(1).getMetricName(), "temperature");
		assertEquals(eventMetrics.get(1).getEventValue(), 25, Double.NaN);
		assertEquals(eventMetrics.get(2).getMetricName(), "depth");
		assertTrue(eventMetrics.get(2).getHold());
		assertEquals(eventMetrics.get(3).getMetricName(), "vibration");
		assertEquals(eventMetrics.get(3).getEventValue(), 0, Double.NaN);
	}

	@Test
	public void loadDevice() throws Exception {
		// setup
		InputStream inputStream = this.getClass().getClassLoader()
				.getResourceAsStream("deviceLoad/hvactest.properties");
		MultipartFile multipartFile = Mockito.mock(MultipartFile.class);
		Mockito.when(multipartFile.getInputStream()).thenReturn(inputStream);

		// execute
		dao.loadNewDevice(multipartFile, null);

		// assert
		assertNotNull(dao.getDevice("hvactest"));
	}

	@Test
	public void getDevicesInOrder() throws Exception {
		// execute
		List<String> devices = dao.getDeviceNames();

		// assert
		assertEquals("hvac", devices.get(0));
		assertEquals("smartThermostat", devices.get(1));
	}

	@Test
	public void getTypesInOrder() throws Exception {
		// execute
		List<Map<String, Object>> devices = dao.getTypes(false);

		// assert
		assertEquals("HVAC", (String) devices.get(0).get("display"));
		assertEquals("Smart Thermostat", (String) devices.get(1).get("display"));
	}

	@Test
	public void loadDeviceWithBooleanMetric() throws Exception {
		// setup
		InputStream inputStream = this.getClass().getClassLoader()
				.getResourceAsStream("deviceLoad/template.properties");
		MultipartFile multipartFile = Mockito.mock(MultipartFile.class);
		Mockito.when(multipartFile.getInputStream()).thenReturn(inputStream);

		// execute
		dao.loadNewDevice(multipartFile, null);
		PropertyDeviceDetails device = dao.getDevice("templatedevice");

		// assert
		assertNotNull(device);
		PropertyMetric metric = device.getMetrics().get(4);
		assertEquals("metric5", metric.getName());
		assertTrue(metric.getBoolSet());
	}

}
