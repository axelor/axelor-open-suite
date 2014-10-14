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
package com.axelor.apps.account.web;

import java.math.BigDecimal;
import java.util.Map;

import com.axelor.apps.account.db.Invoice;
import com.axelor.apps.account.db.InvoiceLine;
import com.axelor.apps.account.service.invoice.InvoiceLineService;
import com.axelor.apps.account.service.invoice.generator.line.InvoiceLineManagement;
import com.axelor.apps.base.db.PriceList;
import com.axelor.apps.base.db.PriceListLine;
import com.axelor.apps.base.service.PriceListService;
import com.axelor.exception.AxelorException;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.google.inject.Inject;

public class InvoiceLineController {

	@Inject
	private InvoiceLineService invoiceLineService;
	
	@Inject
	private PriceListService priceListService;

	public void compute(ActionRequest request, ActionResponse response) throws AxelorException {

		InvoiceLine invoiceLine = request.getContext().asType(InvoiceLine.class);
		BigDecimal exTaxTotal = BigDecimal.ZERO;
		BigDecimal accountingExTaxTotal = BigDecimal.ZERO;
		BigDecimal companyExTaxTotal = BigDecimal.ZERO;

		if(invoiceLine.getPrice() != null && invoiceLine.getQty() != null) {

			exTaxTotal = InvoiceLineManagement.computeAmount(invoiceLine.getQty(), invoiceLineService.computeDiscount(invoiceLine));
		}

		if(exTaxTotal != null) {

			Invoice invoice = invoiceLine.getInvoice();

			if(invoice == null) {
				invoice = request.getContext().getParentContext().asType(Invoice.class);
			}

			if(invoice != null) {
				accountingExTaxTotal = invoiceLineService.getAccountingExTaxTotal(exTaxTotal, invoice);
				companyExTaxTotal = invoiceLineService.getCompanyExTaxTotal(exTaxTotal, invoice);
			}
		}
		response.setValue("exTaxTotal", exTaxTotal);
		response.setValue("accountingExTaxTotal", accountingExTaxTotal);
		response.setValue("companyExTaxTotal", companyExTaxTotal);

	}

	public void getProductInformation(ActionRequest request, ActionResponse response) throws AxelorException {

		InvoiceLine invoiceLine = request.getContext().asType(InvoiceLine.class);

		Invoice invoice = invoiceLine.getInvoice();

		if(invoice == null)  {
			invoice = request.getContext().getParentContext().asType(Invoice.class);
		}

		if(invoice != null && invoiceLine.getProduct() != null)  {

			try  {
			
				boolean isPurchase = invoiceLineService.isPurchase(invoice);
				
				BigDecimal price = invoiceLineService.getUnitPrice(invoice, invoiceLine, isPurchase);
				
				response.setValue("taxLine", invoiceLineService.getTaxLine(invoice, invoiceLine, isPurchase));
				response.setValue("productName", invoiceLine.getProduct().getName());
				
				PriceList priceList = invoice.getPriceList();
				if(priceList != null)  {
					PriceListLine priceListLine = invoiceLineService.getPriceListLine(invoiceLine, priceList);
					
					Map<String, Object> discounts = priceListService.getDiscounts(priceList, priceListLine, price);
					
					response.setValue("discountAmount", discounts.get("discountAmount"));
					response.setValue("discountTypeSelect", discounts.get("discountTypeSelect"));
					if(discounts.get("price") != null)  {
						price = (BigDecimal) discounts.get("price");
					}
				}
				
				response.setValue("price", price);
			}
			catch(Exception e) {
				response.setFlash(e.getMessage());
				this.resetProductInformation(response);
			}
		}
		else {
			this.resetProductInformation(response);
		}
	}
	
	
	public void resetProductInformation(ActionResponse response)  {
		
		response.setValue("taxLine", null);
		response.setValue("productName", null);
		response.setValue("discountAmount", null);
		response.setValue("discountTypeSelect", null);
		response.setValue("price", null);
		
	}
	
	
	public void getDiscount(ActionRequest request, ActionResponse response) throws AxelorException {

		InvoiceLine invoiceLine = request.getContext().asType(InvoiceLine.class);

		Invoice invoice = invoiceLine.getInvoice();

		if(invoice == null)  {
			invoice = request.getContext().getParentContext().asType(Invoice.class);
		}

		if(invoice != null && invoiceLine.getProduct() != null)  {

			try  {
			
				BigDecimal price = invoiceLine.getPrice();
				
				PriceList priceList = invoice.getPriceList();
				if(priceList != null)  {
					PriceListLine priceListLine = invoiceLineService.getPriceListLine(invoiceLine, priceList);
					
					Map<String, Object> discounts = priceListService.getDiscounts(priceList, priceListLine, price);
					
					response.setValue("discountAmount", discounts.get("discountAmount"));
					response.setValue("discountTypeSelect", discounts.get("discountTypeSelect"));
					if(discounts.get("price") != null)  {
						price = (BigDecimal) discounts.get("price");
					}
				}
				
				response.setValue("price", price);
			}
			catch(Exception e) {
				response.setFlash(e.getMessage());
			}
		}
	}
}
