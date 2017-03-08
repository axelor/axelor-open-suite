/**
 * Axelor Business Solutions
 *
 * Copyright (C) 2017 Axelor (<http://axelor.com>).
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

import com.axelor.apps.base.db.Bank;
import com.axelor.apps.base.db.BankDetails;
import com.axelor.apps.base.db.Partner;
import com.axelor.apps.base.db.repo.BankRepository;
import com.axelor.apps.tool.StringTool;
import com.google.inject.Inject;

public class BankDetailsService {
	
	@Inject
	private BankRepository bankRepo;
	
	/**
	 * Méthode qui permet d'extraire les informations de l'iban
	 * Met à jour les champs suivants :
	 * 		<ul>
     *      	<li>BankCode</li>
     *      	<li>SortCode</li>
     *      	<li>AccountNbr</li>
     *      	<li>BbanKey</li>
     *      	<li>Bank</li>
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
		}
		return bankDetails;
	}
	
	
	/**
	 * Méthode permettant de créer un RIB
	 * 
	 * @param accountNbr
	 * @param bankCode
	 * @param bbanKey
	 * @param bank
	 * @param ownerName
	 * @param partner
	 * @param sortCode
	 * 
	 * @return
	 */
	public BankDetails createBankDetails(String accountNbr, String bankCode, String bbanKey, Bank bank, String ownerName, Partner partner, String sortCode) {
		BankDetails bankDetails = new BankDetails();
		
		bankDetails.setAccountNbr(accountNbr);
		bankDetails.setBankCode(bankCode);
		bankDetails.setBbanKey(bbanKey);
		bankDetails.setBank(bank);
		bankDetails.setOwnerName(ownerName);
		bankDetails.setPartner(partner);
		bankDetails.setSortCode(sortCode);
		
		return bankDetails;
	}

}
