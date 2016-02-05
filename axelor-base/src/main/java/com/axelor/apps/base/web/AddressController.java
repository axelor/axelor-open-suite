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
import com.axelor.apps.base.db.IPartner;
import com.axelor.apps.base.db.Partner;
import com.axelor.apps.base.db.PartnerAddress;
import com.axelor.apps.base.db.PickListEntry;
import com.axelor.apps.base.db.repo.AddressRepository;
import com.axelor.apps.base.db.repo.PartnerAddressRepository;
import com.axelor.apps.base.db.repo.PartnerRepository;
import com.axelor.apps.base.exceptions.IExceptionMessage;
import com.axelor.apps.base.service.AddressService;
import com.axelor.apps.base.service.MapService;
import com.axelor.apps.base.service.PartnerService;
import com.axelor.apps.base.service.administration.GeneralService;
import com.axelor.apps.base.service.user.UserService;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.axelor.rpc.Context;
import com.google.inject.Inject;
import com.qas.web_2005_02.AddressLineType;
import com.qas.web_2005_02.PicklistEntryType;
import com.qas.web_2005_02.QAAddressType;
import com.qas.web_2005_02.QAPicklistType;
import com.qas.web_2005_02.VerifyLevelType;

public class AddressController {

	@Inject
	private AddressService addressService;
	
	@Inject
	private AddressRepository addressRepo;

	@Inject
	protected GeneralService generalService;
	
	@Inject
	private PartnerService partnerService;
	
	@Inject
	private PartnerRepository partnerRepo;
	
	
	
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
		Map<String,Object> retDict = Beans.get(AddressService.class).validate(generalService.getGeneral().getQasWsdlUrl(), search);
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
				com.qas.web_2005_02.Address address = Beans.get(AddressService.class).select(generalService.getGeneral().getQasWsdlUrl(), moniker);
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

		int size = Beans.get(AddressService.class).export(addressExport.getPath());

		response.setValue("log", size+" adresses exportées");
	}

	public void viewMap(ActionRequest request, ActionResponse response)  {
		Address address = request.getContext().asType(Address.class);
		address = addressService.checkLatLang(address,false);
		BigDecimal latit = address.getLatit();
		BigDecimal longit = address.getLongit();
		BigDecimal zero = BigDecimal.ZERO;
		if(zero.compareTo(latit) != 0 && zero.compareTo(longit) != 0){
			Map<String,Object> mapView = new HashMap<String,Object>();
			mapView.put("title", "Map");
			mapView.put("resource",  Beans.get(MapService.class).getMapUrl(latit, longit));
			mapView.put("viewType", "html");
			response.setView(mapView);
		}
		else
			response.setFlash(String.format(I18n.get(IExceptionMessage.ADDRESS_5),address.getFullName()));

		response.setReload(true);
	}

	public void viewDirection(ActionRequest request, ActionResponse response)  {
		Partner currPartner = Beans.get(UserService.class).getUserPartner();
		if(currPartner == null){
			response.setFlash(I18n.get(IExceptionMessage.ADDRESS_7));
			return;
		}
		if (generalService.getGeneral().getMapApiSelect() != IAdministration.MAP_API_GOOGLE) {
			response.setFlash(I18n.get(IExceptionMessage.ADDRESS_6));
			return;
		}
		Address departureAddress = partnerService.getDeliveryAddress(currPartner);
		if (departureAddress == null) {
			response.setFlash(I18n.get(IExceptionMessage.ADDRESS_7));
			return;
		}
		departureAddress = addressService.checkLatLang(departureAddress,false);
		BigDecimal dLat = departureAddress.getLatit();
		BigDecimal dLon = departureAddress.getLongit();
		BigDecimal zero = BigDecimal.ZERO;
		if(zero.compareTo(dLat) == 0 || zero.compareTo(dLat) == 0){
			response.setFlash(String.format(I18n.get(IExceptionMessage.ADDRESS_5),departureAddress.getFullName()));
			return;
		}

		Address arrivalAddress = request.getContext().asType(Address.class);
		arrivalAddress = addressService.checkLatLang(arrivalAddress,false);
		BigDecimal aLat = arrivalAddress.getLatit();
		BigDecimal aLon =  arrivalAddress.getLongit();
		if(zero.compareTo(aLat) == 0 || zero.compareTo(aLat) == 0){
			response.setFlash(String.format(I18n.get(IExceptionMessage.ADDRESS_5),arrivalAddress.getFullName()));
			return;
		}

		Map<String,Object> mapView = new HashMap<String,Object>();
		mapView.put("title", "Map");
		mapView.put("resource", Beans.get(MapService.class).getDirectionUrl(dLat, dLon, aLat, aLon));
		mapView.put("viewType", "html");
		response.setView(mapView);
		response.setReload(true);

	}

	public void checkLatLang(ActionRequest request, ActionResponse response) {
		Address address = request.getContext().asType(Address.class);
		addressService.checkLatLang(address, true);
		response.setReload(true);
	}


	public void createPartnerAddress(ActionRequest request, ActionResponse response){

		Context context = request.getContext();
		LOG.debug("Context fields: {}",context.keySet());
		Address address = context.asType(Address.class);

		Context parentContext = context.getParentContext();
		LOG.debug("Parent Context fields: {}",parentContext.keySet());
		if(parentContext.isEmpty()){
			return;
		}

		String parentModel = (String) parentContext.get("_model");
		LOG.debug("Partner modelPartnerFieldMap: {}",IPartner.modelPartnerFieldMap);
		LOG.debug("Parent model: {}",parentModel);
		String parnterField = IPartner.modelPartnerFieldMap.get(parentModel);
		Partner partner = (Partner) parentContext.get(parnterField);
		if(partner == null || partner.getId() == null){
			return;
		}

		PartnerAddress partnerAddress = Beans.get(PartnerAddressRepository.class).all().filter("self.partner.id = ? AND self.address.id = ?", partner.getId(), address.getId()).fetchOne();
		
		LOG.debug("Partner address: {}",partnerAddress);
		if(partnerAddress ==  null){
			partner = partnerRepo.find(partner.getId());
			address = addressRepo.find(address.getId());
			Boolean invoicing = (Boolean)context.get("isInvoicingAddr");
			Boolean delivery = (Boolean)context.get("isDeliveryAddr");
			Boolean isDefault = (Boolean)context.get("isDefault");
			LOG.debug("Address isDelivery : {} , isInvoicing: {}",delivery,invoicing);

			partnerService.addPartnerAddress(partner, address, isDefault, invoicing, delivery);
			partnerService.savePartner(partner);
		}
				
		
	}

}
