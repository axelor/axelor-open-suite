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
package com.axelor.apps.tool;

import java.math.BigDecimal;
import java.math.RoundingMode;

import org.joda.time.LocalDate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.axelor.apps.tool.date.DateTool;

/**
 * Outils simplifiant l'utilisation des nombres.
 * 
 */
public final class DecimalTool {

	private static final Logger LOG = LoggerFactory.getLogger(DecimalTool.class);
	
	/**
	 * Proratiser une valeur en fonction de date.
	 * 
	 * @param fromDate
	 * 			Date de début de la période de conso.
	 * @param toDate
	 * 			Date de fin de la période de conso.
	 * @param date
	 * 			Date de proratisation.
	 * @param value
	 * 			Valeur initiale.
	 * 
	 * @return
	 * 			La quantité proratisée.
	 */
	public static BigDecimal prorata(LocalDate fromDate, LocalDate toDate, LocalDate date, BigDecimal value, int scale){
		
		BigDecimal prorataValue = BigDecimal.ZERO;
		
		if (fromDate == null || toDate == null || date == null) { return prorataValue; }
		
		BigDecimal totalDays = new BigDecimal(DateTool.daysBetween(fromDate, toDate, false));
		BigDecimal days = new BigDecimal(DateTool.daysBetween(date, toDate, false));
		
		prorataValue = prorata(totalDays, days, value, scale);
		
		LOG.debug("Proratisation ({} pour {} à {}) à la date du {} : {}", new Object[] {value, fromDate, toDate, date, prorataValue});
		
		return prorataValue;
		
	}

	/**
	 * Proratiser une valeur en fonction du nombre de jours. (Règle de 3)
	 * 
	 * @param totalDays
	 *          Le nombre total de jour.
	 * @param days
	 *          Le nombre de jour.
	 * @param value
	 *          La valeur à proratiser.
	 * 
	 * @return 
	 * 			La valeur proratisée.
	 */
	public static BigDecimal prorata(BigDecimal totalDays, BigDecimal days, BigDecimal value, int scale) {

		BigDecimal prorataValue = BigDecimal.ZERO;

		if (totalDays.compareTo(prorataValue) == 0) {
			return prorataValue;
		}
		else {
			prorataValue = (days.multiply(value).divide(totalDays, scale, BigDecimal.ROUND_HALF_EVEN)).setScale(scale, RoundingMode.HALF_EVEN);
		}

		LOG.debug("Proratisation d'une valeur sur un total de jour {} pour {} jours et une valeur de {} : {}", new Object[] { totalDays, days, value, prorataValue });

		return prorataValue;
	}
	
	public static BigDecimal prorata(LocalDate fromDate, LocalDate toDate, LocalDate date, BigDecimal value){
		
		return prorata(fromDate, toDate, date, value, 2);
		
	}
	
	public static BigDecimal prorata(BigDecimal totalDays, BigDecimal days, BigDecimal value) {
		
		return prorata(totalDays, days, value, 2);
		
	}
	
	/**
	 * Fonction permettant d'obtenir le pourcentage d'une valeur.
	 * 
	 * @param value
	 * 			Valeur initiale.
	 * @param percent
	 * 			Pourcentage (format : 10%).
	 * @param scale
	 * 			Précision.
	 * 
	 * @return
	 * 			Le pourcentage de la valeur initiale.
	 */
	public static BigDecimal percent(BigDecimal value, BigDecimal percent, int scale){
		
		return value.multiply(percent).divide(new BigDecimal("100"), scale, RoundingMode.HALF_EVEN);
		
	}
}
