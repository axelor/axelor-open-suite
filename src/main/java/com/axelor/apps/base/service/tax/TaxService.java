/**
 * Axelor Business Solutions
 *
 * Copyright (C) 2012-2014 Axelor (<http://axelor.com>).
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
package com.axelor.apps.base.service.tax;

import java.math.BigDecimal;

import org.joda.time.LocalDate;

import com.axelor.apps.account.db.Tax;
import com.axelor.apps.account.db.TaxLine;
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
		
		throw new AxelorException(String.format("%s :\n Veuillez configurer une version de taxe pour la taxe %s", tax.getName()), IException.CONFIGURATION_ERROR);
	}

}