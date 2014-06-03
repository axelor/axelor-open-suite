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
import com.axelor.apps.base.db.PriceList;
import com.axelor.apps.base.db.PriceListLine;
import com.axelor.apps.base.db.Product;
import com.axelor.apps.base.service.CurrencyService;
import com.axelor.apps.base.service.PriceListService;
import com.axelor.apps.production.db.BillOfMaterial;
import com.axelor.apps.supplychain.db.SalesOrder;
import com.axelor.apps.supplychain.db.SalesOrderLine;
import com.axelor.apps.supplychain.db.SalesOrderSubLine;
import com.axelor.db.JPA;
import com.axelor.exception.AxelorException;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;

public class SalesOrderLineService {

	private static final Logger LOG = LoggerFactory.getLogger(SalesOrderLineService.class); 
	
	@Inject
	private CurrencyService currencyService;
	
	@Inject
	private AccountManagementService accountManagementService;
	
	@Inject
	private PriceListService priceListService;
	
	@Inject
	private SalesOrderSubLineService salesOrderSubLineService;
	
	
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
	
	
	public BigDecimal getUnitPrice(SalesOrder salesOrder, SalesOrderLine salesOrderLine) throws AxelorException  {
		
		Product product = salesOrderLine.getProduct();
		
		return currencyService.getAmountCurrencyConverted(
			product.getSaleCurrency(), salesOrder.getCurrency(), product.getSalePrice(), salesOrder.getCreationDate());  
		
	}
	
	
	public TaxLine getTaxLine(SalesOrder salesOrder, SalesOrderLine salesOrderLine) throws AxelorException  {
		
		return accountManagementService.getTaxLine(
				salesOrder.getCreationDate(), salesOrderLine.getProduct(), salesOrder.getCompany(), salesOrder.getClientPartner().getFiscalPosition(), false);
		
	}
	
	
	public BigDecimal computeSalesOrderLine(SalesOrderLine salesOrderLine) throws AxelorException  {
		
		BigDecimal exTaxTotal = BigDecimal.ZERO;
		
		if(salesOrderLine.getSalesOrderSubLineList() != null && !salesOrderLine.getSalesOrderSubLineList().isEmpty())  {
			for(SalesOrderSubLine salesOrderSubLine : salesOrderLine.getSalesOrderSubLineList())  {
				
				salesOrderSubLine.setCompanyExTaxTotal(salesOrderSubLineService.getCompanyExTaxTotal(salesOrderLine.getExTaxTotal(), salesOrderLine.getSalesOrder()));
				
				exTaxTotal = exTaxTotal.add(salesOrderSubLine.getExTaxTotal());
			}
		}
		else  {
			return salesOrderLine.getExTaxTotal();
		}
		
		return exTaxTotal;
	}

	
	public BigDecimal getCompanyExTaxTotal(BigDecimal exTaxTotal, SalesOrder salesOrder) throws AxelorException  {
		
		return currencyService.getAmountCurrencyConverted(
				salesOrder.getCurrency(), salesOrder.getCompany().getCurrency(), exTaxTotal, salesOrder.getCreationDate());  
	}
	
	
	public BigDecimal getCompanyCostPrice(SalesOrder salesOrder, SalesOrderLine salesOrderLine) throws AxelorException  {
		
		Product product = salesOrderLine.getProduct();
		
		return currencyService.getAmountCurrencyConverted(
				product.getPurchaseCurrency(), salesOrder.getCompany().getCurrency(), product.getCostPrice(), salesOrder.getCreationDate());  
	}
	
	
	public PriceListLine getPriceListLine(SalesOrderLine salesOrderLine, PriceList priceList)  {
		
		return priceListService.getPriceListLine(salesOrderLine.getProduct(), salesOrderLine.getQty(), priceList);
	
	}
	
	
	public BigDecimal computeDiscount(SalesOrderLine salesOrderLine)  {
		
		return priceListService.computeDiscount(salesOrderLine.getPrice(), salesOrderLine.getDiscountTypeSelect(), salesOrderLine.getDiscountAmount());
		
	}
	
	@Transactional(rollbackOn = {AxelorException.class, Exception.class})
	public BillOfMaterial customizeBillOfMaterial(SalesOrderLine salesOrderLine)  {
		
		BillOfMaterial billOfMaterial = salesOrderLine.getBillOfMaterial();
		
		if(billOfMaterial != null)  {
			return JPA.copy(billOfMaterial, true);
		}
		
		return null;
		
	}
	
}
