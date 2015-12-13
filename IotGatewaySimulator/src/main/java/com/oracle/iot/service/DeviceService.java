package com.oracle.iot.service;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.oracle.iot.dao.DeviceCentralDao;
import com.oracle.iot.dao.DeviceDaoInMemory;
import com.oracle.iot.dao.DevicePropertiesLoaderDao;
import com.oracle.iot.model.IOTDevice;
import com.oracle.iot.model.PropertyDevice;
import com.oracle.iot.model.PropertyDeviceDetails;

@Service
public class DeviceService {

	private static final Logger logger = Logger.getLogger(DeviceService.class);

	@Resource
	private DeviceDaoInMemory deviceDao;

	@Resource
	private DevicePropertiesLoaderDao loaderDao;

	@Resource
	private DeviceCentralDao centralDao;

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

	public List<String> getAllNames() {
		return loaderDao.getNames();
	}

	public List<Map<String, Object>> getEnabledTypes() {
		return loaderDao.getTypes(false);
	}

	public PropertyDeviceDetails load(MultipartFile propertyFile, MultipartFile imageFile) {
		return loaderDao.loadNewDevice(propertyFile, imageFile);
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

	public boolean uploadToDeviceCentral(String name, MultipartFile propertyFile, MultipartFile imageFile) {
		try {
			String device = IOUtils.toString(propertyFile.getInputStream());
			String image = null;
			if (imageFile != null && !imageFile.isEmpty()) {
				byte[] imageBytes = imageFile.getBytes();
				image = Base64.encodeBase64String(imageBytes);
			} else {
				InputStream stream = this.getClass().getClassLoader().getResourceAsStream("pictures/widget.png");
				byte[] imageBytes = IOUtils.toByteArray(stream);
				image = Base64.encodeBase64String(imageBytes);
			}
			centralDao.saveDevice(name, device, image);
			return true;
		} catch (Exception e) {
			logger.error("Error uploading device", e);
			return false;
		}
	}

	public List<Map<String, Object>> getAllDeviceCentral(List<Map<String, Object>> localDevices) {
		List<String> centralDevices = centralDao.getDeviceNames();
		List<Map<String, Object>> centralItems = new ArrayList<>();
		for (String name : centralDevices) {
			Map<String, Object> centralItem = new LinkedHashMap<>();
			centralItem.put("name", name.replaceAll("\\s", ""));
			centralItem.put("display", name);
			Map<String, Object> localDevice = findLocally(name, localDevices);
			if (localDevice != null) {
				centralItem.put("enabled", localDevice.get("enabled"));
				centralItem.put("disabled", true);// disable device if it is
													// already downloaded
			} else {
				centralItem.put("disabled", false);
			}
			centralItems.add(centralItem);
		}
		Collections.sort(centralItems, new Comparator<Map<String, Object>>() {

			@Override
			public int compare(Map<String, Object> o1, Map<String, Object> o2) {
				String display1 = (String) o1.get("name");
				String display2 = (String) o2.get("name");
				return display1.compareToIgnoreCase(display2);
			}

		});
		return centralItems;
	}

	private Map<String, Object> findLocally(String name, List<Map<String, Object>> localDevices) {
		for (Map<String, Object> localDevice : localDevices) {
			if (name.equalsIgnoreCase((String) localDevice.get("display"))) {
				return localDevice;
			}
		}
		return null;
	}

	public void downloadFromDeviceCentral(List<Map<String, Object>> centralDevices) {
		List<String> localDevices = getAllNames();
		for (Map<String, Object> centralDevice : centralDevices) {
			// if we already have it locally then ignore it
			if (!localDevices.contains(centralDevice.get("display")) && (Boolean) centralDevice.get("enabled")) {
				Map<String, Object> downloadDevice = centralDao.downloadDevice((String) centralDevice.get("display"));
				loaderDao.loadNewDevice((String) downloadDevice.get("DEVICE"), (String) downloadDevice.get("PICTURE"));
			}
		}

	}
}
