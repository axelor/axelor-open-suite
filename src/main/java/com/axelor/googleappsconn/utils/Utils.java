package com.axelor.googleappsconn.utils;

import java.util.Date;
import com.google.api.client.util.DateTime;

public class Utils {

	public static String getDateFormated(DateTime dateTime) throws Exception {
		if (dateTime == null)
			return "";
		Date date = new Date(dateTime.getValue());
		String fomratedDate = date.toString();
		return fomratedDate;
	}

}
