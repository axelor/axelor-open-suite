package com.axelor.apps.base.web

import groovy.util.logging.Slf4j
import au.com.bytecode.opencsv.CSVWriter

import com.axelor.apps.base.db.Address
import com.google.inject.Inject

@Slf4j
class AddressService {
	
	@Inject
	private com.axelor.apps.tool.address.AddressService ads
	
	def boolean check(wsdlUrl) {
		return ads.doCanSearch(wsdlUrl)
	}
	
	def validate(wsdlUrl, search) {
		return ads.doSearch(wsdlUrl, search)
	}

	def select(wsdlUrl, moniker) {
		return ads.doGetAddress(wsdlUrl, moniker)
	}

	def export(path) {
		def addresses = Address.all().filter("self.certifiedOk IS FALSE").fetch()
		
	
		def csv = new CSVWriter(new java.io.FileWriter(path), "|".charAt(0), CSVWriter.NO_QUOTE_CHARACTER)
		def header = ["Id","AddressL1","AddressL2","AddressL3"
			,"AddressL4","AddressL5","AddressL6", "CodeINSEE"]
		//"AddressL7|"+
		//"inseeCode"

		csv.writeNext(header as String[])
		for (a in addresses) {
			def items = [a.id, a.payerPartner?.name, a.addressL2,
				a.addressL3, a.addressL4, a.addressL5, a.addressL6, a.inseeCode]
			
			
			csv.writeNext(items.collect { it?"$it":""} as String[])
		}
		csv.close()
		log.info("{} exported", path)
		
		return addresses.size()
			
	}
}

