package com.axelor.apps.account.service.batch;

import com.axelor.apps.account.db.repo.InvoicePaymentRepository;
import com.axelor.apps.account.db.repo.InvoiceRepository;
import com.axelor.apps.account.service.app.AppAccountService;
import com.axelor.apps.account.service.payment.invoice.payment.InvoicePaymentCreateService;
import com.google.inject.Inject;

public class BatchCreditTransferCustomerRefund extends BatchCreditTransferInvoice {

	@Inject
	public BatchCreditTransferCustomerRefund(AppAccountService appAccountService, InvoiceRepository invoiceRepo,
			InvoicePaymentCreateService invoicePaymentCreateService,
			InvoicePaymentRepository invoicePaymentRepository) {
		super(appAccountService, invoiceRepo, invoicePaymentCreateService, invoicePaymentRepository);
	}

	@Override
	protected void process() {
		processInvoices(InvoiceRepository.OPERATION_TYPE_CLIENT_REFUND);
	}

}
