package com.oracle.iot.dao;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.springframework.stereotype.Repository;

import com.oracle.iot.model.DeviceType;
import com.oracle.iot.model.IOTDevice;

@Repository
public class DeviceDaoInMemory {
	// private Map<String, IOTDevice> devices = new LinkedHashMap<String,
	// IOTDevice>(); - used originally but the IOT client jar can barely handle
	// being one device at a time :)
	private IOTDevice device = null;

	public boolean exists(String id) {
		return (this.device != null && this.device.getId().equals(id));
	}

	public boolean insert(DeviceType deviceType, String id, String secret) {
		if (exists(id)) {
			return false;
		}
		if (this.device != null) {
			throw new RuntimeException("Device exists, please remove and close properly first");
		}
		this.device = deviceType.getDevice(id, secret).copy();
		return true;

	}

	public boolean insert(IOTDevice device) {
		if (exists(device.getId())) {
			return false;
		}
		if (this.device != null) {
			throw new RuntimeException("Device exists, please remove and close properly first");
		}
		this.device = device.copy();
		return true;

	}

	public List<IOTDevice> findAll() {
		if (device == null) {
			return new ArrayList<IOTDevice>();
		} else {
			return new ArrayList<IOTDevice>(Arrays.asList(device.copy()));
		}
	}

	public Boolean delete(String id) {
		if (this.device == null || !this.device.getId().equals(id))
			return false;
		this.device = null;
		return true;
	}

	public IOTDevice findById(String id) {
		if (this.device != null && this.device.getId().equals(id)) {
			return device.copy();
		}
		return null;
	}

	public Boolean updateAll(List<IOTDevice> allDevices) {
		boolean updated = false;
		for (IOTDevice device : allDevices) {
			if (this.device != null && this.device.getId().equals(device.getId())) {
				this.device = device.copy();
				updated = true;
			}
		}
		return updated;
	}

	public Boolean update(IOTDevice update) {
		if (this.device != null && this.device.getId().equals(update.getId())) {
			this.device = update.copy();
			return true;
		}
		return false;
	}

	public void deleteAll() {
		device = null;
	}
}
