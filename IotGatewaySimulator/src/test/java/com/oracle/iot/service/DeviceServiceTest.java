package com.oracle.iot.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.oracle.iot.dao.DeviceDaoInMemory;
import com.oracle.iot.model.DeviceType;
import com.oracle.iot.model.IOTDevice;

@RunWith(MockitoJUnitRunner.class)
public class DeviceServiceTest {

	@Mock
	DeviceDaoInMemory deviceDao;

	@InjectMocks
	DeviceService deviceService;

	@Test
	public void create() throws Exception {
		// setup
		String id = "Test-123";
		when(deviceDao.insert(DeviceType.CABLE_MODEM, id, "secret")).thenReturn(true);

		// execute
		boolean created = deviceService.create(DeviceType.CABLE_MODEM.name(), id, "secret");

		// assert
		assertTrue(created);
	}

	@Test
	public void delete() throws Exception {
		// setup
		String id = "Test-123";
		when(deviceDao.delete(id)).thenReturn(true);

		// execute
		boolean created = deviceService.delete(id);

		// assert
		assertTrue(created);
	}

	@Test
	public void findAll() throws Exception {
		// setup
		String id = "Test-123";
		when(deviceDao.findAll()).thenReturn(Arrays.asList(DeviceType.CABLE_MODEM.getDevice(id, "secret")));

		// execute
		List<IOTDevice> devices = deviceService.findAll();

		// assert
		assertNotNull(devices);
		assertEquals(devices.size(), 1);
		assertEquals(devices.get(0).getId(), id);
	}

	@Test
	public void findById() throws Exception {
		// setup
		String id = "Test-123";
		when(deviceDao.findById(id)).thenReturn(DeviceType.CABLE_MODEM.getDevice(id, "secret"));

		// execute
		IOTDevice device = deviceService.findById(id);

		// assert
		assertNotNull(device);
		assertEquals(device.getId(), id);
	}

	@Test
	public void updateAll() throws Exception {
		// setup
		String id = "Test-123";
		List<IOTDevice> devices = (Arrays.asList(DeviceType.CABLE_MODEM.getDevice(id, "secret")));
		when(deviceDao.updateAll(devices)).thenReturn(true);

		// execute
		boolean updated = deviceService.updateAll(devices);

		// assert
		assertTrue(updated);
	}

	@Test
	public void updateDevice() throws Exception {
		// setup
		String id = "Test-123";
		IOTDevice device = DeviceType.CABLE_MODEM.getDevice(id, "secret");
		when(deviceDao.update(device)).thenReturn(true);

		// execute
		boolean updated = deviceService.updateDevice(device);

		// assert
		assertTrue(updated);
	}

	@Test
	public void getCurrentDeviceExistingDevice() throws Exception {
		// setup
		String id = "Test-123";
		when(deviceDao.findAll()).thenReturn(Arrays.asList(DeviceType.CABLE_MODEM.getDevice(id, "secret")));

		// execute
		IOTDevice device = deviceService.getCurrentDevice();

		// assert
		assertNotNull(device);
		assertEquals(device.getId(), id);
	}

	@Test
	public void getCurrentDeviceNoExistingDevice() throws Exception {
		// setup
		when(deviceDao.findAll()).thenReturn(new ArrayList<IOTDevice>());

		// execute
		IOTDevice device = deviceService.getCurrentDevice();

		// assert
		assertNull(device);
	}
}
