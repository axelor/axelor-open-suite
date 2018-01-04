/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2018 Axelor (<http://axelor.com>).
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
package com.axelor.apps.account.service.move;

import java.util.ArrayList;
import java.util.List;

import com.axelor.apps.account.db.Move;
import com.axelor.apps.account.db.MoveLine;
import com.axelor.apps.account.service.AccountCustomerService;
import com.axelor.apps.account.service.AccountingService;
import com.axelor.apps.base.db.Partner;
import com.axelor.exception.AxelorException;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;

public class MoveCustAccountService {

	protected AccountCustomerService accountCustomerService;

	@Inject
	public MoveCustAccountService(AccountCustomerService accountCustomerService) {

		this.accountCustomerService = accountCustomerService;

	}


	public void flagPartners(Move move) throws AxelorException {

		accountCustomerService.flagPartners(this.getPartnerOfMove(move), move.getCompany());

	}


	/**
	 * Mise à jour du compte client
	 *
	 * @param move
	 *
	 * @throws AxelorException
	 */
	@Transactional(rollbackOn = {AxelorException.class, Exception.class})
	public void updateCustomerAccount(Move move) throws AxelorException {

		if( AccountingService.getUpdateCustomerAccount() )  {
			accountCustomerService.updatePartnerAccountingSituation(this.getPartnerOfMove(move), move.getCompany(), true, true, false);
		}
		else  {
			this.flagPartners(move);
		}
	}

	
	/**
	 * Méthode permettant de récupérer la liste des tiers distincts impactés par l'écriture
	 * @param move
	 * 			Une écriture
	 * @return
	 */
	public List<Partner> getPartnerOfMove(Move move)  {
		List<Partner> partnerList = new ArrayList<Partner>();
		for(MoveLine moveLine : move.getMoveLineList())  {
			if(moveLine.getAccount() != null && moveLine.getAccount().getUseForPartnerBalance() && moveLine.getPartner() != null
					&& !partnerList.contains(moveLine.getPartner()))  {
				partnerList.add(moveLine.getPartner());
			}
		}
		return partnerList;
	}

		
}