package com.oracle.iot.service;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.oracle.iot.dao.DevicePropertiesLoaderDao;

@Service
public class DevicePropertiesLoaderService {

	@Resource
	private DevicePropertiesLoaderDao dao;

	public Boolean load(MultipartFile multipartFile) {
		return dao.loadNewDevice(multipartFile);
	}

}
