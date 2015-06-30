package com.axelor.apps.business.project.service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import com.axelor.apps.account.db.Invoice;
import com.axelor.apps.account.db.InvoiceLine;
import com.axelor.apps.account.service.invoice.generator.InvoiceLineGenerator;
import com.axelor.apps.base.db.IPriceListLine;
import com.axelor.apps.base.db.Product;
import com.axelor.apps.businessproject.db.ElementsToInvoice;
import com.axelor.exception.AxelorException;

public class ElementsToInvoiceService {

	public List<InvoiceLine> createInvoiceLines(Invoice invoice, List<ElementsToInvoice> elementsToInvoiceList) throws AxelorException  {

		List<InvoiceLine> invoiceLineList = new ArrayList<InvoiceLine>();

		for(ElementsToInvoice elementsToInvoice : elementsToInvoiceList)  {

			invoiceLineList.addAll(this.createInvoiceLine(invoice, elementsToInvoice));

		}

		return invoiceLineList;

	}

	public List<InvoiceLine> createInvoiceLine(Invoice invoice, ElementsToInvoice elementsToInvoice) throws AxelorException  {

		Product product = elementsToInvoice.getProduct();

		InvoiceLineGenerator invoiceLineGenerator = new InvoiceLineGenerator(invoice, product, product.getName(), elementsToInvoice.getSalePrice(),
				null,elementsToInvoice.getQty(),elementsToInvoice.getUnit(),10,BigDecimal.ZERO,IPriceListLine.AMOUNT_TYPE_NONE,
				elementsToInvoice.getSalePrice().multiply(elementsToInvoice.getQty()),null,false)  {

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
