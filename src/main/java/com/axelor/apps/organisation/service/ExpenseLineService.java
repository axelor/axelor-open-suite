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
package com.axelor.apps.organisation.service;

import java.math.BigDecimal;
import java.math.RoundingMode;

import org.joda.time.LocalDate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.axelor.apps.account.db.FiscalPosition;
import com.axelor.apps.account.db.TaxLine;
import com.axelor.apps.account.service.AccountManagementService;
import com.axelor.apps.base.db.Product;
import com.axelor.apps.base.service.CurrencyService;
import com.axelor.apps.base.service.administration.GeneralService;
import com.axelor.apps.organisation.db.Expense;
import com.axelor.apps.organisation.db.ExpenseLine;
import com.axelor.apps.supplychain.db.SalesOrder;
import com.axelor.apps.supplychain.db.SalesOrderLine;
import com.axelor.exception.AxelorException;
import com.google.inject.Inject;


public class ExpenseLineService {

	private static final Logger LOG = LoggerFactory.getLogger(ExpenseLineService.class);
	
	@Inject
	private CurrencyService currencyService;
	
	@Inject
	private GeneralService gs;
	
	@Inject
	private AccountManagementService accountManagementService;
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
	public static BigDecimal computeAmount(BigDecimal quantity, BigDecimal price) {

		BigDecimal amount = quantity.multiply(price).setScale(2, RoundingMode.HALF_EVEN);

		LOG.debug("Calcul du montant HT avec une quantité de {} pour {} : {}", new Object[] { quantity, price, amount });

		return amount;
	}
	
	
	public BigDecimal getUnitPrice(Expense expense, ExpenseLine expenseLine) throws AxelorException  {
		
		Product product = expenseLine.getProduct();
		
		BigDecimal unitPrice = currencyService.getAmountCurrencyConverted(
			product.getPurchaseCurrency(), expense.getCurrency(), product.getPurchasePrice(), expenseLine.getDate());  
		
		if(expenseLine.getTaxLine() != null)  {
			unitPrice = unitPrice.add(expenseLine.getTaxLine().getValue().multiply(unitPrice));
		}
		
		return unitPrice;
	}
	
	
	public BigDecimal getCompanyTotal(BigDecimal total, Expense expense) throws AxelorException  {
		
		return currencyService.getAmountCurrencyConverted(
				expense.getCurrency(), expense.getCompany().getCurrency(), total, expense.getDate());  
	}
	
	public TaxLine getTaxLine(Expense expense,ExpenseLine line) throws AxelorException  {
		
		FiscalPosition fiscalPosition = null;
		if(expense.getUserInfo().getPartner() != null)
			fiscalPosition = expense.getCompany().getPartner().getFiscalPosition();
		LocalDate today = gs.getTodayDate();
		if(line.getDate() != null)
			today = line.getDate();
		return accountManagementService.getTaxLine(
				today, line.getProduct(), expense.getCompany(), fiscalPosition, false);
		
	}
			
		
}
