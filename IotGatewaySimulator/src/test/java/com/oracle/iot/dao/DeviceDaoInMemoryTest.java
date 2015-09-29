package com.oracle.iot.dao;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.oracle.iot.model.DeviceType;
import com.oracle.iot.model.IOTDevice;
import com.oracle.iot.model.devices.CableModem;

public class DeviceDaoInMemoryTest {

	DeviceDaoInMemory dao = new DeviceDaoInMemory();

	@Before
	public void setUp() {
		dao.deleteAll();
	}

	@After
	public void tearDown() {
		dao.deleteAll();
	}

	@Test
	public void insert() throws Exception {
		// setup
		String id = "Test-123";

		// execute
		dao.insert(DeviceType.CABLE_MODEM, id, "secret");

		// assert
		assertTrue(dao.exists(id));
	}

	@Test
	public void delete() throws Exception {
		// setup
		String id = "Test-123";

		// execute
		dao.insert(DeviceType.CABLE_MODEM, id, "secret");
		dao.delete(id);

		// assert
		assertFalse(dao.exists(id));
	}

	@Test
	public void getObjectById() throws Exception {
		// setup
		String id = "Test-123";

		// execute
		dao.insert(DeviceType.CABLE_MODEM, id, "secret");
		IOTDevice device = dao.findById(id);

		// assert
		assertEquals(device.getId(), id);
		assertEquals(device.getSecret(), "secret");
	}

	@Test
	public void getObjectByAll() throws Exception {
		// setup
		String id = "Test-123";

		// execute
		dao.insert(DeviceType.CABLE_MODEM, id, "secret");
		List<IOTDevice> devices = dao.findAll();

		// assert
		boolean found = false;
		for (IOTDevice item : devices) {
			if (item.getId().equals(id)) {
				found = true;
				break;
			}
		}
		assertTrue(found);
	}

	@Test
	public void updateAll() throws Exception {
		// setup
		String id = "Test-123";

		// execute
		dao.insert(DeviceType.CABLE_MODEM, id, "secret");
		CableModem device = (CableModem) dao.findById(id);
		device.animateMetrics();

		dao.updateAll(Arrays.asList((IOTDevice) device));

		// assert
		CableModem actual = (CableModem) dao.findById(id);
		assertEquals(device.getMetrics(), actual.getMetrics());
	}

	@Test
	public void update() throws Exception {
		// setup
		String id = "Test-123";

		// execute
		dao.insert(DeviceType.CABLE_MODEM, id, "secret");
		CableModem device = (CableModem) dao.findById(id);
		device.animateMetrics();

		dao.update((IOTDevice) device);

		// assert
		CableModem actual = (CableModem) dao.findById(id);
		assertEquals(device.getMetrics(), actual.getMetrics());
	}
}
