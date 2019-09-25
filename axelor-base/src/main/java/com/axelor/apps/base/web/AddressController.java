/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2019 Axelor (<http://axelor.com>).
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

import com.axelor.apps.base.db.Address;
import com.axelor.apps.base.db.AddressExport;
import com.axelor.apps.base.db.AppBase;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.Partner;
import com.axelor.apps.base.db.PartnerAddress;
import com.axelor.apps.base.db.PickListEntry;
import com.axelor.apps.base.db.repo.AddressRepository;
import com.axelor.apps.base.db.repo.AppBaseRepository;
import com.axelor.apps.base.db.repo.PartnerAddressRepository;
import com.axelor.apps.base.db.repo.PartnerRepository;
import com.axelor.apps.base.exceptions.IExceptionMessage;
import com.axelor.apps.base.service.AddressService;
import com.axelor.apps.base.service.MapService;
import com.axelor.apps.base.service.PartnerService;
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.auth.AuthUtils;
import com.axelor.db.mapper.Mapper;
import com.axelor.exception.service.TraceBackService;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.axelor.rpc.Context;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.qas.web_2005_02.AddressLineType;
import com.qas.web_2005_02.PicklistEntryType;
import com.qas.web_2005_02.QAAddressType;
import com.qas.web_2005_02.QAPicklistType;
import com.qas.web_2005_02.VerifyLevelType;
import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
public class AddressController {

  @Inject protected AddressService addressService;
  @Inject protected AppBaseService appBaseService;

  private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  public void check(ActionRequest request, ActionResponse response) {

    AppBase appBase = request.getContext().asType(AppBase.class);
    LOG.debug("validate g = {}", appBase);
    LOG.debug("validate g.qasWsdlUrl = {}", appBase.getQasWsdlUrl());

    String msg =
        Beans.get(AddressService.class).check(appBase.getQasWsdlUrl())
            ? appBase.getQasWsdlUrl() + " " + I18n.get(IExceptionMessage.ADDRESS_1)
            : I18n.get(IExceptionMessage.ADDRESS_2);
    response.setFlash(msg);
  }

  public void validate(ActionRequest request, ActionResponse response) {

    Address a = request.getContext().asType(Address.class);
    LOG.debug("validate a = {}", a);
    String search = a.getAddressL4() + " " + a.getAddressL6();
    Map<String, Object> retDict =
        Beans.get(AddressService.class)
            .validate(appBaseService.getAppBase().getQasWsdlUrl(), search);
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
        response.setFlash("Ligne 1: " + addL1);
      }
    } else if (verifyLevel != null
        && (verifyLevel.value().equals("Multiple")
            || verifyLevel.value().equals("StreetPartial")
            || verifyLevel.value().equals("InteractionRequired")
            || verifyLevel.value().equals("PremisesPartial"))) {
      LOG.debug("retDict.verifyLevel = {}", retDict.get("verifyLevel"));
      QAPicklistType qaPicklist = (QAPicklistType) retDict.get("qaPicklist");
      List<PickListEntry> pickList = new ArrayList<>();
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

    if (!a.getPickList().isEmpty()) {

      // if (a.pickList*.selected.count { it == true} > 0)
      //	pickedEntry = a.pickList.find {it.selected == true}
      pickedEntry = a.getPickList().get(0);
      LOG.debug("select pickedEntry = {}", pickedEntry);
      String moniker = pickedEntry.getMoniker();
      if (moniker != null) {
        com.qas.web_2005_02.Address address =
            Beans.get(AddressService.class)
                .select(appBaseService.getAppBase().getQasWsdlUrl(), moniker);
        LOG.debug("select address = {}", address);
        // addressL4: title="N° et Libellé de la voie"
        // addressL6: title="Code Postal - Commune"/>
        response.setValue("addressL2", address.getQAAddress().getAddressLine().get(1));
        response.setValue("addressL3", address.getQAAddress().getAddressLine().get(2));
        response.setValue("addressL4", address.getQAAddress().getAddressLine().get(3));
        response.setValue("addressL5", address.getQAAddress().getAddressLine().get(4));
        response.setValue("addressL6", address.getQAAddress().getAddressLine().get(5));
        response.setValue("inseeCode", address.getQAAddress().getAddressLine().get(6));
        response.setValue("certifiedOk", true);
        response.setValue("pickList", new ArrayList<QAPicklistType>());
      } else {
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

    } else response.setFlash(I18n.get(IExceptionMessage.ADDRESS_4));
  }

  public void export(ActionRequest request, ActionResponse response) throws IOException {

    AddressExport addressExport = request.getContext().asType(AddressExport.class);

    int size = Beans.get(AddressService.class).export(addressExport.getPath());

    response.setValue("log", size + " adresses exportées");
  }

  public void viewMap(ActionRequest request, ActionResponse response) {
    try {
      Address address = request.getContext().asType(Address.class);
      address = Beans.get(AddressRepository.class).find(address.getId());
      Optional<Pair<BigDecimal, BigDecimal>> latLong = addressService.getOrUpdateLatLong(address);

      if (latLong.isPresent()) {
        MapService mapService = Beans.get(MapService.class);
        Map<String, Object> mapView = new HashMap<>();
        mapView.put("title", "Map");
        mapView.put("resource", mapService.getMapUrl(latLong.get(), address.getFullName()));
        mapView.put("viewType", "html");
        response.setView(mapView);
      } else {
        response.setFlash(
            String.format(I18n.get(IExceptionMessage.ADDRESS_5), address.getFullName()));
      }
      response.setReload(true);
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  public void viewDirection(ActionRequest request, ActionResponse response) {
    try {
      MapService mapService = Beans.get(MapService.class);
      String key = null;
      if (appBaseService.getAppBase().getMapApiSelect() == AppBaseRepository.MAP_API_GOOGLE) {
        key = mapService.getGoogleMapsApiKey();
      }

      Company company = AuthUtils.getUser().getActiveCompany();
      if (company == null) {
        response.setFlash(I18n.get(IExceptionMessage.PRODUCT_NO_ACTIVE_COMPANY));
        return;
      }
      Address departureAddress = company.getAddress();
      if (departureAddress == null) {
        response.setFlash(I18n.get(IExceptionMessage.ADDRESS_7));
        return;
      }

      departureAddress = Beans.get(AddressRepository.class).find(departureAddress.getId());
      Optional<Pair<BigDecimal, BigDecimal>> departureLatLong =
          addressService.getOrUpdateLatLong(departureAddress);

      if (!departureLatLong.isPresent()) {
        response.setFlash(
            String.format(I18n.get(IExceptionMessage.ADDRESS_5), departureAddress.getFullName()));
        return;
      }

      Address arrivalAddress = request.getContext().asType(Address.class);
      arrivalAddress = Beans.get(AddressRepository.class).find(arrivalAddress.getId());
      Optional<Pair<BigDecimal, BigDecimal>> arrivalLatLong =
          addressService.getOrUpdateLatLong(arrivalAddress);

      if (!arrivalLatLong.isPresent()) {
        response.setFlash(
            String.format(I18n.get(IExceptionMessage.ADDRESS_5), arrivalAddress.getFullName()));
        return;
      }

      Map<String, Object> mapView = new HashMap<>();
      mapView.put("title", "Map");
      mapView.put(
          "resource",
          mapService.getDirectionUrl(key, departureLatLong.get(), arrivalLatLong.get()));
      mapView.put("viewType", "html");
      response.setView(mapView);
      response.setReload(true);
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  public void updateLatLong(ActionRequest request, ActionResponse response) {
    try {
      Address address = request.getContext().asType(Address.class);
      address = Beans.get(AddressRepository.class).find(address.getId());
      addressService.resetLatLong(address);
      addressService.updateLatLong(address);
      response.setReload(true);
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  public void createPartnerAddress(ActionRequest request, ActionResponse response) {
    Context context = request.getContext();
    Context parentContext = context.getParent();
    if (parentContext.isEmpty()) {
      return;
    }

    String parentModel = (String) parentContext.get("_model");
    LOG.debug("Create partner address : Parent model = {}", parentModel);

    String partnerField = PartnerAddressRepository.modelPartnerFieldMap.get(parentModel);
    LOG.debug("Create partner address : Parent field = {}", partnerField);

    Partner partner = null;
    if (parentContext.get(partnerField) instanceof Partner) {
      partner = (Partner) parentContext.get(partnerField);
    } else if (parentContext.get(partnerField) instanceof Map) {
      partner = Mapper.toBean(Partner.class, (Map<String, Object>) parentContext.get(partnerField));
    }

    LOG.debug("Create partner address : Partner = {}", partner);

    if (partner == null || partner.getId() == null) {
      return;
    }
    Address address = context.asType(Address.class);

    PartnerAddress partnerAddress =
        Beans.get(PartnerAddressRepository.class)
            .all()
            .filter("self.partner.id = ? AND self.address.id = ?", partner.getId(), address.getId())
            .fetchOne();

    LOG.debug("Create partner address : Partner Address = {}", partnerAddress);

    if (partnerAddress == null) {
      partner = Beans.get(PartnerRepository.class).find(partner.getId());
      address = Beans.get(AddressRepository.class).find(address.getId());
      Boolean invoicing = (Boolean) context.get("isInvoicingAddr");
      if (invoicing == null) {
        invoicing = false;
      }
      Boolean delivery = (Boolean) context.get("isDeliveryAddr");
      if (delivery == null) {
        delivery = false;
      }
      Boolean isDefault = (Boolean) context.get("isDefault");
      if (isDefault == null) {
        isDefault = false;
      }
      PartnerService partnerService = Beans.get(PartnerService.class);
      partnerService.addPartnerAddress(partner, address, isDefault, invoicing, delivery);
      partnerService.savePartner(partner);
    }
  }

  public void autocompleteAddress(ActionRequest request, ActionResponse response) {
    Address address = request.getContext().asType(Address.class);
    Beans.get(AddressService.class).autocompleteAddress(address);
    response.setValues(address);
  }
}
