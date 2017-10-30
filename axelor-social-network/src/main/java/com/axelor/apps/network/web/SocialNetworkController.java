/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2017 Axelor (<http://axelor.com>).
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
package com.axelor.apps.network.web;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import com.axelor.apps.base.db.Partner;
import com.axelor.apps.base.db.repo.PartnerRepository;
import com.axelor.apps.network.db.SnProfile;
import com.axelor.apps.network.db.SnSearch;
import com.axelor.apps.network.db.SnUsersList;
import com.axelor.apps.network.db.repo.SnProfileRepository;
import com.axelor.apps.network.db.repo.SnUsersListRepository;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.axelor.meta.schema.actions.ActionView;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.google.inject.persist.Transactional;

public class SocialNetworkController {
	
	
	@Inject
	SnProfileRepository snProfileRepository;
	
	@Inject
	SnUsersListRepository snUsersListRepository;
	
	public void searchPartner(ActionRequest request, ActionResponse response) {
		Partner partner = Beans.get(PartnerRepository.class).find(new Long(request.getContext().get("_idPartner").toString()));
		
		response.setValue("partner", partner);
	}
	
	public SnSearch searchProfile(ActionRequest request, ActionResponse response) {
		SnSearch snSearch = request.getContext().asType(SnSearch.class);
		
		Partner partner = Beans.get(PartnerRepository.class).find(snSearch.getPartner().getId());
		
		String lastName = snSearch.getLastName();
		String firstName = snSearch.getFirstName();

		HashMap<String, String> listCriterias = new HashMap<>();
		
		listCriterias.put("lastname", lastName);
		listCriterias.put("firstName", firstName);
				
		
		List<Map> requestedElements = new ArrayList<>();
		HashMap<String, String> elements = new HashMap<>();
		List<SnUsersList> listResult = new ArrayList<>();
		
		elements.put("lastName", "PHAN");
		elements.put("firstName", "Jeannine");
		elements.put("email", "j.phan@axelor.com");
		elements.put("uniqueID", "1234");
		elements.put("sn", "facebook");
		requestedElements.add(elements);

		elements = new HashMap<>();
		elements.put("lastName", "PHAN");
		elements.put("firstName", "Jeannine");
		elements.put("email", "j.phan@axelor.com");
		elements.put("uniqueID", "9876");
		elements.put("sn", "linkedin");
		requestedElements.add(elements);

		elements = new HashMap<>();
		elements.put("lastName", "DUBAUX");
		elements.put("firstName", "Geoffrey");
		elements.put("email", "g.dubaux@axelor.com");
		elements.put("uniqueID", "abcd");
		elements.put("sn", "facebook");
		requestedElements.add(elements);
		
		for(Map element : requestedElements) {
			if(element.containsValue(lastName)){
				if(element.containsValue(firstName)){
					listResult.add(addUserIntoList(element, partner));
					
				}
			}
		}
		
		snSearch.setSnProfile(listResult);
		snSearch.setPartner(partner);
		
		response.setValue("snProfile", listResult);
		
		return snSearch;
	
	}
	
	public SnUsersList addUserIntoList(Map element, Partner partner) {
		SnUsersList newUser = new SnUsersList();
		
		newUser.setLastName(element.get("lastName").toString());
		newUser.setFirstName(element.get("firstName").toString());
		newUser.setEmail(element.get("email").toString());
		newUser.setUniqueID(element.get("uniqueID").toString());
		newUser.setSn(element.get("sn").toString());
		newUser.setPartner(partner);
		
		return newUser;
		
	}
	
	public void seeProfile(ActionRequest request, ActionResponse response) {
		String userUniqueID = request.getContext().asType(SnUsersList.class).getUniqueID();
		
		Partner partner = request.getContext().asType(SnUsersList.class).getPartner();
		
		List<Map> requestedElements = new ArrayList<>();
		HashMap<String, String> elements = new HashMap<>();
		
		elements.put("lastName", "PHAN");
		elements.put("firstName", "Jeannine");
		elements.put("email", "j.phan@axelor.com");
		elements.put("uniqueID", "1234");
		elements.put("birthDate", "1993-06-03");
		elements.put("sexe", "F");
		elements.put("diploma", "bac L");
		elements.put("sn", "facebook");
		requestedElements.add(elements);

		elements = new HashMap<>();
		elements.put("lastName", "PHAN");
		elements.put("firstName", "Jeannine");
		elements.put("email", "j.phan@axelor.com");
		elements.put("uniqueID", "9876");
		elements.put("birthDate", "2000-07-02");
		elements.put("maritalStatus", "single");
		elements.put("sn", "linkedin");
		requestedElements.add(elements);

		elements = new HashMap<>();
		elements.put("lastName", "DUBAUX");
		elements.put("firstName", "Geoffrey");
		elements.put("email", "g.dubaux@axelor.com");
		elements.put("uniqueID", "abcd");
		elements.put("birthDate", "1990-06-06");
		elements.put("sn", "facebook");
		requestedElements.add(elements);
		
		String lastName = null;
		String firstName = null;
		String email = null;
		String uniqueID = null;
		LocalDate birth = null;
		String sn = null;
		String other = null;
		
		
		
		for(Map element : requestedElements) {
			if(element.containsValue(userUniqueID)){
				DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
				birth = LocalDate.parse(element.get("birthDate").toString(), formatter);
				
				lastName = element.get("lastName").toString();
				firstName = element.get("firstName").toString();
				email = element.get("email").toString();
				uniqueID = element.get("uniqueID").toString();
				sn = element.get("sn").toString();
				
			}
		}
		
		response.setView(ActionView
		  		.define(I18n.get("Bon"))
		  		.model("com.axelor.apps.network.db.SnUsersList")
		  		.add("form", "sn-user-profile-form")
		  		.param("popup", "reload")
		  		.context("_lastName", lastName)
		  		.context("_firstName", firstName)
		  		.context("_email", email)
		  		.context("_uniqueID", uniqueID)
		  		.context("_birthDate", birth)
		  		.context("_sn", sn)
		  		.context("_partner", partner)
		  		.map());
		
	}

	@Transactional
	public void importProfile(ActionRequest request, ActionResponse response) {
		
		SnUsersList snUser = request.getContext().asType(SnUsersList.class);
		snUser = snUsersListRepository.find(snUser.getId());
		Integer snType = 0;
		String snUserSN = snUser.getSn();

		SnProfile snProfile = new SnProfile();
		snProfile.setUniqueID(snUser.getUniqueID());
		snProfile.setPartner(snUser.getPartner());
		if(snUserSN.equals("facebook")) {
			snType = 1;
		} else if (snUserSN.equals("linkedin")) {
			snType = 2;
		} else if (snUserSN.equals("twitter")) {
			snType = 3;
		}
		
		snProfile.setSnTypeSelect(snType);
		
		snProfile.setSnUserDetail(snUser);
		snUser.setSnProfile(snProfile);
		snProfileRepository.save(snProfile);
	}
	
}
