package com.oracle.iot.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import com.oracle.iot.dao.DeviceDaoInMemory;
import com.oracle.iot.dao.DevicePropertiesLoaderDao;
import com.oracle.iot.model.IOTDevice;
import com.oracle.iot.model.PropertyDevice;
import com.oracle.iot.model.PropertyDeviceDetails;

@RunWith(MockitoJUnitRunner.class)
public class DeviceServiceTest {

	@Mock
	DeviceDaoInMemory deviceDao;

	@Mock
	DevicePropertiesLoaderDao loaderDao;

	@InjectMocks
	DeviceService deviceService;

	@Test
	public void create() throws Exception {
		// setup
		String id = "Test-123";
		PropertyDeviceDetails deviceDetails = Mockito.mock(PropertyDeviceDetails.class);
		when(loaderDao.getDevice("hvac")).thenReturn(deviceDetails);
		when(deviceDao.insert(any(PropertyDevice.class))).thenReturn(true);

		// execute
		boolean created = deviceService.create("hvac", id, "secret");

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
		IOTDevice device = Mockito.mock(IOTDevice.class);
		when(device.getId()).thenReturn(id);
		when(deviceDao.findAll()).thenReturn(Arrays.asList(device));

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
		IOTDevice device = Mockito.mock(IOTDevice.class);
		when(device.getId()).thenReturn(id);
		when(deviceDao.findById(id)).thenReturn(device);

		// execute
		IOTDevice actualDevice = deviceService.findById(id);

		// assert
		assertNotNull(actualDevice);
		assertEquals(actualDevice.getId(), id);
	}

	@Test
	public void updateAll() throws Exception {
		// setup
		String id = "Test-123";
		IOTDevice device = Mockito.mock(IOTDevice.class);
		when(device.getId()).thenReturn(id);
		List<IOTDevice> devices = (Arrays.asList(device));
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
		IOTDevice device = Mockito.mock(IOTDevice.class);
		when(device.getId()).thenReturn(id);
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
		IOTDevice device = Mockito.mock(IOTDevice.class);
		when(device.getId()).thenReturn(id);
		when(deviceDao.findAll()).thenReturn(Arrays.asList(device));

		// execute
		IOTDevice actualDevice = deviceService.getCurrentDevice();

		// assert
		assertNotNull(actualDevice);
		assertEquals(actualDevice.getId(), id);
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
