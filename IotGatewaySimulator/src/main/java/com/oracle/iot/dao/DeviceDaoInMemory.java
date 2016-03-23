package com.oracle.iot.dao;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Repository;

import com.oracle.iot.model.IOTDevice;

@Repository
public class DeviceDaoInMemory {
	private Map<String, IOTDevice> devices = new LinkedHashMap<String, IOTDevice>();
	// - used originally but the IOT client jar can barely handle
	// being one device at a time :)
	// private IOTDevice device = null;

	public boolean exists(String id) {
		return (this.devices.get(id) != null);
	}

	public boolean insert(IOTDevice device) {
		if (exists(device.getId())) {
			return false;
		}
		if (this.devices.get(device.getId()) != null) {
			throw new RuntimeException("Device exists, please remove and close properly first");
		}
		this.devices.put(device.getId(), device.copy());
		return true;

	}

	public List<IOTDevice> findAll() {
		if (devices.size() == 0) {
			return new ArrayList<IOTDevice>();
		} else {
			return new ArrayList<IOTDevice>(devices.values());
		}
	}

	public Boolean delete(String id) {
		if (this.devices.get(id) == null)
			return false;
		this.devices.remove(id);
		return true;
	}

	public IOTDevice findById(String id) {
		if (this.devices.get(id) != null) {
			return this.devices.get(id).copy();
		}
		return null;
	}

	public Boolean updateAll(List<IOTDevice> allDevices) {
		boolean updated = false;
		for (IOTDevice device : allDevices) {
			this.devices.put(device.getId(), device.copy());
			updated = true;
		}
		return updated;
	}

	public Boolean update(IOTDevice update) {
		this.devices.put(update.getId(), update.copy());
		return true;
	}

	public void deleteAll() {
		devices.clear();
	}
}
