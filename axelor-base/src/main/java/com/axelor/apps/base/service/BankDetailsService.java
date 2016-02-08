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
package com.axelor.apps.base.service;

import com.axelor.apps.base.db.BankDetails;
import com.axelor.apps.base.db.Bic;
import com.axelor.apps.base.db.Partner;
import com.axelor.apps.base.db.repo.BicRepository;
import com.axelor.apps.tool.StringTool;
import com.google.inject.Inject;

public class BankDetailsService {
	
	@Inject
	private BicRepository bicRepo;
	
	/**
	 * Méthode qui permet d'extraire les informations de l'iban
	 * Met à jour les champs suivants :
	 * 		<ul>
     *      	<li>BankCode</li>
     *      	<li>SortCode</li>
     *      	<li>AccountNbr</li>
     *      	<li>BbanKey</li>
     *      	<li>CountryCode</li>
     *      	<li>Bic</li>
	 * 		</ul>
	 * 
	 * @param bankDetails
	 * @return BankDetails
	 */
	public BankDetails detailsIban(BankDetails bankDetails){
		
		if(bankDetails.getIban()!=null){
			
			bankDetails.setBankCode(StringTool.extractStringFromRight(bankDetails.getIban(),23,5));
			bankDetails.setSortCode(StringTool.extractStringFromRight(bankDetails.getIban(),18,5));
			bankDetails.setAccountNbr(StringTool.extractStringFromRight(bankDetails.getIban(),13,11));
			bankDetails.setBbanKey(StringTool.extractStringFromRight(bankDetails.getIban(),2,2));
			bankDetails.setCountryCode(StringTool.extractStringFromRight(bankDetails.getIban(),27,2));
			Bic bic = bicRepo.all().filter(
					"self.countryCode = ?1 " +
					"AND self.sortCode = ?2 " +
					"AND self.bankCode = ?3", bankDetails.getCountryCode(), bankDetails.getSortCode(), bankDetails.getBankCode()).fetchOne();
			if(bic != null){
				bankDetails.setBic(bic.getCode());
			}
			else{
				bankDetails.setBic(null);
			}
			
		}
		return bankDetails;
	}
	
	
	/**
	 * Méthode permettant de créer un RIB
	 * 
	 * @param accountNbr
	 * @param bankAddress
	 * @param bankCode
	 * @param bbanKey
	 * @param bic
	 * @param countryCode
	 * @param ownerName
	 * @param payerPartner
	 * @param sortCode
	 * 
	 * @return
	 */
	public BankDetails createBankDetails(String accountNbr, String bankAddress, String bankCode, String bbanKey, String bic, 
			String countryCode, String ownerName, Partner partner, String sortCode)  {
		BankDetails bankDetails = new BankDetails();
		bankDetails.setAccountNbr(accountNbr);
		bankDetails.setBankAddress(bankAddress);
		bankDetails.setBankCode(bankCode);
		bankDetails.setBbanKey(bbanKey);
		bankDetails.setBic(bic);
		bankDetails.setCountryCode(countryCode);
		
		bankDetails.setOwnerName(ownerName);
		bankDetails.setPartner(partner);
		
		bankDetails.setSortCode(sortCode);
		
		return bankDetails;
	}

}
