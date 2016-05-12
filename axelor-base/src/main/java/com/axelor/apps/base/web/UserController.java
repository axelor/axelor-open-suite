package com.axelor.apps.base.web;

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
}
