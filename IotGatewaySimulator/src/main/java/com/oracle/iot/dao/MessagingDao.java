package com.oracle.iot.dao;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URL;
import java.nio.charset.Charset;
import java.security.GeneralSecurityException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.codec.binary.Base64;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.http.converter.FormHttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.stereotype.Repository;
import org.springframework.web.client.RestTemplate;

import com.oracle.iot.client.ActivationPolicyRequest;
import com.oracle.iot.client.ActivationPolicyResponse;
import com.oracle.iot.client.DirectActivationRequest;
import com.oracle.iot.client.DirectActivationResponse;
import com.oracle.iot.client.HttpClient;
import com.oracle.iot.client.HttpClient.HttpResponse;
import com.oracle.iot.model.AccessToken;
import com.oracle.iot.model.IOTConnection;
import com.oracle.json.Json;
import com.oracle.json.JsonArray;
import com.oracle.json.JsonArrayBuilder;
import com.oracle.json.JsonObject;
import com.oracle.json.JsonReader;
import com.oracle.json.JsonStructure;

import oracle.iot.client.ClientException;
import oracle.iot.message.HttpRequestMessage;
import oracle.iot.message.Message;
import oracle.iot.message.MessageParsingException;

@Repository
public class MessagingDao {
	private org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(MessagingDao.class);

	// private final static String ENDPOINTS_URL = "/iot/api/v1/endpoints";
	private final static String TOKEN_URL = "/iot/api/v1/oauth2/token";
	private final static String POLICY_URL = "/iot/api/v1/activation/policy";
	private final static String ACTIVATION_URL = "/iot/api/v1/activation/direct";
	private final static String MESSAGES_URL = "/iot/api/v1/messages";

	private Map<String, IOTConnection> connectionMap = new LinkedHashMap<String, IOTConnection>();

	public byte[] activateDevice(String deviceId) throws IOException, GeneralSecurityException {
		IOTConnection connection = connectionMap.get(deviceId);
		if (connection != null) {
			ActivationPolicyResponse activationPolicyResponse = getActivationPolicy(connection);

			String algorithm = activationPolicyResponse.getKeyType();
			int keySize = activationPolicyResponse.getKeySize();
			String signatureAlgorithm = activationPolicyResponse.getHashAlgorithm();

			final KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance(algorithm);
			keyPairGenerator.initialize(keySize);
			KeyPair keyPair = keyPairGenerator.genKeyPair();

			DirectActivationRequest directActivationRequest = createDirectActivationRequest(connection, keyPair,
					signatureAlgorithm);
			log.info("directActivationRequest: " + directActivationRequest.toString());

			DirectActivationResponse directActivationResponse = postDirectActivationRequest(connection,
					directActivationRequest, deviceId);

			log.info("directActivationResponse: Endpoint state is: " + directActivationResponse.getEndpointState());

			return keyPair.getPrivate().getEncoded();
		}
		return null;
	}

	private DirectActivationResponse postDirectActivationRequest(IOTConnection connection,
			DirectActivationRequest directActivationRequest, String endpointId)
					throws IOException, GeneralSecurityException {

		String payloadString = directActivationRequest.toJson();
		byte[] payload = payloadString.getBytes(IOTConnection.UTF_8);

		// Send public key here...
		final Map<String, String> headers = new HashMap<String, String>();
		headers.put("X-ActivationId", endpointId);
		headers.put("Content-Type", "application/json");
		headers.put("Accept", "application/json");
		headers.put("Authorization", getAuthHeader(connection));

		HttpClient.HttpResponse response = post(ACTIVATION_URL, payload, headers, connection);
		int status = response.getStatus();
		if (status == 401) {
			throw new IllegalStateException("endpoint already activated");
		}
		if (status != 200) {
			throw new IOException("HTTP " + status);
		}

		JsonReader reader = null;
		try {
			ByteArrayInputStream is = new ByteArrayInputStream(response.getData());
			reader = Json.createReader(is);
			JsonObject json = reader.readObject();
			DirectActivationResponse directActivationResponse = DirectActivationResponse.fromJson(json);
			log.info(directActivationResponse.toString());
			return directActivationResponse;
		} finally {
			if (reader != null) {
				reader.close();
			}
		}

	}

	private String getAuthHeader(IOTConnection connection) {
		String auth = connection.getUsername() + ":" + connection.getPassword();
		byte[] encodedAuth = Base64.encodeBase64(auth.getBytes(Charset.forName("US-ASCII")));
		String authHeader = "Basic " + new String(encodedAuth);
		return authHeader;
	}

