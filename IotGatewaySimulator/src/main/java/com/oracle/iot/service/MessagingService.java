package com.oracle.iot.service;

import java.io.IOException;
import java.security.GeneralSecurityException;

import javax.annotation.Resource;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Service;

import com.oracle.iot.dao.MessagingDao;
import com.oracle.iot.model.IOTDevice;

import oracle.iot.client.device.VirtualDevice;

@Service
public class MessagingService {
	Logger log = Logger.getLogger(MessagingService.class);
	@Resource
	private MessagingDao dao;

	public void sendMessages(IOTDevice device, String iotcsServer, Integer iotcsPort, Boolean sendMessages,
			String weblogicTrust, String trustPassword) {
		System.setProperty("com.oracle.iot.client.server.cn", iotcsServer);
		System.setProperty("com.oracle.iot.client.server.port", String.valueOf(iotcsPort));
		try {
			device.animateMetrics();
			if (sendMessages) {
				VirtualDevice virtualDevice = dao.getVirtualDevice(device, iotcsServer, iotcsPort, weblogicTrust, trustPassword);
				device.update(virtualDevice);
			}
		} catch (GeneralSecurityException e) {
			log.error("Error getting device", e);
		} catch (IOException e) {
			log.error("Error getting device", e);
		}
	}

	public Boolean sendAlert(IOTDevice device, String alert, String iotcsServer, Integer iotcsPort,
			Boolean sendMessages, String weblogicTrust, String trustPassword) {
		if (sendMessages && device != null) {
			try {
				VirtualDevice virtualDevice = dao.getVirtualDevice(device, iotcsServer, iotcsPort, weblogicTrust,
						trustPassword);
				device.alert(virtualDevice, alert);
			} catch (GeneralSecurityException e) {
				log.error("Error getting device", e);
			} catch (IOException e) {
				log.error("Error getting device", e);
			}
		}
		return true;
	}

}
