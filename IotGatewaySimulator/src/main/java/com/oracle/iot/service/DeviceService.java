package com.oracle.iot.service;

import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;

import com.oracle.iot.dao.DeviceDaoInMemory;
import com.oracle.iot.dao.DevicePropertiesLoaderDao;
import com.oracle.iot.model.IOTDevice;
import com.oracle.iot.model.PropertyDevice;
import com.oracle.iot.model.PropertyDeviceDetails;

@Service
public class DeviceService {

	@Resource
	private DeviceDaoInMemory deviceDao;

	@Resource
	private DevicePropertiesLoaderDao loaderDao;

	public boolean create(String name, String id, String secret) {
		PropertyDeviceDetails deviceDetails = loaderDao.getDevice(name);
		return deviceDao.insert(new PropertyDevice(deviceDetails, id, secret));
	}

	public List<IOTDevice> findAll() {
		return deviceDao.findAll();
	}

	public Boolean delete(String id) {
		return deviceDao.delete(id);
	}

	public IOTDevice findById(String id) {
		return deviceDao.findById(id);
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

	public List<Map<String, Object>> getAllTypes() {
		return loaderDao.getTypes(true);
	}

	public List<Map<String, Object>> getEnabledTypes() {
		return loaderDao.getTypes(false);
	}

	public void updateTypes(List<Map<String, Object>> devices) {
		for (Map<String, Object> item : devices) {
			String name = (String) item.get("name");
			Boolean enabled = (Boolean) item.get("enabled");

			PropertyDeviceDetails device = loaderDao.getDevice(name);
			if (enabled) {
				device.enable();
			} else {
				device.disable();
			}
		}
	}

}
