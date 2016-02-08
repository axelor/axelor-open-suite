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
package com.axelor.apps.sale.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.axelor.apps.account.db.TaxLine;
import com.axelor.apps.base.db.IAdministration;
import com.axelor.apps.base.db.IPriceListLine;
import com.axelor.apps.base.db.PriceList;
import com.axelor.apps.base.db.PriceListLine;
import com.axelor.apps.base.db.Product;
import com.axelor.apps.base.db.Unit;
import com.axelor.apps.base.db.repo.GeneralRepository;
import com.axelor.apps.base.service.CurrencyService;
import com.axelor.apps.base.service.PriceListService;
import com.axelor.apps.base.service.administration.GeneralService;
import com.axelor.apps.base.service.tax.AccountManagementService;
import com.axelor.apps.sale.db.SaleOrder;
import com.axelor.apps.sale.db.SaleOrderLine;
import com.axelor.exception.AxelorException;
import com.axelor.inject.Beans;
import com.google.inject.Inject;

public class SaleOrderLineServiceImpl implements SaleOrderLineService {

	private final Logger logger = LoggerFactory.getLogger(getClass());

	@Inject
	private CurrencyService currencyService;

	@Inject
	private PriceListService priceListService;

	@Inject
	protected GeneralService generalService;


	/**
	 * Compute the excluded tax total amount of a sale order line.
	 *
	 * @param quantity
	 *          The quantity.
	 * @param price
	 *          The unit price.
	 * @return
	 * 			The excluded tax total amount.
	 */
	public BigDecimal computeAmount(SaleOrderLine saleOrderLine) {

		BigDecimal price = this.computeDiscount(saleOrderLine);

		BigDecimal amount = computeAmount(saleOrderLine.getQty(), price);
				
		return amount;
	}
	
	public BigDecimal computeAmount(BigDecimal quantity, BigDecimal price) {

		BigDecimal amount = quantity.multiply(price).setScale(IAdministration.DEFAULT_NB_DECIMAL_DIGITS, RoundingMode.HALF_EVEN);
		
		logger.debug("Calcul du montant HT avec une quantité de {} pour {} : {}", new Object[] { quantity, price, amount });

		return amount;
	}


	public BigDecimal getUnitPrice(SaleOrder saleOrder, SaleOrderLine saleOrderLine, TaxLine taxLine) throws AxelorException  {

		Product product = saleOrderLine.getProduct();

		BigDecimal price = this.convertUnitPrice(product, taxLine, product.getSalePrice(), saleOrder);
		
		return currencyService.getAmountCurrencyConverted(
			product.getSaleCurrency(), saleOrder.getCurrency(), price, saleOrder.getCreationDate())
			.setScale(generalService.getNbDecimalDigitForUnitPrice(), RoundingMode.HALF_UP);

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

	public BigDecimal convertUnitPrice(Product product, TaxLine taxLine, BigDecimal price, SaleOrder saleOrder){

		if(taxLine == null)  {  return price;  }
		
		if(product.getInAti() && !saleOrder.getInAti()){
			price = price.divide(taxLine.getValue().add(BigDecimal.ONE), 2, BigDecimal.ROUND_HALF_UP);
		}
		else if(!product.getInAti() && saleOrder.getInAti()){
			price = price.add(price.multiply(taxLine.getValue()));
		}
		return price;
	}

	public Map<String,Object> getDiscount(SaleOrder saleOrder, SaleOrderLine saleOrderLine, BigDecimal price)  {
		
		PriceList priceList = saleOrder.getPriceList();
		if(priceList != null)  {
			int discountTypeSelect = 0;
			
			PriceListLine priceListLine = this.getPriceListLine(saleOrderLine, priceList);
			if(priceListLine != null){
				discountTypeSelect = priceListLine.getTypeSelect();
			}
			
			Map<String, Object> discounts = priceListService.getDiscounts(priceList, priceListLine, price);
			if(discounts != null){
				int computeMethodDiscountSelect = generalService.getGeneral().getComputeMethodDiscountSelect();
				if((computeMethodDiscountSelect == GeneralRepository.INCLUDE_DISCOUNT_REPLACE_ONLY && discountTypeSelect == IPriceListLine.TYPE_REPLACE) 
						|| computeMethodDiscountSelect == GeneralRepository.INCLUDE_DISCOUNT)  {
					
					price = priceListService.computeDiscount(price, (int) discounts.get("discountTypeSelect"), (BigDecimal) discounts.get("discountAmount"));
					discounts.put("price", price);
				}
			}
			return discounts;
		}
		
		return null;
		
	}

	public int getDiscountTypeSelect(SaleOrder saleOrder, SaleOrderLine saleOrderLine){
		PriceList priceList = saleOrder.getPriceList();
		if(priceList != null)  {
			PriceListLine priceListLine = this.getPriceListLine(saleOrderLine, priceList);

			return priceListLine.getTypeSelect();
		}
		return 0;
	}
	
	public Unit getSaleUnit(SaleOrderLine saleOrderLine)  {
		Unit unit = saleOrderLine.getProduct().getSalesUnit();
		if(unit == null){
			unit = saleOrderLine.getProduct().getUnit();
		}
		return unit;
	}
	
	public boolean unitPriceShouldBeUpdate(SaleOrder saleOrder, Product product)  {
		
		if(product != null && product.getInAti() != saleOrder.getInAti())  {
			return true;
		}
		return false;
		
	}
}
