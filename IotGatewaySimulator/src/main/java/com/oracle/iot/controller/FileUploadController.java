package com.oracle.iot.controller;

import java.util.List;
import java.util.Map;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
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

	@RequestMapping(value = "/device/setup", method = RequestMethod.GET)
	public ModelAndView setupPage(@RequestParam(required = false) String message) {
		ModelAndView modelAndView = new ModelAndView("setup");
		List<Map<String, Object>> localDevices = deviceService.getAllTypes();
		modelAndView.addObject("devices", localDevices);
		modelAndView.addObject("centralDevices", deviceService.getAllDeviceCentral(localDevices));
		modelAndView.addObject("message", message);
		return modelAndView;
	}

	@RequestMapping(value = "/device/setup/upload", method = RequestMethod.GET)
	public @ResponseBody String uploadDeviceGet() {
		return "You can upload a properties file by posting to this same URL.";
	}

	@RequestMapping(value = "/device/setup/upload", method = RequestMethod.POST)
	public ModelAndView uploadDevicePost(@ModelAttribute("uploadForm") FileUploadForm uploadForm) {
		MultipartFile propertyFile = uploadForm.getFiles().get(0);
		MultipartFile imageFile = uploadForm.getFiles().get(1);
		ModelAndView modelAndView = new ModelAndView(new RedirectView("/device/setup", true));
		if (!propertyFile.isEmpty()) {
			PropertyDeviceDetails device = deviceService.load(propertyFile, imageFile);
			if (device != null) {
				if (deviceService.uploadToDeviceCentral(device.getName(), propertyFile, imageFile)) {
					modelAndView.addObject("message", "Device Loaded Locally and Uploaded to Device Central");
				} else {
					modelAndView.addObject("message", "Device only Loaded Locally");
				}
			} else {
				modelAndView.addObject("message", "Error uploading file");
			}
		} else {
			modelAndView.addObject("message", "Error with proeprty file selected");
		}
		return modelAndView;
	}

	@RequestMapping(value = "/device/setup/select", method = RequestMethod.POST)
	public ModelAndView selectDevices(HttpServletRequest request) {
		logger.info("updating available devices");
		List<Map<String, Object>> devices = deviceService.getAllTypes();
		updateLocalDevices(request, devices);
		ModelAndView modelAndView = new ModelAndView(new RedirectView("/device/setup", true));
		modelAndView.addObject("message", "Devices Saved");
		return modelAndView;
	}

	private void updateLocalDevices(HttpServletRequest request, List<Map<String, Object>> devices) {
		for (Map<String, Object> item : devices) {
			Object attribute = request.getParameter("devices." + (String) item.get("name"));
			Boolean enabled = attribute != null;
			item.put("enabled", enabled);
		}
		deviceService.updateTypes(devices);
	}

	@RequestMapping(value = "/device/setup/centralselect", method = RequestMethod.POST)
	public ModelAndView selectCentralDevices(HttpServletRequest request) {
		logger.info("updating available devices");
		List<Map<String, Object>> localDevices = deviceService.getAllTypes();
		List<Map<String, Object>> devices = deviceService.getAllDeviceCentral(localDevices);
		updateCentralDevices(request, devices);
		ModelAndView modelAndView = new ModelAndView(new RedirectView("/device/setup", true));
		modelAndView.addObject("message", "Devices Central Devices Downloaded");
		return modelAndView;
	}

	private void updateCentralDevices(HttpServletRequest request, List<Map<String, Object>> centralDevices) {
		for (Map<String, Object> item : centralDevices) {
			Object attribute = request.getParameter("devices." + (String) item.get("name"));
			Boolean enabled = attribute != null;
			item.put("enabled", enabled);
		}
		deviceService.downloadFromDeviceCentral(centralDevices);
	}
}
