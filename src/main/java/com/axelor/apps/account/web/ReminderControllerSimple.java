package com.axelor.apps.account.web;

import java.util.HashMap;
import java.util.Map;

import com.axelor.apps.base.db.Mail;
import com.axelor.apps.base.db.Partner;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;

public class ReminderControllerSimple {

	public void showReminderMail(ActionRequest request, ActionResponse response) {
		
		Partner partner = request.getContext().asType(Partner.class);
		
		Map<String,Object> mapView = new HashMap<String,Object>();
		mapView.put("title", "Courriers");
		mapView.put("resource", Mail.class.getName());
		mapView.put("domain", "self.base.id = "+partner.getId()+" AND self.typeSelect = 1");
		response.setView(mapView);	
	}
	
	public void showReminderEmail(ActionRequest request, ActionResponse response) {
		
		Partner partner = request.getContext().asType(Partner.class);
		
		Map<String,Object> mapView = new HashMap<String,Object>();
		mapView.put("title", "Emails");
		mapView.put("resource", Mail.class.getName());
		mapView.put("domain", "self.base.id = "+partner.getId()+" AND self.typeSelect = 0");
		response.setView(mapView);		
	}
}
