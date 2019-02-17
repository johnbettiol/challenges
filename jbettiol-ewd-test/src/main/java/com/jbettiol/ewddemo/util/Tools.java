package com.jbettiol.ewddemo.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Tools {

	public static long parseFilesize(String in) {
		in = in.trim();
		in = in.replaceAll(",", ".");
		try {
			return Long.parseLong(in);
		} catch (NumberFormatException e) {
		}
		final Matcher m = Pattern.compile("([\\d.,]+)\\s*(\\w)").matcher(in);
		m.find();
		int scale = 1;
		switch (m.group(2).charAt(0)) {
		case 'G':
			scale *= 1024;
		case 'M':
			scale *= 1024;
		case 'K':
			scale *= 1024;
			break;
		default:
			throw new IllegalArgumentException();
		}
		return Math.round(Double.parseDouble(m.group(1)) * scale);
	}
}
