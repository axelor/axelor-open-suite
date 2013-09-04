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
package com.axelor.apps.tool.address;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import javax.xml.namespace.QName;
import javax.xml.ws.Service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.axelor.apps.tool.address.AddressService;

import com.qas.web_2005_02.Address;
import com.qas.web_2005_02.EngineEnumType;
import com.qas.web_2005_02.EngineType;
import com.qas.web_2005_02.PromptSetType;
import com.qas.web_2005_02.QACanSearch;
import com.qas.web_2005_02.QAData;
import com.qas.web_2005_02.QADataSet;
import com.qas.web_2005_02.QAGetAddress;
import com.qas.web_2005_02.QAPortType;
import com.qas.web_2005_02.QASearch;
import com.qas.web_2005_02.QASearchOk;
import com.qas.web_2005_02.QASearchResult;

public class AddressService {

	static private QName SERVICE_NAME = null;
	static private QName PORT_NAME = null;
	static private URL wsdlURL = null;
	static private Service service = null;
	static private QAPortType client = null;
	
	private static final Logger LOG = LoggerFactory.getLogger(AddressService.class);
	
	public void setService(String wsdlUrl) throws MalformedURLException {
		// TODO: inject this
		if (this.client == null) {
			this.SERVICE_NAME = new QName("http://www.qas.com/web-2005-02"
					,"ProWeb");

			this.PORT_NAME = new QName("http://www.qas.com/web-2005-02"
					,"QAPortType");

			//def wsdlURL = new URL("http://ip.axelor.com:2021/proweb.wsdl")
			this.wsdlURL = new URL(wsdlUrl);
			//println this.wsdlURL

			this.service = Service.create(this.wsdlURL, this.SERVICE_NAME);
			this.client = service.getPort(QAPortType.class);
			//QAPortType client = service.getPort(PORT_NAME, QAPortType.class)
			LOG.debug("setService  this.client = {}", this.client);

		}
	}
	
	public boolean doCanSearch(String wsdlUrl) {

		try {
			QName SERVICE_NAME = new QName("http://www.qas.com/web-2005-02"
					,"ProWeb");

			QName PORT_NAME = new QName("http://www.qas.com/web-2005-02"
					,"QAPortType");

			//def wsdlURL = new URL("http://ip.axelor.com:2021/proweb.wsdl")
			URL wsdlURL = new URL(wsdlUrl);

			Service service = Service.create(wsdlURL, SERVICE_NAME);
			QAPortType client = service.getPort(QAPortType.class);
			//QAPortType client = service.getPort(PORT_NAME, QAPortType.class)
			LOG.debug("setService  client = {}", client);

			// 1. Pre-check.

			QAData qadata = client.doGetData();
			QADataSet ds = qadata.getDataSet().get(0);

			QACanSearch canSearch = new QACanSearch();
			canSearch.setCountry("FRX");
			canSearch.setLayout("AFNOR INSEE");

			EngineType engType = new EngineType();
			engType.setFlatten(true);

			engType.setValue(EngineEnumType.VERIFICATION);
			canSearch.setEngine(engType);
			QASearchOk resp = client.doCanSearch(canSearch);

			return resp.isIsOk();
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}
	
	public Map<String,Object> doSearch(String wsdlUrl, String searchString) {

		try {
			this.setService(wsdlUrl);
			// 2. Initial search.
			QASearch search = new QASearch();
			search.setCountry("FRX");
			search.setLayout("AFNOR INSEE");
			search.setSearch(searchString);

			EngineType engTypeT = new EngineType();
			engTypeT.setPromptSet(PromptSetType.ONE_LINE); //DEFAULT
			engTypeT.setValue(EngineEnumType.VERIFICATION);
			engTypeT.setFlatten(true);
			search.setEngine(engTypeT);

			QASearchResult respSearch = this.client.doSearch(search);

			Map<String,Object> mapSearch = new HashMap<String,Object>();
			mapSearch.put("verifyLevel", respSearch.getVerifyLevel());
			mapSearch.put("qaPicklist", respSearch.getQAPicklist());
			mapSearch.put("qaAddress", respSearch.getQAAddress());
			return mapSearch;
		} catch (Exception e) {
			e.printStackTrace();
			return new HashMap<String,Object>();
		}
	}

	public Address doGetAddress(String wsdlUrl, String moniker) {
		try {
			this.setService(wsdlUrl);
			
			// 4. Format the final address.
			QAGetAddress getAddress = new QAGetAddress();

			getAddress.setMoniker(moniker);
			getAddress.setLayout("AFNOR INSEE");

			Address formattedAddress = this.client.doGetAddress(getAddress);
			
			return formattedAddress;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
}
