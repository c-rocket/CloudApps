package com.oracle.iot.service;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import com.oracle.iot.dao.MessagingDao;
import com.oracle.iot.model.IOTDevice;

@RunWith(MockitoJUnitRunner.class)
public class MessagingServiceTest {

	@Mock
	MessagingDao dao;

	@InjectMocks
	MessagingService service;

	@Test
	public void sendMessagesServerSendingIsOnExistingPrivateKey() throws Exception {
		// setup
		String id = "Test-123";
		String iotcsServer = "server";
		Integer iotcsPort = 9001;
		Boolean sendMessages = true;
		byte[] key = "privatebyteSizedKey".getBytes();
		IOTDevice device = Mockito.mock(IOTDevice.class);
		when(device.getId()).thenReturn(id);

		when(dao.getPrivateKey(id)).thenReturn(key);

		// execute
		service.sendMessages(device, iotcsServer, iotcsPort, sendMessages, "username","password");

		// assert
		verify(dao, times(1)).createConnection(any(String.class), any(Integer.class), any(String.class), any(String.class), any(String.class), any(String.class));
		verify(dao, times(1)).authenticate(any(byte[].class), any(String.class));
		verify(dao, never()).activateDevice(any(String.class));
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
		IOTDevice device = Mockito.mock(IOTDevice.class);
		when(device.getId()).thenReturn(id);
		when(device.getSecret()).thenReturn(secret);

		when(dao.getPrivateKey(id)).thenReturn(null);

		// execute
		service.sendMessages(device, iotcsServer, iotcsPort, sendMessages,"username","password");

		// assert
		verify(dao, times(1)).createConnection(any(String.class), any(Integer.class), any(String.class), any(String.class), any(String.class), any(String.class));
		verify(dao, never()).authenticate(any(byte[].class), any(String.class));
		verify(dao, times(1)).activateDevice(any(String.class));
		verify(dao, times(1)).savePrivateKey(eq(id), any(byte[].class));
	}
}
