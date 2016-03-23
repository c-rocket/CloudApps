package com.oracle.iot.service;

import org.springframework.stereotype.Service;

@Service
public class SystemConfigService {

	private String host = "localhost";
	private Integer port = 7101;
	private String configFileLocation = "/configs";
	private Boolean sendingMessages = false;
	private String username = "iot";
	private String password = "welcome1";

	public String getHost() {
		return host;
	}

	public Integer getPort() {
		return port;
	}

	public Boolean getMessageStatus() {
		return sendingMessages;
	}

	public Boolean setHost(String host) {
		this.host = host;
		return true;
	}

	public Boolean setPort(Integer port) {
		this.port = port;
		return true;
	}

	public Boolean setMessageStatus(Boolean sendMessages) {
		this.sendingMessages = sendMessages;
		return true;
	}

	public String getConfigFileLocation() {
		return configFileLocation;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}
	
	
}
