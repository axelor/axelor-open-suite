package com.axelor.apps.supplychain.service;

import com.axelor.apps.account.db.AccountingSituation;
import com.axelor.apps.account.db.repo.AccountingSituationRepository;
import com.axelor.apps.account.service.AccountCustomerService;
import com.axelor.apps.account.service.AccountingSituationService;
import com.axelor.apps.base.db.Partner;
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.exception.AxelorException;
import com.axelor.inject.Beans;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;

public class AccountCustomerServiceSupplyChain extends AccountCustomerService {

	@Inject
	public AccountCustomerServiceSupplyChain(AccountingSituationService accountingSituationService,
			AccountingSituationRepository accSituationRepo, AppBaseService appBaseService) {
		super(accountingSituationService, accSituationRepo, appBaseService);
	}

	@Override
	@Transactional(rollbackOn = { AxelorException.class, Exception.class })
	public void updateCustomerAccount(AccountingSituation accountingSituation) throws AxelorException {
		super.updateCustomerAccount(accountingSituation);
		updateCustomerCreditLines(accountingSituation.getPartner());
	}

	@Override
	@Transactional(rollbackOn = { AxelorException.class, Exception.class })
	public AccountingSituation updateAccountingSituationCustomerAccount(AccountingSituation accountingSituation,
			boolean updateCustAccount, boolean updateDueCustAccount, boolean updateDueReminderCustAccount)
			throws AxelorException {

		accountingSituation = super.updateAccountingSituationCustomerAccount(accountingSituation, updateCustAccount,
				updateDueCustAccount, updateDueReminderCustAccount);

		if (updateCustAccount) {
			updateCustomerCreditLines(accountingSituation.getPartner());
		}

		return accountingSituation;
	}

}
