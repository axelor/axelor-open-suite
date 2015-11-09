package com.axelor.apps.account.db.repo;

import java.util.List;

import javax.persistence.PersistenceException;

import com.axelor.apps.account.db.AccountingSituation;
import com.axelor.apps.account.service.AccountingSituationService;
import com.axelor.apps.base.db.Partner;
import com.axelor.apps.base.db.repo.PartnerBaseRepository;
import com.axelor.apps.base.db.repo.PartnerRepository;
import com.axelor.inject.Beans;

public class PartnerAccountRepository extends PartnerBaseRepository {

	@Override
	public Partner save(Partner partner) {
		try {

			if(partner.getId() == null){
				return super.save(partner);
			}
			if(!partner.getIsContact()){
				List<AccountingSituation> accountingSituationList = Beans.get(AccountingSituationService.class).createAccountingSituation(Beans.get(PartnerRepository.class).find(partner.getId()));

				if(accountingSituationList != null) {
					partner.setAccountingSituationList(accountingSituationList);
				}
			}
			

			return super.save(partner);
		} catch (Exception e) {
			throw new PersistenceException(e.getLocalizedMessage());
		}
	}
}
