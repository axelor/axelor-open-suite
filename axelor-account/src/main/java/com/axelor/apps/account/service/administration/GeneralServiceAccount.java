/**
 * Axelor Business Solutions
 *
 * Copyright (C) 2014 Axelor (<http://axelor.com>).
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
package com.axelor.apps.account.service.administration;

import javax.inject.Singleton;

import com.axelor.apps.account.db.Tax;
import com.axelor.apps.base.service.administration.GeneralService;

@Singleton
public class GeneralServiceAccount extends GeneralService {


	/**
	 * Obtenir le message d'erreur pour la facturation.
	 * 
	 * @return
	 */
	public static String getExceptionInvoiceMsg(){
		
			return EXCEPTION;
		
	}
	
	/**
	 * Obtenir le message d'erreur pour la relance.
	 * 
	 * @return
	 */
	public static String getExceptionReminderMsg(){
		
			return EXCEPTION;
		
	}
	
	/**
	 * Obtenir le message d'erreur pour la compta.
	 * 
	 * @return
	 */
	public static String getExceptionAccountingMsg(){
		
			return EXCEPTION;
		
	}
	


// Tax 
	
	/**
	 * Obtenir la tva à 0%
	 * 
	 * @return
	 */
	public static Tax getDefaultExemptionTax(){
		if (getGeneral() != null) { return getGeneral().getDefaultExemptionTax(); }
		else { return null; }
	}
	
	
// Consolidation des écritures de factures
	/**
	 * Savoir si le logger est activé
	 * 
	 * @return
	 */
	public static boolean IsInvoiceMoveConsolidated(){
		
		if (getGeneral() != null){
			return getGeneral().getIsInvoiceMoveConsolidated();
		}
		
		return false;
	}
	
	
}
