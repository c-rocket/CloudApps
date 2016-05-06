package com.oracle.iot.dao;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.stereotype.Repository;

import com.oracle.iot.model.IOTDevice;

import oracle.iot.client.DeviceModel;
import oracle.iot.client.device.DirectlyConnectedDevice;
import oracle.iot.client.device.VirtualDevice;

@Repository
public class MessagingDao {
	private org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(MessagingDao.class);
	private Map<String, VirtualDevice> connectedDevices = new LinkedHashMap<String, VirtualDevice>();


	public VirtualDevice getDevice(IOTDevice device) throws GeneralSecurityException, IOException {
		VirtualDevice virtualDevice = connectedDevices.get(device.getId());
		if (virtualDevice == null) {
			log.info("Activating new device" + device);
			@SuppressWarnings("resource")
			DirectlyConnectedDevice dcd = new DirectlyConnectedDevice();
			// Activate the device
			if (!dcd.isActivated()) {
				dcd.activate(device.getModelURN());
			}
			DeviceModel dcdModel = dcd.getDeviceModel(device.getModelURN());
			virtualDevice = dcd.createVirtualDevice(device.getId(), dcdModel);
			device.addCallbacks(virtualDevice);

			connectedDevices.put(device.getId(), virtualDevice);
		}
		return virtualDevice;
	}


	public void deleteAll() {
		connectedDevices.clear();
	}
}
