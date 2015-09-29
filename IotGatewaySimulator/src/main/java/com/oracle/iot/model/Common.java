package com.oracle.iot.model;

public final class Common {
	public static boolean isNullOrEmpty(String id) {
		return id == null || id.length() == 0;
	}

	public static boolean isNull(Object obj) {
		return obj == null;
	}
}
