package com.axelor.apps.base.web

import groovy.util.logging.Slf4j
import wslite.rest.ContentType
import wslite.rest.RESTClient

import com.axelor.apps.base.db.Address
import com.axelor.apps.base.db.AddressExport
import com.axelor.apps.base.db.General
import com.axelor.apps.base.db.Import
import com.axelor.apps.base.db.PickListEntry
import com.axelor.apps.base.service.administration.GeneralService
import com.axelor.data.Importer
import com.axelor.data.csv.CSVImporter
import com.axelor.data.xml.XMLImporter
import com.axelor.exception.service.TraceBackService
import com.axelor.rpc.ActionRequest
import com.axelor.rpc.ActionResponse
import com.google.inject.Inject
import com.google.inject.Injector


@Slf4j
class AddressController {

	@Inject
	private Injector injector
	
	@Inject
	private AddressService ads
	
	def check(ActionRequest request, ActionResponse response) {

		General g = request.context as General
		log.debug("validate g = {}", g)
		log.debug("validate g.qasWsdlUrl = {}", g.qasWsdlUrl)
		
		def msg = ads.check(g.qasWsdlUrl)?" ${g.qasWsdlUrl} Ok":"Service indisponible, veuillez contacter votre adminstrateur"
		response.flash = msg
		
		
	}


	def validate(ActionRequest request, ActionResponse response) {

		Address a = request.context as Address
		log.debug("validate a = {}", a)
		def search = [a.addressL4, a. addressL6].join(" ")
		def retDict = ads.validate(GeneralService.getGeneral().qasWsdlUrl, search)
		log.debug("validate retDict = {}", retDict)

		log.debug("Niveau de vérification QAS: ${}", retDict.verifyLevel)
		
		if (retDict.qaAddress) {
			log.debug("label = {}", retDict.qaAddress.addressLine*.label)
			log.debug("line = {}", retDict.qaAddress.addressLine*.line)
			//log.debug(retDict.qaAddress.addressLine*.lineContent)
		}

		
		if (retDict.qaPicklist) {
			log.debug("total = {}", retDict.qaPicklist?.total)
			log.debug("picklist = {}", retDict.qaPicklist?.picklistEntry*.picklist)
			log.debug("postcode = {}", retDict.qaPicklist?.picklistEntry*.postcode)
			log.debug("partialAddress = {}", retDict.qaPicklist?.picklistEntry*.partialAddress)
			log.debug("score = {}", retDict.qaPicklist?.picklistEntry*.score)
			log.debug("moniker = {}", retDict.qaPicklist?.picklistEntry*.moniker)
			
			log.debug("retDict.qaPicklist.dump()= {}", retDict.qaPicklist.dump())
			
		}
			

		
		if (retDict.verifyLevel.value in ["Verified"]) {
			log.debug("address Verified")
			
			def address = retDict.qaAddress
			
			log.debug("address.addressLine*.label = {}", address.addressLine*.label) 
			log.debug("address.addressLine*.line = {}", address.addressLine*.line) 
			def addL1 = address.addressLine*.line[0]
			response.values = [
				addressL2 : address.addressLine*.line[1],
				addressL3 : address.addressLine*.line[2],
				addressL4 : address.addressLine*.line[3], //.findAll{ w -> w.size() > 0 }.join(" "),
				addressL5 : address.addressLine*.line[4],
				addressL6 : address.addressLine*.line[5],
				inseeCode : address.addressLine*.line[6],
				certifiedOk : true,
				pickList : []]
			if (addL1) {
				response.flash = "Ligne 1: $addL1"
			}
		} else if (retDict.verifyLevel.value in ["Multiple", "StreetPartial", "InteractionRequired", "PremisesPartial"]) {
			log.debug("retDict.verifyLevel = {}", retDict.verifyLevel)
			def qaPicklist =  retDict.qaPicklist
			def pickList = []
			if (qaPicklist) {
				for (p in qaPicklist.picklistEntry) {
					def e = new PickListEntry()
					e.address = a
					e.moniker = p.moniker
					e.score = p.score
					e.postcode = p.postcode
					e.partialAddress = p.partialAddress
					e.picklist = p.picklist
					
					pickList.add(e)
				}
			} else if (retDict.qaAddress) {
				def address = retDict.qaAddress
				log.debug("no picklist, retDict.qaAddress = {}", retDict.qaAddress)
				
				log.debug("address.addressLine*.label = {}", address.addressLine*.label)
				log.debug("address.addressLine*.line = {}", address.addressLine*.line)
	
				
				def e = new PickListEntry()
				e.address = a
				e.l2 = address.addressLine*.line[1]
				e.l3 = address.addressLine*.line[2]
				e.partialAddress = address.addressLine*.line[3]
				e.l5 = address.addressLine*.line[4]
				e.postcode = address.addressLine*.line[5]
				e.inseeCode = address.addressLine*.line[6]
				
				pickList.add(e)

			}
			response.values = [certifiedOk : false,
				pickList : pickList]
		} else if (retDict.verifyLevel.value in "None") {
			log.debug("address None")
			response.flash = "Aucune addresse correspondante dans la base QAS"
		} 
	}

