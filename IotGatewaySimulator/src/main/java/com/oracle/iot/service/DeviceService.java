package com.oracle.iot.service;

import java.util.List;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;

import com.oracle.iot.dao.DeviceDaoInMemory;
import com.oracle.iot.model.DeviceType;
import com.oracle.iot.model.IOTDevice;

@Service
public class DeviceService {

	@Resource
	private DeviceDaoInMemory deviceDao;

	public boolean create(DeviceType deviceType, String id, String secret) {
		return deviceDao.insert(deviceType, id, secret);
	}

	public List<IOTDevice> findAll() {
		return deviceDao.findAll();
	}

	public Boolean delete(String id) {
		return deviceDao.delete(id);
	}

	public IOTDevice findById(String id) {
		IOTDevice device = deviceDao.findById(id);
		return device;
	}

	public Boolean updateAll(List<IOTDevice> allDevices) {
		return deviceDao.updateAll(allDevices);
	}

	public Boolean updateDevice(IOTDevice device) {
		return deviceDao.update(device);
	}

	public IOTDevice getCurrentDevice() {
		List<IOTDevice> devices = deviceDao.findAll();
		return (devices.size() > 0) ? devices.get(0) : null;

	}

}
