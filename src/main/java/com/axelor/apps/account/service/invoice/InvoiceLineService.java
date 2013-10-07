/**
 * Copyright (c) 2012-2013 Axelor. All Rights Reserved.
 *
 * The contents of this file are subject to the Common Public
 * Attribution License Version 1.0 (the “License”); you may not use
 * this file except in compliance with the License. You may obtain a
 * copy of the License at:
 *
 * http://license.axelor.com/.
 *
 * The License is based on the Mozilla Public License Version 1.1 but
 * Sections 14 and 15 have been added to cover use of software over a
 * computer network and provide for limited attribution for the
 * Original Developer. In addition, Exhibit A has been modified to be
 * consistent with Exhibit B.
 *
 * Software distributed under the License is distributed on an “AS IS”
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See
 * the License for the specific language governing rights and limitations
 * under the License.
 *
 * The Original Code is part of "Axelor Business Suite", developed by
 * Axelor exclusively.
 *
 * The Original Developer is the Initial Developer. The Initial Developer of
 * the Original Code is Axelor.
 *
 * All portions of the code written by Axelor are
 * Copyright (c) 2012-2013 Axelor. All Rights Reserved.
 */
package com.axelor.apps.account.service.invoice;

import java.math.BigDecimal;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.axelor.apps.account.db.Invoice;
import com.axelor.apps.account.db.InvoiceLine;
import com.axelor.apps.account.db.VatLine;
import com.axelor.apps.account.service.AccountManagementService;
import com.axelor.apps.base.db.IPriceListLine;
import com.axelor.apps.base.db.PriceList;
import com.axelor.apps.base.db.PriceListLine;
import com.axelor.apps.base.db.Product;
import com.axelor.apps.base.service.CurrencyService;
import com.axelor.apps.base.service.PriceListService;
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
	
	
	public VatLine getVatLine(Invoice invoice, InvoiceLine invoiceLine, boolean isPurchase) throws AxelorException  {
		
		return accountManagementService.getVatLine(invoice.getInvoiceDate(), invoiceLine.getProduct(), invoice.getCompany(), isPurchase);
		
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
				invoice.getCurrency(), invoice.getClientPartner().getCurrency(), exTaxTotal, invoice.getInvoiceDate());  
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
