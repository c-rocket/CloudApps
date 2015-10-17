package com.oracle.iot.model;

@Deprecated
public enum DeviceType {

	// HVAC2("HVAC2") {
	// public IOTDevice getDevice(String id, String secret) {
	// return new HVAC(id, secret);
	// }
	// },
	// DRILL_SITE("Drill Site") {
	// public IOTDevice getDevice(String id, String secret) {
	// return new DrillSite(id, secret);
	// }
	// },
	// CABLE_MODEM("Cable Modem") {
	// public IOTDevice getDevice(String id, String secret) {
	// return new CableModem(id, secret);
	// }
	// },
	// FLEET_TRUCK("Shipping Truck") {
	// public IOTDevice getDevice(String id, String secret) {
	// return new FleetTruck(id, secret);
	// }
	// },
	// PIPELINE("Pipeline") {
	// public IOTDevice getDevice(String id, String secret) {
	// return new Pipeline(id, secret);
	// }
	// };
	Generic("Generic");

	private String display;

	DeviceType(String display) {
		this.display = display;
	}

	public static DeviceType findByName(String name) {
		if (!Common.isNullOrEmpty(name)) {
			for (DeviceType deviceType : DeviceType.values()) {
				if (deviceType.name().equalsIgnoreCase(name)) {
					return deviceType;
				}
			}
		}
		return null;
	}

	public IOTDevice getDevice(String id, String secret) {
		return null;
	}

	public String getDisplay() {
		return display;
	}
}
