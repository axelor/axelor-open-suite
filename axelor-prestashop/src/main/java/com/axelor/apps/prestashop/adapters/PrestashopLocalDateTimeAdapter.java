package com.axelor.apps.prestashop.adapters;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import javax.xml.bind.annotation.adapters.XmlAdapter;

import com.axelor.common.StringUtils;

public class PrestashopLocalDateTimeAdapter extends XmlAdapter<String, LocalDateTime> {
	private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

	@Override
	public LocalDateTime unmarshal(String v) throws Exception {
		return StringUtils.isEmpty(v)? null : LocalDateTime.parse(v, formatter);
	}

	@Override
	public String marshal(LocalDateTime v) throws Exception {
		return (v == null) ? null : formatter.format(v);
	}

}
