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
