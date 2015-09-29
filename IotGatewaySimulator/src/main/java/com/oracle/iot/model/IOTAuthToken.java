package com.oracle.iot.model;

public class IOTAuthToken {
	private String id;
	private String secret;
	
	public IOTAuthToken(String id, String secret) {
		this.id = id;
		this.secret = secret;
	}
	
	public String getAuthToken(){
		return null;
	}

	public String getId() {
		return this.id;
	}

	public String getSecret() {
		return secret;
	}
	
}
