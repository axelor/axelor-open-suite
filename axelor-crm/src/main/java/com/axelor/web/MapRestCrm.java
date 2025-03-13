/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2005-2025 Axelor (<http://axelor.com>).
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package com.axelor.web;

import com.axelor.apps.base.db.Address;
import com.axelor.apps.base.db.Partner;
import com.axelor.apps.base.service.MapRestService;
import com.axelor.apps.base.service.MapService;
import com.axelor.apps.base.service.PartnerService;
import com.axelor.apps.crm.db.Lead;
import com.axelor.apps.crm.db.Opportunity;
import com.axelor.apps.crm.db.Tour;
import com.axelor.apps.crm.db.TourLine;
import com.axelor.apps.crm.db.repo.LeadRepository;
import com.axelor.apps.crm.db.repo.OpportunityRepository;
import com.axelor.apps.crm.db.repo.TourRepository;
import com.axelor.common.ObjectUtils;
import com.axelor.common.StringUtils;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.base.Strings;
import com.google.inject.Inject;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

@Path("/map")
@Deprecated
public class MapRestCrm {

  @Inject private MapRestService mapRestService;

  @Inject private LeadRepository leadRepo;

  @Inject private OpportunityRepository opportunityRepo;

  @Inject private TourRepository tourRepo;

  private JsonNodeFactory factory = JsonNodeFactory.instance;

  @Path("/lead")
  @GET
  @Produces(MediaType.APPLICATION_JSON)
  @Deprecated
  public JsonNode getLeads() {
    ObjectNode mainNode = factory.objectNode();

    try {
      List<? extends Lead> leads = leadRepo.all().fetch();
      ArrayNode arrayNode = factory.arrayNode();

      for (Lead lead : leads) {

        String fullName = lead.getFirstName() + " " + lead.getName();

        if (lead.getEnterpriseName() != null) {
          fullName = lead.getEnterpriseName() + "<br/>" + fullName;
        }

        ObjectNode objectNode = factory.objectNode();
        objectNode.put("fullName", fullName);
        objectNode.put("fixedPhone", lead.getFixedPhone() != null ? lead.getFixedPhone() : " ");

        if (lead.getEmailAddress() != null) {
          objectNode.put("emailAddress", lead.getEmailAddress().getAddress());
        }

        String addressFullname = lead.getAddress() != null ? lead.getAddress().getFullName() : "";
        objectNode.put("address", addressFullname);

        Map<String, Object> result = Beans.get(MapService.class).getMap(addressFullname);

        if (result != null) {
          objectNode.put("latit", (BigDecimal) result.get("latitude"));
          objectNode.put("longit", (BigDecimal) result.get("longitude"));
        }

        arrayNode.add(objectNode);
      }

      mapRestService.setData(mainNode, arrayNode);
    } catch (Exception e) {
      mapRestService.setError(mainNode, e);
    }

    return mainNode;
  }

  @Path("/opportunity")
  @GET
  @Produces(MediaType.APPLICATION_JSON)
  @Deprecated
  public JsonNode getOpportunities() {
    ObjectNode mainNode = factory.objectNode();

    try {
      List<? extends Opportunity> opportunities = opportunityRepo.all().fetch();
      ArrayNode arrayNode = factory.arrayNode();

      for (Opportunity opportunity : opportunities) {

        Partner partner = opportunity.getPartner();

        if (partner == null) {
          continue;
        }

        ObjectNode objectNode = factory.objectNode();

        String currencyCode = "";

        if (opportunity.getCurrency() != null) {
          currencyCode = opportunity.getCurrency().getCodeISO();
        }

        String amtLabel = "Amount";

        if (!Strings.isNullOrEmpty(I18n.get("amount"))) {
          amtLabel = I18n.get("amount");
        }

        String amount = amtLabel + " : " + opportunity.getAmount() + " " + currencyCode;

        objectNode.put("fullName", opportunity.getName() + "<br/>" + amount);
        objectNode.put(
            "fixedPhone", partner.getFixedPhone() != null ? partner.getFixedPhone() : " ");

        if (partner.getEmailAddress() != null) {
          objectNode.put("emailAddress", partner.getEmailAddress().getAddress());
        }

        Address address = Beans.get(PartnerService.class).getInvoicingAddress(partner);

        if (address != null) {
          String addressString = mapRestService.makeAddressString(address, objectNode);
          objectNode.put("address", addressString);
        }

        arrayNode.add(objectNode);
      }

      mapRestService.setData(mainNode, arrayNode);
    } catch (Exception e) {
      mapRestService.setError(mainNode, e);
    }

    return mainNode;
  }

  @Path("/tour/{id}")
  @GET
  @Produces(MediaType.APPLICATION_JSON)
  @Deprecated
  public JsonNode getTour(@PathParam("id") long id) {
    ObjectNode mainNode = factory.objectNode();

    try {
      Tour tour = tourRepo.find(id);
      List<TourLine> tourLineList = tour.getTourLineList();
      if (ObjectUtils.isEmpty(tourLineList)) {
        return mainNode;
      }

      ArrayNode arrayNode = factory.arrayNode();

      for (TourLine tourLine : tourLineList) {
        ObjectNode objectNode = factory.objectNode();
        Partner partner = tourLine.getPartner();
        objectNode.put("fullName", partner.getFullName());

        Address address = tourLine.getAddress();
        if (!StringUtils.isBlank(address.getFullName())) {
          String addressString = mapRestService.makeAddressString(address, objectNode);
          if (StringUtils.isBlank(addressString)) {
            continue;
          }
          objectNode.put("address", addressString);
        }
        objectNode.put(
            "fixedPhone", partner.getFixedPhone() != null ? partner.getFixedPhone() : "");
        objectNode.put(
            "emailAddress",
            partner.getEmailAddress() != null ? partner.getEmailAddress().getAddress() : "");
        objectNode.put("pinColor", "blue");
        objectNode.put("pinChar", "T");

        arrayNode.add(objectNode);
      }

      mapRestService.setData(mainNode, arrayNode);
    } catch (Exception e) {
      mapRestService.setError(mainNode, e);
    }

    return mainNode;
  }
}
