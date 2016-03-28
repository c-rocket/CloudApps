package com.oracle.iot.model;

import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;
import java.security.SignatureException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.Map;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.apache.log4j.Logger;

import com.oracle.iot.client.DirectActivationRequest;
import com.oracle.iot.message.Base64;

public class IOTConnection {

	private Logger log = Logger.getLogger(IOTConnection.class);

	public static final Charset UTF_8 = Charset.forName("UTF-8");
	public static final String DEFAULT_MESSAGE_DIGEST_ALGORITHM = "HmacSHA256";
	public static final long EXP_CLAIM_DELTA = 15L * 1000L * 60L; // 15 minutes

	private AccessToken token;
	private String password;
	private String username;
	private String server;
	private Integer port;

	private String deviceId;
	private String secret;

	private String symmetricKey;
	private byte[] credentialsPostData;
	private byte[] privateKey;

	public IOTConnection(String username, String password, String server, Integer port) {
		this.username = username;
		this.password = password;
		this.server = server;
		this.port = port;
	}

	public void addDevice(String deviceId, String secret) {
		this.deviceId = deviceId;
		this.secret = secret;

		this.symmetricKey = createSymmetricKey(deviceId, secret);
		this.credentialsPostData = getCredentialsPostData(deviceId, symmetricKey);
	}

	private byte[] getCredentialsPostData(String endpointId, String symmetricKey) {
		log.info("Using client credentials flow");

		StringBuilder builder = new StringBuilder();
		builder.append("grant_type=client_credentials&client_id=");
		builder.append(endpointId);
		builder.append("&client_secret=");
		builder.append(symmetricKey);
		builder.append("&scope=oracle/iot/activation");
		String dataString = builder.toString();
		log.info("client_credentials: " + dataString);

		byte[] data = dataString.getBytes(UTF_8);
		return data;
	}

	private String createSymmetricKey(String endpointId, String sharedSecret) {
		String key = null;
		try {
			Charset charset = UTF_8;
			byte[] content = (endpointId + "\n" + sharedSecret).getBytes(charset);
			SecretKeySpec keySpec = new SecretKeySpec(sharedSecret.getBytes(charset), DEFAULT_MESSAGE_DIGEST_ALGORITHM);
			Mac mac = Mac.getInstance(DEFAULT_MESSAGE_DIGEST_ALGORITHM);
			mac.init(keySpec);
			mac.update(content);
			final byte[] digest = mac.doFinal();
			final String urlEncodedDigest = Base64.getUrlEncoder().encodeToString(digest);
			key = DEFAULT_MESSAGE_DIGEST_ALGORITHM + ":" + urlEncodedDigest;
		} catch (NoSuchAlgorithmException e) {
			log.info(e.toString());
		} catch (InvalidKeyException e) {
			log.info(e.toString());
		}
		return key;
	}

	public String getPassword() {
		return password;
	}

	public String getUsername() {
		return username;
	}

	public String getServer() {
		return server;
	}

	public Integer getPort() {
		return port;
	}

	public String getDeviceId() {
		return deviceId;
	}

	public String getSecret() {
		return secret;
	}

	public String getSymmetricKey() {
		return symmetricKey;
	}

	public byte[] getCredentialsPostData() {
		return credentialsPostData;
	}

	public void setToken(AccessToken token) {
		this.token = new AccessToken(token.getExpires(), token.getTokenType(), token.getToken());
	}

	public String getTokenType() {
		if (token != null) {
			return token.getTokenType();
		}
		return null;
	}

	public String getToken() {
		if (token != null) {
			return token.getToken();
		}
		return null;
	}

	public void setPrivateKey(byte[] privateKey) {
		this.privateKey = privateKey;
	}

	public void setClientCredentials(byte[] encodedKey) {
		PrivateKey privateKey = null;

		if (encodedKey != null && (privateKey = generatePrivateKey(encodedKey)) != null) {
			credentialsPostData = getAssertionCredentialsPostData(this.deviceId, privateKey);
			this.token = null;
		}
	}

	private byte[] getAssertionCredentialsPostData(String deviceId, PrivateKey privateKey) {
		log.info("Using client assertion flow");

		StringBuilder postData = new StringBuilder();
		postData.append("grant_type=client_credentials");
		postData.append("&client_assertion_type=urn%3Aietf%3Aparams%3Aoauth%3Aclient-assertion-type%3Ajwt-bearer"); // already
																													// url-encoded
		postData.append("&client_assertion=" + buildClientAssertion(privateKey));
		postData.append("&scope=");
		String dataString = postData.toString();
		log.info("client_credentials: " + dataString);
		return dataString.getBytes(UTF_8);
	}

	private String buildClientAssertion(PrivateKey privateKey) {
		final long exp = (System.currentTimeMillis() + EXP_CLAIM_DELTA) / 1000L;

		final String header = "{\"typ\":\"JWT\",\"alg\":\"RS256\"}";
		final String claims = "{\"iss\":\"" + this.deviceId + "\"" + ", \"sub\":\"" + this.deviceId + "\""
				+ ", \"aud\":\"oracle/iot/oauth2/token\"" + ", \"exp\":" + exp + "}";

		try {
			StringBuilder inputToSign = new StringBuilder();

			inputToSign.append(Base64.getUrlEncoder().encodeToString(header.getBytes(UTF_8)));
			inputToSign.append(".");
			inputToSign.append(Base64.getUrlEncoder().encodeToString(claims.getBytes(UTF_8)));

			byte[] bytesToSign = inputToSign.toString().getBytes(UTF_8);
			byte[] signedBytes = signSignature(bytesToSign, "SHA256withRSA", privateKey);
			String signature = Base64.getUrlEncoder().encodeToString(signedBytes);

			inputToSign.append(".");
			inputToSign.append(signature);
			return inputToSign.toString();

		} catch (NoSuchAlgorithmException e) {
			log.info(e.toString());
		} catch (InvalidKeyException e) {
			log.info(e.toString());
		} catch (SignatureException e) {
			log.info(e.toString());
		}
		return null;
	}

