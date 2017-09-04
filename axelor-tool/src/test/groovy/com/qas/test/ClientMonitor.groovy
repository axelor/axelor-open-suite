/**
 * Axelor Business Solutions
 *
 * Copyright (C) 2016 Axelor (<http://axelor.com>).
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


class ClientMonitor {
	//stubs generated with:
	//arye@dm4:~/projects/axelor/axelor-tool/src/main/java$ ~/opt/cxf/bin/wsdl2java  -client -frontend jaxws21 http://ip.axelor.com:2021/proweb.wsdl
	
	//http://cxf.apache.org/docs/how-do-i-develop-a-client.html
	
	// TODO:
	//http://blog.progs.be/92/cxf-ws-client-dynamic-endpoint-and-loading-wsdl-from-the-classpath
	
	
	@Test
	def void JAXWSProxy() {
		QName SERVICE_NAME = new QName("http://www.qas.com/web-2005-02"
			,"ProWeb")

		QName PORT_NAME = new QName("http://www.qas.com/web-2005-02"
			,"QAPortType")

		// set up TCP/IP monitor under Windows | Preferences
		//http://backup.axelor.com/pub/sftp/proweb.wsdl
		def wsdlURL = new URL("http://localhost:8001/pub/sftp/proweb.wsdl")
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
		
		
	}


		
}
