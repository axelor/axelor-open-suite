package com.axelor.apps.account.ebics.web;

import java.io.IOException;
import java.security.GeneralSecurityException;

import org.jdom.JDOMException;

import com.axelor.apps.account.db.EbicsUser;
import com.axelor.apps.account.db.repo.EbicsUserRepository;
import com.axelor.apps.account.ebics.certificate.CertificateManager;
import com.axelor.apps.account.ebics.service.EbicsService;
import com.axelor.auth.db.User;
import com.axelor.auth.db.repo.UserRepository;
import com.axelor.exception.AxelorException;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;

public class EbicsController {
	
	@Inject
	EbicsUserRepository ebicsUserRepo;
	
	@Inject
	EbicsService ebicsService;
	
	@Inject
	UserRepository userRepo;
	
	@Transactional
	public void generateCertificate(ActionRequest request, ActionResponse response){
		
		EbicsUser ebicsUser = ebicsUserRepo.find(request.getContext().asType(EbicsUser.class).getId());
		
		if (ebicsUser.getStatusSelect() != EbicsUserRepository.STATUS_WAITING_CERTIFICATE_CONFIG 
				&& ebicsUser.getStatusSelect() != EbicsUserRepository.STATUS_CERTIFICATES_SHOULD_BE_RENEW) {
		      return;
	    }
		
		CertificateManager cm = new CertificateManager(ebicsUser);
		try {
			cm.create();
			ebicsUser.setStatusSelect(EbicsUserRepository.STATUS_WAITING_SENDING_SIGNATURE_CERTIFICATE);
			ebicsUserRepo.save(ebicsUser);
		} catch (GeneralSecurityException | IOException e) {
			e.printStackTrace();
		}
		response.setReload(true);
		
	}
	
	public void generateDn(ActionRequest request, ActionResponse response){
		
		EbicsUser ebicsUser = ebicsUserRepo.find(request.getContext().asType(EbicsUser.class).getId());
		User user = userRepo.all().filter("self.ebicsUser = ?1", ebicsUser).fetchOne();
		if (user != null){
			response.setValue("dn", ebicsService.makeDN(ebicsUser.getName(), user.getEmail(), "France", user.getActiveCompany().getCode()) );
		}else{
			response.setValue("dn", ebicsService.makeDN(ebicsUser.getName(), null, "France", null) );
		}
		
		
		
	}
	
	public void sendINIRequest(ActionRequest request, ActionResponse response) throws AxelorException, IOException, JDOMException{
		
		EbicsUser ebicsUser = ebicsUserRepo.find( request.getContext().asType(EbicsUser.class).getId());
		
		ebicsService.sendINIRequest(ebicsUser, null);
		
		response.setReload(true);
		
	}
	
	public void sendHIARequest(ActionRequest request, ActionResponse response) throws AxelorException, IOException {
		
		EbicsUser ebicsUser = ebicsUserRepo.find( request.getContext().asType(EbicsUser.class).getId());
		
		ebicsService.sendHIARequest(ebicsUser, null);
		
		response.setReload(true);
	}
	
	public void sendHPBRequest(ActionRequest request, ActionResponse response) throws AxelorException, IOException {
		
		EbicsUser ebicsUser = ebicsUserRepo.find( request.getContext().asType(EbicsUser.class).getId());
		
		ebicsService.sendHPBRequest(ebicsUser, null);

		response.setReload(true);
	}
	
	public void sendSPRRequest(ActionRequest request, ActionResponse response) throws AxelorException, IOException {
		
		EbicsUser ebicsUser = ebicsUserRepo.find( request.getContext().asType(EbicsUser.class).getId());
		
		ebicsService.revokeSubscriber(ebicsUser, null);

		response.setReload(true);
	}
	
	public void sendFULRequest(ActionRequest request, ActionResponse response) throws AxelorException, IOException {
		
		EbicsUser ebicsUser = ebicsUserRepo.find( request.getContext().asType(EbicsUser.class).getId());
		
		ebicsService.sendFULRequest(ebicsUser, null, null);

		response.setReload(true);
	}
	
	public void sendFDLRequest(ActionRequest request, ActionResponse response) throws AxelorException, IOException {
		
		EbicsUser ebicsUser = ebicsUserRepo.find( request.getContext().asType(EbicsUser.class).getId());
		
		ebicsService.sendFDLRequest(ebicsUser, null, null, null);

		response.setReload(true);
	}
	
	public void sendHTDRequest(ActionRequest request, ActionResponse response) throws AxelorException, IOException {
		
		EbicsUser ebicsUser = ebicsUserRepo.find( request.getContext().asType(EbicsUser.class).getId());
		
		ebicsService.sendHTDRequest(ebicsUser, null, null, null);

		response.setReload(true);
	}


}
