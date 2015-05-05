/**
 * Axelor Business Solutions
 *
 * Copyright (C) 2014 Axelor (<http://axelor.com>).
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

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.axelor.apps.base.db.Address;
import com.axelor.apps.base.db.AddressExport;
import com.axelor.apps.base.db.General;
import com.axelor.apps.base.db.IAdministration;
import com.axelor.apps.base.db.Partner;
import com.axelor.apps.base.db.PickListEntry;
import com.axelor.apps.base.db.repo.AddressRepository;
import com.axelor.apps.base.exceptions.IExceptionMessage;
import com.axelor.apps.base.service.AddressService;
import com.axelor.apps.base.service.MapService;
import com.axelor.apps.base.service.administration.GeneralService;
import com.axelor.apps.base.service.user.UserService;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.google.inject.Inject;
import com.qas.web_2005_02.AddressLineType;
import com.qas.web_2005_02.PicklistEntryType;
import com.qas.web_2005_02.QAAddressType;
import com.qas.web_2005_02.QAPicklistType;
import com.qas.web_2005_02.VerifyLevelType;

public class AddressController {

	@Inject
	private AddressRepository addressRepo;
	

	private static final Logger LOG = LoggerFactory.getLogger(AddressController.class);

	public void check(ActionRequest request, ActionResponse response) {

		General g = request.getContext().asType(General.class);
		LOG.debug("validate g = {}", g);
		LOG.debug("validate g.qasWsdlUrl = {}", g.getQasWsdlUrl());

		String msg = Beans.get(AddressService.class).check(g.getQasWsdlUrl())? g.getQasWsdlUrl()+" "+I18n.get(IExceptionMessage.ADDRESS_1):I18n.get(IExceptionMessage.ADDRESS_2);
		response.setFlash(msg);		
	}

	public void validate(ActionRequest request, ActionResponse response) {

		Address a = request.getContext().asType(Address.class);
		LOG.debug("validate a = {}", a);
		String search = a.getAddressL4()+" "+a.getAddressL6();
		Map<String,Object> retDict = (Map<String, Object>) Beans.get(AddressService.class).validate(GeneralService.getGeneral().getQasWsdlUrl(), search);
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
			response.setFlash(I18n.get(IExceptionMessage.ADDRESS_3));
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
				com.qas.web_2005_02.Address address = Beans.get(AddressService.class).select(GeneralService.getGeneral().getQasWsdlUrl(), moniker);
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
			response.setFlash(I18n.get(IExceptionMessage.ADDRESS_4));
	}

	public void export(ActionRequest request,ActionResponse response) throws IOException{

		AddressExport addressExport = request.getContext().asType(AddressExport.class);

		int size = (Integer) Beans.get(AddressService.class).export(addressExport.getPath());

		response.setValue("log", size+" adresses exportées");
	}

	public void viewMap(ActionRequest request, ActionResponse response)  {
		Address address = request.getContext().asType(Address.class);
		if(address.getId() != null)
			address = addressRepo.find(address.getId());
		String qString = address.getAddressL4()+" ,"+address.getAddressL6();
		Map<String,Object> result = Beans.get(MapService.class).getMap(qString);
		if(result != null){
			Map<String,Object> mapView = new HashMap<String,Object>();
			mapView.put("title", "Map");
			mapView.put("resource", result.get("url"));
			mapView.put("viewType", "html");
			response.setView(mapView);
		}
		else
			response.setFlash(String.format(I18n.get(IExceptionMessage.ADDRESS_5),qString));
	}

	public void directionsMap(ActionRequest request, ActionResponse response)  {
		Partner currPartner = Beans.get(UserService.class).getUserPartner();
		Address departureAddress = currPartner.getDeliveryAddress();
		if (departureAddress != null) {
			Address arrivalAddress = request.getContext().asType(Address.class);
			if(arrivalAddress.getId() != null)
				arrivalAddress = addressRepo.find(arrivalAddress.getId());
			String aString = arrivalAddress.getAddressL4()+" ,"+arrivalAddress.getAddressL6();
			String dString = departureAddress.getAddressL4()+" ,"+departureAddress.getAddressL6();
			if (GeneralService.getGeneral().getMapApiSelect() == IAdministration.MAP_API_GOOGLE) {
				BigDecimal dLat = departureAddress.getLatit();
				BigDecimal dLon = departureAddress.getLongit();
				BigDecimal aLat = arrivalAddress.getLatit();
				BigDecimal aLon =  arrivalAddress.getLongit();
				Map<String, Object> result = Beans.get(MapService.class).getDirectionMapGoogle(dString, dLat, dLon, aString, aLat, aLon);
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
				else response.setFlash(String.format(I18n.get(IExceptionMessage.ADDRESS_5),aString));
			}
			else 
				response.setFlash(I18n.get(IExceptionMessage.ADDRESS_6));
		} else 
			response.setFlash(I18n.get(IExceptionMessage.ADDRESS_7));
	}

	public void checkMapApi(ActionRequest request, ActionResponse response)  {
		response.setFlash(I18n.get(IExceptionMessage.NOT_IMPLEMENTED_METHOD));
	}

}
