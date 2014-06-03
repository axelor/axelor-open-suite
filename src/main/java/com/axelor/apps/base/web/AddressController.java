/**
 * Axelor Business Solutions
 *
 * Copyright (C) 2012-2014 Axelor (<http://axelor.com>).
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
package com.axelor.apps.base.web;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.axelor.apps.account.db.Invoice;
import com.axelor.apps.base.db.Address;
import com.axelor.apps.base.db.AddressExport;
import com.axelor.apps.base.db.General;
import com.axelor.apps.base.db.IAdministration;
import com.axelor.apps.base.db.Import;
import com.axelor.apps.base.db.Partner;
import com.axelor.apps.base.db.PartnerList;
import com.axelor.apps.base.db.PickListEntry;
import com.axelor.apps.base.service.AddressService;
import com.axelor.apps.base.service.administration.GeneralService;
import com.axelor.apps.base.service.user.UserInfoService;
import com.axelor.data.Importer;
import com.axelor.data.csv.CSVImporter;
import com.axelor.data.xml.XMLImporter;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.persist.Transactional;
import com.qas.web_2005_02.AddressLineType;
import com.qas.web_2005_02.PicklistEntryType;
import com.qas.web_2005_02.QAAddressType;
import com.qas.web_2005_02.QAPicklistType;
import com.qas.web_2005_02.VerifyLevelType;

public class AddressController {

	@Inject
	private Injector injector;

	@Inject
	private AddressService ads;

	@Inject 
	private UserInfoService uis;

	private static final Logger LOG = LoggerFactory.getLogger(AddressController.class);

	public void check(ActionRequest request, ActionResponse response) {

		General g = request.getContext().asType(General.class);
		LOG.debug("validate g = {}", g);
		LOG.debug("validate g.qasWsdlUrl = {}", g.getQasWsdlUrl());

		String msg = ads.check(g.getQasWsdlUrl())? g.getQasWsdlUrl()+" Ok":"Service indisponible, veuillez contacter votre adminstrateur";
		response.setFlash(msg);		
	}

	public void validate(ActionRequest request, ActionResponse response) {

		Address a = request.getContext().asType(Address.class);
		LOG.debug("validate a = {}", a);
		String search = a.getAddressL4()+" "+a.getAddressL6();
		Map<String,Object> retDict = (Map<String, Object>) ads.validate(GeneralService.getGeneral().getQasWsdlUrl(), search);
		LOG.debug("validate retDict = {}", retDict);

		VerifyLevelType verifyLevel = (VerifyLevelType) retDict.get("verifyLevel");

		if (verifyLevel != null && verifyLevel.value().equals("Verified")) {

			QAAddressType address = (QAAddressType) retDict.get("qaAddress");
			String addL1;
			List<AddressLineType> addressLineType = address.getAddressLine();
			addL1 = addressLineType.get(0).getLine();
			response.setValue("addressL2", addressLineType.get(1).getLine());
			response.setValue("addressL3", addressLineType.get(2).getLine());
			response.setValue("addressL4", addressLineType.get(3).getLine());
			response.setValue("addressL5", addressLineType.get(4).getLine());
			response.setValue("addressL6", addressLineType.get(5).getLine());
			response.setValue("inseeCode", addressLineType.get(6).getLine());
			response.setValue("certifiedOk", true);
			response.setValue("pickList", new ArrayList<QAPicklistType>());
			if (addL1 != null) {
				response.setFlash("Ligne 1: "+addL1);
			}
		} else if (verifyLevel != null && (verifyLevel.value().equals("Multiple") || verifyLevel.value().equals("StreetPartial") || verifyLevel.value().equals("InteractionRequired") || verifyLevel.value().equals("PremisesPartial"))) {
			LOG.debug("retDict.verifyLevel = {}", retDict.get("verifyLevel"));
			QAPicklistType qaPicklist =  (QAPicklistType) retDict.get("qaPicklist");
			List<PickListEntry> pickList = new ArrayList<PickListEntry>();
			if (qaPicklist != null) {
				for (PicklistEntryType p : qaPicklist.getPicklistEntry()) {
					PickListEntry e = new PickListEntry();
					e.setAddress(a);
					e.setMoniker(p.getMoniker());
					e.setScore(p.getScore().toString());
					e.setPostcode(p.getPostcode());
					e.setPartialAddress(p.getPartialAddress());
					e.setPicklist(p.getPicklist());

					pickList.add(e);
				}
			} else if (retDict.get("qaAddress") != null) {
				QAAddressType address = (QAAddressType) retDict.get("qaAddress");				
				PickListEntry e = new PickListEntry();
				List<AddressLineType> addressLineType = address.getAddressLine();
				e.setAddress(a);
				e.setL2(addressLineType.get(1).getLine());
				e.setL3(addressLineType.get(2).getLine());
				e.setPartialAddress(addressLineType.get(3).getLine());
				e.setL5(addressLineType.get(4).getLine());
				e.setPostcode(addressLineType.get(5).getLine());
				e.setInseeCode(addressLineType.get(6).getLine());

				pickList.add(e);
			}
			response.setValue("certifiedOk", false);
			response.setValue("pickList", pickList);

		} else if (verifyLevel != null && verifyLevel.value().equals("None")) {
			LOG.debug("address None");
			response.setFlash("Aucune addresse correspondante dans la base QAS");
		} 
	}

	public void select(ActionRequest request, ActionResponse response) {

		Address a = request.getContext().asType(Address.class);
		PickListEntry pickedEntry = null;

		if (a.getPickList().size() > 0) {

			//if (a.pickList*.selected.count { it == true} > 0)
			//	pickedEntry = a.pickList.find {it.selected == true}
			pickedEntry = a.getPickList().get(0);
			LOG.debug("select pickedEntry = {}", pickedEntry);
			String moniker = pickedEntry.getMoniker();
			if (moniker != null) {
				com.qas.web_2005_02.Address address = ads.select(GeneralService.getGeneral().getQasWsdlUrl(), moniker);
				LOG.debug("select address = {}", address);
				//addressL4: title="N° et Libellé de la voie"
				//addressL6: title="Code Postal - Commune"/>
				response.setValue("addressL2", address.getQAAddress().getAddressLine().get(1));
				response.setValue("addressL3", address.getQAAddress().getAddressLine().get(2));
				response.setValue("addressL4", address.getQAAddress().getAddressLine().get(3));
				response.setValue("addressL5", address.getQAAddress().getAddressLine().get(4));
				response.setValue("addressL6", address.getQAAddress().getAddressLine().get(5));
				response.setValue("inseeCode", address.getQAAddress().getAddressLine().get(6));
				response.setValue("certifiedOk", true);
				response.setValue("pickList", new ArrayList<QAPicklistType>());
			} 
			else  {
				LOG.debug("missing fields for pickedEntry: {}", pickedEntry);
				response.setValue("addressL2", pickedEntry.getL2());
				response.setValue("addressL3", pickedEntry.getL3());
				response.setValue("addressL4", pickedEntry.getPartialAddress());
				response.setValue("addressL5", pickedEntry.getL5());
				response.setValue("addressL6", pickedEntry.getPostcode());
				response.setValue("inseeCode", pickedEntry.getInseeCode());
				response.setValue("pickList", new ArrayList<QAPicklistType>());
				response.setValue("certifiedOk", true);
			}

		} 
		else 
			response.setFlash("NA");
	}

	public void export(ActionRequest request,ActionResponse response) throws IOException{

		AddressExport addressExport = request.getContext().asType(AddressExport.class);

		int size = (Integer) ads.export(addressExport.getPath());

		response.setValue("log", size+" adresses exportées");
	}

	public void importAddress(ActionRequest request, ActionResponse response) throws IOException{
		Import context = request.getContext().asType(Import.class);

		String path = context.getPath();
		String configPath = context.getConfigPath();
		String type = context.getTypeSelect();

		LOG.debug("using {} importer for config file: {} on directory: {}",type,configPath, path);


		File folder = new File(path);
		File xmlFile = new File(configPath);

		if (!folder.exists()) {
			response.setFlash("Dossier inacessible.");
		} else if (!xmlFile.exists()) {
			response.setFlash("Fichier de mapping inacessible.");
		} else { 
			Importer importer = null;

			if (type.equals("xml")) {
				LOG.debug("using XMLImporter");
				importer = new XMLImporter(injector, configPath, path);
			}
			else {
				LOG.debug("using CSVImporter");
				importer = new CSVImporter(injector, configPath, path);
			}
			Map<String,String[]> mappings = new HashMap<String,String[]>();
			String[] array = new String[1];
			array[1] = "Address.csv";
			mappings.put("contact.address", array);
			importer.run(mappings);
			//importer.run()

			response.setFlash("Import terminé.");
		}
	}


	public void viewMap(ActionRequest request, ActionResponse response)  {
		Address address = request.getContext().asType(Address.class);
		if(address.getId() != null)
			address = Address.find(address.getId());
		String qString = address.getAddressL4()+" ,"+address.getAddressL6();
		BigDecimal latitude = address.getLatit();
		BigDecimal longitude = address.getLongit();
		LOG.debug("latitude...."+latitude);
		LOG.debug("longitude...."+longitude);
		Map<String,Object> result = ads.getMap(qString, latitude, longitude);
		if(result != null){
			Map<String,Object> mapView = new HashMap<String,Object>();
			mapView.put("title", "Map");
			mapView.put("resource", result.get("url"));
			mapView.put("viewType", "html");
			response.setView(mapView);
			if (BigDecimal.ZERO.compareTo(latitude) == 0 || BigDecimal.ZERO.compareTo(longitude) == 0) {
				response.setValue("latit", result.get("latitude"));
				response.setValue("longit", result.get("longitude"));
			}
		}
		else
			response.setFlash(String.format("<B>%s</B> not found",qString));
	}

	public void directionsMap(ActionRequest request, ActionResponse response)  {
		Partner currPartner = uis.getUserPartner();
		Address departureAddress = currPartner.getDeliveryAddress();
		if (departureAddress != null) {
			Address arrivalAddress = request.getContext().asType(Address.class);
			if(arrivalAddress.getId() != null)
				arrivalAddress = Address.find(arrivalAddress.getId());
			String aString = arrivalAddress.getAddressL4()+" ,"+arrivalAddress.getAddressL6();
			String dString = departureAddress.getAddressL4()+" ,"+departureAddress.getAddressL6();
			if (GeneralService.getGeneral().getMapApiSelect() == IAdministration.MAP_API_GOOGLE) {
				BigDecimal dLat = departureAddress.getLatit();
				BigDecimal dLon = departureAddress.getLongit();
				BigDecimal aLat = arrivalAddress.getLatit();
				BigDecimal aLon =  arrivalAddress.getLongit();
				Map<String, Object> result = ads.getDirectionMapGoogle(dString, dLat, dLon, aString, aLat, aLon);
				if(result != null){
					Map<String,Object> mapView = new HashMap<String,Object>();
					mapView.put("title", "Map");
					mapView.put("resource", result.get("url"));
					mapView.put("viewType", "html");
					response.setView(mapView);
					if (BigDecimal.ZERO.compareTo(dLat) == 0 || BigDecimal.ZERO.compareTo(dLon) == 0) {
						response.setValue("latit", result.get("latitude"));
						response.setValue("longit", result.get("longitude"));
					}
				}
				else response.setFlash(String.format("<B>%s</B> not found",aString));
			}
			else 
				response.setFlash("Feature currently not available with Open Street Maps.");
		} else 
			response.setFlash("Current user's partner delivery address not set");
	}

	public void checkMapApi(ActionRequest request, ActionResponse response)  {
		response.setFlash("Not implemented yet!");
	}

	@SuppressWarnings("unchecked")
	@Transactional
	public void viewSalesMap(ActionRequest request, ActionResponse response)  {
		// Only allowed for google maps to prevent overloading OSM
		if (GeneralService.getGeneral().getMapApiSelect() == IAdministration.MAP_API_GOOGLE) {
			PartnerList partnerList = request.getContext().asType(PartnerList.class);

			File file = new File("/home/axelor/www/HTML/latlng_"+partnerList.getId()+".csv");
			//file.write("latitude,longitude,fullName,turnover\n");

			Iterator<Partner> it = (Iterator<Partner>) partnerList.getPartnerSet().iterator();

			while(it.hasNext()) {

				Partner partner = it.next();
				//def address = partner.mainInvoicingAddress
				if (partner.getMainInvoicingAddress() != null) {
					partner.getMainInvoicingAddress().getId();
					Address address = Address.find(partner.getMainInvoicingAddress().getId());
					if (!(address.getLatit() != null && address.getLongit() != null)) {
						String qString = address.getAddressL4()+" ,"+address.getAddressL6();
						LOG.debug("qString = {}", qString);

						Map<String,Object> googleResponse = ads.geocodeGoogle(qString);
						address.setLatit((BigDecimal) googleResponse.get("lat"));
						address.setLongit((BigDecimal) googleResponse.get("lng"));
						address.save();
					}
					if (address.getLatit() != null && address.getLongit() != null) {
						//def turnover = Invoice.all().filter("self.partner.id = ? AND self.status.code = 'val'", partner.id).fetch().sum{ it.inTaxTotal }
						List<Invoice> listInvoice = (List<Invoice>) Invoice.all().filter("self.partner.id = ?", partner.getId()).fetch();
						BigDecimal turnover = BigDecimal.ZERO;
						for(Invoice invoice: listInvoice) {
							turnover.add(invoice.getInTaxTotal());
						}
						/*
						file.withWriterAppend('UTF-8') {
							it.write("${address.latit},${address?.longit},${partner.fullName},${turnover?:0.0}\n")
						}
						 */
					}
				}
			}
			//response.values = [partnerList : partnerList]
			String url = "";
			if (partnerList.getIsCluster())
				url = "http://localhost/HTML/cluster_gmaps_xhr.html?file=latlng_"+partnerList.getId()+".csv";
			else
				url = "http://localhost/HTML/gmaps_xhr.html?file=latlng_"+partnerList.getId()+".csv";

			Map<String,Object> mapView = new HashMap<String,Object>();
			mapView.put("title", "Sales map");
			mapView.put("resource", url);
			mapView.put("viewType", "html");
			response.setView(mapView);
			//response.reload = true

		} else {
			response.setFlash("Not implemented for OSM");
		}
	}
}
