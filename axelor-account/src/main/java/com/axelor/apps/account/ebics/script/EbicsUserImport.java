package com.axelor.apps.account.ebics.script;

import java.io.IOException;
import java.security.cert.CertificateEncodingException;
import java.security.cert.X509Certificate;
import java.util.Map;

import com.axelor.apps.account.db.EbicsBank;
import com.axelor.apps.account.db.EbicsCertificate;
import com.axelor.apps.account.db.EbicsPartner;
import com.axelor.apps.account.db.EbicsUser;
import com.axelor.apps.account.ebics.service.EbicsCertificateService;
import com.axelor.apps.base.db.Bank;
import com.axelor.apps.base.service.BankService;
import com.google.inject.Inject;

public class EbicsUserImport {
	
	@Inject
	private EbicsCertificateService certificateService;
	
	@Inject
	private BankService bankService;
	
	public Object importEbicsUser(Object bean, Map<String,Object> context) {
		
		assert bean instanceof EbicsUser;
		
		EbicsUser user = (EbicsUser) bean;
		
		updateCertificate(user.getA005Certificate());
		updateCertificate(user.getE002Certificate());
		updateCertificate(user.getX002Certificate());
		
		EbicsPartner partner = user.getEbicsPartner() ;
		if (partner != null) {
			EbicsBank ebicsBank = partner.getEbicsBank();
			if (ebicsBank.getVersion() == 0) { 
				for (EbicsCertificate cert : ebicsBank.getEbicsCertificateList()) {
					updateCertificate(cert);
				}
				Bank bank = ebicsBank.getBank();
				if (bank.getVersion() == 0) {
					bankService.computeFullName(bank);
					bankService.splitBic(bank);
				}
			}
		}
		
		return user;
	}
	
	private void updateCertificate(EbicsCertificate cert) {
		
		if (cert == null) {
			return;
		}
		
		String pem = cert.getPemString();
		if (pem == null) {
			return;
		}
		
		try {
			X509Certificate certificate = certificateService.convertToCertificate(pem);
			certificateService.updateCertificate(certificate, cert);
		} catch (IOException | CertificateEncodingException e) {
			e.printStackTrace();
		}
		
	}
}
