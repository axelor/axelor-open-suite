package com.axelor.apps.account.db.repo;

import java.util.List;

import com.axelor.apps.account.db.AccountingSituation;
import com.axelor.apps.account.service.AccountingSituationService;
import com.axelor.apps.base.db.IAdministration;
import com.axelor.apps.base.db.Partner;
import com.axelor.apps.base.db.repo.PartnerBaseRepository;
import com.axelor.apps.base.db.repo.PartnerRepository;
import com.axelor.apps.base.exceptions.IExceptionMessage;
import com.axelor.apps.base.service.administration.SequenceService;
import com.axelor.db.JPA;
import com.axelor.exception.AxelorException;
import com.axelor.exception.db.IException;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;

public class PartnerAccountRepository extends PartnerBaseRepository {

	@Override
	public Partner save(Partner partner) {
		try {

			if (partner.getPartnerSeq() == null){
				String seq = Beans.get(SequenceService.class).getSequenceNumber(IAdministration.PARTNER);
				if (seq == null)
					throw new AxelorException(I18n.get(IExceptionMessage.PARTNER_1),
							IException.CONFIGURATION_ERROR);
			}

			List<AccountingSituation> accountingSituationList = Beans.get(AccountingSituationService.class).createAccountingSituation(Beans.get(PartnerRepository.class).find(partner.getId()));

			if(accountingSituationList != null) {
				partner.setAccountingSituationList(accountingSituationList);
			}

			return JPA.save(partner);
		} catch (Exception e) {
			JPA.em().getTransaction().rollback();
			e.printStackTrace();
		}
		return null;
	}
}