	private static byte[] signSignature(final byte[] signaturePayload, final String signatureAlgorithm,
			PrivateKey privateKey) throws NoSuchAlgorithmException, InvalidKeyException, SignatureException {

		final Signature signature = Signature.getInstance(signatureAlgorithm);
		signature.initSign(privateKey);
		signature.update(signaturePayload);
		return signature.sign();
	}

	private PrivateKey generatePrivateKey(byte[] encoded) {
		PrivateKey privateKey = null;
		try {
			KeySpec keySpec = new PKCS8EncodedKeySpec(encoded);
			KeyFactory keyFactory = KeyFactory.getInstance("RSA");
			privateKey = keyFactory.generatePrivate(keySpec);
		} catch (Exception e) {
			log.error(e.toString());
		}
		return privateKey;
	}

	public byte[] getPrivateKey() {
		return this.privateKey;
	}

	public void signRequest(final DirectActivationRequest directActivationRequest, final String sharedSecret,
			final KeyPair keyPair, final String signatureAlgorithm)
					throws NoSuchAlgorithmException, InvalidKeyException, SignatureException, InvalidKeySpecException {

		DirectActivationRequest.CertificationRequestInfo certificationRequestInfo = directActivationRequest
				.getCertificationRequestInfo();
		DirectActivationRequest.SubjectPublicKeyInfo subjectPublicKeyInfo = certificationRequestInfo
				.getSubjectPublicKeyInfo();

		PublicKey publicKey = keyPair.getPublic();
		subjectPublicKeyInfo.setAlgorithm(publicKey.getAlgorithm());
		subjectPublicKeyInfo.setPublicKey(publicKey.getEncoded());
		subjectPublicKeyInfo.setFormat(publicKey.getFormat());
		subjectPublicKeyInfo.setSecretHashAlgorithm(IOTConnection.DEFAULT_MESSAGE_DIGEST_ALGORITHM);

		byte[] secretHash;
		try {
			secretHash = createDigest(subjectPublicKeyInfo.getSecretHashAlgorithm(),
					certificationRequestInfo.getSubject(), sharedSecret);
		} catch (UnsupportedEncodingException uee) {
			throw new SignatureException(uee);
		}

		byte[] signature = signSignature(getSignaturePayload(certificationRequestInfo, secretHash), signatureAlgorithm,
				keyPair.getPrivate());

		directActivationRequest.setSignatureAlgorithm(signatureAlgorithm);
		directActivationRequest.setSignature(signature);
	}

	private byte[] getSignaturePayload(DirectActivationRequest.CertificationRequestInfo requestInfo,
			byte[] secretHash) {
		DirectActivationRequest.SubjectPublicKeyInfo subjectPublicKeyInfo = requestInfo.getSubjectPublicKeyInfo();
		String payload = requestInfo.getSubject() + "\n" + subjectPublicKeyInfo.getAlgorithm() + "\n"
				+ subjectPublicKeyInfo.getFormat() + "\n" + subjectPublicKeyInfo.getSecretHashAlgorithm() + "\n";
		Map<String, Object> attributes = requestInfo.getAttributes();
		if (attributes != null) {
			for (String attributeKey : attributes.keySet()) {
				String attributeValue = attributes.get(attributeKey).toString();
				if (attributeValue != null)
					attributeValue = "\'" + attributeValue + "\'";
				else
					attributeValue = "null";
				payload += (attributeKey + "=" + attributeValue + "\n");
			}
		}

		byte[] payloadBytes = payload.getBytes(IOTConnection.UTF_8);
		byte[] signatureBytes = new byte[payloadBytes.length + secretHash.length
				+ subjectPublicKeyInfo.getPublicKey().length];
		System.arraycopy(payloadBytes, 0, signatureBytes, 0, payloadBytes.length);
		System.arraycopy(secretHash, 0, signatureBytes, payloadBytes.length, secretHash.length);
		System.arraycopy(subjectPublicKeyInfo.getPublicKey(), 0, signatureBytes,
				payloadBytes.length + secretHash.length, subjectPublicKeyInfo.getPublicKey().length);

		return signatureBytes;
	}

	private byte[] createDigest(String algorithm, String clientId, String sharedSecret)
			throws NoSuchAlgorithmException, InvalidKeyException, UnsupportedEncodingException {

		byte[] content = (clientId + "\n" + sharedSecret).getBytes(IOTConnection.UTF_8);
		byte[] digest;
		if (algorithm.startsWith("Hmac")) {
			// computes RFC 2104-compliant HMAC signature.
			SecretKeySpec key = new SecretKeySpec(sharedSecret.getBytes(IOTConnection.UTF_8), algorithm);
			Mac md = Mac.getInstance(algorithm);
			md.init(key);
			md.update(content);
			digest = md.doFinal();
		} else {
			MessageDigest md = MessageDigest.getInstance(algorithm);
			md.update(content);
			digest = md.digest();
		}
		return digest;
	}
}
