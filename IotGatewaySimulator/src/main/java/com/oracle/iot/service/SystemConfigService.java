package com.oracle.iot.service;

import org.springframework.stereotype.Service;

@Service
public class SystemConfigService {

	private String host = "iotserver";//"localhost";
	private Integer port = 7102;
	private String configFileLocation = "/configs";
	private Boolean sendingMessages = false;
	private String password = "changeit";
	private String weblogicTrust = "C:/Program Files/Java/jdk1.8.0_91/jre/lib/security/cacerts";

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

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getWebLogicTrust() {
		return this.weblogicTrust ;
	}

	public void setWebLogicTrust(String weblogicTrust) {
		this.weblogicTrust = weblogicTrust;
	}
	
	
}
