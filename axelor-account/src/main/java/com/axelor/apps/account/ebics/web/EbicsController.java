/**
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
package com.axelor.apps.account.ebics.web;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import org.apache.commons.codec.digest.DigestUtils;

import com.axelor.apps.account.db.EbicsBank;
import com.axelor.apps.account.db.EbicsCertificate;
import com.axelor.apps.account.db.EbicsUser;
import com.axelor.apps.account.db.repo.BankOrderFileFormatRepository;
import com.axelor.apps.account.db.repo.EbicsBankRepository;
import com.axelor.apps.account.db.repo.EbicsCertificateRepository;
import com.axelor.apps.account.db.repo.EbicsUserRepository;
import com.axelor.apps.account.ebics.certificate.CertificateManager;
import com.axelor.apps.account.ebics.service.EbicsCertificateService;
import com.axelor.apps.account.ebics.service.EbicsService;
import com.axelor.apps.account.exception.IExceptionMessage;
import com.axelor.auth.db.User;
import com.axelor.exception.AxelorException;
import com.axelor.exception.db.IException;
import com.axelor.i18n.I18n;
import com.axelor.meta.MetaFiles;
import com.axelor.meta.db.MetaFile;
import com.axelor.meta.schema.actions.ActionView;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.axelor.rpc.Context;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;

public class EbicsController {
	
	@Inject
	private EbicsUserRepository ebicsUserRepo;
	
	@Inject
	private EbicsService ebicsService;
	
	@Inject
	private EbicsBankRepository bankRepo;
	
	@Inject
	private EbicsCertificateService certificateService;
	
	@Inject
	private EbicsCertificateRepository certificateRepo;
	
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
		User user = ebicsUser.getAssociatedUser();
		if (user != null){
			response.setValue("dn", ebicsService.makeDN(ebicsUser.getName(), user.getEmail(), "France", user.getActiveCompany().getCode()) );
		}else{
			response.setValue("dn", ebicsService.makeDN(ebicsUser.getName(), null, "France", null) );
		}
		
	}
	
	public void sendINIRequest(ActionRequest request, ActionResponse response) {
		
		EbicsUser ebicsUser = ebicsUserRepo.find( request.getContext().asType(EbicsUser.class).getId());
		
		try {
			ebicsService.sendINIRequest(ebicsUser, null);
		}catch (AxelorException e) {
			e.printStackTrace();
			response.setFlash(stripClass(e.getLocalizedMessage()));
		}
		
		response.setReload(true);
		
	}
	
	public void sendHIARequest(ActionRequest request, ActionResponse response) {
		
		EbicsUser ebicsUser = ebicsUserRepo.find( request.getContext().asType(EbicsUser.class).getId());
		
		try {
			ebicsService.sendHIARequest(ebicsUser, null);
		}catch (AxelorException e) {
			e.printStackTrace();
			response.setFlash(stripClass(e.getLocalizedMessage()));
		}
		
		response.setReload(true);
	}
	
	public void sendHPBRequest(ActionRequest request, ActionResponse response) {
		
		EbicsUser ebicsUser = ebicsUserRepo.find( request.getContext().asType(EbicsUser.class).getId());
		
		try {
			X509Certificate[] certificates = ebicsService.sendHPBRequest(ebicsUser, null);
			confirmCertificates(ebicsUser, certificates, response);
		}catch (AxelorException e) {
			e.printStackTrace();
			response.setFlash(stripClass(e.getLocalizedMessage()));
		}
		
		response.setReload(true);
	}
	
	private void confirmCertificates(EbicsUser user, X509Certificate[] certificates, ActionResponse response)  {

		
		try {
			EbicsBank bank = user.getEbicsPartner().getEbicsBank();
			response.setView(ActionView.define("Confirm certificates")
				.model("com.axelor.apps.account.db.EbicsCertificate")
				.add("form", "ebics-certificate-confirmation-form")
				.param("show-toolbar", "false")
				.param("show-confirm", "false")
				.param("popup-save", "false")
				.param("popup", "true")
				.context("ebicsBank", bank)
				.context("url", bank.getUrl())
				.context("hostId", bank.getHostId())
				.context("e002Hash", DigestUtils.sha1Hex(certificates[0].getEncoded()).toUpperCase())
				.context("x002Hash", DigestUtils.sha1Hex(certificates[1].getEncoded()).toUpperCase())
				.context("certificateE002", certificateService.convertToPEMString(certificates[0]))
				.context("certificateX002", certificateService.convertToPEMString(certificates[1])).map());
		}catch(Exception e) {
			response.setFlash("Error in certificate confirmation ");
		}
			
	}
	
	public void sendSPRRequest(ActionRequest request, ActionResponse response) {
		
		EbicsUser ebicsUser = ebicsUserRepo.find( request.getContext().asType(EbicsUser.class).getId());
		
		try {
			ebicsService.sendSPRRequest(ebicsUser, null);
		}catch (AxelorException e) {
			e.printStackTrace();
			response.setFlash(stripClass(e.getLocalizedMessage()));
		}

		response.setReload(true);
	}
	
	public void sendFULRequest(ActionRequest request, ActionResponse response) {
		
		EbicsUser ebicsUser = ebicsUserRepo.find( request.getContext().asType(EbicsUser.class).getId());
		
		try {
			
			EbicsBank ebicsBank = ebicsUser.getEbicsPartner().getEbicsBank();
			
			MetaFile testMetaFile = ebicsBank.getTestFile();
			
			if(ebicsBank.getTestMode() && testMetaFile != null)  { 
				ebicsService.sendFULRequest(ebicsUser, null, MetaFiles.getPath(testMetaFile).toFile(), BankOrderFileFormatRepository.FILE_FORMAT_pain_001_001_02_SCT);
			}
			else  {
				response.setFlash(I18n.get(IExceptionMessage.EBICS_TEST_MODE_NOT_ENABLED));
			}
			
		}catch (AxelorException e) {
			response.setFlash(stripClass(e.getLocalizedMessage()));
		}

		response.setReload(true);
	}
	
	public void sendFDLRequest(ActionRequest request, ActionResponse response) {
		
		EbicsUser ebicsUser = ebicsUserRepo.find( request.getContext().asType(EbicsUser.class).getId());
		
		try {
			ebicsService.sendFDLRequest(ebicsUser, null, null, null, BankOrderFileFormatRepository.FILE_FORMAT_pain_001_001_02_SCT);
		}catch (AxelorException e) {
			response.setFlash(stripClass(e.getLocalizedMessage()));
		}
		
		response.setReload(true);
	}
	
	public void sendHTDRequest(ActionRequest request, ActionResponse response) {
		
		EbicsUser ebicsUser = ebicsUserRepo.find( request.getContext().asType(EbicsUser.class).getId());
		
		try {
			ebicsService.sendHTDRequest(ebicsUser, null, null, null);
		}catch (AxelorException e) {
			response.setFlash(stripClass(e.getLocalizedMessage()));
		}

		response.setReload(true);
	}
	
	public void sendPTKRequest(ActionRequest request, ActionResponse response) {
		
		EbicsUser ebicsUser = ebicsUserRepo.find( request.getContext().asType(EbicsUser.class).getId());
		
		try {
			ebicsService.sendPTKRequest(ebicsUser, null, null, null);
		}catch (AxelorException e) {
			response.setFlash(stripClass(e.getLocalizedMessage()));
		}

		response.setReload(true);
	}
	
	private String stripClass(String msg) {
		
		return msg.replace(AxelorException.class.getName() + ":", "");
	}
	
	public void addCertificates(ActionRequest request, ActionResponse response) throws AxelorException {
		
		Context context = request.getContext();
		
		EbicsBank bank = (EbicsBank)context.get("ebicsBank");
		
		bank = bankRepo.find(bank.getId());
		
		try {
			X509Certificate certificate =  certificateService.convertToCertificate((String)context.get("certificateE002"));
			certificateService.createCertificate(certificate, bank, "encryption");
			
			certificate =  certificateService.convertToCertificate((String)context.get("certificateX002"));
			certificateService.createCertificate(certificate, bank, "authentication");
			
			
		} catch (CertificateException | IOException e) {
			e.printStackTrace();
			throw new AxelorException(I18n.get("Error in adding bank certificate"), IException.CONFIGURATION_ERROR);
		}
		
		response.setCanClose(true);
	}
	
	public void loadCertificate(ActionRequest request, ActionResponse response) throws AxelorException, CertificateEncodingException, IOException {
		
		EbicsCertificate cert = request.getContext().asType(EbicsCertificate.class);
		
		cert = certificateRepo.find(cert.getId());
		
		byte[] certs = cert.getCertificate();
		
		if (certs != null && certs.length > 0) {
			X509Certificate certificate = EbicsCertificateService.getCertificate(certs, cert.getTypeSelect());
			cert = certificateService.updateCertificate(certificate, cert);
			response.setValue("validFrom", cert.getValidFrom());
			response.setValue("validTo", cert.getValidTo());
			response.setValue("issuer", cert.getIssuer());
			response.setValue("subject", cert.getSubject());
			response.setValue("publicKeyModulus", cert.getPublicKeyModulus());
			response.setValue("publicKeyExponent", cert.getPublicKeyExponent());
			response.setValue("fullName", cert.getFullName());
			response.setValue("pemString", cert.getPemString());
			response.setValue("sha2has", cert.getSha2has());
		}
		
	}
	
	
}
