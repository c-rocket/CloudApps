package com.oracle.iot.controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.oracle.iot.controller.converter.DeviceTypeEnumConverter;
import com.oracle.iot.model.Common;
import com.oracle.iot.model.DeviceType;
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

	@InitBinder
	public void initBinder(WebDataBinder binder) {
		binder.registerCustomEditor(DeviceType.class, new DeviceTypeEnumConverter());
	}

	@RequestMapping(value = "/device/create", method = RequestMethod.POST)
	@ResponseBody
	public Boolean createNewDevice(@RequestBody Map<String, Object> device) {
		String id = device.get("id").toString();
		String secret = device.get("secret").toString();
		DeviceType deviceType = DeviceType.findByName(device.get("type").toString());
		if (Common.isNullOrEmpty(id))
			return false;
		if (Common.isNullOrEmpty(secret))
			return false;
		if (Common.isNull(deviceType))
			return false;
		if (deviceService.create(deviceType, id, secret)) {
			return true;
		}
		return false;
	}

	@RequestMapping(value = "/device/list", method = RequestMethod.GET)
	@ResponseBody
	public List<IOTDevice> listDevices() {
		return deviceService.findAll();
	}

	@RequestMapping(value = "/device/delete/{id}", method = RequestMethod.DELETE)
	@ResponseBody
	public Boolean deleteDevice(@PathVariable String id) {
		return deviceService.delete(id);
	}

	@RequestMapping(value = "/device/{id}/show", method = RequestMethod.GET)
	@ResponseBody
	public IOTDevice showDevice(@PathVariable String id) {
		return deviceService.findById(id);
	}

	@RequestMapping(value = "/device/{id}/alerts/{alert}", method = RequestMethod.PUT)
	@ResponseBody
	public Boolean sendAlert(@PathVariable String id, @PathVariable String alert) {
		if (systemConfigService.getMessageStatus()) {
			return messagingService.sendAlert(deviceService.findById(id), alert, systemConfigService.getHost(),
					systemConfigService.getPort());
		} else {
			log.info("Alert captured");
			return true;
		}
	}

	@RequestMapping(value = "/device/{id}/events/{event}", method = RequestMethod.PUT)
	@ResponseBody
	public Boolean triggerEvent(@PathVariable String id, @PathVariable String event) {
		IOTDevice device = deviceService.findById(id);
		Boolean started = device.eventHandler(event);
		deviceService.updateDevice(device);
		return started;
	}

	@RequestMapping(value = "/device/types", method = RequestMethod.GET)
	@ResponseBody
	public List<Map<String, String>> getDeviceTypes() {
		List<Map<String, String>> list = new ArrayList<Map<String, String>>();
		for (DeviceType deviceType : DeviceType.values()) {
			Map<String, String> map = new HashMap<String, String>();
			map.put("name", deviceType.name());
			map.put("display", deviceType.getDisplay());
			list.add(map);
		}
		return list;
	}
}
