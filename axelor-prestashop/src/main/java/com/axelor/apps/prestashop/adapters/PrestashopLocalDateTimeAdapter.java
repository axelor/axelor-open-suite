package com.axelor.apps.prestashop.adapters;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import javax.xml.bind.annotation.adapters.XmlAdapter;

import com.axelor.common.StringUtils;

public class PrestashopLocalDateTimeAdapter extends XmlAdapter<String, LocalDateTime> {
	private final static String NULL_DATETIME = "0000-00-00 00:00:00";
	private final static DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

	@Override
	public LocalDateTime unmarshal(String v) throws Exception {
		return StringUtils.isEmpty(v) || NULL_DATETIME.equals(v) ? null : LocalDateTime.parse(v, formatter);
	}

	@Override
	public String marshal(LocalDateTime v) throws Exception {
		return v == null ? NULL_DATETIME : formatter.format(v);
	}

}
