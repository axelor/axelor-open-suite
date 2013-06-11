package com.axelor.apps.tool;

import java.util.regex.Pattern;

public class EmailTool {
	public static boolean isValidEmailAddress(String email) {
	    final String EMAIL_PATTERN = "[_A-Za-z0-9-]+(\\.[_A-Za-z0-9-]+)*@[A-Za-z0-9]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})";
		Pattern pattern = Pattern.compile(EMAIL_PATTERN);
		return pattern.matcher(email).matches();
	}
}
