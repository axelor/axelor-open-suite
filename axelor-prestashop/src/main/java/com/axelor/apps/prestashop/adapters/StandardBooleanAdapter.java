package com.axelor.apps.prestashop.adapters;

import javax.xml.bind.annotation.adapters.XmlAdapter;

/**
 * And sometimes, Prestashop marshals boolean like the rest of the world…
 *
 */
public class StandardBooleanAdapter extends XmlAdapter<String, Boolean> {
	@Override
	public Boolean unmarshal(String v) throws Exception {
		return v == null ? null : "true".equals(v);
	}

	@Override
	public String marshal(Boolean v) throws Exception {
		return v == null ? null : (v ? "true" : "false");
	}
}
