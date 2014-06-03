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
import com.axelor.i18n.I18n;
import com.google.inject.Inject;

public class CurrencyService {
	
	private static final Logger LOG = LoggerFactory.getLogger(CurrencyService.class);
	
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
			return currencyConversionLine.getExchangeRate();
		}
		else  {
			currencyConversionLine = this.getCurrencyConversionLine(endCurrency, startCurrency, today);
		}
		
		if(currencyConversionLine == null)  {
			throw new AxelorException(String.format(I18n.get(IExceptionMessage.CURRENCY_1), startCurrency, endCurrency, today), IException.CONFIGURATION_ERROR);
		}
		
		return currencyConversionLine.getExchangeRate();
		
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
				return amountToPay.multiply(currencyConversionLine.getExchangeRate()).setScale(2, RoundingMode.HALF_UP);
			}
			else  {
				currencyConversionLine = this.getCurrencyConversionLine(currencyEnd, currencyStart, localDate);
			}
			
			if(currencyConversionLine == null)  {
				throw new AxelorException(String.format(I18n.get(IExceptionMessage.CURRENCY_1), 
						currencyStart.getName(), currencyEnd.getName(), today), IException.CONFIGURATION_ERROR);
			}
			
			BigDecimal exchangeRate = currencyConversionLine.getExchangeRate();
			
			if(exchangeRate == null || exchangeRate.compareTo(BigDecimal.ZERO) == 0)  {
				throw new AxelorException(String.format(I18n.get(IExceptionMessage.CURRENCY_2), 
						currencyStart.getName(), currencyEnd.getName(), today), IException.CONFIGURATION_ERROR);
			}
			
			return amountToPay.divide(exchangeRate, 2, RoundingMode.HALF_UP).setScale(2, RoundingMode.HALF_UP);
		}
		
		return amountToPay;
		
	}
	
}
