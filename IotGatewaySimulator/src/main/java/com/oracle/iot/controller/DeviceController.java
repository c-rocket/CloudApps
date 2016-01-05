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

	@RequestMapping(value = "/device/current", method = RequestMethod.POST)
	@ResponseBody
	public Boolean createNewDevice(@RequestBody Map<String, Object> device) {
		String id = device.get("id").toString();
		String secret = device.get("secret").toString();
		if (Common.isNullOrEmpty(id))
			return false;
		if (Common.isNullOrEmpty(secret))
			return false;

		IOTDevice currentDevice = deviceService.getCurrentDevice();
		if (currentDevice == null) {
			return deviceService.create(device.get("type").toString(), id, secret);
		} else {
			messagingService.close(currentDevice, systemConfigService.getHost(), systemConfigService.getPort());
			deviceService.delete(currentDevice.getId());
			return deviceService.create(device.get("type").toString(), id, secret);
		}
	}

	@RequestMapping(value = "/device/list", method = RequestMethod.GET)
	@ResponseBody
	public List<IOTDevice> listDevices() {
		return deviceService.findAll();
	}

	@RequestMapping(value = "/device/current", method = RequestMethod.DELETE)
	@ResponseBody
	public Boolean deleteDevice() {
		IOTDevice currentDevice = deviceService.getCurrentDevice();
		Boolean removed = deviceService.delete(currentDevice.getId());
		messagingService.close(currentDevice, systemConfigService.getHost(),
				systemConfigService.getPort());
		return removed;
	}

	@RequestMapping(value = "/device/current", method = RequestMethod.GET)
	@ResponseBody
	public IOTDevice showDevice() {
		return deviceService.getCurrentDevice();
	}

	@RequestMapping(value = "/device/current/alerts/{alert}", method = RequestMethod.PUT)
	@ResponseBody
	public Boolean sendAlert(@PathVariable String alert) {
		return messagingService.sendAlert(deviceService.getCurrentDevice(), alert, systemConfigService.getHost(),
				systemConfigService.getPort(), systemConfigService.getMessageStatus());
	}

	@RequestMapping(value = "/device/current/events/{event}", method = RequestMethod.PUT)
	@ResponseBody
	public Boolean triggerEvent(@PathVariable String event) {
		IOTDevice device = deviceService.getCurrentDevice();
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
