package com.axelor.apps.bank.payment.db.repo;

import com.axelor.apps.bank.payment.db.EbicsBank;
import com.axelor.apps.bank.payment.ebics.service.EbicsBankService;
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
