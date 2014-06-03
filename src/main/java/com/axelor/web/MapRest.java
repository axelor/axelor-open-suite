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
package com.axelor.web;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import com.axelor.apps.base.db.Address;
import com.axelor.apps.base.db.Country;
import com.axelor.apps.base.db.Partner;
import com.axelor.apps.base.service.AddressService;
import com.axelor.apps.crm.db.Lead;
import com.axelor.apps.crm.db.Opportunity;
import com.axelor.apps.supplychain.db.SalesOrder;
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

	@Inject AddressService addressService;
	
	@Transactional
	@Path("/partner")
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public JsonNode getPartners() {
		
		List<? extends Partner> customers = Partner.all_().filter("self.customerTypeSelect IN (?, ?) OR self.supplierTypeSelect IN (?, ?) AND self.isContact=?", 2,3,2,3, false).fetch();
		JsonNodeFactory factory = JsonNodeFactory.instance;
		ObjectNode mainNode = factory.objectNode();
		ArrayNode arrayNode = factory.arrayNode();
		
		for (Partner partner : customers) {
			
			ObjectNode objectNode = factory.objectNode();
			objectNode.put("fullName", partner.getFullName());
			objectNode.put("fixedPhone", partner.getFixedPhone() != null ? partner.getFixedPhone() : " ");
			
			if (partner.getEmailAddress() != null) {
				objectNode.put("emailAddress", partner.getEmailAddress().getAddress());
			}
			
			if (partner.getMainInvoicingAddress() != null) {
				Address address = partner.getMainInvoicingAddress();
				String addressString = makeAddressString(address, objectNode);
				address.save();
				objectNode.put("address", addressString);				
			}
			
			objectNode.put("pinColor", partner.getCustomerTypeSelect() == 2 ? "red" : "orange");
			String pinChar = partner.getCustomerTypeSelect() == 2 ? "P" : "C";
			if (partner.getSupplierTypeSelect() == 2 || partner.getSupplierTypeSelect() == 3) {
				pinChar = pinChar + "/S";
			}									
			objectNode.put("pinChar", pinChar);			
			arrayNode.add(objectNode);
		}
		mainNode.put("status", 0);
		mainNode.put("data", arrayNode);
		return mainNode;
	}
	
	@Transactional
	@Path("/customer")
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public JsonNode getCustomers() {
		
		List<? extends Partner> customers = Partner.all_().filter("self.customerTypeSelect=? AND self.isContact=?", 3, false).fetch();
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
				String addressString = makeAddressString(address, objectNode);
				address.save();
				objectNode.put("address", addressString);							
			}
			
			objectNode.put("pinColor", "orange");			
			objectNode.put("pinChar", "C");			
			arrayNode.add(objectNode);
		}
		mainNode.put("status", 0);
		mainNode.put("data", arrayNode);
		return mainNode;
	}

	@Transactional
	@Path("/prospect")
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public JsonNode getProspects() {
		
		List<? extends Partner> customers = Partner.all_().filter("self.customerTypeSelect=? AND self.isContact=?", 2, false).fetch();
		JsonNodeFactory factory = JsonNodeFactory.instance;
		ObjectNode mainNode = factory.objectNode();
		ArrayNode arrayNode = factory.arrayNode();
		
		for (Partner prospect : customers) {
			
			ObjectNode objectNode = factory.objectNode();
			objectNode.put("fullName", prospect.getFullName());
			objectNode.put("fixedPhone", prospect.getFixedPhone() != null ? prospect.getFixedPhone() : " ");
			
			if (prospect.getEmailAddress() != null) {
				objectNode.put("emailAddress", prospect.getEmailAddress().getAddress());
			}
			
			if (prospect.getMainInvoicingAddress() != null) {
				Address address = prospect.getMainInvoicingAddress();
				String addressString = makeAddressString(address, objectNode);
				address.save();
				objectNode.put("address", addressString);							
			}
			
			objectNode.put("pinColor", "red");			
			objectNode.put("pinChar", "P");			
			arrayNode.add(objectNode);
		}
		mainNode.put("status", 0);
		mainNode.put("data", arrayNode);
		return mainNode;
	}

	@Transactional
	@Path("/supplier")
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public JsonNode getSuppliers() {
		
		List<? extends Partner> customers = Partner.all_().filter("self.supplierTypeSelect=? AND self.isContact=?", 2, 3 , false).fetch();
		JsonNodeFactory factory = JsonNodeFactory.instance;
		ObjectNode mainNode = factory.objectNode();
		ArrayNode arrayNode = factory.arrayNode();
		
		for (Partner supplier : customers) {
			
			ObjectNode objectNode = factory.objectNode();
			objectNode.put("fullName", supplier.getFullName());
			objectNode.put("fixedPhone", supplier.getFixedPhone() != null ? supplier.getFixedPhone() : " ");
			
			if (supplier.getEmailAddress() != null) {
				objectNode.put("emailAddress", supplier.getEmailAddress().getAddress());
			}
			
			if (supplier.getMainInvoicingAddress() != null) {
				Address address = supplier.getMainInvoicingAddress();
				String addressString = makeAddressString(address, objectNode);
				address.save();
				objectNode.put("address", addressString);								
			}
			
			objectNode.put("pinColor", "purple");			
			objectNode.put("pinChar", "S");			
			arrayNode.add(objectNode);
		}
		mainNode.put("status", 0);
		mainNode.put("data", arrayNode);
		return mainNode;
	}

	@Path("/lead")
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public JsonNode getLeads() {

		List<? extends Lead> leads = Lead.all_().fetch();
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
			Map<String,Object> latlng =  addressService.getMapGoogle(qString, latit, longit);
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

		List<? extends Opportunity> opportunities = Opportunity.all_().fetch();
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
				String addressString = makeAddressString(address, objectNode);
				address.save();
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

	private String makeAddressString(Address address, ObjectNode objectNode) {

		BigDecimal latit = address.getLatit();
		BigDecimal longit = address.getLongit();
		
		if (BigDecimal.ZERO.compareTo(latit) == 0 || BigDecimal.ZERO.compareTo(longit) == 0) {
			
			String qString = address.getFullName();					
				Map<String,Object> latlng =  addressService.getMapGoogle(qString, latit, longit);
				latit = (BigDecimal) latlng.get("latitude");
				longit = (BigDecimal) latlng.get("longitude");
				address.setLatit(latit);
				address.setLongit(longit);
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
		return addressString.toString();
	}
	
	@Path("/geomap/turnover")
	@GET	
	@Produces(MediaType.APPLICATION_JSON)
	public JsonNode getGeoMapData() {
		
		Map<String, BigDecimal> data = new HashMap<String, BigDecimal>();		
		List<? extends SalesOrder> orders = SalesOrder.all_().filter("self.statusSelect=?", 3).fetch();
		JsonNodeFactory factory = JsonNodeFactory.instance;
		ObjectNode mainNode = factory.objectNode();
		ArrayNode arrayNode = factory.arrayNode();
		
		ArrayNode labelNode = factory.arrayNode();
		labelNode.add("Country");
		labelNode.add("Turnover");
		arrayNode.add(labelNode);
		
		for (SalesOrder so : orders) {
			
			Country country = so.getMainInvoicingAddress().getAddressL7Country();
			BigDecimal value = so.getExTaxTotal();		
			
			if (country != null) {
				String key = country.getName();				
				
				if (data.containsKey(key)) {
					BigDecimal oldValue = data.get(key);
					oldValue = oldValue.add(value);
					data.put(key, oldValue);
				}
				else {
					data.put(key, value);
				}				
			}			
		}
		
		Iterator<String> keys = data.keySet().iterator();
		while(keys.hasNext()) {
			String key = keys.next();
			ArrayNode dataNode  = factory.arrayNode();
			dataNode.add(key);
			dataNode.add(data.get(key));
			arrayNode.add(dataNode);
		}				
		
		mainNode.put("status", 0);
		mainNode.put("data", arrayNode);
		return mainNode;
	}	
}