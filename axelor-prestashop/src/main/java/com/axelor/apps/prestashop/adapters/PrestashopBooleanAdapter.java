package com.axelor.apps.prestashop.adapters;

import javax.xml.bind.annotation.adapters.XmlAdapter;

/**
 * Adapter for brainfucked Prestashop boolean rendering
 *
 */
public class PrestashopBooleanAdapter extends XmlAdapter<Integer, Boolean> {

	@Override
	public Boolean unmarshal(Integer v) throws Exception {
		return v == null ? null : v != 0;
	}

	@Override
	public Integer marshal(Boolean v) throws Exception {
		return v == null ? null : (v ? 1 : 0);
	}

}
