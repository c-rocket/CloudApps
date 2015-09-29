package com.oracle.iot.dao;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.junit.Test;

import com.oracle.iot.model.DeviceType;
import com.oracle.iot.model.IOTDevice;

public class DeviceDaoInMemoryTest {

	DeviceDaoInMemory dao = new DeviceDaoInMemory();

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
}
