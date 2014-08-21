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

import java.util.List;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import com.axelor.apps.base.db.Address;
import com.axelor.apps.base.db.Partner;
import com.axelor.apps.base.service.MapService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.persist.Transactional;

@Path("/map")
public class MapRest {

	@Inject
	private Provider<MapService> mapProvider;
	
	
	@Transactional
	@Path("/partner")
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public JsonNode getPartners() {
		
		List<? extends Partner> customers = Partner.all().filter("self.customerTypeSelect IN (?, ?) OR self.supplierTypeSelect IN (?, ?) AND self.isContact=?", 2,3,2,3, false).fetch();
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
				String addressString = mapProvider.get().makeAddressString(address, objectNode);
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
		
		List<? extends Partner> customers = Partner.all().filter("self.customerTypeSelect=? AND self.isContact=?", 3, false).fetch();
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
				String addressString = mapProvider.get().makeAddressString(address, objectNode);
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
		
		List<? extends Partner> customers = Partner.all().filter("self.customerTypeSelect=? AND self.isContact=?", 2, false).fetch();
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
				String addressString = mapProvider.get().makeAddressString(address, objectNode);
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
		
		List<? extends Partner> customers = Partner.all().filter("self.supplierTypeSelect=? AND self.isContact=?", 2, 3 , false).fetch();
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
				String addressString = mapProvider.get().makeAddressString(address, objectNode);
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

	
}
