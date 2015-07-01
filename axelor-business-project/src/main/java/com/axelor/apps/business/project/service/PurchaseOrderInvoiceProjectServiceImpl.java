package com.axelor.apps.business.project.service;

import java.util.ArrayList;
import java.util.List;

import com.axelor.apps.account.db.Invoice;
import com.axelor.apps.account.db.InvoiceLine;
import com.axelor.apps.purchase.db.PurchaseOrderLine;
import com.axelor.apps.supplychain.service.PurchaseOrderInvoiceServiceImpl;
import com.axelor.exception.AxelorException;

public class PurchaseOrderInvoiceProjectServiceImpl extends PurchaseOrderInvoiceServiceImpl{

	@Override
	public List<InvoiceLine> createInvoiceLines(Invoice invoice, List<PurchaseOrderLine> purchaseOrderLineList) throws AxelorException  {

		List<InvoiceLine> invoiceLineList = new ArrayList<InvoiceLine>();

		for(PurchaseOrderLine purchaseOrderLine : purchaseOrderLineList) {

			invoiceLineList.addAll(this.createInvoiceLine(invoice, purchaseOrderLine));
			invoiceLineList.get(invoiceLineList.size()-1).setProject(purchaseOrderLine.getProjectTask());
			purchaseOrderLine.setInvoiced(true);
		}
		return invoiceLineList;
	}
}
