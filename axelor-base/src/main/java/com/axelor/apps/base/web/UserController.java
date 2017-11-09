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
package com.axelor.apps.base.web;

import com.axelor.app.AppSettings;
import com.axelor.apps.base.db.Partner;
import com.axelor.apps.base.db.repo.PartnerRepository;
import com.axelor.auth.db.User;
import com.axelor.auth.db.repo.UserRepository;
import com.axelor.exception.AxelorException;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.axelor.rpc.Context;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;

public class UserController {

	@Inject 
	protected PartnerRepository partnerRepo;
	
	@Inject
	protected UserRepository userRepo;
	
	@Transactional
	public void setUserPartner (ActionRequest request, ActionResponse response) throws AxelorException {
		Context context = request.getContext();
			
		if(context.get("user_id") != null){
			Partner partner = partnerRepo.find(context.asType(Partner.class).getId());
			User user = userRepo.find(((Integer)context.get("user_id")).longValue());
			user.setPartner(partner);
			userRepo.save(user);
		}		
	}
	
	public void applyApplicationMode(ActionRequest request, ActionResponse response)  {
		 String applicationMode = AppSettings.get().get("application.mode", "prod");
		 if ("dev".equals(applicationMode)) {
			 response.setAttr("testing", "hidden", false);
		 }
	}

}
