package com.axelor.apps.prestashop.adapters;

import java.time.LocalDate;

import javax.xml.bind.annotation.adapters.XmlAdapter;

public class PrestashopLocalDateAdapter extends XmlAdapter<String, LocalDate> {
	private static final String NULL_DATE = "0000-00-00";

	@Override
	public LocalDate unmarshal(String v) throws Exception {
		return v == null || NULL_DATE.equals(v) ? null : LocalDate.parse(v);
	}

	@Override
	public String marshal(LocalDate v) throws Exception {
		return v == null ? NULL_DATE : v.toString();
	}

}
