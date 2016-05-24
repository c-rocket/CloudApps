package com.oracle.iot.controller;

import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.oracle.iot.model.Common;
import com.oracle.iot.model.IOTDevice;
import com.oracle.iot.service.DeviceService;
import com.oracle.iot.service.MessagingService;
import com.oracle.iot.service.SystemConfigService;

@Controller
public class DeviceController {

	Logger log = Logger.getLogger(DeviceController.class);

	@Resource
	private DeviceService deviceService;
	@Resource
	private MessagingService messagingService;
	@Resource
	private SystemConfigService systemConfigService;

	@RequestMapping(value = "/device", method = RequestMethod.POST)
	@ResponseBody
	public Boolean createNewDevice(@RequestBody Map<String, Object> device) {
		String id = device.get("id").toString();
		String secret = device.get("secret").toString();
		if (Common.isNullOrEmpty(id))
			return false;
		if (Common.isNullOrEmpty(secret))
			return false;

		IOTDevice currentDevice = deviceService.getDevice(id);
		if (currentDevice == null) {
			return deviceService.create(device.get("type").toString(), id, secret);
		} else {
			return false;
		}
	}

	@RequestMapping(value = "/device/list", method = RequestMethod.GET)
	@ResponseBody
	public List<Map<String, Object>> listDevices() {
		return deviceService.findAll();
	}

	@RequestMapping(value = "/device/{id}", method = RequestMethod.DELETE)
	@ResponseBody
	public Boolean deleteDevice(@PathVariable String id) {
		return deviceService.delete(id);
	}

	@RequestMapping(value = "/device/{id}", method = RequestMethod.GET)
	@ResponseBody
	public IOTDevice showDevice(@PathVariable String id) {
		return deviceService.getDevice(id);
	}
	
	@RequestMapping(value = "/device/{id}/update", method = RequestMethod.GET)
	@ResponseBody
	public Map<String,Object> showDeviceUpdate(@PathVariable String id) {
		return deviceService.getDeviceUpdate(id);
	}

	@RequestMapping(value = "/device/{id}/alerts/{alert}", method = RequestMethod.PUT)
	@ResponseBody
	public Boolean sendAlert(@PathVariable String id, @PathVariable String alert) {
		return messagingService.sendAlert(deviceService.getDevice(id), alert, systemConfigService.getHost(),
				systemConfigService.getPort(), systemConfigService.getMessageStatus(),
				systemConfigService.getWebLogicTrust(), systemConfigService.getPassword());
	}

	@RequestMapping(value = "/device/{id}/events/{event}", method = RequestMethod.PUT)
	@ResponseBody
	public Boolean triggerEvent(@PathVariable String id, @PathVariable String event) {
		IOTDevice device = deviceService.getDevice(id);
		Boolean started = device.eventHandler(event);
		deviceService.updateDevice(device);
		return started;
	}

	@RequestMapping(value = "/device/types", method = RequestMethod.GET)
	@ResponseBody
	public List<Map<String, Object>> getDeviceTypes() {
		return deviceService.getEnabledTypes();
	}
}
