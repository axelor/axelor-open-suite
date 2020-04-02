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
package com.axelor.web;

import com.axelor.apps.base.db.Address;
import com.axelor.apps.base.db.Partner;
import com.axelor.apps.base.db.PartnerAddress;
import com.axelor.apps.base.db.repo.PartnerRepository;
import com.axelor.apps.base.exceptions.IExceptionMessage;
import com.axelor.apps.base.service.MapRestService;
import com.axelor.apps.base.service.PartnerService;
import com.axelor.apps.tool.service.TranslationService;
import com.axelor.auth.AuthUtils;
import com.axelor.common.StringUtils;
import com.axelor.exception.AxelorException;
import com.axelor.exception.db.repo.TraceBackRepository;
import com.axelor.i18n.I18n;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.inject.Inject;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

@Path("/map")
public class MapRest {

  @Inject private MapRestService mapRestService;

  @Inject private TranslationService translationService;

  @Inject private PartnerService partnerService;

  @Inject private PartnerRepository partnerRepo;

  JsonNodeFactory nodeFactory = JsonNodeFactory.instance;

  @Path("/partner")
  @GET
  @Produces(MediaType.APPLICATION_JSON)
  public JsonNode getPartners() {
    ObjectNode mainNode = nodeFactory.objectNode();

    try {
      List<? extends Partner> partners =
          partnerRepo
              .all()
              .filter(
                  "self.isCustomer = true OR self.isSupplier = true AND self.isContact=?", false)
              .fetch();

      ArrayNode arrayNode = nodeFactory.arrayNode();

      for (Partner partner : partners) {

        ObjectNode objectNode = nodeFactory.objectNode();
        objectNode.put("fullName", partner.getFullName());

        if (partner.getFixedPhone() != null) {
          objectNode.put("fixedPhone", partner.getFixedPhone());
        }

        if (partner.getEmailAddress() != null) {
          objectNode.put("emailAddress", partner.getEmailAddress().getAddress());
        }

        Address address = partnerService.getInvoicingAddress(partner);
        if (address != null && StringUtils.notBlank(address.getFullName())) {
          String addressString = mapRestService.makeAddressString(address, objectNode);
          objectNode.put("address", addressString);
        }

        objectNode.put("pinColor", partner.getIsProspect() ? "red" : "orange");
        String pinChar = partner.getIsProspect() ? "P" : "C";
        if (partner.getIsSupplier()) {
          pinChar = pinChar + "/S";
        }
        objectNode.put("pinChar", pinChar);
        arrayNode.add(objectNode);
      }

      mapRestService.setData(mainNode, arrayNode);
    } catch (Exception e) {
      mapRestService.setError(mainNode, e);
    }

    return mainNode;
  }

  @Path("/partner/{id}")
  @GET
  @Produces(MediaType.APPLICATION_JSON)
  public JsonNode getPartner(@PathParam("id") long id) {
    ObjectNode mainNode = nodeFactory.objectNode();

    try {
      Partner partner = partnerRepo.find(id);

      if (partner == null) {
        throw new AxelorException(
            Partner.class,
            TraceBackRepository.CATEGORY_NO_VALUE,
            I18n.get(IExceptionMessage.PARTNER_NOT_FOUND));
      }

      ArrayNode arrayNode = nodeFactory.arrayNode();
      String pinColor = getPinColor(partner);

      List<PartnerAddress> partnerAddressList =
          partner.getPartnerAddressList() != null
              ? partner.getPartnerAddressList()
              : Collections.emptyList();

      for (PartnerAddress partnerAddress : partnerAddressList) {
        if (partnerAddress.getAddress() == null) {
          continue;
        }
        ObjectNode objectNode = nodeFactory.objectNode();
        objectNode.put("fullName", getFullName(partnerAddress));

        if (partnerAddress.getIsDefaultAddr()) {
          if (!StringUtils.isBlank(partner.getFixedPhone())) {
            objectNode.put("fixedPhone", partner.getFixedPhone());
          }

          if (partner.getEmailAddress() != null) {
            objectNode.put("emailAddress", partner.getEmailAddress().getAddress());
          }
        }

        Address address = partnerAddress.getAddress();

        if (!StringUtils.isBlank(address.getFullName())) {
          String addressString;
          addressString = mapRestService.makeAddressString(address, objectNode);
          objectNode.put("address", addressString);
        }

        objectNode.put("pinColor", pinColor);
        objectNode.put("pinChar", getPinChar(partnerAddress));

        arrayNode.add(objectNode);
      }

      mapRestService.setData(mainNode, arrayNode);
    } catch (Exception e) {
      mapRestService.setError(mainNode, e);
    }

    return mainNode;
  }

