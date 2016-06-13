package com.oracle.iot.dao;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.stereotype.Repository;

import com.oracle.iot.client.impl.trust.TrustedAssetsProvisioner;
import com.oracle.iot.model.IOTDevice;

import oracle.iot.client.DeviceModel;
import oracle.iot.client.device.DirectlyConnectedDevice;
import oracle.iot.client.device.VirtualDevice;

@Repository
public class MessagingDao {
	private org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(MessagingDao.class);
	private Map<String, VirtualDevice> connectedDevices = new LinkedHashMap<String, VirtualDevice>();

	public VirtualDevice getVirtualDevice(IOTDevice device, String iotcsServer, Integer iotcsPort, String weblogicTrust,
			String trustPassword) throws GeneralSecurityException, IOException {
		VirtualDevice virtualDevice = connectedDevices.get(device.getId());
		if (virtualDevice == null) {
			String store = null;
			try {
				store = TrustedAssetsProvisioner.provision(createProvisionerArgs(device, iotcsServer, iotcsPort, weblogicTrust, trustPassword));
			} catch (IllegalArgumentException e) {
				log.debug("Key already Exists, the existing one will be used", e);
				store = System.getProperty("user.dir") + "/" + device.getId() + ".jks";
			}

			System.setProperty("oracle.iot.client.trustedAssetsStore", store);
			System.setProperty("oracle.iot.client.trustedAssetsStorePassword", trustPassword);
			@SuppressWarnings("resource")
			DirectlyConnectedDevice dcd = new DirectlyConnectedDevice();
			// Activate the device
			if (!dcd.isActivated()) {
				log.info(device.getModelURN());
				dcd.activate(device.getModelURN());
			}
			DeviceModel dcdModel = dcd.getDeviceModel(device.getModelURN());
			virtualDevice = dcd.createVirtualDevice(device.getId(), dcdModel);
			device.addCallbacks(virtualDevice);

			connectedDevices.put(device.getId(), virtualDevice);
		}
		return virtualDevice;
	}

	private String[] createProvisionerArgs(IOTDevice device, String iotcsServer, Integer iotcsPort,
			String weblogicTrust, String trustPassword) {
		String[] args = new String[14];
		args[0] = "-serverHost";
		args[1] = iotcsServer;
		args[2] = "-serverPort";
		args[3] = String.valueOf(iotcsPort);
		args[4] = "-truststore";
		args[5] = weblogicTrust;
		args[6] = "-truststorePassword";
		args[7] = trustPassword;
		args[8] = "-taStorePassword";
		args[9] = trustPassword;
		args[10] = "-sharedSecret";
		args[11] = device.getSecret();
		args[12] = "-deviceId";
		args[13] = device.getId();
		log.info("Activating new device" + device + ", " + args.toString());
		return args;
	}

	public void deleteAll() {
		connectedDevices.clear();
	}
}
