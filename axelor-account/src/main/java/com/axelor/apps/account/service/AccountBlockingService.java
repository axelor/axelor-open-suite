package com.axelor.apps.account.service;

import org.joda.time.LocalDate;

import com.axelor.apps.base.db.Blocking;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.Partner;
import com.axelor.apps.base.service.BlockingService;
import com.axelor.apps.base.service.administration.GeneralService;
import com.axelor.inject.Beans;
import com.google.inject.Inject;

public class AccountBlockingService extends BlockingService {

	private LocalDate today;

	@Inject
	public AccountBlockingService() {

		this.today = Beans.get(GeneralService.class).getTodayDate();
	}

	
	
	/**
	 * Le tiers est t'il bloqué en prélèvement
	 *
	 * @return
	 */
	public boolean isDebitBlockingBlocking(Blocking blocking){

		if (blocking != null && blocking.getDebitBlockingOk()){

			if (blocking.getDebitBlockingToDate() != null && blocking.getDebitBlockingToDate().isBefore(today)){
				return false;
			}
			else {
				return true;
			}

		}

		return false;
	}


	/**
	 * Le tiers est t'il bloqué en prélèvement
	 *
	 * @return
	 */
	public boolean isDebitBlockingBlocking(Partner partner, Company company){

		return this.isDebitBlockingBlocking(
				this.getBlocking(partner, company));
	}
}
