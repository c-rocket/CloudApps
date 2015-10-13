package com.oracle.iot.service;

import java.util.List;

import javax.annotation.Resource;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Service;

import com.oracle.iot.client.ConnectionManager;
import com.oracle.iot.client.TrustManager;
import com.oracle.iot.dao.MessagingDao;
import com.oracle.iot.model.IOTDevice;

import oracle.iot.client.device.async.AsyncDeviceClient;
import oracle.iot.message.DataMessage;
import oracle.iot.message.Message;

@Service
public class MessagingService {
	Logger log = Logger.getLogger(MessagingService.class);
	@Resource
	private MessagingDao dao;

	public boolean sendMessages(List<IOTDevice> devices, String iotcsServer, Integer iotcsPort, Boolean sendMessages) {
		boolean error = false;
		// Note that right now there will only ever be one device as the client
		// library does not support multiples through their Asyn library class
		for (IOTDevice device : devices) {
			DataMessage message = device.createMessage();
			if (sendMessages && !error) {
				AsyncDeviceClient DEVICE_CLIENT = dao.getAsyncClient(iotcsServer, iotcsPort, device.getId());
				error = !getDeviceClientConnection(DEVICE_CLIENT, iotcsServer, iotcsPort, device);
				if (!error) {
					DEVICE_CLIENT.sendMessage(message);
				}
			}
		}
		return !error;
	}

	private boolean getDeviceClientConnection(AsyncDeviceClient client, String iotcsServer, Integer iotcsPort,
			IOTDevice device) {
		System.setProperty("com.oracle.iot.client.server.cn", iotcsServer);
		byte[] privateKey = dao.getPrivateKey(device.getId());
		if (privateKey == null) {
			try {
				privateKey = client.activate(device.getSecret());
				dao.savePrivateKey(device.getId(), privateKey);
			} catch (final IllegalStateException EXCEPTION) {
				log.error("The device has already been activated, but there is no private key", EXCEPTION);
				log.error("Enroll a new device and try again.", EXCEPTION);
				return false;
			} catch (Exception e) {
				log.error("Error activating client", e);
				return false;
			}
		} else {
			// Authenticate with, and connect to, the server
			System.out.println("\nConnecting with client-assertion...");
			try {
				client.authenticate(privateKey);
			} catch (Exception e) {
				log.error("Error authenticating client", e);
				return false;
			}
		}
		return true;
	}

	public Boolean sendAlert(IOTDevice device, String alert, String iotcsServer, Integer iotcsPort,
			Boolean sendMessages) {
		if (sendMessages) {
			Message message = device.createAlertMessage(alert);
			AsyncDeviceClient DEVICE_CLIENT = dao.getAsyncClient(iotcsServer, iotcsPort, device.getId());
			getDeviceClientConnection(DEVICE_CLIENT, iotcsServer, iotcsPort, device);
			DEVICE_CLIENT.sendMessage(message);
		}
		return true;
	}

	public void close(IOTDevice device, String iotcsServer, Integer iotcsPort, Boolean sendMessages) {
		if (sendMessages) {
			AsyncDeviceClient DEVICE_CLIENT = dao.getAsyncClient(iotcsServer, iotcsPort, device.getId());
			getDeviceClientConnection(DEVICE_CLIENT, iotcsServer, iotcsPort, device);
			TrustManager trustManager = TrustManager.getInstance(DEVICE_CLIENT);
			ConnectionManager.getInstance(trustManager).close();
			DEVICE_CLIENT.close();
		}
	}
}
