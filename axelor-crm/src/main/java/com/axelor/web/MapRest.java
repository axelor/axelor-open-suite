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
package com.axelor.web;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import com.axelor.apps.base.db.Address;
import com.axelor.apps.base.db.Partner;
import com.axelor.apps.base.db.repo.AddressRepository;
import com.axelor.apps.base.service.MapService;
import com.axelor.apps.crm.db.Lead;
import com.axelor.apps.crm.db.Opportunity;
import com.axelor.apps.crm.service.LeadService;
import com.axelor.apps.crm.service.OpportunityService;
import com.axelor.i18n.I18n;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.base.Strings;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;

@Path("/map")
public class MapRest {

	@Inject MapService mapService;
	
	@Inject
	private LeadService leadService;
	
	@Inject
	private OpportunityService opportunityService;
	
	@Inject
	private AddressRepository addressRepo;
	
	@Path("/lead")
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public JsonNode getLeads() {

		List<? extends Lead> leads = leadService.all().fetch();
		JsonNodeFactory factory = JsonNodeFactory.instance;
		ObjectNode mainNode = factory.objectNode();
		ArrayNode arrayNode = factory.arrayNode();

		for (Lead lead : leads) {
			
			String fullName = lead.getFirstName() + " " + lead.getName();
			
			if (lead.getEnterpriseName() != null) {
				fullName = lead.getEnterpriseName() + "</br>" + fullName;
			}			
			ObjectNode objectNode = factory.objectNode();
			objectNode.put("fullName", fullName);
			objectNode.put("fixedPhone", lead.getFixedPhone() != null ? lead.getFixedPhone() : " ");
			
			if (lead.getEmailAddress() != null) {
				objectNode.put("emailAddress", lead.getEmailAddress().getAddress());
			}
			
			StringBuilder addressString = new StringBuilder();
			
			if (lead.getPrimaryAddress() != null) {
				addressString.append(lead.getPrimaryAddress() + "</br>");
			}
			if (lead.getPrimaryCity() != null) {
				addressString.append(lead.getPrimaryCity() + "</br>");
			}
			if (lead.getPrimaryPostalCode() != null) {
				addressString.append(lead.getPrimaryPostalCode() + "</br>");
			}
			if (lead.getPrimaryState() != null) {
				addressString.append(lead.getPrimaryState() + "</br>");
			}
			if (lead.getPrimaryCountry() != null) {
				addressString.append(lead.getPrimaryCountry().getName());
			}						 
			
			BigDecimal latit = BigDecimal.ZERO;
			BigDecimal longit = BigDecimal.ZERO;				
			String qString = addressString.toString().replaceAll("</br>", " ");					
			Map<String,Object> latlng =  mapService.getMapGoogle(qString, latit, longit);
			latit = (BigDecimal) latlng.get("latitude");
			longit = (BigDecimal) latlng.get("longitude");	
			
			objectNode.put("latit", latit);
			objectNode.put("longit", longit);						
			objectNode.put("address", addressString.toString());
			
			objectNode.put("pinColor", "yellow");
			objectNode.put("pinChar", "L");			
			arrayNode.add(objectNode);	
		}
		mainNode.put("status", 0);
		mainNode.put("data", arrayNode);
		return mainNode;
	}
	
	@Transactional
	@Path("/opportunity")
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public JsonNode getOpportunities() {

		List<? extends Opportunity> opportunities = opportunityService.all().fetch();
		JsonNodeFactory factory = JsonNodeFactory.instance;
		ObjectNode mainNode = factory.objectNode();
		ArrayNode arrayNode = factory.arrayNode();

		for (Opportunity opportunity : opportunities) {
			
			Partner partner = opportunity.getPartner();
			if (partner == null) continue;
			
			ObjectNode objectNode = factory.objectNode();
			
			String currencyCode = "";
			if (opportunity.getCurrency() != null) {
				currencyCode = opportunity.getCurrency().getCode();
			}
			
			String amtLabel = "Amount";
			if (!Strings.isNullOrEmpty(I18n.get("amount"))) {
				amtLabel = I18n.get("amount");				
			}
			String amount = amtLabel + " : " +opportunity.getAmount() + " " + currencyCode;
			
			objectNode.put("fullName", opportunity.getName() + "</br>" + amount);
			objectNode.put("fixedPhone", partner.getFixedPhone() != null ? partner.getFixedPhone() : " ");
			
			if (partner.getEmailAddress() != null) {
				objectNode.put("emailAddress", partner.getEmailAddress().getAddress());
			}
			
			if (partner.getMainInvoicingAddress() != null) {
				Address address = partner.getMainInvoicingAddress();
				String addressString = mapService.makeAddressString(address, objectNode);
				addressRepo.save(address);
				objectNode.put("address", addressString);							
			}
			
			objectNode.put("pinColor", "pink");
			objectNode.put("pinChar", "O");			
			arrayNode.add(objectNode);	
		}
		mainNode.put("status", 0);
		mainNode.put("data", arrayNode);
		return mainNode;
	}	

}
