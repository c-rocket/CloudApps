package com.oracle.iot.dao;

import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.stereotype.Repository;

@Repository
public class MessagingDao {

	Map<String, byte[]> privateKeys = new LinkedHashMap<String, byte[]>();

	public byte[] getPrivateKey(String id) {
		return privateKeys.get(id);
	}

	public void savePrivateKey(String id, byte[] privateKey) {
		privateKeys.put(id, privateKey);
	}

	public void deletePrivateKey(String id) {
		privateKeys.remove(id);
	}

}
