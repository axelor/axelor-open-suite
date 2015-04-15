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
package com.axelor.apps.supplychain.service.invoice.generator;

import java.math.BigDecimal;

import com.axelor.apps.account.db.Invoice;
import com.axelor.apps.account.db.InvoiceLine;
import com.axelor.apps.account.db.InvoiceLineType;
import com.axelor.apps.account.db.TaxLine;
import com.axelor.apps.account.service.invoice.generator.InvoiceLineGenerator;
import com.axelor.apps.base.db.Product;
import com.axelor.apps.base.db.Unit;
import com.axelor.apps.base.service.administration.GeneralService;
import com.axelor.apps.sale.db.SaleOrderLine;
import com.axelor.exception.AxelorException;

/**
 * Classe de création de ligne de facture abstraite.
 *
 */
public abstract class InvoiceLineGeneratorSupplyChain extends InvoiceLineGenerator {

	protected SaleOrderLine saleOrderLine;

	protected InvoiceLineGeneratorSupplyChain( Invoice invoice, Product product, String productName, BigDecimal price, String description, BigDecimal qty,
			Unit unit, TaxLine taxLine, InvoiceLineType invoiceLineType, int sequence, BigDecimal discountAmount, int discountTypeSelect, BigDecimal exTaxTotal, boolean isTaxInvoice, SaleOrderLine saleOrderLine) {

		super(invoice, product, productName, price, description, qty, unit, taxLine, invoiceLineType, sequence, discountAmount, discountTypeSelect, exTaxTotal, isTaxInvoice);
        this.saleOrderLine = saleOrderLine;
    }

	protected InvoiceLineGeneratorSupplyChain( Invoice invoice, Product product, String productName, BigDecimal price, String description, BigDecimal qty,
			Unit unit, InvoiceLineType invoiceLineType, int sequence, BigDecimal discountAmount, int discountTypeSelect, BigDecimal exTaxTotal, boolean isTaxInvoice, SaleOrderLine saleOrderLine) {

		super(invoice, product, productName, price, description, qty, unit, invoiceLineType, sequence, discountAmount, discountTypeSelect, exTaxTotal, isTaxInvoice);
        this.saleOrderLine = saleOrderLine;
    }


	/**
	 * @return
	 * @throws AxelorException
	 */
	protected InvoiceLine createInvoiceLine() throws AxelorException  {

		InvoiceLine invoiceLine = super.createInvoiceLine();

		if (GeneralService.getGeneral().getManageAmountInvoiceByLine()){
			invoiceLine.setSaleOrderLine(saleOrderLine);
		}

		return invoiceLine;

	}

}