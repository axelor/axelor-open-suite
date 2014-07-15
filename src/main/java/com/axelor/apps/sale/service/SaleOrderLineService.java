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
import com.axelor.apps.base.db.PriceList;
import com.axelor.apps.base.db.PriceListLine;
import com.axelor.apps.base.db.Product;
import com.axelor.apps.base.service.CurrencyService;
import com.axelor.apps.base.service.PriceListService;
import com.axelor.apps.production.db.BillOfMaterial;
import com.axelor.apps.sale.db.SaleOrder;
import com.axelor.apps.sale.db.SaleOrderLine;
import com.axelor.apps.sale.db.SaleOrderSubLine;
import com.axelor.db.JPA;
import com.axelor.exception.AxelorException;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;

public class SaleOrderLineService {

	private static final Logger LOG = LoggerFactory.getLogger(SaleOrderLineService.class); 
	
	@Inject
	private CurrencyService currencyService;
	
	@Inject
	private AccountManagementService accountManagementService;
	
	@Inject
	private PriceListService priceListService;
	
	@Inject
	private SaleOrderSubLineService saleOrderSubLineService;
	
	
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
	
	
	public BigDecimal getUnitPrice(SaleOrder saleOrder, SaleOrderLine saleOrderLine) throws AxelorException  {
		
		Product product = saleOrderLine.getProduct();
		
		return currencyService.getAmountCurrencyConverted(
			product.getSaleCurrency(), saleOrder.getCurrency(), product.getSalePrice(), saleOrder.getCreationDate());  
		
	}
	
	
	public TaxLine getTaxLine(SaleOrder saleOrder, SaleOrderLine saleOrderLine) throws AxelorException  {
		
		return accountManagementService.getTaxLine(
				saleOrder.getCreationDate(), saleOrderLine.getProduct(), saleOrder.getCompany(), saleOrder.getClientPartner().getFiscalPosition(), false);
		
	}
	
	
	public BigDecimal computeSaleOrderLine(SaleOrderLine saleOrderLine) throws AxelorException  {
		
		BigDecimal exTaxTotal = BigDecimal.ZERO;
		
		if(saleOrderLine.getSaleOrderSubLineList() != null && !saleOrderLine.getSaleOrderSubLineList().isEmpty())  {
			for(SaleOrderSubLine saleOrderSubLine : saleOrderLine.getSaleOrderSubLineList())  {
				
				saleOrderSubLine.setCompanyExTaxTotal(saleOrderSubLineService.getCompanyExTaxTotal(saleOrderLine.getExTaxTotal(), saleOrderLine.getSaleOrder()));
				
				exTaxTotal = exTaxTotal.add(saleOrderSubLine.getExTaxTotal());
			}
		}
		else  {
			return saleOrderLine.getExTaxTotal();
		}
		
		return exTaxTotal;
	}

	
	public BigDecimal getCompanyExTaxTotal(BigDecimal exTaxTotal, SaleOrder saleOrder) throws AxelorException  {
		
		return currencyService.getAmountCurrencyConverted(
				saleOrder.getCurrency(), saleOrder.getCompany().getCurrency(), exTaxTotal, saleOrder.getCreationDate());  
	}
	
	
	public BigDecimal getCompanyCostPrice(SaleOrder saleOrder, SaleOrderLine saleOrderLine) throws AxelorException  {
		
		Product product = saleOrderLine.getProduct();
		
		return currencyService.getAmountCurrencyConverted(
				product.getPurchaseCurrency(), saleOrder.getCompany().getCurrency(), product.getCostPrice(), saleOrder.getCreationDate());  
	}
	
	
	public PriceListLine getPriceListLine(SaleOrderLine saleOrderLine, PriceList priceList)  {
		
		return priceListService.getPriceListLine(saleOrderLine.getProduct(), saleOrderLine.getQty(), priceList);
	
	}
	
	
	public BigDecimal computeDiscount(SaleOrderLine saleOrderLine)  {
		
		return priceListService.computeDiscount(saleOrderLine.getPrice(), saleOrderLine.getDiscountTypeSelect(), saleOrderLine.getDiscountAmount());
		
	}
	
	@Transactional(rollbackOn = {AxelorException.class, Exception.class})
	public BillOfMaterial customizeBillOfMaterial(SaleOrderLine saleOrderLine)  {
		
		BillOfMaterial billOfMaterial = saleOrderLine.getBillOfMaterial();
		
		if(billOfMaterial != null)  {
			return JPA.copy(billOfMaterial, true);
		}
		
		return null;
		
	}
	
}
