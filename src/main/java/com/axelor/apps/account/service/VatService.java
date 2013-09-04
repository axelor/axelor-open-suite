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
package com.axelor.apps.account.service;

import java.math.BigDecimal;

import org.joda.time.LocalDate;

import com.axelor.apps.account.db.Vat;
import com.axelor.apps.account.db.VatLine;
import com.axelor.apps.base.service.administration.GeneralService;
import com.axelor.apps.tool.date.DateTool;
import com.axelor.exception.AxelorException;
import com.axelor.exception.db.IException;

public class VatService {

	
	/**
	 * Fonction permettant de récupérer le taux de TVA d'une TVA
	 * @param vat
	 * 			Une TVA
	 * @return
	 * 			Le taux de TVA
	 * @throws AxelorException
	 */
	public BigDecimal getVatRate(Vat vat, LocalDate localDate) throws AxelorException  {
		
		return this.getVatLine(vat, localDate).getValue();
	}
	
	
	/**
	 * Fonction permettant de récupérer le taux de TVA d'une TVA
	 * @param vat
	 * 			Une TVA
	 * @return
	 * 			Le taux de TVA
	 * @throws AxelorException
	 */
	public VatLine getVatLine(Vat vat, LocalDate localDate) throws AxelorException  {
		
		if (vat != null && vat.getVatLineList() != null && !vat.getVatLineList().isEmpty())  {
			
			for (VatLine vatLine : vat.getVatLineList()) {
				
				if (DateTool.isBetween(vatLine.getStartDate(), vatLine.getEndDate(), localDate)) {
					return vatLine;
				}
			}
		}
		
		throw new AxelorException(String.format("%s :\n Veuillez configurer une version de TVA pour la TVA %s",
			GeneralService.getExceptionAccountingMsg(), vat.getName()), IException.CONFIGURATION_ERROR);
	}

}