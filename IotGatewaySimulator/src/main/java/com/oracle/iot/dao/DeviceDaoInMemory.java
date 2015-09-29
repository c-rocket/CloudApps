package com.oracle.iot.dao;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Repository;

import com.oracle.iot.model.Constants;
import com.oracle.iot.model.DeviceType;
import com.oracle.iot.model.IOTDevice;
import com.oracle.iot.model.devices.CableModem;
import com.oracle.iot.model.devices.DrillSite;
import com.oracle.iot.model.devices.FleetTruck;
import com.oracle.iot.model.devices.HVAC;
import com.oracle.iot.model.devices.Pipeline;

@Repository
public class DeviceDaoInMemory {
	final Map<String, IOTDevice> devices = new HashMap<String, IOTDevice>();

	private void bootstrapDevices() {
		for (int i = 1; i <= 1; i++) {
			String id = i + "-HvacTest";
			devices.put(id, new HVAC(id, "secret" + id));

			id = i + "-ModemTest";
			devices.put(id, new CableModem(id, "secret" + id));

			id = i + "-DrillSiteTest";
			devices.put(id, new DrillSite(id, "secret" + id));

			id = i + "-FleetTruckTest";
			devices.put(id, new FleetTruck(id, "secret" + id));

			id = i + "-PipelineTest";
			devices.put(id, new Pipeline(id, "secret" + id));
		}
	}

	public boolean exists(String id) {
		return devices.get(id) != null;
	}

	public boolean insert(DeviceType device, String id, String secret) {
		if (!exists(id)) {
			devices.put(id, device.getDevice(id, secret).copy());
			return true;
		}
		return false;

	}

	public List<IOTDevice> findAll() {
		if (devices.size() == 0) {
			bootstrapDevices();
		}
		List<IOTDevice> foundDevices = Constants.copyToList(devices.values());
		foundDevices.sort(new Comparator<IOTDevice>() {

			@Override
			public int compare(IOTDevice o1, IOTDevice o2) {
				return o2.getCreateDate().compareTo(o1.getCreateDate());
			}
		});
		return foundDevices;
	}

	public Boolean delete(String id) {
		if (devices.get(id) == null)
			return false;
		devices.remove(id);
		return true;
	}

	public IOTDevice findById(String id) {
		return devices.get(id).copy();
	}

	public Boolean updateAll(List<IOTDevice> allDevices) {
		for (IOTDevice device : allDevices) {
			devices.put(device.getId(), device.copy());
		}
		return true;
	}

	public Boolean update(IOTDevice device) {
		devices.put(device.getId(), device.copy());
		return true;
	}
}
