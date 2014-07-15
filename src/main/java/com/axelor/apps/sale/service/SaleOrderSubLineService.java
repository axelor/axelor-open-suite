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
package com.axelor.apps.sale.service;

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
import com.axelor.apps.sale.db.SaleOrder;
import com.axelor.apps.sale.db.SaleOrderSubLine;
import com.axelor.exception.AxelorException;
import com.google.inject.Inject;

public class SaleOrderSubLineService {

	private static final Logger LOG = LoggerFactory.getLogger(SaleOrderSubLineService.class); 
	
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
	
	
	public BigDecimal getUnitPrice(SaleOrder saleOrder, SaleOrderSubLine saleOrderSubLine) throws AxelorException  {
		
		Product product = saleOrderSubLine.getProduct();
		
		return currencyService.getAmountCurrencyConverted(
			product.getSaleCurrency(), saleOrder.getCurrency(), product.getSalePrice(), saleOrder.getCreationDate());  
		
	}
	
	
	public TaxLine getTaxLine(SaleOrder saleOrder, SaleOrderSubLine saleOrderSubLine) throws AxelorException  {
		
		return accountManagementService.getTaxLine(
				saleOrder.getCreationDate(), saleOrderSubLine.getProduct(), saleOrder.getCompany(), saleOrder.getClientPartner().getFiscalPosition(), false);
		
	}
	
	
	public BigDecimal getCompanyExTaxTotal(BigDecimal exTaxTotal, SaleOrder saleOrder) throws AxelorException  {
		
		return currencyService.getAmountCurrencyConverted(
				saleOrder.getCurrency(), saleOrder.getCompany().getCurrency(), exTaxTotal, saleOrder.getCreationDate());  
	}
	
	
	public BigDecimal getCompanyCostPrice(SaleOrder saleOrder, SaleOrderSubLine saleOrderSubLine) throws AxelorException  {
		
		Product product = saleOrderSubLine.getProduct();
		
		return currencyService.getAmountCurrencyConverted(
				product.getPurchaseCurrency(), saleOrder.getCompany().getCurrency(), product.getCostPrice(), saleOrder.getCreationDate());  
	}
	
	
	public PriceListLine getPriceListLine(SaleOrderSubLine saleOrderSubLine, PriceList priceList)  {
		
		return priceListService.getPriceListLine(saleOrderSubLine.getProduct(), saleOrderSubLine.getQty(), priceList);
	
	}
	
	
	public BigDecimal computeDiscount(SaleOrderSubLine saleOrderSubLine)  {
		BigDecimal unitPrice = saleOrderSubLine.getPrice();
		
		if(saleOrderSubLine.getDiscountTypeSelect() == IPriceListLine.AMOUNT_TYPE_FIXED)  {
			return  unitPrice.add(saleOrderSubLine.getDiscountAmount());
		}
		else if(saleOrderSubLine.getDiscountTypeSelect() == IPriceListLine.AMOUNT_TYPE_PERCENT)  {
			return unitPrice.multiply(
					BigDecimal.ONE.add(
							saleOrderSubLine.getDiscountAmount().divide(new BigDecimal(100))));
		}
		
		return unitPrice;
	}
	
}
