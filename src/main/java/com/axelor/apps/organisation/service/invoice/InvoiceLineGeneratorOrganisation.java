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
package com.axelor.apps.organisation.service.invoice;

import java.math.BigDecimal;
import java.util.List;

import com.axelor.apps.account.db.Invoice;
import com.axelor.apps.account.db.InvoiceLine;
import com.axelor.apps.account.db.InvoiceLineType;
import com.axelor.apps.account.db.TaxLine;
import com.axelor.apps.account.service.invoice.generator.InvoiceLineGenerator;
import com.axelor.apps.base.db.Product;
import com.axelor.apps.base.db.Unit;
import com.axelor.apps.organisation.db.Task;
import com.axelor.exception.AxelorException;

/**
 * Classe de création de ligne de facture abstraite.
 * 
 */
public class InvoiceLineGeneratorOrganisation extends InvoiceLineGenerator {
	
	protected Task task; 
	

	protected InvoiceLineGeneratorOrganisation() {
		
		super();
		
	}
	
	protected InvoiceLineGeneratorOrganisation(int type) {
		
		super(type);
		
	}
	
	protected InvoiceLineGeneratorOrganisation( Invoice invoice ) {

		super(invoice);
        
    }
	
	protected InvoiceLineGeneratorOrganisation( Invoice invoice, int type ) {

		super(invoice, type);
        
    }
	
	protected InvoiceLineGeneratorOrganisation( Invoice invoice, Product product, String productName, BigDecimal price, String description, BigDecimal qty, Unit unit, 
			TaxLine taxLine, Task task, InvoiceLineType invoiceLineType, BigDecimal discountAmount, int discountTypeSelect, BigDecimal exTaxTotal, boolean isTaxInvoice) {

		super(invoice, product, productName, price, description, qty,
				unit, taxLine, invoiceLineType, discountAmount, discountTypeSelect, exTaxTotal, isTaxInvoice);
		
		this.task = task;
        
    }
	
	protected InvoiceLineGeneratorOrganisation( Invoice invoice, Product product, String productName, BigDecimal price, String description, BigDecimal qty,
			Unit unit, Task task, InvoiceLineType invoiceLineType, BigDecimal discountAmount, int discountTypeSelect, BigDecimal exTaxTotal, boolean isTaxInvoice) {

		super(invoice, product, productName, price, description, qty,
				unit, invoiceLineType, discountAmount, discountTypeSelect, exTaxTotal, isTaxInvoice);
		
		this.task = task;
		
    }
	
	
	/**
	 * @return
	 * @throws AxelorException 
	 */
	@Override
	protected InvoiceLine createInvoiceLine() throws AxelorException  {
		
		InvoiceLine invoiceLine = super.createInvoiceLine();
		
		invoiceLine.setTask(task);
		
		return invoiceLine;
		
	}

	@Override
	public List<InvoiceLine> creates() throws AxelorException {
		// TODO Auto-generated method stub
		return null;
	}
	
	
}