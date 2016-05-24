package com.oracle.iot.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.RedirectView;

import com.oracle.iot.controller.bind.FileUploadForm;
import com.oracle.iot.model.PropertyDeviceDetails;
import com.oracle.iot.service.DeviceService;

@Controller
public class FileUploadController {

	private static final Logger logger = Logger.getLogger(FileUploadController.class);
	@Resource
	private DeviceService deviceService;

	@RequestMapping(value = "/device/setup/types", method = RequestMethod.GET)
	public @ResponseBody List<Map<String, Object>> getTypes() {
		return deviceService.getAllTypes();
	}

	@RequestMapping(value = "/device/setup/types", method = RequestMethod.PUT)
	public @ResponseBody Boolean updateTypes(@RequestBody List<Map<String, Object>> deviceTypes) {
		deviceService.updateTypes(deviceTypes);
		return true;
	}

	@RequestMapping(value = "/device/setup/devicecentral", method = RequestMethod.GET)
	public @ResponseBody Map<String, List<Map<String, Object>>> getDeviceCentralTypes() {
		List<Map<String, Object>> localDevices = deviceService.getAllTypes();
		return deviceService.getAllDeviceCentral(localDevices);
	}

	@RequestMapping(value = "/device/setup/devicecentral", method = RequestMethod.PUT)
	public @ResponseBody Boolean updateDeviceCentralTypes(
			@RequestBody Map<String, List<Map<String, Object>>> deviceTypes) {
		List<Map<String, Object>> localDevices = deviceService.getAllTypes();
		updateCentralDevices(deviceTypes, localDevices);
		return true;
	}

	@RequestMapping(value = "/device/setup/upload", method = RequestMethod.GET)
	public @ResponseBody String uploadDeviceGet() {
		return "You can upload a properties file by posting to this same URL.";
	}

	@RequestMapping(value = "/device/setup/upload", method = RequestMethod.POST)
	public ModelAndView uploadDevicePost(@ModelAttribute("uploadForm") FileUploadForm uploadForm) {
		MultipartFile propertyFile = uploadForm.getFiles().get(0);
		MultipartFile imageFile = uploadForm.getFiles().get(1);
		ModelAndView modelAndView = new ModelAndView(new RedirectView("/?root=uploadDevice", true));
		if (!propertyFile.isEmpty()) {
			PropertyDeviceDetails device = deviceService.load(propertyFile, imageFile);
			deviceService.uploadToDeviceCentral(device.getDisplayName(), uploadForm.getIndustry(), propertyFile,
					imageFile);
		} else {
			logger.error("Error with proeprty file selected");
			modelAndView.addObject("message", "Error with proeprty file selected");
		}
		return modelAndView;
	}

	private void updateCentralDevices(Map<String, List<Map<String, Object>>> deviceTypes,
			List<Map<String, Object>> localDevices) {
		List<Map<String, Object>> devicesToDownload = new ArrayList<>();
		for (List<Map<String, Object>> list : deviceTypes.values()) {
			for (Map<String, Object> item : list) {
				Boolean enabled = (Boolean) item.get("enabled");
				if (enabled && !exist((String) item.get("name"), localDevices)) {
					devicesToDownload.add(item);
				}
			}
		}
		deviceService.downloadFromDeviceCentral(devicesToDownload);
	}

	private boolean exist(String name, List<Map<String, Object>> localDevices) {
		for (Map<String, Object> item : localDevices) {
			if (name.equalsIgnoreCase((String) item.get("name"))) {
				return true;
			}
		}
		return false;
	}
}
