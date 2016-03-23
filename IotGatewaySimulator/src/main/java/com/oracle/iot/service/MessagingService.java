package com.oracle.iot.service;

import javax.annotation.Resource;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Service;

import com.oracle.iot.client.ConnectionManager;
import com.oracle.iot.client.TrustManager;
import com.oracle.iot.dao.MessagingDao;
import com.oracle.iot.model.DeviceResource;
import com.oracle.iot.model.IOTDevice;

import oracle.iot.client.ClientException;
import oracle.iot.client.device.async.AsyncDeviceClient;
import oracle.iot.client.device.async.MessageReceipt;
import oracle.iot.message.DataMessage;
import oracle.iot.message.Message;

@Service
public class MessagingService {
	Logger log = Logger.getLogger(MessagingService.class);
	@Resource
	private MessagingDao dao;

	public MessageReceipt sendMessages(IOTDevice currentDevice, String iotcsServer, Integer iotcsPort,
			Boolean sendMessages) throws ClientException {
		DataMessage message = currentDevice.createMessage();
		if (sendMessages) {
			AsyncDeviceClient DEVICE_CLIENT = dao.getAsyncClient(iotcsServer, iotcsPort, currentDevice.getId(),
					currentDevice.getResources());
			boolean madeConnection = getDeviceClientConnection(DEVICE_CLIENT, iotcsServer, iotcsPort, currentDevice);
			// sends true if client connection is made
			if (madeConnection) {
				return DEVICE_CLIENT.sendMessage(message);
			}
		} else {
			return null;
		}
		throw new RuntimeException("could not send message");
	}

	private boolean getDeviceClientConnection(AsyncDeviceClient client, String iotcsServer, Integer iotcsPort,
			IOTDevice device) throws ClientException {
		System.setProperty("com.oracle.iot.client.server.cn", iotcsServer);
		byte[] privateKey = dao.getPrivateKey(device.getId());
		try {
			if (privateKey == null) {
				privateKey = client.activate(device.getSecret());
				for (DeviceResource resource : device.getResources()) {
					client.registerRequestHandler(resource.getResource(), resource.getHandler());
				}
				dao.savePrivateKey(device.getId(), privateKey);
			} else {
				// Authenticate with, and connect to, the server
				System.out.println("\nConnecting with client-assertion...");
				client.authenticate(privateKey);
			}
		} catch (Exception e) {
			log.error("Error activating/authenticating", e);
			TrustManager trustManager = TrustManager.getInstance(client);
			ConnectionManager.getInstance(trustManager).close();
			trustManager.close();
			client.close();
			throw new RuntimeException("Error activating", e);
		}
		return true;
	}

	public Boolean sendAlert(IOTDevice device, String alert, String iotcsServer, Integer iotcsPort,
			Boolean sendMessages) {
		if (sendMessages && device != null) {
			Message message = device.createAlertMessage(alert);
			AsyncDeviceClient DEVICE_CLIENT = dao.getAsyncClient(iotcsServer, iotcsPort, device.getId(),
					device.getResources());
			try {
				getDeviceClientConnection(DEVICE_CLIENT, iotcsServer, iotcsPort, device);
				DEVICE_CLIENT.sendMessage(message);
			} catch (Exception e) {
				log.error("Error sending alert", e);
				return false;
			}
		}
		return true;
	}

	public void close(IOTDevice device, String iotcsServer, Integer iotcsPort) {
		if (dao.exists(device.getId())) {
			AsyncDeviceClient DEVICE_CLIENT = dao.getAsyncClient(iotcsServer, iotcsPort, device.getId(),
					device.getResources());
			try {
				getDeviceClientConnection(DEVICE_CLIENT, iotcsServer, iotcsPort, device);

				for (DeviceResource resource : device.getResources()) {
					DEVICE_CLIENT.unregisterRequestHandler(resource.getResource());
				}

				TrustManager trustManager = TrustManager.getInstance(DEVICE_CLIENT);
				ConnectionManager.getInstance(trustManager).close();
				trustManager.close();
				DEVICE_CLIENT.close();
			} catch (Exception e) {
				log.error("Problem closing connection", e);
			}
		}
	}
}
