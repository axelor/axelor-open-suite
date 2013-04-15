package com.axelor.apps.tool.address

import groovy.util.logging.Slf4j

import javax.xml.namespace.QName
import javax.xml.ws.Service

import com.qas.web_2005_02.Address
import com.qas.web_2005_02.EngineEnumType
import com.qas.web_2005_02.EngineType
import com.qas.web_2005_02.PromptSetType
import com.qas.web_2005_02.QACanSearch
import com.qas.web_2005_02.QAData
import com.qas.web_2005_02.QADataSet
import com.qas.web_2005_02.QAGetAddress
import com.qas.web_2005_02.QAPortType
import com.qas.web_2005_02.QASearch
import com.qas.web_2005_02.QASearchResult

@Slf4j
class AddressService {

	static private QName SERVICE_NAME = null
	static private QName PORT_NAME = null
	static private URL wsdlURL = null
	static private Service service = null
	static private QAPortType client = null

	def void setService(wsdlUrl) {
		// TODO: inject this
		if (this.client == null) {
			this.SERVICE_NAME = new QName("http://www.qas.com/web-2005-02"
					,"ProWeb")

			this.PORT_NAME = new QName("http://www.qas.com/web-2005-02"
					,"QAPortType")

			//def wsdlURL = new URL("http://ip.axelor.com:2021/proweb.wsdl")
			this.wsdlURL = new URL(wsdlUrl)
			//println this.wsdlURL

			this.service = Service.create(this.wsdlURL, this.SERVICE_NAME);
			this.client = service.getPort(QAPortType.class);
			//QAPortType client = service.getPort(PORT_NAME, QAPortType.class)
			log.debug("setService  this.client = {}", this.client)

		}
	}

	def boolean doCanSearch(wsdlUrl) {

		try {
			QName SERVICE_NAME = new QName("http://www.qas.com/web-2005-02"
					,"ProWeb")

			QName PORT_NAME = new QName("http://www.qas.com/web-2005-02"
					,"QAPortType")

			//def wsdlURL = new URL("http://ip.axelor.com:2021/proweb.wsdl")
			URL wsdlURL = new URL(wsdlUrl)


			Service service = Service.create(wsdlURL, SERVICE_NAME);
			QAPortType client = service.getPort(QAPortType.class);
			//QAPortType client = service.getPort(PORT_NAME, QAPortType.class)
			log.debug("setService  client = {}", client)

			// 1. Pre-check.

			QAData qadata = client.doGetData()
			QADataSet ds = qadata.dataSet[0]


			QACanSearch canSearch = new QACanSearch()
			canSearch.country = "FRX"
			canSearch.layout = "AFNOR INSEE"

			EngineType engType = new EngineType()
			engType.setFlatten(true)

			engType.value = EngineEnumType.VERIFICATION
			canSearch.engine = engType
			def resp = client.doCanSearch(canSearch)

			return resp.isOk
		} catch (Exception e) {
			e.printStackTrace()
			return false
		}
	}

	def doSearch(wsdlUrl, searchString) {

		try {

			this.setService(wsdlUrl)


			// 2. Initial search.
			QASearch search = new QASearch()
			search.country = "FRX"
			search.layout = "AFNOR INSEE" 
			search.search = searchString

			EngineType engTypeT = new EngineType()
			engTypeT.promptSet = PromptSetType.ONE_LINE //DEFAULT
			engTypeT.value = EngineEnumType.VERIFICATION
			engTypeT.flatten = true
			search.engine = engTypeT

			search.engine = engTypeT
			QASearchResult respSearch = this.client.doSearch(search)


			return [verifyLevel: respSearch.verifyLevel,
				qaPicklist: respSearch.qaPicklist,
				qaAddress: respSearch.qaAddress]
		} catch (Exception e) {
			e.printStackTrace()
			return [:]
		}
	}

	def doGetAddress(wsdlUrl, moniker) {
		try {
			this.setService(wsdlUrl)
			
			// 4. Format the final address.
			QAGetAddress getAddress = new QAGetAddress()

			getAddress.moniker = moniker
			getAddress.layout = "AFNOR INSEE"

			Address formattedAddress = this.client.doGetAddress(getAddress)
			println formattedAddress.dump()
			println formattedAddress.qaAddress.addressLine*.label
			println formattedAddress.qaAddress.addressLine*.line
			
			return formattedAddress
		} catch (Exception e) {
			e.printStackTrace()

		}
	}
}