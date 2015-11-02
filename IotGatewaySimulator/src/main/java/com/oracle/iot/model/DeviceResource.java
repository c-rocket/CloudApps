package com.oracle.iot.model;

import oracle.iot.client.device.Resource;
import oracle.iot.message.RequestMessageHandler;

public class DeviceResource {
	private Resource resource;
	private RequestMessageHandler handler;

	public DeviceResource(Resource resource, RequestMessageHandler handler) {
		super();
		this.resource = resource;
		this.handler = handler;
	}

	public Resource getResource() {
		return resource;
	}

	public RequestMessageHandler getHandler() {
		return handler;
	}

}
