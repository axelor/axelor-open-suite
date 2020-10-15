/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2020 Axelor (<http://axelor.com>).
 *
 * This program is free software: you can redistribute it and/or  modify
 * it under the terms of the GNU Affero General Public License, version 3,
 * as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.qas.test;

import static org.junit.Assert.*

import javax.xml.namespace.QName
import javax.xml.ws.Service

import org.junit.Test

import com.qas.web_2005_02.Address
import com.qas.web_2005_02.EngineEnumType
import com.qas.web_2005_02.EngineType
import com.qas.web_2005_02.ProWeb
import com.qas.web_2005_02.PromptSetType
import com.qas.web_2005_02.QACanSearch
import com.qas.web_2005_02.QAData
import com.qas.web_2005_02.QADataSet
import com.qas.web_2005_02.QAGetAddress
import com.qas.web_2005_02.QAGetLayouts
import com.qas.web_2005_02.QALayouts
import com.qas.web_2005_02.QAPortType
import com.qas.web_2005_02.QASearch
import com.qas.web_2005_02.QASearchResult


class Client {
	//stubs generated with:
	//arye@dm4:~/projects/axelor/axelor-tool/src/main/java$ ~/opt/cxf/bin/wsdl2java  -client -frontend jaxws21 http://ip.axelor.com:2021/proweb.wsdl
	
	//http://cxf.apache.org/docs/how-do-i-develop-a-client.html
	
	@Test
	def void WSDL2JavaClient() {
		ProWeb service = new ProWeb()
		def serviceName = service.getServiceName()
		println serviceName
	}
	
	
	@Test
	def void JAXWSProxy() {
		QName SERVICE_NAME = new QName("http://www.qas.com/web-2005-02"
			,"ProWeb")

		QName PORT_NAME = new QName("http://www.qas.com/web-2005-02"
			,"QAPortType")

		def wsdlURL = new URL("http://ip.axelor.com:2021/proweb.wsdl")
		println wsdlURL

		Service service = Service.create(wsdlURL, SERVICE_NAME);
		QAPortType client = service.getPort(QAPortType.class);
		//QAPortType client = service.getPort(PORT_NAME, QAPortType.class)
		println client.dump()

		
		
		QAGetLayouts getLayouts = new QAGetLayouts()
		getLayouts.country = "FRX"
		
		QALayouts layouts = client.doGetLayouts(getLayouts)
		println "layouts= "+layouts.layout
		println layouts.layout*.name
		
		
				
		
		// 1. Pre-check.
		print "1. Pre-check."

		QAData qadata = client.doGetData()
		println qadata
		println qadata.dataSet
		QADataSet ds = qadata.dataSet[0]
		println ds.name
		println ds.id
		
		
		QACanSearch canSearch = new QACanSearch()
		canSearch.country = "FRX"
		canSearch.layout = "AFNOR INSEE" 
		
		EngineType engType = new EngineType()
		engType.setFlatten(true)
		
		engType.value = EngineEnumType.VERIFICATION 
		canSearch.engine = engType
		def resp = client.doCanSearch(canSearch)
		println resp.isOk 
        
		
		
		// 2. Initial search.
		QASearch search = new QASearch()
		search.country = "FRX"
		search.layout = "AFNOR INSEE" 
		//search.search = "55  de bercy 75012"  //qaPicklist=com.qas.web_2005_02.QAPicklistType@2dd59d3c qaAddress=null verifyLevel=MULTIPLE>
		//search.search = "55 rue de bercyi 75012"  //qaPicklist=null qaAddress=com.qas.web_2005_02.QAAddressType@25c7f37d verifyLevel=INTERACTION_REQUIRED>
		search.search = "110 rue PETIT 75019 paris" //qaPicklist=null qaAddress=com.qas.web_2005_02.QAAddressType@391da0 verifyLevel=VERIFIED>
		
		EngineType engTypeT = new EngineType()
		engTypeT.promptSet = PromptSetType.DEFAULT
		engTypeT.value = EngineEnumType.VERIFICATION
		search.engine = engTypeT

		search.engine = engTypeT
		QASearchResult respSearch = client.doSearch(search)
		println respSearch.dump()


		if (respSearch.qaAddress) {
			println respSearch.qaAddress.addressLine*.label
			println respSearch.qaAddress.addressLine*.line
			//println respSearch.qaAddress.addressLine*.lineContent
		}

		
		if (respSearch.qaPicklist) {
			println respSearch.qaPicklist?.total
			println respSearch.qaPicklist?.picklistEntry*.picklist
			println respSearch.qaPicklist?.picklistEntry*.postcode
			println respSearch.qaPicklist?.picklistEntry*.partialAddress
			println respSearch.qaPicklist?.picklistEntry*.score
			println respSearch.qaPicklist?.picklistEntry*.moniker
			
			println respSearch.qaPicklist.dump()
			
		}
		
		// 3. OPTIONAL: Refine
		//DoRefine
		
		
		// 4. Format the final address.
		//DoGetAddress
		QAGetAddress getAddress = new QAGetAddress()
		if (respSearch.qaPicklist?.picklistEntry) {
			def moniker = respSearch.qaPicklist?.picklistEntry[0].moniker
			getAddress.moniker = moniker
			getAddress.layout = "AFNOR INSEE"
			
			Address formattedAddress = client.doGetAddress(getAddress)
			println formattedAddress.dump()
			println formattedAddress.qaAddress.addressLine*.label
			println formattedAddress.qaAddress.addressLine*.line
			
		}
		



		
		
			
	}


		
}
