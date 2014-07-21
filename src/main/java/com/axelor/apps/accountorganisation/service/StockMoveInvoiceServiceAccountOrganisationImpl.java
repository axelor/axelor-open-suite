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
package com.axelor.apps.accountorganisation.service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import com.axelor.apps.account.db.Invoice;
import com.axelor.apps.account.db.InvoiceLine;
import com.axelor.apps.base.db.Product;
import com.axelor.apps.organisation.db.Task;
import com.axelor.apps.organisation.service.invoice.InvoiceLineGeneratorOrganisation;
import com.axelor.apps.supplychain.db.StockMoveLine;
import com.axelor.apps.supplychain.service.StockMoveInvoiceServiceImpl;
import com.axelor.exception.AxelorException;
import com.axelor.exception.db.IException;

public class StockMoveInvoiceServiceAccountOrganisationImpl extends StockMoveInvoiceServiceImpl  {
	
	@Override
	public List<InvoiceLine> createInvoiceLine(Invoice invoice, StockMoveLine stockMoveLine) throws AxelorException {
		
		Product product = stockMoveLine.getProduct();
		
		if (product == null)
			throw new AxelorException(String.format("Produit incorrect dans le mouvement de stock %s ", stockMoveLine.getStockMove().getStockMoveSeq()), IException.CONFIGURATION_ERROR);

		Task task = null;
		if(invoice.getProject() != null)  {
			task = invoice.getProject().getDefaultTask();
		}
		
		InvoiceLineGeneratorOrganisation invoiceLineGenerator = new InvoiceLineGeneratorOrganisation(invoice, product, product.getName(), stockMoveLine.getPrice(), 
				product.getDescription(), stockMoveLine.getQty(), stockMoveLine.getUnit(), task, product.getInvoiceLineType(), BigDecimal.ZERO, 0, null, false)  {
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
}
