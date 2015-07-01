package com.axelor.apps.business.project.service;

import java.util.ArrayList;
import java.util.List;

import com.axelor.apps.account.db.Invoice;
import com.axelor.apps.account.db.InvoiceLine;
import com.axelor.apps.sale.db.SaleOrderLine;
import com.axelor.apps.supplychain.service.SaleOrderInvoiceServiceImpl;
import com.axelor.exception.AxelorException;

public class SaleOrderInvoiceProjectServiceImpl extends SaleOrderInvoiceServiceImpl{
	@Override
	public List<InvoiceLine> createInvoiceLines(Invoice invoice, List<SaleOrderLine> saleOrderLineList) throws AxelorException  {

		List<InvoiceLine> invoiceLineList = new ArrayList<InvoiceLine>();

		for(SaleOrderLine saleOrderLine : saleOrderLineList)  {

			invoiceLineList.addAll(this.createInvoiceLine(invoice, saleOrderLine));
			invoiceLineList.get(invoiceLineList.size()-1).setProject(saleOrderLine.getProject());
			saleOrderLine.setInvoiced(true);
		}

		return invoiceLineList;

	}
}
