/**
 * Copyright (c) 2012-2013 Axelor. All Rights Reserved.
 *
 * The contents of this file are subject to the Common Public
 * Attribution License Version 1.0 (the “License”); you may not use
 * this file except in compliance with the License. You may obtain a
 * copy of the License at:
 *
 * http://license.axelor.com/.
 *
 * The License is based on the Mozilla Public License Version 1.1 but
 * Sections 14 and 15 have been added to cover use of software over a
 * computer network and provide for limited attribution for the
 * Original Developer. In addition, Exhibit A has been modified to be
 * consistent with Exhibit B.
 *
 * Software distributed under the License is distributed on an “AS IS”
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See
 * the License for the specific language governing rights and limitations
 * under the License.
 *
 * The Original Code is part of "Axelor Business Suite", developed by
 * Axelor exclusively.
 *
 * The Original Developer is the Initial Developer. The Initial Developer of
 * the Original Code is Axelor.
 *
 * All portions of the code written by Axelor are
 * Copyright (c) 2012-2013 Axelor. All Rights Reserved.
 */
package com.axelor.apps.base.web;

import org.apache.commons.validator.routines.checkdigit.IBANCheckDigit;

import com.axelor.apps.base.db.BankDetails;
import com.axelor.apps.base.service.BankDetailsService;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.google.inject.Inject;

public class BankDetailsController {

	@Inject
	private BankDetailsService bds;
	
	public void onChangeIban(ActionRequest request,ActionResponse response) {
		
		BankDetails bankDetails = request.getContext().asType(BankDetails.class);
		
		if(bankDetails.getIban() != null) {
			if (!IBANCheckDigit.IBAN_CHECK_DIGIT.isValid(bankDetails.getIban())) {	
				response.setFlash("L'IBAN saisi est invalide. <br> Soit l'IBAN ne respecte pas la norme, soit le format de saisie n'est pas correct. L'IBAN doit être saisi sans espaces tel que présenté ci-dessous: <br> FR0000000000000000000000000");
				response.setColor("iban", "#FF0000");
			}
			else{
				bankDetails = bds.detailsIban(bankDetails);
				if(bankDetails.getBic() == null){
					response.setFlash("Aucun Bic correspondant pour l'établissement bancaire.");
				}
				response.setValue("bankCode", bankDetails.getBankCode());
				response.setValue("sortCode", bankDetails.getSortCode());
				response.setValue("accountNbr", bankDetails.getAccountNbr());
				response.setValue("bbanKey", bankDetails.getBbanKey());
				response.setValue("countryCode", bankDetails.getCountryCode());
				response.setValue("bic", bankDetails.getBic());				
			}
		}
	}
}
