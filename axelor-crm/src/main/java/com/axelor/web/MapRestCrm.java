/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2005-2026 Axelor (<http://axelor.com>).
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
import com.axelor.apps.crm.db.Lead;
import com.axelor.apps.crm.db.Opportunity;
import com.axelor.apps.crm.db.Tour;
import com.axelor.apps.crm.db.TourLine;
import com.axelor.apps.crm.db.repo.OpportunityRepository;
import com.axelor.apps.crm.db.repo.TourRepository;
import com.axelor.common.StringUtils;
import com.axelor.db.JPA;
import com.axelor.i18n.I18n;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import org.apache.commons.collections.CollectionUtils;

@Path("/map")
public class MapRestCrm {

  @Inject private MapRestService mapRestService;

  @Inject private OpportunityRepository opportunityRepo;

  @Inject private TourRepository tourRepo;

  private JsonNodeFactory factory = JsonNodeFactory.instance;

  @Path("/lead")
  @GET
  @Produces(MediaType.APPLICATION_JSON)
  public JsonNode getLeads() {
    ObjectNode mainNode = factory.objectNode();

    try {
      List<Lead> leads =
          JPA.em()
              .createQuery(
                  "SELECT self FROM Lead self "
                      + "LEFT JOIN FETCH self.address address "
                      + "LEFT JOIN FETCH address.country "
                      + "LEFT JOIN FETCH self.emailAddress",
                  Lead.class)
              .getResultList();
      ArrayNode arrayNode = factory.arrayNode();

      for (Lead lead : leads) {

        Address address = lead.getAddress();
        if (address == null || !address.getIsValidLatLong()) {
          continue;
        }

        ObjectNode objectNode = factory.objectNode();

        String addressString = mapRestService.makeAddressString(address, objectNode);
        if (StringUtils.isBlank(addressString)) {
          continue;
        }
        objectNode.put("address", addressString);

        String fullName = lead.getFirstName() + " " + lead.getName();

        if (lead.getEnterpriseName() != null) {
          fullName = lead.getEnterpriseName() + "<br/>" + fullName;
        }

        objectNode.put("fullName", fullName);
        objectNode.put("fixedPhone", lead.getFixedPhone() != null ? lead.getFixedPhone() : " ");

        if (lead.getEmailAddress() != null) {
          objectNode.put("emailAddress", lead.getEmailAddress().getAddress());
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
  public JsonNode getOpportunities() {
    ObjectNode mainNode = factory.objectNode();

    try {
      List<? extends Opportunity> opportunities = opportunityRepo.all().fetch();

      List<Partner> partnerList =
          opportunities.stream()
              .map(Opportunity::getPartner)
              .filter(Objects::nonNull)
              .distinct()
              .collect(Collectors.toList());
      Map<Partner, Address> invoicingAddressMap = mapRestService.getInvoicingAddresses(partnerList);

      ArrayNode arrayNode = factory.arrayNode();

      for (Opportunity opportunity : opportunities) {

        Partner partner = opportunity.getPartner();

        if (partner == null) {
          continue;
        }

        Address address = invoicingAddressMap.get(partner);
        if (address == null || !address.getIsValidLatLong()) {
          continue;
        }

        ObjectNode objectNode = factory.objectNode();

        String addressString = mapRestService.makeAddressString(address, objectNode);
        if (StringUtils.isBlank(addressString)) {
          continue;
        }
        objectNode.put("address", addressString);

        String currencyCode = "";

        if (opportunity.getCurrency() != null) {
          currencyCode = opportunity.getCurrency().getCodeISO();
        }

        String amount = I18n.get("Amount") + " : " + opportunity.getAmount() + " " + currencyCode;

        objectNode.put("fullName", opportunity.getName() + "<br/>" + amount);
        objectNode.put(
            "fixedPhone", partner.getFixedPhone() != null ? partner.getFixedPhone() : " ");

        if (partner.getEmailAddress() != null) {
          objectNode.put("emailAddress", partner.getEmailAddress().getAddress());
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
  public JsonNode getTour(@PathParam("id") long id) {
    ObjectNode mainNode = factory.objectNode();

    try {
      Tour tour = tourRepo.find(id);
      if (tour == null || CollectionUtils.isEmpty(tour.getTourLineList())) {
        return mainNode;
      }
      List<TourLine> tourLineList = tour.getTourLineList();

      ArrayNode arrayNode = factory.arrayNode();

      for (TourLine tourLine : tourLineList) {

        Address address = tourLine.getAddress();
        if (address == null
            || !address.getIsValidLatLong()
            || StringUtils.isBlank(address.getFullName())) {
          continue;
        }

        ObjectNode objectNode = factory.objectNode();

        String addressString = mapRestService.makeAddressString(address, objectNode);
        if (StringUtils.isBlank(addressString)) {
          continue;
        }
        objectNode.put("address", addressString);

        Partner partner = tourLine.getPartner();
        objectNode.put("fullName", partner.getFullName());
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
