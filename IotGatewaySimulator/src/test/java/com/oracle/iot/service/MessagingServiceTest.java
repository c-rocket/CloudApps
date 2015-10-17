package com.oracle.iot.service;

import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import com.oracle.iot.dao.MessagingDao;
import com.oracle.iot.model.DeviceType;
import com.oracle.iot.model.IOTDevice;

import oracle.iot.client.device.async.AsyncDeviceClient;

@RunWith(PowerMockRunner.class)
@PrepareForTest(AsyncDeviceClient.class)
public class MessagingServiceTest {

	@Mock
	MessagingDao dao;

	@InjectMocks
	MessagingService service;

	AsyncDeviceClient mockedClient;

	@Before
	public void setUp() {
		mockedClient = PowerMockito.mock(AsyncDeviceClient.class);
		PowerMockito.mockStatic(AsyncDeviceClient.class);
		when(dao.getAsyncClient(any(String.class), any(Integer.class), any(String.class))).thenReturn(mockedClient);
	}

	@Test
	public void sendMessagesServerSendingIsOnExistingPrivateKey() throws Exception {
		// setup
		String id = "Test-123";
		String iotcsServer = "server";
		Integer iotcsPort = 9001;
		Boolean sendMessages = true;
		byte[] key = "privatebyteSizedKey".getBytes();
		List<IOTDevice> devices = Arrays.asList(DeviceType.CABLE_MODEM.getDevice(id, "secret"));

		when(dao.getPrivateKey(id)).thenReturn(key);

		// execute
		boolean sent = service.sendMessages(devices, iotcsServer, iotcsPort, sendMessages);

		// assert
		assertTrue(sent);
		verify(dao, times(1)).getAsyncClient(any(String.class), any(Integer.class), any(String.class));
		verify(dao, never()).savePrivateKey(id, key);
	}

	@Test
	public void sendMessagesServerSendingIsOnNoExistingPrivateKey() throws Exception {
		// setup
		String id = "Test-123";
		String secret = "secret";
		String iotcsServer = "server";
		Integer iotcsPort = 9001;
		Boolean sendMessages = true;
		byte[] key = "privatebyteSizedKey".getBytes();
		List<IOTDevice> devices = Arrays.asList(DeviceType.CABLE_MODEM.getDevice(id, secret));

		when(dao.getPrivateKey(id)).thenReturn(null);
		PowerMockito.when(mockedClient.activate(secret)).thenReturn(key);

		// execute
		boolean sent = service.sendMessages(devices, iotcsServer, iotcsPort, sendMessages);

		// assert
		assertTrue(sent);
		verify(dao, times(1)).getAsyncClient(any(String.class), any(Integer.class), any(String.class));
		verify(dao, times(1)).savePrivateKey(eq(id), eq(key));
	}
}
