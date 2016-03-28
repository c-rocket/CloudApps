package com.oracle.iot.service;

import javax.annotation.Resource;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Service;

import com.oracle.iot.dao.MessagingDao;
import com.oracle.iot.model.IOTDevice;

import oracle.iot.client.ClientException;
import oracle.iot.message.DataMessage;
import oracle.iot.message.Message;

@Service
public class MessagingService {
	Logger log = Logger.getLogger(MessagingService.class);
	@Resource
	private MessagingDao dao;

	public void sendMessages(IOTDevice device, String iotcsServer, Integer iotcsPort, Boolean sendMessages,
			String username, String password) throws ClientException {
		DataMessage message = device.createMessage();
		if (sendMessages) {
			dao.createConnection(iotcsServer, iotcsPort, username, password, device.getId(), device.getSecret());
			boolean madeConnection = getDeviceClientConnection(device);
			// sends true if client connection is made
			if (madeConnection) {
				dao.sendMessage(device.getId(), message);
			}
		} 
	}

	private boolean getDeviceClientConnection(IOTDevice device) throws ClientException {
		byte[] privateKey = dao.getPrivateKey(device.getId());
		try {
			if (privateKey == null) {
				privateKey = dao.activateDevice(device.getId());
//				for (DeviceResource resource : device.getResources()) {
//					dao.registerRequestHandler(device.getId(), resource.getResource(), resource.getHandler());
//				}
				dao.savePrivateKey(device.getId(), privateKey);
			} else {
				// Authenticate with, and connect to, the server
				System.out.println("\nConnecting with client-assertion...");
				dao.authenticate(privateKey, device.getId());
			}
		} catch (Exception e) {
			log.error("Error activating/authenticating", e);
			throw new RuntimeException("Error activating", e);
		}
		return true;
	}

	public Boolean sendAlert(IOTDevice device, String alert, String iotcsServer, Integer iotcsPort,
			Boolean sendMessages) {
		if (sendMessages && device != null) {
			Message message = device.createAlertMessage(alert);
			try {
				dao.createConnection(iotcsServer, iotcsPort, "username", "password", device.getId(), device.getSecret());
				getDeviceClientConnection(device);
				dao.sendMessage(device.getId(), message);
			} catch (Exception e) {
				log.error("Error sending alert", e);
				return false;
			}
		}
		return true;
	}

}
