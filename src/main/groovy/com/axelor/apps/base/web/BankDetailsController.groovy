package com.axelor.apps.base.web

import org.apache.commons.validator.routines.checkdigit.IBANCheckDigit

import com.axelor.apps.base.service.BankDetailsService;
import com.axelor.apps.base.db.BankDetails
import com.axelor.rpc.ActionRequest
import com.axelor.rpc.ActionResponse
import com.google.inject.Inject

class BankDetailsController {
	
	@Inject
	private BankDetailsService bds
	
	void onChangeIban(ActionRequest request,ActionResponse response) {
		
		BankDetails bankDetails = request.context as BankDetails
		
		if(bankDetails.iban) {
			

			if (!IBANCheckDigit.IBAN_CHECK_DIGIT.isValid(bankDetails.iban)) {
				
				response.flash = "L'IBAN saisi est invalide. <br> Soit l'IBAN ne respecte pas la norme, soit le format de saisie n'est pas correct. L'IBAN doit être saisi sans espaces tel que présenté ci-dessous: <br> FR0000000000000000000000000"
				response.setColor("iban", "#FF0000")
				
			}
			else{
				
				bankDetails = bds.detailsIban(bankDetails)
				if(bankDetails.getBic() == null){
					response.flash = "Aucun Bic correspondant pour l'établissement bancaire."
				}
				response.values = [
					"bankCode":bankDetails.bankCode,
					"sortCode":bankDetails.sortCode,
					"accountNbr":bankDetails.accountNbr,
					"bbanKey":bankDetails.bbanKey,
					"countryCode":bankDetails.countryCode,
					"bic": bankDetails.bic
				]
				
			}
		}
	}
}
