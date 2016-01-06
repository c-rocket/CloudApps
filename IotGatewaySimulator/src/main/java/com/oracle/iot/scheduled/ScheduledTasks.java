package com.oracle.iot.scheduled;

import javax.annotation.Resource;

import org.apache.log4j.Logger;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.oracle.iot.model.IOTDevice;
import com.oracle.iot.service.DeviceService;
import com.oracle.iot.service.MessagingService;
import com.oracle.iot.service.SystemConfigService;

import oracle.iot.client.device.async.MessageReceipt;
import oracle.iot.client.device.async.MessageReceipt.Status;

@Component
public class ScheduledTasks {

	private final Logger log = Logger.getLogger(ScheduledTasks.class);

	@Resource
	private MessagingService messageService;
	@Resource
	private SystemConfigService systemConfigService;
	@Resource
	private DeviceService deviceService;

	private MessageReceipt previousMessage = null;
	private Integer waitFor = 0;

	private static final Integer MAX_WAIT_RETRIES = 25; // 25 * 2 seconds = wait
														// for 50 seconds

	// execute 5 seconds after the last message was sent
	@Scheduled(fixedDelay = 2000)
	public void reportCurrentTime() {
		// check if sending messages is turned on
		IOTDevice currentDevice = deviceService.getCurrentDevice();
		Boolean sendingMessages = systemConfigService.getMessageStatus();
		try {
			if (currentDevice != null && !waiting()) {
				previousMessage = messageService.sendMessages(currentDevice, systemConfigService.getHost(),
						systemConfigService.getPort(), sendingMessages);
				if (sendingMessages) {
					deviceService.updateDevice(currentDevice);
				}
				waitFor = 0;
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

	private void disableMessages() {
		if (systemConfigService.getMessageStatus()) {
			log.error("Error detected, turning off messages!");
			systemConfigService.setMessageStatus(false);
		}
	}

	private boolean waiting() {
		if (waitFor >= MAX_WAIT_RETRIES) {
			throw new RuntimeException("I've been waiting forever!");
		}
		if (previousMessage != null) {
			waitFor++;
			log.info("Previous Message: " + previousMessage.getStatus().name());
			return !previousMessage.getStatus().equals(Status.SUCCESS);
		}
		return false;
	}
}
