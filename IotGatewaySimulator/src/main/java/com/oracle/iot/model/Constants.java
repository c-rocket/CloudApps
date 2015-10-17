package com.oracle.iot.model;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;

public final class Constants {

	public static String formatChartLabel(DateTime date) {
		return DateTimeFormat.forPattern("MM/dd/yy kk:mm:ss").print(date);
	}

	public static Map<String, List<Double>> copy(Map<String, List<Double>> map) {
		Map<String, List<Double>> copy = new LinkedHashMap<String, List<Double>>();
		for (String key : map.keySet()) {
			copy.put(key, Constants.clone(map.get(key)));
		}
		return copy;
	}

	private static List<Double> clone(List<Double> list) {
		List<Double> copy = new ArrayList<Double>();
		for (Double item : list) {
			copy.add(item);
		}
		return copy;
	}

	public static List<String> copyStringList(List<String> list) {
		List<String> copy = new ArrayList<String>();
		for (String item : list) {
			copy.add(item);
		}
		return copy;
	}

	public static List<IOTDevice> copyToList(Collection<IOTDevice> values) {
		List<IOTDevice> copy = new ArrayList<IOTDevice>();
		for (IOTDevice value : values) {
			copy.add(value.copy());
		}
		return copy;
	}

	public static double randomDouble(double min, double max, int scale) {
		Random r = new Random();
		return new BigDecimal(min + (max - min) * r.nextDouble()).setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue();
	}

	public static List<List<Double>> copyListofLists(List<List<Double>> values) {
		List<List<Double>> copy = new ArrayList<List<Double>>();
		for (List<Double> value : values) {
			copy.add(Constants.copyDoubleList(value));
		}
		return copy;
	}

	private static List<Double> copyDoubleList(List<Double> values) {
		List<Double> copy = new ArrayList<Double>();
		for (Double value : values) {
			copy.add(value);
		}
		return copy;
	}

	public static Double RandomTenPercent(Double value) {
		Double plusTen = value * 1.1;
		Double minusTen = value * 0.9;
		return randomDouble(minusTen, plusTen, 2);
	}

	public static Double doubleOrNull(String str) {
		if (str != null && str.length() > 0) {
			return Double.valueOf(str);
		}
		return null;
	}

}
