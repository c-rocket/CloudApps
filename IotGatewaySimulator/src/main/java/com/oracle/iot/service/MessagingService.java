package com.oracle.iot.service;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.util.Arrays;
import java.util.List;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Service;

import com.oracle.iot.model.IOTDevice;

import oracle.iot.client.ClientException;
import oracle.iot.client.device.async.AsyncDeviceClient;
import oracle.iot.message.DataMessage;

@Service
public class MessagingService {
	Logger log = Logger.getLogger(MessagingService.class);

	public void sendMessages(List<IOTDevice> devices, String iotcsServer, Integer iotcsPort, Boolean sendMessages) {
		for (IOTDevice device : devices) {
			DataMessage message = device.createMessage();
			if (sendMessages) {
				try {
					final AsyncDeviceClient DEVICE_CLIENT = getDeviceClientConnection(iotcsServer, iotcsPort, device);
					DEVICE_CLIENT.sendMessage(message);

				} catch (final IllegalStateException EXCEPTION) {
					// Just means the device was already activated
					System.err.println(EXCEPTION.toString() + "\nNo private key.");
					System.exit(-1);
				} catch (final Exception EXCEPTION) {
					System.err.println(EXCEPTION.getMessage());
					EXCEPTION.printStackTrace();
					System.exit(-1);
				}
			}
		}
	}

	private AsyncDeviceClient getDeviceClientConnection(String iotcsServer, Integer iotcsPort, IOTDevice device)
			throws ClientException {
		System.setProperty("com.oracle.iot.client.server.cn", iotcsServer);
		final AsyncDeviceClient DEVICE_CLIENT = new AsyncDeviceClient(iotcsServer, iotcsPort, device.getId());
		byte[] privateKey = getPrivateKey(device);
		boolean activated = (privateKey != null);
		if (!activated) {
			try {
				privateKey = DEVICE_CLIENT.activate(device.getSecret());
				savePrivateKey(device, privateKey);
			} catch (final IllegalStateException EXCEPTION) {
				System.err.println("The device has already been activated, but there is no private key");
				System.err.println("Enroll a new device and try again.");
				System.exit(-1);
			}
		} else {
			// Authenticate with, and connect to, the server
			System.out.println("\nConnecting with client-assertion...");
			DEVICE_CLIENT.authenticate(privateKey);
		}
		return DEVICE_CLIENT;
	}

	private byte[] getPrivateKey(IOTDevice device) {
		InputStream is = null;
		ClassLoader classLoader = getClass().getClassLoader();
		URL resource = classLoader.getResource("configs/" + device.getId() + ".bin");
		File file = (resource != null) ? new File(resource.getFile()) : null;

		try {
			if (file != null && file.exists()) {
				is = new FileInputStream(file);
				byte[] buf = new byte[1024];
				int count = 0;
				int c = -1;
				while ((c = is.read()) != -1) {
					buf[count++] = (byte) c;
					if (count == buf.length) {
						buf = Arrays.copyOf(buf, buf.length + 256);
					}
				}
				System.out.println("\nPrivate key loaded...");
				return Arrays.copyOf(buf, count);
			} else {
				System.out.println("\nPrivate key file does not exist...");
			}
		} catch (IOException ioe) {
			System.err.println(ioe.getMessage());
		} finally {
			if (is != null) {
				try {
					is.close();
				} catch (IOException e) {
				} finally {
					is = null;
				}
			}
		}
		return null;
	}

	private void savePrivateKey(IOTDevice device, final byte[] PRIVATE_KEY) {
		OutputStream os = null;
		try {
			ClassLoader classLoader = getClass().getClassLoader();
			File file = new File(classLoader.getResource("configs/" + device.getId() + ".bin").getFile());

			if (!file.exists()) {
				file.createNewFile();
				os = new FileOutputStream(file);
				os.write(PRIVATE_KEY);
			}
		} catch (IOException e) {
			System.err.println("could not save private key data: " + e.toString());
		} finally {
			if (os != null) {
				try {
					os.close();
				} catch (IOException ioe) {
				}
			}
		}
	}

	public Boolean sendAlert(IOTDevice device, String alert, String iotcsServer, Integer iotcsPort) {
		try {
			final AsyncDeviceClient DEVICE_CLIENT = getDeviceClientConnection(iotcsServer, iotcsPort, device);
			DEVICE_CLIENT.sendMessage(device.createAlertMessage(alert));
			return true;
		} catch (final IllegalStateException EXCEPTION) {
			// Just means the device was already activated
			System.err.println(EXCEPTION.toString() + "\nNo private key.");
			System.exit(-1);
		} catch (final Exception EXCEPTION) {
			System.err.println(EXCEPTION.getMessage());
			EXCEPTION.printStackTrace();
			System.exit(-1);
		}
		return false;
	}
}
