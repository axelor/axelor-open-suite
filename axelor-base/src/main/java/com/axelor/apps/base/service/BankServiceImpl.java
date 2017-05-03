package com.axelor.apps.base.service;

import com.axelor.apps.base.db.Bank;
import com.axelor.apps.base.db.Country;
import com.axelor.apps.base.db.repo.CountryRepository;
import com.axelor.inject.Beans;

public class BankServiceImpl implements BankService {
	@Override
	public void splitBic(Bank bank) {
		String bic = bank.getCode();

		// BNPA FR PP XXX
		// 0123 45 67 8910

		bank.setBusinessPartyPrefix(bic.substring(0, 4));

		String alpha2 = bic.substring(4, 6);
		Country country = Beans.get(CountryRepository.class).all().filter("alpha2code = ?", alpha2).fetchOne();
		bank.setCountry(country);

		bank.setBusinessPartySuffix(bic.substring(6, 8));

		String branchId;
		try {
			branchId = bic.substring(8, 11);
		} catch (IndexOutOfBoundsException e) {
			branchId = "XXX";
		} catch (Exception e) {
			throw e;
		}
		bank.setBranchIdentifier(branchId);
	}

	@Override
	public void computeFullName(Bank bank) {
		bank.setFullName(bank.getCode() + " - " + bank.getBankName());
	}
}
