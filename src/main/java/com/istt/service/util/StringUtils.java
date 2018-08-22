package com.istt.service.util;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class StringUtils extends org.apache.commons.lang3.StringUtils {
	public static String convertDateToString(Date date, String format) throws ParseException {
		String sDate = "";
		if (date != null) {

			DateFormat parser = new SimpleDateFormat(format);
			sDate = parser.format(date);

		}
		return sDate;
	}
	
	public static String getRequestMapping(String urlRequest) {
	    if (StringUtils.isNotBlank(urlRequest)) {
	      String[] slipPatch = urlRequest.trim().split("/");
	      if (slipPatch.length >= 1) {
	        return slipPatch[1];
	      }
	    }
	    return EMPTY;
	  }
}
