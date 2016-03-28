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
	@Scheduled(fixedDelay = 5000)
	public void reportCurrentTime() {
		Boolean sendingMessages = systemConfigService.getMessageStatus();
		for (IOTDevice device : deviceService.getAll()) {
			try {
				if (device != null) {
					messageService.sendMessages(device, systemConfigService.getHost(), systemConfigService.getPort(),
							sendingMessages, systemConfigService.getUsername(), systemConfigService.getPassword());
					if (sendingMessages) {
						deviceService.updateDevice(device);
					}
				}
			} catch (final IllegalStateException ise) {
				log.error("The device has already been activated, but there is no private key", ise);
				log.error("Enroll a new device and try again.", ise);
				disableMessages();
			} catch (Exception e) {
				log.error("Error sending message", e);
				disableMessages();
			}
		}
	}

	private void disableMessages() {
		if (systemConfigService.getMessageStatus()) {
			log.error("Error detected, turning off messages!");
			systemConfigService.setMessageStatus(false);
		}
	}
}
