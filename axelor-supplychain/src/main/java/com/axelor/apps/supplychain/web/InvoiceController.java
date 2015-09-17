package com.axelor.apps.supplychain.web;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.axelor.apps.account.db.AdvancePaymentAccount;
import com.axelor.apps.account.db.Invoice;
import com.axelor.apps.account.db.repo.AdvancePaymentAccountRepository;
import com.axelor.apps.account.db.repo.InvoiceRepository;
import com.axelor.apps.account.service.MoveService;
import com.axelor.apps.supplychain.service.AdvancePaymentService;
import com.axelor.apps.supplychain.service.SaleOrderInvoiceService;
import com.axelor.apps.supplychain.service.SaleOrderInvoiceServiceImpl;
import com.axelor.db.JPA;
import com.axelor.exception.AxelorException;
import com.axelor.inject.Beans;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.google.inject.Inject;

public class InvoiceController {
	
	private static final Logger LOG = LoggerFactory.getLogger(SaleOrderInvoiceServiceImpl.class);

	@Inject
	protected SaleOrderInvoiceService saleOrderInvoiceService;
	
	@Inject
	public AdvancePaymentService advancePaymentService;

	public void fillInLines(ActionRequest request, ActionResponse response) {
		Invoice invoice = request.getContext().asType(Invoice.class);
		saleOrderInvoiceService.fillInLines(invoice);
		response.setValues(invoice);
	}
	
	
	public void changeOnInvoiceAdvancePayment(ActionRequest request, ActionResponse response) throws AxelorException
	{
		Invoice invoiceContext = request.getContext().asType(Invoice.class);
		
		if (invoiceContext.getId() == null || invoiceContext.getId() <= 0)
			return;
		
		Invoice invoiceDB = Beans.get(InvoiceRepository.class).find(invoiceContext.getId());
		
		for (AdvancePaymentAccount advancePaymentDB : invoiceDB.getAdvancePaymentList()) 
		{
			boolean isInDB = false;
			for (AdvancePaymentAccount advancePaymentContext : invoiceContext.getAdvancePaymentList()) 
			{
				if (advancePaymentDB.getId() == advancePaymentContext.getId())
					isInDB = true;
				if (advancePaymentDB.getId() != null && !advancePaymentService.equalsTo(advancePaymentContext, advancePaymentDB) && advancePaymentDB.getId() == advancePaymentContext.getId())
				{
					advancePaymentDB.setAdvancePaymentDate(advancePaymentContext.getAdvancePaymentDate());
					advancePaymentDB.setAmount(advancePaymentContext.getAmount());
					advancePaymentDB.setCurrency(advancePaymentContext.getCurrency());
					advancePaymentDB.setPaymentMode(advancePaymentContext.getPaymentMode());
					advancePaymentDB.setTypeSelect(advancePaymentContext.getTypeSelect());
				}
			}
			if (!isInDB)
			{
				LOG.debug("L'écriture de la Ligne (montant = {}) (Move = {}) va etre supprimée !", advancePaymentDB.getAmount(), advancePaymentDB.getMove().getReference());
				advancePaymentService.deleteLine(advancePaymentDB);
			}
		}
		
		for (AdvancePaymentAccount advancePaymentAccount : invoiceContext.getAdvancePaymentList())
		{

			advancePaymentService.addMoveToInvoiceAdvancePayment(invoiceDB, advancePaymentAccount);
		}
		
		response.setReload(true);
	}
	
}
