/**
 * Axelor Business Solutions
 *
 * Copyright (C) 2016 Axelor (<http://axelor.com>).
 *
 * This program is free software: you can redistribute it and/or  modify
 * it under the terms of the GNU Affero General Public License, version 3,
 * as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
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
