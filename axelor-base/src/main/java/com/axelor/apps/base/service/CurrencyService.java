/**
 * Axelor Business Solutions
 *
 * Copyright (C) 2016 Axelor (<http://axelor.com>).
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
import com.axelor.inject.Beans;
import com.google.inject.Inject;

public class CurrencyService {
	
	private final Logger log = LoggerFactory.getLogger(getClass());

	protected GeneralService generalService;

	private LocalDate today;

	@Inject
	public CurrencyService(GeneralService generalService) {

		this.generalService = generalService;
		this.today = generalService.getTodayDate();
	}


	public CurrencyService(LocalDate today) {

		this.generalService = Beans.get(GeneralService.class);
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

		List<CurrencyConversionLine> currencyConversionLineList = generalService.getCurrencyConfigurationLineList();

		if(currencyConversionLineList == null)  {
			return null;
		}
		
		log.debug("Currency from: {}, Currency to: {}, localDate: {}", startCurrency, endCurrency, localDate);
		
		for(CurrencyConversionLine ccl : currencyConversionLineList)  {
			
			String cclStartCode = ccl.getStartCurrency().getCode();
			String cclEndCode = ccl.getEndCurrency().getCode();
			String startCode = startCurrency.getCode();
			String endCode = endCurrency.getCode();
			LocalDate fromDate = ccl.getFromDate();
			LocalDate toDate = ccl.getToDate();
			
			if(cclStartCode.equals(startCode) && cclEndCode.equals(endCode)){
					if((fromDate.isBefore(localDate) || fromDate.equals(localDate)) 
					&& (toDate == null || toDate.isAfter(localDate) || toDate.isEqual(localDate)))  {
						return ccl;
					}
			}
		}
		
		return null;
	}


	public BigDecimal getAmountCurrencyConverted(Currency currencyStart, Currency currencyEnd, BigDecimal amountToPay, LocalDate localDate) throws AxelorException  {

		// Si la devise source est différente de la devise d'arrivée
		// Alors on convertit
		if(currencyStart != null && currencyEnd != null && !currencyStart.equals(currencyEnd))  {
			// CONVERTIR

			CurrencyConversionLine currencyConversionLine = this.getCurrencyConversionLine(currencyStart, currencyEnd, this.getDateToConvert(localDate));
			if(currencyConversionLine != null)  {
				return amountToPay.multiply(currencyConversionLine.getExchangeRate());
			}
			else  {
				currencyConversionLine = this.getCurrencyConversionLine(currencyEnd, currencyStart, this.getDateToConvert(localDate));
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

			return amountToPay.divide(exchangeRate, generalService.getNbDecimalDigitForUnitPrice(), RoundingMode.HALF_UP);
		}

		return amountToPay;

	}

	public LocalDate getDateToConvert(LocalDate date)  {

		if(date == null)  {  date = this.today;  }

		return date;
	}

}
