/**
 * Axelor Business Solutions
 *
 * Copyright (C) 2012-2014 Axelor (<http://axelor.com>).
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
package com.axelor.apps.account.service;

import com.axelor.apps.account.db.Reimbursement;
import com.axelor.apps.account.db.repo.ReimbursementRepository;
import com.axelor.apps.base.db.BankDetails;
import com.axelor.apps.base.db.Partner;
import com.axelor.apps.base.service.PartnerService;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;

public class ReimbursementService extends  ReimbursementRepository{
	
	@Inject
	private PartnerService partnerService;
	
	/**
	 * Procédure permettant de mettre à jour la liste des RIBs du tiers
	 * @param reimbursement
	 * 				Un remboursement
	 */
	@Transactional
	public void updatePartnerCurrentRIB(Reimbursement reimbursement)  {
		BankDetails bankDetails = reimbursement.getBankDetails();
		Partner partner = reimbursement.getPartner();

		if(bankDetails != null && partner != null && !bankDetails.equals(partner.getBankDetails()))  {
			partner.setBankDetails(bankDetails);
			partnerService.save(partner);
		}
	}
	
	
	
	
}
