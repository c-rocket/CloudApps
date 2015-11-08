package com.oracle.iot.util;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;

import com.oracle.iot.model.IOTDevice;

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

	public static Double randomDoubleWithinVariation(Double value, Double variationPercent) {
		if (variationPercent.compareTo(0d) == 0) {
			return value;
		}
		Double plus = value * (1 + variationPercent);
		Double minus = value * (1 - variationPercent);
		return randomDouble(minus, plus, 2);
	}

	public static Double doubleOrNull(String str) {
		if (str != null && str.length() > 0) {
			return Double.valueOf(str);
		}
		return null;
	}

	public static Double scale(Double value, int scale) {
		if (value == null) {
			return null;
		}
		return new BigDecimal(value).setScale(scale, BigDecimal.ROUND_HALF_UP).doubleValue();
	}

	public static List<String> removeWhiteSpace(List<String> asList) {
		if (asList == null) {
			return null;
		}
		List<String> list = new ArrayList<String>();
		for (String item : asList) {
			if (item != null)
				list.add(item.replaceAll("\\s+", ""));
		}
		return list;
	}

	public static String removeWhiteSpace(String str) {
		if (str == null) {
			return null;
		}
		return str.replaceAll("\\s+", "");
	}

	public static boolean isWithinVariation(Double value, Double setValue, Double variationPercent) {
		if (variationPercent.compareTo(0d) == 0) {
			return value.compareTo(setValue) == 0;
		}
		Double plus = setValue * (1 + variationPercent);
		Double minus = setValue * (1 - variationPercent);
		if (setValue >= 0) {
			return value <= plus && value >= minus;
		} else {
			return value >= plus && value <= minus;
		}
	}

}
