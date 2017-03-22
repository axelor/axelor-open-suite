package com.axelor.apps.bankpayment.db.repo;

import com.axelor.apps.bankpayment.db.EbicsBank;
import com.axelor.apps.bankpayment.ebics.service.EbicsBankService;
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
