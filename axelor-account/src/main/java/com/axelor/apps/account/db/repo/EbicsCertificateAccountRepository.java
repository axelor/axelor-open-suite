package com.axelor.apps.account.db.repo;

import com.axelor.apps.account.db.EbicsCertificate;
import com.axelor.apps.account.ebics.service.EbicsCertificateService;
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
