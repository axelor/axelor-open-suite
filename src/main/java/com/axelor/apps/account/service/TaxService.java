/**
 * Copyright (c) 2012-2014 Axelor. All Rights Reserved.
 *
 * The contents of this file are subject to the Common Public
 * Attribution License Version 1.0 (the "License"); you may not use
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
 * Software distributed under the License is distributed on an "AS IS"
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

import java.math.BigDecimal;

import org.joda.time.LocalDate;

import com.axelor.apps.account.db.Tax;
import com.axelor.apps.account.db.TaxLine;
import com.axelor.apps.base.service.administration.GeneralService;
import com.axelor.apps.tool.date.DateTool;
import com.axelor.exception.AxelorException;
import com.axelor.exception.db.IException;

public class TaxService {

	
	/**
	 * Fonction permettant de récupérer le taux de TVA d'une TVA
	 * @param tax
	 * 			Une TVA
	 * @return
	 * 			Le taux de TVA
	 * @throws AxelorException
	 */
	public BigDecimal getTaxRate(Tax tax, LocalDate localDate) throws AxelorException  {
		
		return this.getTaxLine(tax, localDate).getValue();
	}
	
	
	/**
	 * Fonction permettant de récupérer le taux de TVA d'une TVA
	 * @param tax
	 * 			Une TVA
	 * @return
	 * 			Le taux de TVA
	 * @throws AxelorException
	 */
	public TaxLine getTaxLine(Tax tax, LocalDate localDate) throws AxelorException  {
		
		if (tax != null && tax.getTaxLineList() != null && !tax.getTaxLineList().isEmpty())  {
			
			for (TaxLine taxLine : tax.getTaxLineList()) {
				
				if (DateTool.isBetween(taxLine.getStartDate(), taxLine.getEndDate(), localDate)) {
					return taxLine;
				}
			}
		}
		
		throw new AxelorException(String.format("%s :\n Veuillez configurer une version de taxe pour la taxe %s",
			GeneralService.getExceptionAccountingMsg(), tax.getName()), IException.CONFIGURATION_ERROR);
	}

}