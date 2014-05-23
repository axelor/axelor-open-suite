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
import com.axelor.apps.base.service.AddressService;
import com.axelor.apps.crm.db.Lead;
import com.axelor.apps.crm.db.Opportunity;
import com.axelor.db.Model;
import com.axelor.i18n.I18n;
import com.axelor.i18n.I18nBundle;
import com.axelor.i18n.I18nControl;
import com.axelor.meta.db.MetaModel;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.base.Strings;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;

@Path("/map")
public class MapRest {

	@Inject AddressService addressService;
	
	@Path("/partner")
	@GET
	@Transactional
	@Produces(MediaType.APPLICATION_JSON)
	public JsonNode getPartners() {
		
		List<Partner> customers = Partner.all().filter("self.customerTypeSelect IN (?,?) AND self.isContact=?", 2,3, false).fetch();
		JsonNodeFactory factory = JsonNodeFactory.instance;
		ObjectNode mainNode = factory.objectNode();
		ArrayNode arrayNode = factory.arrayNode();
		
		for (Partner customer : customers) {
			
			ObjectNode objectNode = factory.objectNode();
			objectNode.put("fullName", customer.getFullName());
			objectNode.put("fixedPhone", customer.getFixedPhone() != null ? customer.getFixedPhone() : " ");
			
			if (customer.getEmailAddress() != null) {
				objectNode.put("emailAddress", customer.getEmailAddress().getAddress());
			}
			
			if (customer.getMainInvoicingAddress() != null) {
				Address address = customer.getMainInvoicingAddress();
				BigDecimal latit = address.getLatit();
				BigDecimal longit = address.getLongit();
				
				if (BigDecimal.ZERO.compareTo(latit) == 0 || BigDecimal.ZERO.compareTo(longit) == 0) {
					
					String qString = address.getFullName();					
 					Map<String,Object> latlng =  addressService.getMapGoogle(qString, latit, longit);
 					latit = (BigDecimal) latlng.get("latitude");
 					longit = (BigDecimal) latlng.get("longitude");
 					address.setLatit(latit);
 					address.setLongit(longit);
 					address.save();
				}
				
				objectNode.put("latit", latit);
				objectNode.put("longit", longit);
				StringBuilder addressString = new StringBuilder();
				if (address.getAddressL2() != null) {
					addressString.append(address.getAddressL2() + "</br>");
				}
				if (address.getAddressL3() != null) {
					addressString.append(address.getAddressL3() + "</br>");
				}
				if (address.getAddressL4() != null) {
					addressString.append(address.getAddressL4() + "</br>");
				}
				if (address.getAddressL5() != null) {
					addressString.append(address.getAddressL5() + "</br>");
				}
				if (address.getAddressL6() != null) {
					addressString.append(address.getAddressL6());
				}						 
				if (address.getAddressL7Country() != null) {
					addressString = addressString.append("</br>" + address.getAddressL7Country().getName());
				}
				objectNode.put("address", addressString.toString());				
			}
			
			objectNode.put("pinColor", customer.getCustomerTypeSelect() == 2 ? "red" : "orange");
			objectNode.put("pinChar", customer.getCustomerTypeSelect() == 2 ? "P" : "C");			
			arrayNode.add(objectNode);
		}
		mainNode.put("status", 0);
		mainNode.put("data", arrayNode);
		return mainNode;
	}
	@Path("/lead")
	@GET
	@Transactional
	@Produces(MediaType.APPLICATION_JSON)
	public JsonNode getLeads() {

		List<Lead> leads = Lead.all().fetch();
		JsonNodeFactory factory = JsonNodeFactory.instance;
		ObjectNode mainNode = factory.objectNode();
		ArrayNode arrayNode = factory.arrayNode();

		for (Lead lead : leads) {
			
			Partner partner = lead.getPartner();
			if (partner == null) continue;
			String fullName = lead.getFirstName() + " " + lead.getName();
			
			if (lead.getEnterpriseName() != null) {
				fullName = lead.getEnterpriseName() + "</br>" + fullName;
			}			
			ObjectNode objectNode = factory.objectNode();
			objectNode.put("fullName", fullName);
			objectNode.put("fixedPhone", partner.getFixedPhone() != null ? partner.getFixedPhone() : " ");
			
			if (partner.getEmailAddress() != null) {
				objectNode.put("emailAddress", partner.getEmailAddress().getAddress());
			}
			
			if (partner.getMainInvoicingAddress() != null) {
				Address address = partner.getMainInvoicingAddress();
				BigDecimal latit = address.getLatit();
				BigDecimal longit = address.getLongit();
				
				if (BigDecimal.ZERO.compareTo(latit) == 0 || BigDecimal.ZERO.compareTo(longit) == 0) {
					
					String qString = address.getFullName();					
 					Map<String,Object> latlng =  addressService.getMapGoogle(qString, latit, longit);
 					latit = (BigDecimal) latlng.get("latitude");
 					longit = (BigDecimal) latlng.get("longitude");
 					address.setLatit(latit);
 					address.setLongit(longit);
 					address.save();
				}
				
				objectNode.put("latit", latit);
				objectNode.put("longit", longit);
				StringBuilder addressString = new StringBuilder();
				
				if (address.getAddressL2() != null) {
					addressString.append(address.getAddressL2() + "</br>");
				}
				if (address.getAddressL3() != null) {
					addressString.append(address.getAddressL3() + "</br>");
				}
				if (address.getAddressL4() != null) {
					addressString.append(address.getAddressL4() + "</br>");
				}
				if (address.getAddressL5() != null) {
					addressString.append(address.getAddressL5() + "</br>");
				}
				if (address.getAddressL6() != null) {
					addressString.append(address.getAddressL6());
				}						 
				if (address.getAddressL7Country() != null) {
					addressString = addressString.append("</br>" + address.getAddressL7Country().getName());
				}
				objectNode.put("address", addressString.toString());				
			}
			
			objectNode.put("pinColor", "yellow");
			objectNode.put("pinChar", "L");			
			arrayNode.add(objectNode);	
		}
		mainNode.put("status", 0);
		mainNode.put("data", arrayNode);
		return mainNode;
	}
	@Path("/opportunity")
	@GET
	@Transactional
	@Produces(MediaType.APPLICATION_JSON)
	public JsonNode getOpportunities() {

		List<Opportunity> opportunities = Opportunity.all().fetch();
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
				BigDecimal latit = address.getLatit();
				BigDecimal longit = address.getLongit();
				
				if (BigDecimal.ZERO.compareTo(latit) == 0 || BigDecimal.ZERO.compareTo(longit) == 0) {
					
					String qString = address.getFullName();					
 					Map<String,Object> latlng =  addressService.getMapGoogle(qString, latit, longit);
 					latit = (BigDecimal) latlng.get("latitude");
 					longit = (BigDecimal) latlng.get("longitude");
 					address.setLatit(latit);
 					address.setLongit(longit);
 					address.save();
				}
				
				objectNode.put("latit", latit);
				objectNode.put("longit", longit);
				StringBuilder addressString = new StringBuilder();
				
				if (address.getAddressL2() != null) {
					addressString.append(address.getAddressL2() + "</br>");
				}
				if (address.getAddressL3() != null) {
					addressString.append(address.getAddressL3() + "</br>");
				}
				if (address.getAddressL4() != null) {
					addressString.append(address.getAddressL4() + "</br>");
				}
				if (address.getAddressL5() != null) {
					addressString.append(address.getAddressL5() + "</br>");
				}
				if (address.getAddressL6() != null) {
					addressString.append(address.getAddressL6());
				}						 
				if (address.getAddressL7Country() != null) {
					addressString = addressString.append("</br>" + address.getAddressL7Country().getName());
				}
				objectNode.put("address", addressString.toString());				
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