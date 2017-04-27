package com.axelor.apps.base.service;

import com.axelor.apps.base.db.Bank;

public interface BankService {
	public void splitBic(Bank bank);

	public void computeFullName(Bank bank);
}
