package com.axelor.apps.base.service;

import java.util.Set;

import com.axelor.apps.base.db.BankDetails;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.General;
import com.axelor.apps.base.service.administration.GeneralService;
import com.axelor.inject.Beans;

public class CompanyServiceImpl implements CompanyService {

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void checkMultiBanks(Company company) {
		if (countActiveBankDetails(company) > 1) {
			GeneralService generalService = Beans.get(GeneralService.class);
			General general = generalService.getGeneral();
			if (!general.getManageMultiBanks()) {
				generalService.setManageMultiBanks(true);
			}
		}

	}

	/**
	 * Count the number of active bank details on the provided company.
	 * 
	 * @param company
	 *            the company on which we count the number of active bank
	 *            details
	 * @return the number of active bank details
	 */
	private int countActiveBankDetails(Company company) {
		int count = 0;
		Set<BankDetails> bankDetailsSet = company.getBankDetailsSet();

		if (bankDetailsSet != null) {
			for (BankDetails bankDetails : bankDetailsSet) {
				if (bankDetails.getActive()) {
					++count;
				}
			}
		}

		return count;
	}

}
