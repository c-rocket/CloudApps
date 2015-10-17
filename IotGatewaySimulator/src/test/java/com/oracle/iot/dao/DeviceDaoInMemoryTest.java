package com.oracle.iot.dao;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import com.oracle.iot.model.IOTDevice;
import com.oracle.iot.model.PropertyDevice;
import com.oracle.iot.model.PropertyDeviceDetails;

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
	public void insertNoExisting() throws Exception {
		// setup
		String id = "Test-123";

		// execute
		PropertyDeviceDetails deviceDetails = Mockito.mock(PropertyDeviceDetails.class);
		boolean inserted = dao.insert(new PropertyDevice(deviceDetails, id, "secret"));

		// assert
		assertTrue(inserted);
		assertTrue(dao.exists(id));
	}

	@Test(expected = RuntimeException.class)
	public void insertExistingDifferentId() throws Exception {
		// setup
		String id = "Test-123";
		String id2 = "Test-1234";

		// execute
		PropertyDeviceDetails deviceDetails = Mockito.mock(PropertyDeviceDetails.class);
		dao.insert(new PropertyDevice(deviceDetails, id, "secret"));
		dao.insert(new PropertyDevice(deviceDetails, id2, "secret"));

		// assert - expected Exception
	}

	@Test
	public void insertExistingSameId() throws Exception {
		// setup
		String id = "Test-123";
		String id2 = "Test-123";

		// execute
		PropertyDeviceDetails deviceDetails = Mockito.mock(PropertyDeviceDetails.class);
		dao.insert(new PropertyDevice(deviceDetails, id, "secret"));
		boolean inserted = dao.insert(new PropertyDevice(deviceDetails, id2, "secret"));

		// assert
		assertFalse(inserted);
		assertTrue(dao.exists(id));
	}

	@Test
	public void deleteIdExists() throws Exception {
		// setup
		String id = "Test-123";

		// execute
		PropertyDeviceDetails deviceDetails = Mockito.mock(PropertyDeviceDetails.class);
		dao.insert(new PropertyDevice(deviceDetails, id, "secret"));
		boolean deleted = dao.delete(id);

		// assert
		assertFalse(dao.exists(id));
		assertTrue(deleted);
	}

	@Test
	public void deleteIdDoesNotExist() throws Exception {
		// setup
		String id = "Test-123";

		// execute
		boolean deleted = dao.delete(id);

		// assert
		assertFalse(dao.exists(id));
		assertFalse(deleted);
	}

	@Test
	public void findByIdObjectExists() throws Exception {
		// setup
		String id = "Test-123";

		// execute
		PropertyDeviceDetails deviceDetails = Mockito.mock(PropertyDeviceDetails.class);
		dao.insert(new PropertyDevice(deviceDetails, id, "secret"));
		IOTDevice device = dao.findById(id);

		// assert
		assertEquals(device.getId(), id);
		assertEquals(device.getSecret(), "secret");
	}

	@Test
	public void findByIdObjectDoesNotExist() throws Exception {
		// setup
		String id = "Test-123";

		// execute
		IOTDevice device = dao.findById(id);

		// assert
		assertNull(device);
	}

	@Test
	public void findAllExistingDevice() throws Exception {
		// setup
		String id = "Test-123";

		// execute
		PropertyDeviceDetails deviceDetails = Mockito.mock(PropertyDeviceDetails.class);
		dao.insert(new PropertyDevice(deviceDetails, id, "secret"));
		List<IOTDevice> devices = dao.findAll();

		// assert
		assertEquals(devices.size(), 1);
		assertEquals(devices.get(0).getId(), id);
	}

	@Test
	public void findAllNoExistingDevice() throws Exception {
		// execute
		List<IOTDevice> devices = dao.findAll();

		// assert
		assertEquals(devices.size(), 0);
	}

	@Test
	public void updateAllDeviceExists() throws Exception {
		// setup
		String id = "Test-123";

		// execute
		PropertyDeviceDetails deviceDetails = Mockito.mock(PropertyDeviceDetails.class);
		dao.insert(new PropertyDevice(deviceDetails, id, "secret"));
		PropertyDevice device = (PropertyDevice) dao.findById(id);
		device.animateMetrics();

		boolean updated = dao.updateAll(Arrays.asList((IOTDevice) device));

		// assert
		PropertyDevice actual = (PropertyDevice) dao.findById(id);
		assertEquals(device.getMetrics(), actual.getMetrics());
		assertTrue(updated);
	}

	@Test
	public void updateAllDeviceDoesNotExists() throws Exception {
		// setup
		String id = "Test-123";

		// execute
		PropertyDeviceDetails deviceDetails = Mockito.mock(PropertyDeviceDetails.class);
		PropertyDevice device = new PropertyDevice(deviceDetails, id, "secret");
		device.animateMetrics();

		boolean updated = dao.updateAll(Arrays.asList((IOTDevice) device));

		// assert
		assertFalse(updated);
	}

	@Test
	public void updateDeviceExists() throws Exception {
		// setup
		String id = "Test-123";

		// execute
		PropertyDeviceDetails deviceDetails = Mockito.mock(PropertyDeviceDetails.class);
		dao.insert(new PropertyDevice(deviceDetails, id, "secret"));
		PropertyDevice device = (PropertyDevice) dao.findById(id);
		device.animateMetrics();

		boolean updated = dao.update((IOTDevice) device);

		// assert
		PropertyDevice actual = (PropertyDevice) dao.findById(id);
		assertEquals(device.getMetrics(), actual.getMetrics());
		assertTrue(updated);
	}

	@Test
	public void updateDeviceDoesNotExist() throws Exception {
		// setup
		String id = "Test-123";

		// execute
		PropertyDeviceDetails deviceDetails = Mockito.mock(PropertyDeviceDetails.class);
		PropertyDevice device = new PropertyDevice(deviceDetails, id, "secret");
		device.animateMetrics();

		boolean updated = dao.update((IOTDevice) device);

		// assert
		assertFalse(updated);
	}

	@Test
	public void existsInserted() throws Exception {
		// setup
		String id = "Test-123";
		String id2 = "ABC-123";

		// execute
		PropertyDeviceDetails deviceDetails = Mockito.mock(PropertyDeviceDetails.class);
		dao.insert(new PropertyDevice(deviceDetails, id, "secret"));
		boolean exists = dao.exists(id);
		boolean exists2 = dao.exists(id2);

		// assert
		assertTrue(exists);
		assertFalse(exists2);
	}

	@Test
	public void existsNoInsert() throws Exception {
		// setup
		String id = "Test-123";

		// execute
		boolean exists = dao.exists(id);

		// assert
		assertFalse(exists);
	}

	@Test
	public void deleteAllIdExists() throws Exception {
		// setup
		String id = "Test-123";

		// execute
		PropertyDeviceDetails deviceDetails = Mockito.mock(PropertyDeviceDetails.class);
		dao.insert(new PropertyDevice(deviceDetails, id, "secret"));
		dao.deleteAll();

		// assert
		assertFalse(dao.exists(id));
	}

	@Test
	public void deleteAllIdDoesNotExist() throws Exception {
		// setup
		String id = "Test-123";

		// execute
		dao.deleteAll();

		// assert
		assertFalse(dao.exists(id));
	}
}
