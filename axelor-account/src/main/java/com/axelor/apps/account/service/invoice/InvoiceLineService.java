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
				product.getPurchaseCurrency(), invoice.getCurrency(), product.getPurchasePrice(), invoice.getInvoiceDate()).setScale(IAdministration.NB_DECIMAL_UNIT_PRICE, RoundingMode.HALF_UP);  
		}
		else  {  
			return currencyService.getAmountCurrencyConverted(
				product.getSaleCurrency(), invoice.getCurrency(), product.getSalePrice(), invoice.getInvoiceDate()).setScale(IAdministration.NB_DECIMAL_UNIT_PRICE, RoundingMode.HALF_UP);  
		}
	}
	
	
	public boolean isPurchase(Invoice invoice)  {
		int operation = invoice.getOperationTypeSelect();
		if(operation == 1 || operation == 2)  { return true; }
		else  { return false; }
	}
	
	
	public BigDecimal getAccountingExTaxTotal(BigDecimal exTaxTotal, Invoice invoice) throws AxelorException  {
		
		return currencyService.getAmountCurrencyConverted(
				invoice.getCurrency(), invoice.getPartner().getCurrency(), exTaxTotal, invoice.getInvoiceDate()).setScale(IAdministration.NB_DECIMAL_TOTAL, RoundingMode.HALF_UP);  
	}
	
	
	public BigDecimal getCompanyExTaxTotal(BigDecimal exTaxTotal, Invoice invoice) throws AxelorException  {
		
		return currencyService.getAmountCurrencyConverted(
				invoice.getCurrency(), invoice.getCompany().getCurrency(), exTaxTotal, invoice.getInvoiceDate()).setScale(IAdministration.NB_DECIMAL_UNIT_PRICE, RoundingMode.HALF_UP);  
	}
	
	
	public PriceListLine getPriceListLine(InvoiceLine invoiceLine, PriceList priceList)  {
		
		return priceListService.getPriceListLine(invoiceLine.getProduct(), invoiceLine.getQty(), priceList);
	
	}
	
	
	public BigDecimal computeDiscount(InvoiceLine invoiceLine)  {
		
		return priceListService.computeDiscount(invoiceLine.getPrice(), invoiceLine.getDiscountTypeSelect(), invoiceLine.getDiscountAmount());

	}
}
