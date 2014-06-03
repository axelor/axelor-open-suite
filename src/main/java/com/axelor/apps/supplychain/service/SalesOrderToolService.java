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
package com.axelor.apps.supplychain.service;

import java.math.BigDecimal;
import java.math.RoundingMode;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.axelor.apps.base.service.CurrencyService;
import com.axelor.apps.supplychain.db.SalesOrder;
import com.axelor.exception.AxelorException;
import com.google.inject.Inject;

public class SalesOrderToolService {

	private static final Logger LOG = LoggerFactory.getLogger(SalesOrderToolService.class); 
	
	@Inject
	private CurrencyService currencyService;
	
	
	/**
	 * Calculer le montant HT d'une ligne de devis.
	 * 
	 * @param quantity
	 *          Quantité.
	 * @param price
	 *          Le prix.
	 * 
	 * @return 
	 * 			Le montant HT de la ligne.
	 */
	public BigDecimal computeAmount(BigDecimal quantity, BigDecimal price) {

		BigDecimal amount = quantity.multiply(price).setScale(2, RoundingMode.HALF_EVEN);

		LOG.debug("Calcul du montant HT avec une quantité de {} pour {} : {}", new Object[] { quantity, price, amount });

		return amount;
	}
	
	
	public BigDecimal getAccountingExTaxTotal(BigDecimal exTaxTotal, SalesOrder salesOrder) throws AxelorException  {
		
		return currencyService.getAmountCurrencyConverted(
				salesOrder.getCurrency(), salesOrder.getClientPartner().getCurrency(), exTaxTotal, salesOrder.getCreationDate());  
	}
	
}
