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
package com.axelor.apps.accountorganisation.service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import com.axelor.apps.account.db.Invoice;
import com.axelor.apps.account.db.InvoiceLine;
import com.axelor.apps.account.db.TaxLine;
import com.axelor.apps.account.db.repo.InvoiceRepository;
import com.axelor.apps.account.service.invoice.generator.InvoiceGenerator;
import com.axelor.apps.base.db.Product;
import com.axelor.apps.base.db.ProductVariant;
import com.axelor.apps.base.db.Unit;
import com.axelor.apps.organisation.db.Task;
import com.axelor.apps.organisation.service.invoice.InvoiceGeneratorOrganisation;
import com.axelor.apps.organisation.service.invoice.InvoiceLineGeneratorOrganisation;
import com.axelor.apps.sale.db.SaleOrder;
import com.axelor.apps.sale.db.SaleOrderLine;
import com.axelor.apps.sale.db.SaleOrderSubLine;
import com.axelor.apps.supplychain.exception.IExceptionMessage;
import com.axelor.apps.supplychain.service.SaleOrderInvoiceServiceImpl;
import com.axelor.exception.AxelorException;
import com.axelor.exception.db.IException;
import com.axelor.i18n.I18n;
import com.google.inject.Inject;

public class SaleOrderInvoiceServiceAccountOrganisationImpl extends SaleOrderInvoiceServiceImpl {

	@Override
	public InvoiceGenerator createInvoiceGenerator(SaleOrder saleOrder) throws AxelorException  {
		
		if(saleOrder.getCurrency() == null)  {
			throw new AxelorException(String.format(I18n.get(IExceptionMessage.SO_INVOICE_6), saleOrder.getSaleOrderSeq()), IException.CONFIGURATION_ERROR);
		}
		
		InvoiceGeneratorOrganisation invoiceGenerator = new InvoiceGeneratorOrganisation(InvoiceRepository.OPERATION_TYPE_CLIENT_SALE, saleOrder.getCompany(),saleOrder.getPaymentCondition(), 
				saleOrder.getPaymentMode(), saleOrder.getMainInvoicingAddress(), saleOrder.getClientPartner(), saleOrder.getContactPartner(), 
				saleOrder.getCurrency(), saleOrder.getProject(), saleOrder.getPriceList(), saleOrder.getSaleOrderSeq(), saleOrder.getExternalReference()) {
			
			@Override
			public Invoice generate() throws AxelorException {
				
				return super.createInvoiceHeader();
			}
		};
		
		return invoiceGenerator;
		
	}
	
	public List<InvoiceLine> createInvoiceLine(Invoice invoice, Product product, String productName, BigDecimal price, String description, BigDecimal qty,
			Unit unit, TaxLine taxLine, Task task, ProductVariant productVariant, BigDecimal discountAmount, int discountTypeSelect, BigDecimal exTaxTotal) throws AxelorException  {
		
		InvoiceLineGeneratorOrganisation invoiceLineGenerator = new InvoiceLineGeneratorOrganisation(invoice, product, productName, price, description, qty, unit, taxLine, task, product.getInvoiceLineType(), 
				discountAmount, discountTypeSelect, exTaxTotal, false)  {
			@Override
			public List<InvoiceLine> creates() throws AxelorException {
				
				InvoiceLine invoiceLine = this.createInvoiceLine();
				
				List<InvoiceLine> invoiceLines = new ArrayList<InvoiceLine>();
				invoiceLines.add(invoiceLine);
				
				return invoiceLines;
			}
		};
		
		return invoiceLineGenerator.creates();
	}
	
	
	@Override
	public List<InvoiceLine> createInvoiceLine(Invoice invoice, SaleOrderLine saleOrderLine) throws AxelorException  {
		
		return this.createInvoiceLine(invoice, saleOrderLine.getProduct(), saleOrderLine.getProductName(), 
				saleOrderLine.getPrice(), saleOrderLine.getDescription(), saleOrderLine.getQty(), saleOrderLine.getUnit(), saleOrderLine.getTaxLine(), 
				saleOrderLine.getTask(), saleOrderLine.getProductVariant(), saleOrderLine.getDiscountAmount(), saleOrderLine.getDiscountTypeSelect(), saleOrderLine.getExTaxTotal());
		
		
	}
	
	@Override
	public List<InvoiceLine> createInvoiceLine(Invoice invoice, SaleOrderSubLine saleOrderSubLine) throws AxelorException  {
		
		return this.createInvoiceLine(invoice, saleOrderSubLine.getProduct(), saleOrderSubLine.getProductName(), 
				saleOrderSubLine.getPrice(), saleOrderSubLine.getDescription(), saleOrderSubLine.getQty(), saleOrderSubLine.getUnit(), 
				saleOrderSubLine.getTaxLine(), saleOrderSubLine.getSaleOrderLine().getTask(), saleOrderSubLine.getProductVariant(), 
				saleOrderSubLine.getDiscountAmount(), saleOrderSubLine.getDiscountTypeSelect(), saleOrderSubLine.getExTaxTotal());
		
	}
	
}