  @Path("/customer")
  @GET
  @Produces(MediaType.APPLICATION_JSON)
  public JsonNode getCustomers() {

    ObjectNode mainNode = nodeFactory.objectNode();
    try {
      List<? extends Partner> customers =
          partnerRepo.all().filter("self.isCustomer = true AND self.isContact=?", false).fetch();

      ArrayNode arrayNode = nodeFactory.arrayNode();

      for (Partner customer : customers) {

        ObjectNode objectNode = nodeFactory.objectNode();
        objectNode.put("fullName", customer.getFullName());
        objectNode.put(
            "fixedPhone", customer.getFixedPhone() != null ? customer.getFixedPhone() : " ");

        if (customer.getEmailAddress() != null) {
          objectNode.put("emailAddress", customer.getEmailAddress().getAddress());
        }

        Address address = partnerService.getInvoicingAddress(customer);
        if (address != null) {
          String addressString = mapRestService.makeAddressString(address, objectNode);
          objectNode.put("address", addressString);
        }

        objectNode.put("pinColor", "orange");
        objectNode.put("pinChar", "C");
        arrayNode.add(objectNode);
      }

      mapRestService.setData(mainNode, arrayNode);
    } catch (Exception e) {
      mapRestService.setError(mainNode, e);
    }

    return mainNode;
  }

  @Path("/prospect")
  @GET
  @Produces(MediaType.APPLICATION_JSON)
  public JsonNode getProspects() {

    ObjectNode mainNode = nodeFactory.objectNode();

    try {
      List<? extends Partner> customers =
          partnerRepo.all().filter("self.isProspect = true AND self.isContact=?", false).fetch();
      ArrayNode arrayNode = nodeFactory.arrayNode();

      for (Partner prospect : customers) {

        ObjectNode objectNode = nodeFactory.objectNode();
        objectNode.put("fullName", prospect.getFullName());
        objectNode.put(
            "fixedPhone", prospect.getFixedPhone() != null ? prospect.getFixedPhone() : " ");

        if (prospect.getEmailAddress() != null) {
          objectNode.put("emailAddress", prospect.getEmailAddress().getAddress());
        }

        Address address = partnerService.getInvoicingAddress(prospect);
        if (address != null) {
          String addressString = mapRestService.makeAddressString(address, objectNode);
          objectNode.put("address", addressString);
        }

        objectNode.put("pinColor", "red");
        objectNode.put("pinChar", "P");
        arrayNode.add(objectNode);
      }

      mapRestService.setData(mainNode, arrayNode);
    } catch (Exception e) {
      mapRestService.setError(mainNode, e);
    }

    return mainNode;
  }

  @Path("/supplier")
  @GET
  @Produces(MediaType.APPLICATION_JSON)
  public JsonNode getSuppliers() {
    ObjectNode mainNode = nodeFactory.objectNode();

    try {
      ArrayNode arrayNode = nodeFactory.arrayNode();
      List<? extends Partner> customers =
          partnerRepo.all().filter("self.isSupplier = true AND self.isContact=?", false).fetch();

      for (Partner supplier : customers) {

        ObjectNode objectNode = nodeFactory.objectNode();
        objectNode.put("fullName", supplier.getFullName());
        objectNode.put(
            "fixedPhone", supplier.getFixedPhone() != null ? supplier.getFixedPhone() : " ");

        if (supplier.getEmailAddress() != null) {
          objectNode.put("emailAddress", supplier.getEmailAddress().getAddress());
        }

        Address address = partnerService.getInvoicingAddress(supplier);
        if (address != null) {
          String addressString = mapRestService.makeAddressString(address, objectNode);
          objectNode.put("address", addressString);
        }

        objectNode.put("pinColor", "purple");
        objectNode.put("pinChar", "S");
        arrayNode.add(objectNode);
      }

      mapRestService.setData(mainNode, arrayNode);
    } catch (Exception e) {
      mapRestService.setError(mainNode, e);
    }

    return mainNode;
  }

  @Path("translation/{key}")
  @GET
  @Produces(MediaType.APPLICATION_JSON)
  public JsonNode getTranslation(@PathParam("key") String key) {

    ObjectNode mainNode = nodeFactory.objectNode();

    String language = AuthUtils.getUser().getLanguage();
    String translation = translationService.getTranslation(key, language);

    if (translation.equals(key)) {
      translation = translation.substring(ITranslation.PREFIX.length());
    }

    mainNode.put("translation", translation);

    return mainNode;
  }

  protected String getFullName(PartnerAddress partnerAddress) {
    String fullName = partnerAddress.getPartner().getFullName();

    if (StringUtils.isBlank(fullName)) {
      fullName = "";
    }

    List<String> texts = new ArrayList<>();

    if (partnerAddress.getIsDefaultAddr()) {
      texts.add(I18n.get(ITranslation.DEFAULT));
    }

    if (partnerAddress.getIsInvoicingAddr()) {
      texts.add(I18n.get(ITranslation.INVOICING));
    }

    if (partnerAddress.getIsDeliveryAddr()) {
      texts.add(I18n.get(ITranslation.DELIVERY));
    }

    if (!texts.isEmpty()) {
      fullName += String.format(" (%s)", String.join(", ", texts));
    }

    return fullName;
  }

  protected String getPinColor(Partner partner) {
    return partner.getIsProspect() ? "red" : "orange";
  }

  protected String getPinChar(PartnerAddress partnerAddress) {
    if (partnerAddress.getIsDefaultAddr()) {
      return "";
    }

    if (partnerAddress.getIsInvoicingAddr()) {
      return I18n.get(ITranslation.PIN_CHAR_INVOICING);
    }

    if (partnerAddress.getIsDeliveryAddr()) {
      return I18n.get(ITranslation.PIN_CHAR_DELIVERY);
    }

    return "";
  }
}
