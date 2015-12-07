package com.oracle.iot.dao;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.codec.binary.Base64;
import org.apache.log4j.Logger;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import com.oracle.iot.model.OracleDocsFolder;
import com.oracle.iot.model.OracleDocsItem;

//Not worth the trouble right now, passwords would expire and the push of documents is very difficult to get working.
@Deprecated
@org.springframework.stereotype.Repository
public class DeviceDocsDao {

	private static Logger logger = Logger.getLogger(DeviceDocsDao.class);
	private String docsBase = "https://oradocs-corp.documents.us2.oraclecloud.com/documents/api/1.1/";
	private String devicesFolder = docsBase + "folders/FDF71AFEAEEEB40FEE29D244F6C3FF17C1177A968060";
	private String picturesFolder = docsBase + "folders/F4618313E7F78303D613A9F8F6C3FF17C1177A968060";
	private String username = "";
	private String password = "";

	private List<OracleDocsItem> docsDevices = new ArrayList<OracleDocsItem>();

	public List<OracleDocsItem> getAvailableDevices() {
		try {
			HttpEntity<String> request = buildGetAuthenticatedRequest();
			RestTemplate restTemplate = new RestTemplate();
			OracleDocsFolder folder = restTemplate
					.exchange(devicesFolder + "/items", HttpMethod.GET, request, OracleDocsFolder.class).getBody();

			for (OracleDocsItem item : folder.getItems()) {
				if (item.getName().endsWith(".properties")) {
					docsDevices.add(item);
				}
			}

		} catch (Exception e) {
			logger.error("Error getting devices", e);
		}
		return docsDevices;
	}

	public String downloadFile(OracleDocsItem item) {
		try {
			HttpEntity<String> request = buildGetAuthenticatedRequest();
			RestTemplate restTemplate = new RestTemplate();
			String file = restTemplate
					.exchange(docsBase + "files/" + item.getId() + "/data", HttpMethod.GET, request, String.class)
					.getBody();
			return file;
		} catch (Exception e) {
			logger.error("Error downloading device", e);
		}
		return null;
	}

	public String uploadDevice(String name, String device, String image) {
		try {
			HttpHeaders headers = new HttpHeaders();
			headers.setContentType(MediaType.MULTIPART_FORM_DATA);
			String plainCreds = username + ":" + password;
			byte[] plainCredsBytes = plainCreds.getBytes();
			byte[] base64CredsBytes = Base64.encodeBase64(plainCredsBytes);
			String base64Creds = new String(base64CredsBytes);
			headers.add("Authorization", "Basic " + base64Creds);

			// creating an HttpEntity for the JSON part
			HttpHeaders jsonHeader = new HttpHeaders();
			jsonHeader.setContentType(MediaType.APPLICATION_JSON);
			jsonHeader.add("name", "jsonInputParameters");
			jsonHeader.add("Authorization", "Basic " + base64Creds);
			MultiValueMap<String, Object> parentIdMap = new LinkedMultiValueMap<>();
			parentIdMap.add("parentID", "F4618313E7F78303D613A9F8F6C3FF17C1177A968060");
			HttpEntity<MultiValueMap<String, Object>> jsonHttpEntity = new HttpEntity<>(parentIdMap, jsonHeader);

			// creating an HttpEntity for the binary part
			HttpHeaders fileHeader = new HttpHeaders();
			fileHeader.add("filename", name + ".properties");
			fileHeader.add("name", "primaryFile");
			fileHeader.setContentType(MediaType.TEXT_PLAIN);
			fileHeader.add("Authorization", "Basic " + base64Creds);
			HttpEntity<String> filePart = new HttpEntity<>(device, fileHeader);

			MultiValueMap<String, Object> multipartRequest = new LinkedMultiValueMap<>();
			multipartRequest.add("parameters", jsonHttpEntity);
			multipartRequest.add("file", filePart);

			HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(multipartRequest, headers);

			ResponseEntity<String> exchange = new RestTemplate().exchange(docsBase + "files/data", HttpMethod.POST,
					requestEntity, String.class);
			String body = exchange.getBody();
			logger.error(body);
			return body;
		} catch (Exception e) {
			logger.error("Error pulling files", e);
		}
		return null;
	}

	public String downloadPicture(String name) {
		try {
			HttpEntity<String> request = buildGetAuthenticatedRequest();
			RestTemplate restTemplate = new RestTemplate();
			OracleDocsFolder folder = restTemplate
					.exchange(picturesFolder + "/items", HttpMethod.GET, request, OracleDocsFolder.class).getBody();
			String picture = null;
			for (OracleDocsItem item : folder.getItems()) {
				if (item.getName().equalsIgnoreCase(name)) {
					picture = restTemplate.exchange(docsBase + "files/" + item.getId() + "/data", HttpMethod.GET,
							request, String.class).getBody();
				}
			}
			return picture;

		} catch (Exception e) {
			logger.error("Error downloaded pictures", e);
		}
		return null;
	}

	private HttpEntity<String> buildGetAuthenticatedRequest() {
		String plainCreds = username + ":" + password;
		byte[] plainCredsBytes = plainCreds.getBytes();
		byte[] base64CredsBytes = Base64.encodeBase64(plainCredsBytes);
		String base64Creds = new String(base64CredsBytes);

		HttpHeaders headers = new HttpHeaders();
		headers.add("Authorization", "Basic " + base64Creds);
		HttpEntity<String> request = new HttpEntity<String>(headers);
		return request;
	}
}
