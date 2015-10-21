package com.oracle.iot.controller;

import javax.annotation.Resource;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import com.oracle.iot.service.DevicePropertiesLoaderService;

@Controller
public class FileUploadController {

	@Resource
	private DevicePropertiesLoaderService service;

	@RequestMapping(value = "/device/setup", method = RequestMethod.GET)
	public String setupPage() {
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
}
