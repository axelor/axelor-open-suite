/**
 * Copyright (c) 2012-2014 Axelor. All Rights Reserved.
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
 * Copyright (c) 2012-2014 Axelor. All Rights Reserved.
 */
package com.axelor.apps.account.service;

import com.axelor.apps.account.db.Account;
import com.axelor.apps.account.db.AccountEquiv;
import com.axelor.apps.account.db.FiscalPosition;
import com.axelor.apps.account.db.Tax;
import com.axelor.apps.account.db.TaxEquiv;

public class FiscalPositionService {
	

	public Account getAccount(FiscalPosition fiscalPosition, Account account)  {
		
		if(fiscalPosition != null && fiscalPosition.getAccountEquivList() != null)  {
			for(AccountEquiv accountEquiv : fiscalPosition.getAccountEquivList())  {
				
				if(accountEquiv.getFromAccount().equals(account) && accountEquiv.getToAccount() != null)  {
					return accountEquiv.getToAccount();
				}
			}
		}
		
		return account;
	}
	
	
	public Tax getTax(FiscalPosition fiscalPosition, Tax tax)  {
		
		if(fiscalPosition != null && fiscalPosition.getTaxEquivList() != null)  {
			for(TaxEquiv taxEquiv : fiscalPosition.getTaxEquivList())  {
				
				if(taxEquiv.getFromTax().equals(tax) && taxEquiv.getToTax() != null)  {
					return taxEquiv.getToTax();
				}
			}
		}
		
		return tax;
	}
}
