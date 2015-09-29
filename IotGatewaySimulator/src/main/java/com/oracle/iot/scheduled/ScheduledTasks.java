package com.oracle.iot.scheduled;

import java.util.List;

import javax.annotation.Resource;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.oracle.iot.model.IOTDevice;
import com.oracle.iot.service.DeviceService;
import com.oracle.iot.service.MessagingService;
import com.oracle.iot.service.SystemConfigService;

@Component
public class ScheduledTasks {

	@Resource
	private MessagingService messageService;
	@Resource
	private SystemConfigService systemConfigService;
	@Resource
	private DeviceService deviceService;

	// execute 5 seconds after the last message was sent
	@Scheduled(fixedDelay = 1000)
	public void reportCurrentTime() {
		// check if sending messages is turned on
		List<IOTDevice> allDevices = deviceService.findAll();
		if (allDevices != null && allDevices.size() > 0) {
			messageService.sendMessages(allDevices, systemConfigService.getHost(), systemConfigService.getPort(),
					systemConfigService.getMessageStatus());
			deviceService.updateAll(allDevices);
		}
	}
}
