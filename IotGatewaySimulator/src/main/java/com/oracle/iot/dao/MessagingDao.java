package com.oracle.iot.dao;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Repository;

import com.oracle.iot.model.DeviceResource;

import oracle.iot.client.device.async.AsyncDeviceClient;

@Repository
public class MessagingDao {

	private Map<String, byte[]> privateKeys = new LinkedHashMap<String, byte[]>();

	private AsyncDeviceClient client;

	public byte[] getPrivateKey(String id) {
		return privateKeys.get(id);
	}

	public void savePrivateKey(String id, byte[] privateKey) {
		privateKeys.put(id, privateKey);
	}

	public void deletePrivateKey(String id) {
		privateKeys.remove(id);
	}

	public void deleteAll() {
		privateKeys = new LinkedHashMap<String, byte[]>();
	}

	public AsyncDeviceClient getAsyncClient(String iotcsServer, Integer iotcsPort, String id,
			List<DeviceResource> resources) {
		if (!exists(id)) {
			this.client = new AsyncDeviceClient(iotcsServer, iotcsPort, id);
		}
		return this.client;
	}

	public String getCurrentId() {
		if (this.client == null) {
			return null;
		} else {
			return client.getEndpointId();
		}
	}

	public boolean exists(String id) {
		return client != null && client.getEndpointId().equalsIgnoreCase(id);
	}

}
