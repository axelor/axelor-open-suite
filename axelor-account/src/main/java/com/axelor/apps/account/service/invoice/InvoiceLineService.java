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
package com.axelor.apps.account.service.invoice;

import java.math.BigDecimal;
import java.math.RoundingMode;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.axelor.apps.account.db.Invoice;
import com.axelor.apps.account.db.InvoiceLine;
import com.axelor.apps.account.db.TaxLine;
import com.axelor.apps.account.db.repo.InvoiceRepository;
import com.axelor.apps.base.db.IAdministration;
import com.axelor.apps.base.db.IPriceListLine;
import com.axelor.apps.base.db.PriceList;
import com.axelor.apps.base.db.PriceListLine;
import com.axelor.apps.base.db.Product;
import com.axelor.apps.base.service.CurrencyService;
import com.axelor.apps.base.service.PriceListService;
import com.axelor.apps.base.service.administration.GeneralService;
import com.axelor.apps.base.service.tax.AccountManagementService;
import com.axelor.exception.AxelorException;
import com.google.inject.Inject;

public class InvoiceLineService {

	private static final Logger LOG = LoggerFactory.getLogger(InvoiceLineService.class);


	@Inject
	private AccountManagementService accountManagementService;

	@Inject
	private CurrencyService currencyService;

	@Inject
	private PriceListService priceListService;


	public TaxLine getTaxLine(Invoice invoice, InvoiceLine invoiceLine, boolean isPurchase) throws AxelorException  {

		return accountManagementService.getTaxLine(
				GeneralService.getTodayDate(), invoiceLine.getProduct(), invoice.getCompany(), invoice.getPartner().getFiscalPosition(), isPurchase);

	}


	public BigDecimal getUnitPrice(Invoice invoice, InvoiceLine invoiceLine, boolean isPurchase) throws AxelorException  {

		Product product = invoiceLine.getProduct();

		if(isPurchase)  {
			return currencyService.getAmountCurrencyConverted(
				product.getPurchaseCurrency(), invoice.getCurrency(), product.getPurchasePrice(), invoice.getInvoiceDate()).setScale(GeneralService.getNbDecimalDigitForUnitPrice(), RoundingMode.HALF_UP);  
		}
		else  {
			return currencyService.getAmountCurrencyConverted(
				product.getSaleCurrency(), invoice.getCurrency(), product.getSalePrice(), invoice.getInvoiceDate()).setScale(GeneralService.getNbDecimalDigitForUnitPrice(), RoundingMode.HALF_UP);  
		}
	}


	public boolean isPurchase(Invoice invoice)  {
		int operation = invoice.getOperationTypeSelect();
		if(operation == 1 || operation == 2)  { return true; }
		else  { return false; }
	}


	public BigDecimal getAccountingExTaxTotal(BigDecimal exTaxTotal, Invoice invoice) throws AxelorException  {

		return currencyService.getAmountCurrencyConverted(
				invoice.getCurrency(), invoice.getPartner().getCurrency(), exTaxTotal, invoice.getInvoiceDate()).setScale(IAdministration.DEFAULT_NB_DECIMAL_DIGITS, RoundingMode.HALF_UP);  
	}


	public BigDecimal getCompanyExTaxTotal(BigDecimal exTaxTotal, Invoice invoice) throws AxelorException  {

		return currencyService.getAmountCurrencyConverted(
				invoice.getCurrency(), invoice.getCompany().getCurrency(), exTaxTotal, invoice.getInvoiceDate()).setScale(GeneralService.getNbDecimalDigitForUnitPrice(), RoundingMode.HALF_UP);  
	}


	public PriceListLine getPriceListLine(InvoiceLine invoiceLine, PriceList priceList)  {

		return priceListService.getPriceListLine(invoiceLine.getProduct(), invoiceLine.getQty(), priceList);

	}

	public BigDecimal computeDiscount(InvoiceLine invoiceLine, Invoice invoice)  {
		BigDecimal unitPrice = BigDecimal.ZERO;
		
		if(invoice.getOperationTypeSelect()<InvoiceRepository.OPERATION_TYPE_CLIENT_SALE){
			unitPrice = invoiceLine.getProduct().getCostPrice();
		}
		
		if(invoice.getOperationTypeSelect()>=InvoiceRepository.OPERATION_TYPE_CLIENT_SALE){
			unitPrice = invoiceLine.getProduct().getSalePrice();
		}

		if(invoiceLine.getDiscountTypeSelect() == IPriceListLine.AMOUNT_TYPE_FIXED)  {
			return  unitPrice.add(invoiceLine.getDiscountAmount());
		}
		else if(invoiceLine.getDiscountTypeSelect() == IPriceListLine.AMOUNT_TYPE_PERCENT)  {
			return unitPrice.multiply(
					BigDecimal.ONE.add(
							invoiceLine.getDiscountAmount().divide(new BigDecimal(100))));
		}

		return unitPrice;
	}

	public BigDecimal convertUnitPrice(InvoiceLine invoiceLine, Invoice invoice){
		BigDecimal price = invoiceLine.getProduct().getSalePrice();
		if(invoice.getOperationTypeSelect()<2){
			price = invoiceLine.getProduct().getPurchasePrice();
		}

		if(invoiceLine.getProduct().getInAti() && !invoice.getInAti()){
			price = price.divide(invoiceLine.getTaxLine().getValue().add(new BigDecimal(1)), 2, BigDecimal.ROUND_HALF_UP);
		}
		else if(!invoiceLine.getProduct().getInAti() && invoice.getInAti()){
			price = price.add(price.multiply(invoiceLine.getTaxLine().getValue()));
		}
		return price;
	}

	public BigDecimal convertDiscountAmount(InvoiceLine invoiceLine, Invoice invoice){
		BigDecimal discountAmount = BigDecimal.ZERO;
		if(invoiceLine.getDiscountTypeSelect() == IPriceListLine.AMOUNT_TYPE_FIXED){
			discountAmount = invoiceLine.getProduct().getSalePrice().subtract(this.computeDiscount(invoiceLine,invoice));
			if(invoice.getOperationTypeSelect()<2){
				discountAmount = invoiceLine.getProduct().getPurchasePrice().subtract(this.computeDiscount(invoiceLine,invoice));
			}
		}
		else{
			discountAmount = (invoiceLine.getProduct().getSalePrice().subtract(this.computeDiscount(invoiceLine,invoice))).multiply(new BigDecimal(100)).divide(invoiceLine.getProduct().getSalePrice());
			if(invoice.getOperationTypeSelect()<2){
				discountAmount = (invoiceLine.getProduct().getPurchasePrice().subtract(this.computeDiscount(invoiceLine,invoice))).multiply(new BigDecimal(100)).divide(invoiceLine.getProduct().getPurchasePrice());
			}
		}

		if(invoiceLine.getProduct().getInAti() && !invoice.getInAti()){
			discountAmount = discountAmount.divide(invoiceLine.getTaxLine().getValue().add(new BigDecimal(1)), 2, BigDecimal.ROUND_HALF_UP);
		}
		else if(!invoiceLine.getProduct().getInAti() && invoice.getInAti()){
			discountAmount = discountAmount.add(discountAmount.multiply(invoiceLine.getTaxLine().getValue()));
		}
		return discountAmount;
	}
	
	public int getDiscountTypeSelect(Invoice invoice, InvoiceLine invoiceLine){
		PriceList priceList = invoice.getPriceList();
		if(priceList != null)  {
			PriceListLine priceListLine = this.getPriceListLine(invoiceLine, priceList);

			return (int) priceListLine.getTypeSelect();
		}
		return 0;
	}
}
