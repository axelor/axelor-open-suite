package com.axelor.apps.base.db.repo;

import com.axelor.apps.base.db.Bank;
import com.axelor.apps.base.service.BankService;
import com.google.inject.Inject;

public class BankBaseRepository extends BankRepository {

	@Inject
	BankService bankService;
	
	@Override
	public Bank save(Bank bank) {
		bankService.splitBic(bank);
		bankService.computeFullName(bank);
		
		return super.save(bank);
	}

}
