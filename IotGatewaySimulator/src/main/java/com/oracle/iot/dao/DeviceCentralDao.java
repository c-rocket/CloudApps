package com.oracle.iot.dao;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.converter.FormHttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.stereotype.Repository;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

@Repository
public class DeviceCentralDao {

	private static final Logger logger = Logger.getLogger(DeviceCentralDao.class);
	private String connectionURL = "http://localhost:8480/devicecentral";//"http://129.152.152.157/devicecentral";
	private String deviceNamesURL = connectionURL + "/devices/list";
	private String deviceUploadURL = connectionURL + "/device/save";
	private String deviceDeleteURL = connectionURL + "/device/delete";
	private String deviceDownloadURL = connectionURL + "/device/show";

	public List<Map<String, Object>> getDeviceNames() {
		try {
			return getRestTemplate().getForObject(deviceNamesURL, List.class);
		} catch (Exception e) {
			logger.error("Error getting names", e);
			return new ArrayList<Map<String, Object>>();
		}
	}

	public Boolean saveDevice(String name, String industry, String device, String image) {
		try {
			MultiValueMap<String, Object> params = new LinkedMultiValueMap<String, Object>();

			params.add("deviceName", name);
			params.add("industry", industry);
			params.add("deviceFile", device);
			params.add("imageFile", image);
			getRestTemplate().postForObject(deviceUploadURL, params, Map.class);
			return true;
		} catch (Exception e) {
			logger.error("Error Saving Device", e);
			return false;
		}

	}

	public Map<String, Object> downloadDevice(String name) {
		try {
			HttpHeaders httpHeaders = new HttpHeaders();
			httpHeaders.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));

			MultiValueMap<String, Object> params = new LinkedMultiValueMap<String, Object>();

			params.add("name", name);
			return getRestTemplate().postForObject(deviceDownloadURL, params, Map.class);
		} catch (Exception e) {
			logger.error("Cannot download devices", e);
			return new LinkedHashMap<>();
		}
	}

	public Boolean deleteByName(String name) {
		try {
			HttpHeaders httpHeaders = new HttpHeaders();
			httpHeaders.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));

			MultiValueMap<String, Object> params = new LinkedMultiValueMap<String, Object>();

			params.add("name", name);
			getRestTemplate().postForObject(deviceDeleteURL, params, Map.class);
			return true;
		} catch (Exception e) {
			logger.error("Cannot delete device", e);
			return false;
		}
	}

	public RestTemplate getRestTemplate() {
		RestTemplate restTemplate = new RestTemplate();
		FormHttpMessageConverter formConverter = new FormHttpMessageConverter();
		formConverter.setCharset(Charset.forName("UTF8"));
		restTemplate.getMessageConverters().add(formConverter);
		restTemplate.getMessageConverters().add(new MappingJackson2HttpMessageConverter());
		// restTemplate.setRequestFactory(new
		// HttpComponentsClientHttpRequestFactory());
		return restTemplate;
	}
}
