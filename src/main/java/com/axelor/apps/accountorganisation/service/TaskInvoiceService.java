package com.axelor.apps.accountorganisation.service;

import java.math.BigDecimal;
import java.util.ArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.axelor.apps.account.db.Account;
import com.axelor.apps.account.db.AccountingSituation;
import com.axelor.apps.account.db.IInvoice;
import com.axelor.apps.account.db.Invoice;
import com.axelor.apps.account.db.InvoiceLine;
import com.axelor.apps.account.db.VatLine;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.Partner;
import com.axelor.apps.base.db.Product;
import com.axelor.apps.base.service.administration.GeneralService;
import com.axelor.apps.organisation.db.Task;
import com.axelor.apps.supplychain.db.SalesOrder;
import com.axelor.apps.supplychain.db.SalesOrderLine;
import com.google.inject.persist.Transactional;

public class TaskInvoiceService {
	
	private static final Logger LOG = LoggerFactory.getLogger(TaskInvoiceService.class);
	
	@Transactional
	public void createInvoice(Task task) {

		// Check if the field task.isToInvoice = true
		if(task.getIsToInvoice()) {
			SalesOrderLine salesOrderLine = task.getSalesOrderLine();
			// Check if task.salesOrderLine and task.salesOrderLine.salesOrder are not empty
			if(salesOrderLine != null && salesOrderLine.getSalesOrder() != null) {
				SalesOrder salesOrder = salesOrderLine.getSalesOrder();
				Invoice invoice = new Invoice();
				invoice.setCompany(salesOrder.getCompany());
				invoice.setPartner(salesOrder.getClientPartner());
				invoice.setPaymentMode(salesOrder.getPaymentMode());
				invoice.setPaymentCondition(salesOrder.getPaymentCondition());
				invoice.setPartnerAccount(getCustomerAccount(salesOrder.getClientPartner(), salesOrder.getCompany()));
				invoice.setInvoiceDate(GeneralService.getTodayDate());
				invoice.setOperationTypeSelect(IInvoice.CLIENT_SALE);
				invoice.setJournal(salesOrder.getCompany().getCustomerSalesJournal());
				invoice.setCurrency(salesOrder.getCurrency());
				// Create InvoiceLine
				invoice.setInvoiceLineList(new ArrayList<InvoiceLine>());
				// Need to check if product is empty or not ?
				InvoiceLine invoiceLine = createInvoiceLine(task.getProduct(), task.getQty(), salesOrderLine.getVatLine(), invoice);
				invoice.getInvoiceLineList().add(invoiceLine);
				invoice.save();
			}
		}
	}

	@Transactional
	public InvoiceLine createInvoiceLine(Product product, BigDecimal qty, VatLine vatLine, Invoice parentInvoice) {

		InvoiceLine invoiceLine = new InvoiceLine();
		invoiceLine.setInvoice(parentInvoice);
		invoiceLine.setInvoiceLineType(product.getInvoiceLineType());
		invoiceLine.setProduct(product);
		invoiceLine.setQty(qty);
		invoiceLine.setVatLine(vatLine);
		invoiceLine.setProductName(product.getName());
		invoiceLine.save();
		return invoiceLine;
	}


	public Account getCustomerAccount(Partner partner, Company company)  {

		Account partnerAccount = null;

		for(AccountingSituation accountingSituation : partner.getAccountingSituationList())  {

			if(accountingSituation.getCompany().equals(company))  {
				partnerAccount = accountingSituation.getCustomerAccount();
			}
		}

		if(partnerAccount == null)  {
			partnerAccount = company.getCustomerAccount();
		}

		if(partnerAccount == null)  {

			// TODO ajouter message d'erreur configuration manquante dans la société

		}
		return partnerAccount;
	}
		
	
}
