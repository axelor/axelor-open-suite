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
	
	@SuppressWarnings("unchecked")
	public Map<String,Object> validate(String wsdlUrl, String search) {
		return (Map<String, Object>) ads.doSearch(wsdlUrl, search);
	}
	
	public Address select(String wsdlUrl, String moniker) {
		return (Address) ads.doGetAddress(wsdlUrl, moniker);
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
