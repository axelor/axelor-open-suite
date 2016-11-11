package com.axelor.apps.account.ebics.web;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Locale;

import org.jdom.JDOMException;

import com.axelor.apps.account.db.EbicsUser;
import com.axelor.apps.account.db.repo.EbicsUserRepository;
import com.axelor.apps.account.ebics.certificate.CertificateManager;
import com.axelor.apps.account.ebics.client.EbicsProduct;
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
		
		CertificateManager cm = new CertificateManager(ebicsUser);
		try {
			cm.create();
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
		
		EbicsProduct ebicsProduct = new EbicsProduct("Test", Locale.FRENCH, "01");
		ebicsService.sendINIRequest(ebicsUser, ebicsProduct);
		
		response.setReload(true);
		
	}
	
	public void sendHIARequest(ActionRequest request, ActionResponse response) throws AxelorException, IOException, JDOMException{
		
		EbicsUser ebicsUser = ebicsUserRepo.find( request.getContext().asType(EbicsUser.class).getId());
		
		EbicsProduct ebicsProduct = new EbicsProduct("Test", Locale.FRENCH, "01");
		ebicsService.sendHIARequest(ebicsUser, ebicsProduct);
		
		response.setReload(true);
	}
	
	public void sendHPBRequest(ActionRequest request, ActionResponse response) throws AxelorException, IOException, JDOMException{
		
		EbicsUser ebicsUser = ebicsUserRepo.find( request.getContext().asType(EbicsUser.class).getId());
		
		EbicsProduct ebicsProduct = new EbicsProduct("Test", Locale.FRENCH, "01");
		ebicsService.sendHPBRequest(ebicsUser, ebicsProduct);

		response.setReload(true);
	}
	
	public void sendSPRRequest(ActionRequest request, ActionResponse response) throws AxelorException, IOException, JDOMException{
		
		EbicsUser ebicsUser = ebicsUserRepo.find( request.getContext().asType(EbicsUser.class).getId());
		
		EbicsProduct ebicsProduct = new EbicsProduct("Test", Locale.FRENCH, "01");
		ebicsService.revokeSubscriber(ebicsUser, ebicsProduct);

		response.setReload(true);
	}


}
