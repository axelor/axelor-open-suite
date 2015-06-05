/**
 * Axelor Business Solutions
 *
 * Copyright (C) 2014 Axelor (<http://axelor.com>).
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
import com.axelor.apps.base.db.IPriceListLine;
import com.axelor.apps.base.db.IAdministration;
import com.axelor.apps.base.db.PriceList;
import com.axelor.apps.base.db.PriceListLine;
import com.axelor.apps.base.db.Product;
import com.axelor.apps.base.service.CurrencyService;
import com.axelor.apps.base.service.PriceListService;
import com.axelor.apps.base.service.administration.GeneralService;
import com.axelor.apps.base.service.tax.AccountManagementService;
import com.axelor.apps.sale.db.SaleOrder;
import com.axelor.apps.sale.db.SaleOrderLine;
import com.axelor.apps.sale.db.repo.SaleOrderLineRepository;
import com.axelor.exception.AxelorException;
import com.axelor.inject.Beans;
import com.google.inject.Inject;

public class SaleOrderLineService extends SaleOrderLineRepository{

	private static final Logger LOG = LoggerFactory.getLogger(SaleOrderLineService.class);

	@Inject
	private CurrencyService currencyService;

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

		BigDecimal amount = quantity.multiply(price).setScale(IAdministration.DEFAULT_NB_DECIMAL_DIGITS, RoundingMode.HALF_EVEN);

		LOG.debug("Calcul du montant HT avec une quantité de {} pour {} : {}", new Object[] { quantity, price, amount });

		return amount;
	}


	public BigDecimal getUnitPrice(SaleOrder saleOrder, SaleOrderLine saleOrderLine) throws AxelorException  {

		Product product = saleOrderLine.getProduct();

		return currencyService.getAmountCurrencyConverted(
			product.getSaleCurrency(), saleOrder.getCurrency(), product.getSalePrice(), saleOrder.getCreationDate())
			.setScale(GeneralService.getNbDecimalDigitForUnitPrice(), RoundingMode.HALF_UP);
		
	}


	public TaxLine getTaxLine(SaleOrder saleOrder, SaleOrderLine saleOrderLine) throws AxelorException  {

		return Beans.get(AccountManagementService.class).getTaxLine(
				saleOrder.getCreationDate(), saleOrderLine.getProduct(), saleOrder.getCompany(), saleOrder.getClientPartner().getFiscalPosition(), false);

	}


	public BigDecimal getAmountInCompanyCurrency(BigDecimal exTaxTotal, SaleOrder saleOrder) throws AxelorException  {

		return currencyService.getAmountCurrencyConverted(
				saleOrder.getCurrency(), saleOrder.getCompany().getCurrency(), exTaxTotal, saleOrder.getCreationDate())
				.setScale(IAdministration.DEFAULT_NB_DECIMAL_DIGITS, RoundingMode.HALF_UP);  
	}


	public BigDecimal getCompanyCostPrice(SaleOrder saleOrder, SaleOrderLine saleOrderLine) throws AxelorException  {

		Product product = saleOrderLine.getProduct();

		return currencyService.getAmountCurrencyConverted(
				product.getPurchaseCurrency(), saleOrder.getCompany().getCurrency(), product.getCostPrice(), saleOrder.getCreationDate())
				.setScale(IAdministration.DEFAULT_NB_DECIMAL_DIGITS, RoundingMode.HALF_UP);  
	}


	public PriceListLine getPriceListLine(SaleOrderLine saleOrderLine, PriceList priceList)  {

		return priceListService.getPriceListLine(saleOrderLine.getProduct(), saleOrderLine.getQty(), priceList);

	}


	public BigDecimal computeDiscount(SaleOrderLine saleOrderLine)  {

		return priceListService.computeDiscount(saleOrderLine.getPrice(), saleOrderLine.getDiscountTypeSelect(),saleOrderLine.getDiscountAmount());

	}

	public BigDecimal convertUnitPrice(SaleOrderLine saleOrderLine, SaleOrder saleOrder){
		BigDecimal price = saleOrderLine.getProduct().getSalePrice();

		if(saleOrderLine.getProduct().getInAti() && !saleOrder.getInAti()){
			price = price.divide(saleOrderLine.getTaxLine().getValue().add(new BigDecimal(1)), 2, BigDecimal.ROUND_HALF_UP);
		}
		else if(!saleOrderLine.getProduct().getInAti() && saleOrder.getInAti()){
			price = price.add(price.multiply(saleOrderLine.getTaxLine().getValue()));
		}
		return price;
	}

	public BigDecimal convertDiscountAmount(SaleOrderLine saleOrderLine, SaleOrder saleOrder){
		BigDecimal discountAmount = BigDecimal.ZERO;
		if(saleOrderLine.getDiscountTypeSelect() == IPriceListLine.AMOUNT_TYPE_FIXED){
			discountAmount = saleOrderLine.getProduct().getSalePrice().subtract(this.computeDiscount(saleOrderLine));
		}
		else{
			discountAmount = (saleOrderLine.getProduct().getSalePrice().subtract(this.computeDiscount(saleOrderLine))).multiply(new BigDecimal(100)).divide(saleOrderLine.getProduct().getSalePrice());
		}
		if(saleOrderLine.getProduct().getInAti() && !saleOrder.getInAti()){
			discountAmount = discountAmount.divide(saleOrderLine.getTaxLine().getValue().add(new BigDecimal(1)), 2, BigDecimal.ROUND_HALF_UP);

		}
		else if(!saleOrderLine.getProduct().getInAti() && saleOrder.getInAti()){
			discountAmount = discountAmount.add(discountAmount.multiply(saleOrderLine.getTaxLine().getValue()));
		}
		return discountAmount;
	}
}
