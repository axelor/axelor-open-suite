package com.axelor.apps.account.service;

import com.axelor.apps.account.db.Reimbursement;
import com.axelor.apps.base.db.BankDetails;
import com.axelor.apps.base.db.Partner;
import com.google.inject.persist.Transactional;

public class ReimbursementService {
	
	
	/**
	 * Procédure permettant de mettre à jour la liste des RIBs dans le contrat
	 * @param reimbursement
	 * 				Un remboursement
	 */
	@Transactional
	public void updateContractCurrentRIB(Reimbursement reimbursement)  {
		BankDetails bankDetails = reimbursement.getBankDetails();
		Partner partner = reimbursement.getPartner();

		if(bankDetails != null && partner != null && !bankDetails.equals(partner.getBankDetails()))  {
			partner.setBankDetails(bankDetails);
			partner.save();
		}
	}
	
	
	
	
}