	def select(ActionRequest request, ActionResponse response) {

		Address a = request.context as Address
		log.debug("select a = {}", a)
		log.debug("select a.pickList = {}", a.pickList)
		log.debug("select a.pickList*.selected = {}", a.pickList*.selected)
		def pickedEntry = null
		if (a.pickList?.size > 0) {
			if (a.pickList*.selected.count { it == true} > 0)
				pickedEntry = a.pickList.find {it.selected == true}
			else 
				pickedEntry = a.pickList[0]
				log.debug("select pickedEntry = {}", pickedEntry)
			def moniker = pickedEntry.moniker
			if (moniker) {
				def address = ads.select(GeneralService.getGeneral().qasWsdlUrl, moniker)
				log.debug("select address = {}", address)
				//addressL4: title="N° et Libellé de la voie"
				//addressL6: title="Code Postal - Commune"/>
				def addL1 = address.qaAddress.addressLine*.line[0]
				response.values = [
					addressL2 : address.qaAddress.addressLine*.line[1],
					addressL3 : address.qaAddress.addressLine*.line[2],
					addressL4 : address.qaAddress.addressLine*.line[3], //.findAll{ w -> w.size() > 0 }.join(" "),
					addressL5 : address.qaAddress.addressLine*.line[4],
					addressL6 : address.qaAddress.addressLine*.line[5],
					inseeCode : address.qaAddress.addressLine*.line[6],
					pickList : [],
					certifiedOk : true	]
			} else  {
				log.debug("missing fields for pickedEntry: {}", pickedEntry)
				response.values = [
					addressL2 : pickedEntry.l2,
					addressL3 : pickedEntry.l3,
					addressL4 : pickedEntry.partialAddress,
					addressL5 : pickedEntry.l5,
					addressL6 : pickedEntry.postcode,
					inseeCode : pickedEntry.inseeCode,
					pickList : [],
					certifiedOk : true	]
			}

		} else 
			response.flash = "NA"
	}

	void export(ActionRequest request,ActionResponse response){
		AddressExport addressExport = request.context as AddressExport
		

		def size = ads.export(addressExport.path)
		

		response.values = [log: "$size adresses exportées"]
	}

	void importAddress(ActionRequest request, ActionResponse response){
		Import context = request.context as Import
		
		String path = context.path
		String configPath = context.configPath
		String type = context.typeSelect
		
		log.debug("using {} importer for config file: {} on directory: {}",type,configPath, path)
		
		
		File folder = new File(path)
		File xmlFile = new File(configPath)
		
		if (!folder.exists()) {
			response.flash = "Dossier inacessible."
		} else if (!xmlFile.exists()) {
			response.flash = "Fichier de mapping inacessible."
		} else { 
			Importer importer = null
			
			if (type.equals("xml")) {
				log.debug("using XMLImporter")
				importer = new XMLImporter(injector, configPath, path)
			}
			else {
				log.debug("using CSVImporter")
				importer = new CSVImporter(injector, configPath, path)
			}
			def mappings = [ "contact.address" : ["Address.csv"] as String[]]
			importer.run(mappings)
			//importer.run()
			
			response.flash = "Import terminé."
		}
	}
	
	def void viewMap(ActionRequest request, ActionResponse response)  {
		
		Address address = request.context as Address
		def qString = "${address.addressL4} ,${address.addressL6}"
		log.debug("qString = {}", qString)
		
		try {

			def restClient = new RESTClient("http://nominatim.openstreetmap.org/")
			def restResponse = restClient.get( path:'/search',
									   accept: ContentType.JSON,
									   query:[q: qString,
											format:'xml',
											polygon: true,
											addressdetails: true],
									   headers:["HTTP referrer":"axelor"],
									   connectTimeout: 5000,
									   readTimeout: 10000,
									   followRedirects: false,
									   useCaches: false,
									   sslTrustAllCerts: true )
			
			print restResponse.parsedResponseContent.text
			def searchresults = new XmlSlurper().parseText(restResponse.parsedResponseContent.text)
			def places = searchresults.place
			log.debug("places.size() = {}", places.size())
			if (places.size() > 1) {
				response.flash = "<B>$qString</B> matches multiple locations. First selected."
			}
			def firstPlaceFound = places[0]
			log.debug("firstPlaceFound = {}", firstPlaceFound)
			
			if (firstPlaceFound) {
				log.debug("firstPlaceFound.@lat = {}", firstPlaceFound.@lat)
				log.debug("firstPlaceFound.@lat.text() = {}", firstPlaceFound.@lat.text())
				log.debug("firstPlaceFound.@lon = {}", firstPlaceFound.@lon)
				log.debug("firstPlaceFound.@lon.text() = {}", firstPlaceFound.@lon.text())
				def latit = new BigDecimal(firstPlaceFound.@lat.text())
				def longit = new BigDecimal(firstPlaceFound.@lon.text())
				
				
		
				String url = "http://localhost/Leaflet/examples/oneMarker.html?x=$latit&y=$longit&z=18"
				response.view = [
					"title": "Map",
					"resource": url,
					"viewType": "html"
				]
				response.values = [
					latit: latit,
					longit: longit
				]
			}
			else {
				response.flash = "<B>$qString</B> not found"
			}

		}
		catch(Exception e)  {
			TraceBackService.trace(response, e) 
		}
	}
	
}
