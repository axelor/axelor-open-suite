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
package com.axelor.apps.base.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

import org.joda.time.LocalDate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.axelor.apps.base.db.Currency;
import com.axelor.apps.base.db.CurrencyConversionLine;
import com.axelor.apps.base.exceptions.IExceptionMessage;
import com.axelor.apps.base.service.administration.GeneralService;
import com.axelor.exception.AxelorException;
import com.axelor.exception.db.IException;
import com.axelor.meta.service.MetaTranslations;
import com.google.inject.Inject;

public class CurrencyService {
	
	private static final Logger LOG = LoggerFactory.getLogger(CurrencyService.class);
	
	@Inject
	private MetaTranslations metaTranslations;
	
	private LocalDate today;

	@Inject
	public CurrencyService() {

		this.today = GeneralService.getTodayDate();
	}
	
	
	public CurrencyService(LocalDate today) {

		this.today = today;
	}
	
	
	public BigDecimal getCurrencyConversionRate(Currency startCurrency, Currency endCurrency) throws AxelorException  {
		
		CurrencyConversionLine currencyConversionLine = this.getCurrencyConversionLine(startCurrency, endCurrency, today);
		if(currencyConversionLine != null)  {
			return currencyConversionLine.getConversionRate();
		}
		else  {
			currencyConversionLine = this.getCurrencyConversionLine(endCurrency, startCurrency, today);
		}
		
		if(currencyConversionLine == null)  {
			throw new AxelorException(String.format(metaTranslations.get(IExceptionMessage.CURRENCY_1), startCurrency, endCurrency, today), IException.CONFIGURATION_ERROR);
		}
		
		return currencyConversionLine.getConversionRate();
		
	}
	
	
	private CurrencyConversionLine getCurrencyConversionLine(Currency startCurrency, Currency endCurrency, LocalDate localDate)  {
		
		List<CurrencyConversionLine> currencyConversionLineList = GeneralService.getCurrencyConfigurationLineList();
			
		if(currencyConversionLineList != null)  {
			for(CurrencyConversionLine currencyConversionLine : currencyConversionLineList)  {
				if(currencyConversionLine.getStartCurrency().equals(startCurrency) && currencyConversionLine.getEndCurrency().equals(endCurrency) && 
						currencyConversionLine.getFromDate().isBefore(localDate) && (currencyConversionLine.getToDate() == null || currencyConversionLine.getToDate().isAfter(localDate)))  {
					return currencyConversionLine;
				}
			}
		}
		return null;
	}
	
	
	public BigDecimal getAmountCurrencyConverted(Currency currencyStart, Currency currencyEnd, BigDecimal amountToPay, LocalDate localDate) throws AxelorException  {
		
		// Si la devise source est différente de la devise d'arrivée 
		// Alors on convertit
		if(!currencyStart.equals(currencyEnd))  {
			// CONVERTIR
			
			CurrencyConversionLine currencyConversionLine = this.getCurrencyConversionLine(currencyStart, currencyEnd, localDate);
			if(currencyConversionLine != null)  {
				return amountToPay.multiply(currencyConversionLine.getConversionRate()).setScale(2, RoundingMode.HALF_UP);
			}
			else  {
				currencyConversionLine = this.getCurrencyConversionLine(currencyEnd, currencyStart, localDate);
			}
			
			if(currencyConversionLine == null)  {
				throw new AxelorException(String.format(metaTranslations.get(IExceptionMessage.CURRENCY_1), 
						currencyStart.getName(), currencyEnd.getName(), today), IException.CONFIGURATION_ERROR);
			}
			
			return amountToPay.divide(currencyConversionLine.getConversionRate(), 2, RoundingMode.HALF_UP).setScale(2, RoundingMode.HALF_UP);
		}
		
		return amountToPay;
		
	}
	
}
