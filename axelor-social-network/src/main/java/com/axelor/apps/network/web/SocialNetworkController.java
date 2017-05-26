package com.axelor.apps.network.web;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.axelor.apps.base.db.Partner;
import com.axelor.apps.base.db.repo.PartnerRepository;
import com.axelor.apps.network.db.SnProfile;
import com.axelor.apps.network.db.SnSearch;
import com.axelor.apps.network.db.SnUsersList;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.axelor.meta.schema.actions.ActionView;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;

public class SocialNetworkController {
	
	
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
					listResult.add(addUserIntoList(element));
					
				}
			}
		}
		
		snSearch.setSnProfile(listResult);
		
		response.setValue("snProfile", listResult);
		
		return snSearch;
	
	}
	
	public SnUsersList addUserIntoList(Map element) {
		SnUsersList newUser = new SnUsersList();
		
		newUser.setLastName(element.get("lastName").toString());
		newUser.setFirstName(element.get("firstName").toString());
		newUser.setEmail(element.get("email").toString());
		newUser.setUniqueID(element.get("uniqueID").toString());
		newUser.setSn(element.get("sn").toString());
		
		return newUser;
		
	}
	
	public void seeProfile(ActionRequest request, ActionResponse response) {
		String userUniqueID = request.getContext().asType(SnUsersList.class).getUniqueID();
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
				
				System.out.println(birth);
				
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
		  		.map());
		
	}
	
	
	public void importProfile(ActionRequest request, ActionResponse response) {
		SnProfile userProfile = new SnProfile();
	}

	
}
