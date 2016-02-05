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
package com.axelor.apps.base.web;

import org.apache.commons.validator.routines.checkdigit.IBANCheckDigit;

import com.axelor.apps.base.db.BankDetails;
import com.axelor.apps.base.exceptions.IExceptionMessage;
import com.axelor.apps.base.service.BankDetailsService;
import com.axelor.i18n.I18n;
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
				response.setFlash(I18n.get(IExceptionMessage.BANK_DETAILS_1));
				response.setColor("iban", "#FF0000");
			}
			else{
				bankDetails = bds.detailsIban(bankDetails);
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
