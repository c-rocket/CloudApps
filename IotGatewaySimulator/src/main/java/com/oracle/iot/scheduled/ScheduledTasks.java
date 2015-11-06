package com.oracle.iot.scheduled;

import javax.annotation.Resource;

import org.apache.log4j.Logger;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.oracle.iot.model.IOTDevice;
import com.oracle.iot.service.DeviceService;
import com.oracle.iot.service.MessagingService;
import com.oracle.iot.service.SystemConfigService;

@Component
public class ScheduledTasks {

	private final Logger log = Logger.getLogger(ScheduledTasks.class);

	@Resource
	private MessagingService messageService;
	@Resource
	private SystemConfigService systemConfigService;
	@Resource
	private DeviceService deviceService;

	// execute 5 seconds after the last message was sent
	@Scheduled(fixedDelay = 2000)
	public void reportCurrentTime() {
		// check if sending messages is turned on
		IOTDevice currentDevice = deviceService.getCurrentDevice();
		if (currentDevice != null) {
			log.debug("Device: " + currentDevice.getId());
			boolean sent = messageService.sendMessages(currentDevice, systemConfigService.getHost(),
					systemConfigService.getPort(), systemConfigService.getMessageStatus());
			deviceService.updateDevice(currentDevice);
			// we only care about sending if the message status was supposed to
			// send
			if (!sent && systemConfigService.getMessageStatus()) {
				log.error("Error detected, turning off messages!");
				systemConfigService.setMessageStatus(false);
			}
		}
	}
}
