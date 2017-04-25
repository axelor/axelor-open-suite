package com.axelor.apps.base.service;

import java.util.Set;

import com.axelor.apps.base.db.BankDetails;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.General;
import com.axelor.apps.base.service.administration.GeneralService;
import com.axelor.exception.AxelorException;
import com.axelor.inject.Beans;
import com.google.inject.persist.Transactional;

public class CompanyServiceImpl implements CompanyService {

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.axelor.apps.base.service.CompanyService#checkMultiBanks(com.axelor.
	 * apps.base.db.Company)
	 */
	@Override
	public void checkMultiBanks(Company company) {
		int count = 0;

		Set<BankDetails> bankDetailsSet = company.getBankDetailsSet();

		if (bankDetailsSet == null) {
			return;
		}

		for (BankDetails bankDetails : bankDetailsSet) {
			if (bankDetails.getActive()) {
				++count;
			}
		}

		if (count > 1) {
			EnableManageMultiBanks();
		}

	}

	/**
	 * Enable the boolean manageMultiBanks in the general object.
	 */
	@Transactional(rollbackOn = { AxelorException.class, Exception.class })
	void EnableManageMultiBanks() {
		General general = Beans.get(GeneralService.class).getGeneral();
		general.setManageMultiBanks(true);
	}

}
