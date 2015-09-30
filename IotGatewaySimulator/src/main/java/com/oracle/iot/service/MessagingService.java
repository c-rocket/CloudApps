package com.oracle.iot.service;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Service;

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

	Map<String, AsyncDeviceClient> clients = new LinkedHashMap<String, AsyncDeviceClient>();

	public void sendMessages(List<IOTDevice> devices, String iotcsServer, Integer iotcsPort, Boolean sendMessages) {
		for (IOTDevice device : devices) {
			DataMessage message = device.createMessage();
			if (sendMessages) {
				getDeviceClientConnection(iotcsServer, iotcsPort, device);
				sendMessage(device.getId(), message);
			}
		}
	}

	private void sendMessage(String id, Message message) {
		clients.get(id).sendMessage(message);
	}

	private void getDeviceClientConnection(String iotcsServer, Integer iotcsPort, IOTDevice device) {
		System.setProperty("com.oracle.iot.client.server.cn", iotcsServer);
		if (clients.get(device.getId()) == null) {
			AsyncDeviceClient DEVICE_CLIENT = new AsyncDeviceClient(iotcsServer, iotcsPort, device.getId());
			clients.put(device.getId(), DEVICE_CLIENT);
		}
		byte[] privateKey = dao.getPrivateKey(device.getId());
		if (privateKey == null) {
			try {
				privateKey = clients.get(device.getId()).activate(device.getSecret());
				savePrivateKey(device, privateKey);
			} catch (final IllegalStateException EXCEPTION) {
				clients.get(device.getId()).close();
				clients.remove(device.getId());
				System.err.println("The device has already been activated, but there is no private key");
				System.err.println("Enroll a new device and try again.");
				System.exit(-1);
			} catch (Exception e) {
				e.printStackTrace();
				clients.get(device.getId()).close();
				clients.remove(device.getId());
			}
		} else {
			// Authenticate with, and connect to, the server
			System.out.println("\nConnecting with client-assertion...");
			try {
				clients.get(device.getId()).authenticate(privateKey);
			} catch (Exception e) {
				e.printStackTrace();
				dao.deletePrivateKey(device.getId());
				clients.get(device.getId()).close();
				clients.remove(device.getId());
			}
		}
	}

	private void savePrivateKey(IOTDevice device, final byte[] PRIVATE_KEY) {
		dao.savePrivateKey(device.getId(), PRIVATE_KEY);
	}

	public Boolean sendAlert(IOTDevice device, String alert, String iotcsServer, Integer iotcsPort) {
		getDeviceClientConnection(iotcsServer, iotcsPort, device);
		sendMessage(device.getId(), device.createAlertMessage(alert));
		return true;
	}
}
