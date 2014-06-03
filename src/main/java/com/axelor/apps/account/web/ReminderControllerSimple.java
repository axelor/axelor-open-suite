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
