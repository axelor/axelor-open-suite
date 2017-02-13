package com.axelor.apps.account.db.repo;

import com.axelor.apps.account.db.EbicsBank;
import com.axelor.apps.account.ebics.service.EbicsBankService;
import com.google.inject.Inject;

public class EbicsBankAccountRepository extends EbicsBankRepository {
	
	@Inject
	EbicsBankService ebicsBankService;
	
	@Override
	public EbicsBank save(EbicsBank ebicsBank) {
		ebicsBankService.computeFullName(ebicsBank);

		return super.save(ebicsBank);
	}
}
