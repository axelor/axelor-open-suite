package com.axelor.apps.account.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

import org.joda.time.LocalDate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.axelor.apps.base.db.Currency;
import com.axelor.apps.base.db.CurrencyConversionLine;
import com.axelor.apps.base.service.administration.GeneralService;
import com.axelor.exception.AxelorException;
import com.axelor.exception.db.IException;
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
			return currencyConversionLine.getConversionRate();
		}
		else  {
			currencyConversionLine = this.getCurrencyConversionLine(endCurrency, startCurrency, today);
		}
		
		if(currencyConversionLine == null)  {
			throw new AxelorException(String.format("Aucune conversion trouvée de la devise '%s' à la devise '%s' à la date du %s", 
					startCurrency, endCurrency, today), IException.CONFIGURATION_ERROR);
		}
		
		return currencyConversionLine.getConversionRate();
		
	}
	
	
	public CurrencyConversionLine getCurrencyConversionLine(Currency startCurrency, Currency endCurrency, LocalDate localDate)  {
		
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
				throw new AxelorException(String.format("Aucune conversion trouvée de la devise '%s' à la devise '%s' à la date du %s", 
						currencyStart.getName(), currencyEnd.getName(), today), IException.CONFIGURATION_ERROR);
			}
			
			return amountToPay.divide(currencyConversionLine.getConversionRate(), 2, RoundingMode.HALF_UP).setScale(2, RoundingMode.HALF_UP);
		}
		
		return amountToPay;
		
	}
	
}
