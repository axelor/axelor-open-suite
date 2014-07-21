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
package com.axelor.apps.account.service.invoice;

import java.math.BigDecimal;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.axelor.apps.account.db.Invoice;
import com.axelor.apps.account.db.InvoiceLine;
import com.axelor.apps.account.db.TaxLine;
import com.axelor.apps.base.db.IPriceListLine;
import com.axelor.apps.base.db.PriceList;
import com.axelor.apps.base.db.PriceListLine;
import com.axelor.apps.base.db.Product;
import com.axelor.apps.base.service.CurrencyService;
import com.axelor.apps.base.service.PriceListService;
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
				invoice.getInvoiceDate(), invoiceLine.getProduct(), invoice.getCompany(), invoice.getPartner().getFiscalPosition(), isPurchase);
		
	}
	
	
	public BigDecimal getUnitPrice(Invoice invoice, InvoiceLine invoiceLine, boolean isPurchase) throws AxelorException  {
		
		Product product = invoiceLine.getProduct();
		
		if(isPurchase)  {  
			return currencyService.getAmountCurrencyConverted(
				product.getPurchaseCurrency(), invoice.getCurrency(), product.getPurchasePrice(), invoice.getInvoiceDate());  
		}
		else  {  
			return currencyService.getAmountCurrencyConverted(
				product.getSaleCurrency(), invoice.getCurrency(), product.getSalePrice(), invoice.getInvoiceDate());  
		}
	}
	
	
	public boolean isPurchase(Invoice invoice)  {
		int operation = invoice.getOperationTypeSelect();
		if(operation == 1 || operation == 2)  { return true; }
		else  { return false; }
	}
	
	
	public BigDecimal getAccountingExTaxTotal(BigDecimal exTaxTotal, Invoice invoice) throws AxelorException  {
		
		return currencyService.getAmountCurrencyConverted(
				invoice.getCurrency(), invoice.getPartner().getCurrency(), exTaxTotal, invoice.getInvoiceDate());  
	}
	
	
	public BigDecimal getCompanyExTaxTotal(BigDecimal exTaxTotal, Invoice invoice) throws AxelorException  {
		
		return currencyService.getAmountCurrencyConverted(
				invoice.getCurrency(), invoice.getCompany().getCurrency(), exTaxTotal, invoice.getInvoiceDate());  
	}
	
	
	public PriceListLine getPriceListLine(InvoiceLine invoiceLine, PriceList priceList)  {
		
		return priceListService.getPriceListLine(invoiceLine.getProduct(), invoiceLine.getQty(), priceList);
	
	}
	
	
	public BigDecimal computeDiscount(InvoiceLine invoiceLine)  {
		BigDecimal unitPrice = invoiceLine.getPrice();
		
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
}
