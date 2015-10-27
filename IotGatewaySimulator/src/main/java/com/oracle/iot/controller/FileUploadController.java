package com.oracle.iot.controller;

import java.util.List;
import java.util.Map;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import com.oracle.iot.service.DevicePropertiesLoaderService;
import com.oracle.iot.service.DeviceService;

@Controller
public class FileUploadController {

	private static final Logger logger = Logger.getLogger(FileUploadController.class);
	@Resource
	private DevicePropertiesLoaderService service;
	@Resource
	private DeviceService deviceService;

	@RequestMapping(value = "/device/setup", method = RequestMethod.GET)
	public String setupPage(Model model) {
		model.addAttribute("devices", deviceService.getAllTypes());
		return "setup";
	}

	@RequestMapping(value = "/device/setup/upload", method = RequestMethod.GET)
	public @ResponseBody String uploadDeviceGet() {
		return "You can upload a properties file by posting to this same URL.";
	}

	@RequestMapping(value = "/device/setup/upload", method = RequestMethod.POST)
	public String uploadDevicePost(@RequestParam("file") MultipartFile multipartFile) {
		if (!multipartFile.isEmpty()) {
			if (service.load(multipartFile)) {
				return "uploadSuccess";
			}
			return "uploadError";
		} else {
			return "uploadError";
		}
	}

	@RequestMapping(value = "/device/setup/select", method = RequestMethod.POST)
	public String selectDevices(HttpServletRequest request) {
		logger.info("updating available devices");
		List<Map<String, Object>> devices = deviceService.getAllTypes();
		for (Map<String, Object> item : devices) {
			Object attribute = request.getParameter("devices." + (String) item.get("name"));
			Boolean enabled = attribute != null;
			item.put("enabled", enabled);
		}
		deviceService.updateTypes(devices);
		return "devicesSaved";
	}
}
