package com.axelor.apps.bank.payment.db.repo;

import com.axelor.apps.bank.payment.db.EbicsCertificate;
import com.axelor.apps.bank.payment.ebics.service.EbicsCertificateService;
import com.google.inject.Inject;

public class EbicsCertificateAccountRepository extends EbicsCertificateRepository {
	
	@Inject
	private EbicsCertificateService certificateService;
	
	@Override
	public EbicsCertificate save(EbicsCertificate entity) {
		
		certificateService.computeFullName(entity);
		
		return super.save(entity);
	}
}
