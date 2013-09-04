/**
 * Copyright (c) 2012-2013 Axelor. All Rights Reserved.
 *
 * The contents of this file are subject to the Common Public
 * Attribution License Version 1.0 (the “License”); you may not use
 * this file except in compliance with the License. You may obtain a
 * copy of the License at:
 *
 * http://license.axelor.com/.
 *
 * The License is based on the Mozilla Public License Version 1.1 but
 * Sections 14 and 15 have been added to cover use of software over a
 * computer network and provide for limited attribution for the
 * Original Developer. In addition, Exhibit A has been modified to be
 * consistent with Exhibit B.
 *
 * Software distributed under the License is distributed on an “AS IS”
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See
 * the License for the specific language governing rights and limitations
 * under the License.
 *
 * The Original Code is part of "Axelor Business Suite", developed by
 * Axelor exclusively.
 *
 * The Original Developer is the Initial Developer. The Initial Developer of
 * the Original Code is Axelor.
 *
 * All portions of the code written by Axelor are
 * Copyright (c) 2012-2013 Axelor. All Rights Reserved.
 */
package com.axelor.apps.base.web;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import au.com.bytecode.opencsv.CSVWriter;

import com.axelor.apps.base.db.Address;
import com.google.inject.Inject;

public class AddressService {

	@Inject
	private com.axelor.apps.tool.address.AddressService ads;
	
	private static final Logger LOG = LoggerFactory.getLogger(AddressService.class);
	
	public boolean check(String wsdlUrl) {
		return ads.doCanSearch(wsdlUrl);
	}
	
	public Map<String,Object> validate(String wsdlUrl, String search) {
		return (Map<String, Object>) ads.doSearch(wsdlUrl, search);
	}
	
	public com.qas.web_2005_02.Address select(String wsdlUrl, String moniker) {
		return ads.doGetAddress(wsdlUrl, moniker);
	}
	
	public int export(String path) throws IOException {
		List<Address> addresses = (List<Address>) Address.all().filter("self.certifiedOk IS FALSE").fetch();
		
		CSVWriter csv = new CSVWriter(new java.io.FileWriter(path), "|".charAt(0), CSVWriter.NO_QUOTE_CHARACTER);
		List<String> header = new ArrayList<String>();
		header.add("Id");
		header.add("AddressL1");
		header.add("AddressL2");
		header.add("AddressL3");
		header.add("AddressL4");
		header.add("AddressL5");
		header.add("AddressL6");
		header.add("CodeINSEE");
		
		csv.writeNext(header.toArray(new String[header.size()]));
		List<String> items = new ArrayList<String>();
		for (Address a : addresses) {
			
			items.add(a.getId() != null ? a.getId().toString(): "");
			items.add(a.getAddressL2() != null ? a.getAddressL2(): "");
			items.add(a.getAddressL3() != null ? a.getAddressL3(): "");
			items.add(a.getAddressL4() != null ? a.getAddressL4(): "");
			items.add(a.getAddressL5() != null ? a.getAddressL5(): "");
			items.add(a.getAddressL6() != null ? a.getAddressL6(): "");
			items.add(a.getInseeCode() != null ? a.getInseeCode(): "");
			
			csv.writeNext(items.toArray(new String[items.size()]));
			items.clear();
		}
		csv.close();
		LOG.info("{} exported", path);
		
		return addresses.size();
	}
}
