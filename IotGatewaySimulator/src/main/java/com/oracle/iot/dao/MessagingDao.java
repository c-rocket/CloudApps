package com.oracle.iot.dao;

import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.stereotype.Repository;

import oracle.iot.client.device.async.AsyncDeviceClient;

@Repository
public class MessagingDao {

	private Map<String, byte[]> privateKeys = new LinkedHashMap<String, byte[]>();

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

	public AsyncDeviceClient getAsyncClient(String iotcsServer, Integer iotcsPort, String id) {
		return new AsyncDeviceClient(iotcsServer, iotcsPort, id);
	}

}
