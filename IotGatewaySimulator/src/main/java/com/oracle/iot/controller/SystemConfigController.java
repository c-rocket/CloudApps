package com.oracle.iot.controller;

import java.util.HashMap;
import java.util.Map;

import javax.annotation.Resource;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.oracle.iot.service.SystemConfigService;

@Controller
public class SystemConfigController {

	@Resource
	private SystemConfigService systemConfigService;

	@RequestMapping(value = "/system/config", method = RequestMethod.GET)
	@ResponseBody
	public Map<String, Object> getHost() {
		Map<String, Object> result = new HashMap<String, Object>();
		result.put("server", systemConfigService.getHost());
		result.put("port", systemConfigService.getPort());
		result.put("sendingMessages", systemConfigService.getMessageStatus());
		return result;
	}

	@RequestMapping(value = "/system/config", method = RequestMethod.PUT)
	@ResponseBody
	public Boolean setHost(@RequestBody Map<String, Object> config) {
		systemConfigService.setHost((String) config.get("server"));
		systemConfigService.setPort((Integer) config.get("port"));
		systemConfigService.setMessageStatus((Boolean) config.get("sendingMessages"));

		return true;
	}
}