	private DirectActivationRequest createDirectActivationRequest(IOTConnection connection, KeyPair keyPair,
			String signatureAlgorithm) {
		final DirectActivationRequest.SubjectPublicKeyInfo subjectPublicKeyInfo = new DirectActivationRequest.SubjectPublicKeyInfo();

		final DirectActivationRequest.CertificationRequestInfo certificationRequestInfo = new DirectActivationRequest.CertificationRequestInfo();
		certificationRequestInfo.setSubjectPublicKeyInfo(subjectPublicKeyInfo);
		certificationRequestInfo.setSubject(connection.getDeviceId());

		final DirectActivationRequest request = new DirectActivationRequest();
		request.setCertificationRequestInfo(certificationRequestInfo);

		try {
			connection.signRequest(request, connection.getSecret(), keyPair, signatureAlgorithm);
		} catch (Exception e) {
			log.info(e.toString());
		}

		return request;
	}

	private ActivationPolicyResponse getActivationPolicy(IOTConnection connection) throws IOException {
		Map<String, String> headers = new HashMap<String, String>();
		headers.put("X-ActivationId", connection.getDeviceId());
		headers.put("Content-Type", "application/json");
		headers.put("Accept", "application/json");
		headers.put("Authorization", getAuthHeader(connection));

		ActivationPolicyRequest policyRequest = createActivationPolicyRequest();
		String payloadString = policyRequest.toJson();
		byte[] payload = payloadString.getBytes(IOTConnection.UTF_8);

		final HttpClient.HttpResponse response = post(POLICY_URL, payload, headers, connection);
		int status = response.getStatus();
		if (status == 401) {
			throw new IllegalStateException(connection.getDeviceId());
		}
		if (status != 200) {
			throw new IOException("HTTP " + status);
		}

		JsonReader reader = null;
		try {
			ByteArrayInputStream is = new ByteArrayInputStream(response.getData());
			reader = Json.createReader(is);
			JsonObject json = reader.readObject();
			ActivationPolicyResponse activationPolicyResponse = ActivationPolicyResponse.fromJson(json);
			log.info(activationPolicyResponse.toString());
			return activationPolicyResponse;
		} finally {
			if (reader != null) {
				reader.close();
			}
		}
	}

	private HttpResponse post(String restApi, byte[] payload, Map<String, String> headers, IOTConnection connection)
			throws IOException {

		final URL url = new URL("https", connection.getServer(), connection.getPort(), restApi);
		String url2 = url.toExternalForm();

		if (connection.getToken() == null) {
			// connection.setToken(renewAccessToken(connection));
		}
		final Map<String, String> _headers = new HashMap<String, String>(headers);
		// _headers.put("Authorization", connection.getTokenType() + " " +
		// connection.getToken());
		_headers.put("Authorization", getAuthHeader(connection));
		
		getRestTemplate().postForObject(url2, payload, Map.class,headers);
		return null;

//		final HttpClient httpClient = new HttpClient(url);
//
//		try {
//			httpClient.wait((long) 30000);
//		} catch (InterruptedException e) {
//			log.error("Error setting timeout", e);
//		}
//		HttpClient.HttpResponse response = httpClient.post(payload, _headers);
//		log.debug("POST " + url.toExternalForm() + " reponse = " + response.getStatus());
//
//		if (response.getStatus() == 401) {
//			connection.setToken(renewAccessToken(connection));
//			_headers.put("Authorization", connection.getTokenType() + " " + connection.getToken());
//			response = httpClient.post(payload, _headers);
//		}

//		return response;
	}

	private AccessToken renewAccessToken(IOTConnection connection) throws IOException {
		URL url = new URL("https", connection.getServer(), connection.getPort(), TOKEN_URL);

		HashMap<String, String> headers = new HashMap<String, String>();
		headers.put("Content-Type", "application/x-www-form-urlencoded");
		headers.put("Accept", "application/json");
		headers.put("Authorization", getAuthHeader(connection));

		log.info("POST " + TOKEN_URL);

		HttpClient httpClient = new HttpClient(url);
		HttpClient.HttpResponse response = httpClient.post(connection.getCredentialsPostData(), headers);
		int status = response.getStatus();
		if (status != 200) {
			throw new IOException("HTTP " + status);
		}

		JsonReader reader = null;
		final byte[] data = response.getData();
		if (data == null || data.length == 0) {
			throw new IOException("empty payload");
		}

		try {
			ByteArrayInputStream is = new ByteArrayInputStream(data);
			reader = Json.createReader(is);
			JsonObject json = reader.readObject();
			AccessToken _accessToken = AccessToken.fromJSON(json);
			return _accessToken;
		} finally {
			if (reader != null) {
				reader.close();
			}
		}
	}

