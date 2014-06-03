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

import com.axelor.apps.account.db.TaxLine;
import com.axelor.apps.account.service.AccountManagementService;
import com.axelor.apps.base.db.IPriceListLine;
import com.axelor.apps.base.db.PriceList;
import com.axelor.apps.base.db.PriceListLine;
import com.axelor.apps.base.db.Product;
import com.axelor.apps.base.service.CurrencyService;
import com.axelor.apps.base.service.PriceListService;
import com.axelor.apps.supplychain.db.SalesOrder;
import com.axelor.apps.supplychain.db.SalesOrderSubLine;
import com.axelor.exception.AxelorException;
import com.google.inject.Inject;

public class SalesOrderSubLineService {

	private static final Logger LOG = LoggerFactory.getLogger(SalesOrderSubLineService.class); 
	
	@Inject
	private CurrencyService currencyService;
	
	@Inject
	private AccountManagementService accountManagementService;
	
	@Inject
	private PriceListService priceListService;
	
	
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
	
	
	public BigDecimal getUnitPrice(SalesOrder salesOrder, SalesOrderSubLine salesOrderSubLine) throws AxelorException  {
		
		Product product = salesOrderSubLine.getProduct();
		
		return currencyService.getAmountCurrencyConverted(
			product.getSaleCurrency(), salesOrder.getCurrency(), product.getSalePrice(), salesOrder.getCreationDate());  
		
	}
	
	
	public TaxLine getTaxLine(SalesOrder salesOrder, SalesOrderSubLine salesOrderSubLine) throws AxelorException  {
		
		return accountManagementService.getTaxLine(
				salesOrder.getCreationDate(), salesOrderSubLine.getProduct(), salesOrder.getCompany(), salesOrder.getClientPartner().getFiscalPosition(), false);
		
	}
	
	
	public BigDecimal getCompanyExTaxTotal(BigDecimal exTaxTotal, SalesOrder salesOrder) throws AxelorException  {
		
		return currencyService.getAmountCurrencyConverted(
				salesOrder.getCurrency(), salesOrder.getCompany().getCurrency(), exTaxTotal, salesOrder.getCreationDate());  
	}
	
	
	public BigDecimal getCompanyCostPrice(SalesOrder salesOrder, SalesOrderSubLine salesOrderSubLine) throws AxelorException  {
		
		Product product = salesOrderSubLine.getProduct();
		
		return currencyService.getAmountCurrencyConverted(
				product.getPurchaseCurrency(), salesOrder.getCompany().getCurrency(), product.getCostPrice(), salesOrder.getCreationDate());  
	}
	
	
	public PriceListLine getPriceListLine(SalesOrderSubLine salesOrderSubLine, PriceList priceList)  {
		
		return priceListService.getPriceListLine(salesOrderSubLine.getProduct(), salesOrderSubLine.getQty(), priceList);
	
	}
	
	
	public BigDecimal computeDiscount(SalesOrderSubLine salesOrderSubLine)  {
		BigDecimal unitPrice = salesOrderSubLine.getPrice();
		
		if(salesOrderSubLine.getDiscountTypeSelect() == IPriceListLine.AMOUNT_TYPE_FIXED)  {
			return  unitPrice.add(salesOrderSubLine.getDiscountAmount());
		}
		else if(salesOrderSubLine.getDiscountTypeSelect() == IPriceListLine.AMOUNT_TYPE_PERCENT)  {
			return unitPrice.multiply(
					BigDecimal.ONE.add(
							salesOrderSubLine.getDiscountAmount().divide(new BigDecimal(100))));
		}
		
		return unitPrice;
	}
	
}