	private ActivationPolicyRequest createActivationPolicyRequest() {
		final ActivationPolicyRequest policyRequest = new ActivationPolicyRequest();
		policyRequest.setDeviceAttributes(new ActivationPolicyRequest.DeviceAttributes());

		final String osName = System.getProperty("os.name");
		policyRequest.getDeviceAttributes().setOsName(osName);

		final String osVersion = System.getProperty("os.version");
		policyRequest.getDeviceAttributes().setOsVersion(osVersion);

		return policyRequest;
	}

	public void savePrivateKey(String id, byte[] privateKey) {
		IOTConnection connection = connectionMap.get(id);
		if (connection != null) {
			connection.setPrivateKey(privateKey);
		}
	}

	public void deletePrivateKey(String id) {
		connectionMap.remove(id);
	}

	public void deleteAll() {
		connectionMap.clear();
	}

	// public AsyncDeviceClient getAsyncClient(String iotcsServer, Integer
	// iotcsPort, String id,
	// List<DeviceResource> resources) {
	// if (!exists(id)) {
	// this.client = new AsyncDeviceClient(iotcsServer, iotcsPort, id);
	// }
	// return this.client;
	// }

	public RestTemplate getRestTemplate() {
		HttpComponentsClientHttpRequestFactory factory = new HttpComponentsClientHttpRequestFactory();
        factory.setReadTimeout(20000);
        factory.setConnectTimeout(20000);
		
		RestTemplate restTemplate = new RestTemplate(factory);
		FormHttpMessageConverter formConverter = new FormHttpMessageConverter();
		formConverter.setCharset(Charset.forName("UTF8"));
		restTemplate.getMessageConverters().add(formConverter);
		restTemplate.getMessageConverters().add(new MappingJackson2HttpMessageConverter());
		// restTemplate.setRequestFactory(new
		// HttpComponentsClientHttpRequestFactory());
		return restTemplate;
	}

	public void createConnection(String server, Integer port, String username, String password, String deviceId,
			String secret) {
		IOTConnection connection = connectionMap.get(deviceId);
		if (connection == null && secret != null) {
			connection = new IOTConnection(username, password, server, port);
			connection.addDevice(deviceId, secret);
			connectionMap.put(deviceId, connection);
		}
	}

	public void sendMessage(String id, Message message) {
		IOTConnection connection = connectionMap.get(id);
		if (connection != null) {
			JsonArrayBuilder jsonArrayBuilder = Json.createArrayBuilder();

			jsonArrayBuilder.add(message.toJSON());

			JsonArray jsonArray = jsonArrayBuilder.build();
			if (jsonArray.isEmpty())
				return;

			try {
				log.info("sending message");
				post(jsonArray, connection);

			} catch (IOException e) {
				log.info(e);
			}
		}
	}

	private Boolean post(JsonArray jsonArray, IOTConnection connection) throws IOException {
		final byte[] payload = jsonArray.toString().getBytes(Charset.forName("UTF-8"));

		final Map<String, String> headers = new HashMap<String, String>();
		headers.put("Content-Type", "application/json");
		headers.put("Accept", "application/json");
		headers.put("X-EndpointId", connection.getDeviceId());
		headers.put("Authorization", getAuthHeader(connection));

		final HttpClient.HttpResponse response = post(MESSAGES_URL, payload, headers, connection);

		final int status = response.getStatus();

		if (status == 202) {
			JsonReader reader = null;
			byte[] data = response.getData();
			// if data.length == 2, then it is an empty json array and there are
			// no values in the message.
			if (data != null && data.length > 2) {
				try {
					ByteArrayInputStream is = new ByteArrayInputStream(data);
					reader = Json.createReader(is);
					JsonStructure jsonStructure = reader.read();
					List<Message> incomingMessages = Message.fromJSON(jsonStructure);
					for (Message msg : incomingMessages) {
						if (msg instanceof HttpRequestMessage) {
							log.debug(String.valueOf(msg.toJSON()));
						}
					}
				} catch (MessageParsingException e) {
					log.info(e.getMessage());
				} finally {
					if (reader != null) {
						reader.close();
					}
				}
			}
			return true;

		} else {
			log.info("POST " + MESSAGES_URL + ": received 'HTTP " + status + "'");
			log.debug(jsonArray.toString());
			return false;
		}
	}

	public void authenticate(byte[] privateKey, String deviceId) throws ClientException {
		IOTConnection connection = connectionMap.get(deviceId);
		try {
			connection.setClientCredentials(privateKey);
			connection.setToken(renewAccessToken(connection));

		} catch (Exception e) {
			throw new ClientException(e.getMessage(), e);
		}
	}

	public byte[] getPrivateKey(String id) {
		IOTConnection connection = connectionMap.get(id);
		if (connection != null) {
			return connection.getPrivateKey();
		}
		return null;
	}
}
