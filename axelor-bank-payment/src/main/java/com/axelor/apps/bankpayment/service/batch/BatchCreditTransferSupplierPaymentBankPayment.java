package com.axelor.apps.bankpayment.service.batch;

import java.util.List;

import com.axelor.apps.account.db.InvoicePayment;
import com.axelor.apps.account.db.repo.InvoicePaymentRepository;
import com.axelor.apps.account.db.repo.InvoiceRepository;
import com.axelor.apps.account.service.batch.BatchCreditTransferSupplierPayment;
import com.axelor.apps.account.service.payment.invoice.payment.InvoicePaymentCreateService;
import com.axelor.apps.bankpayment.service.bankorder.BankOrderMergeService;
import com.axelor.apps.base.service.administration.GeneralService;
import com.axelor.exception.db.IException;
import com.axelor.exception.service.TraceBackService;
import com.google.inject.Inject;

public class BatchCreditTransferSupplierPaymentBankPayment extends BatchCreditTransferSupplierPayment {

	protected final BankOrderMergeService bankOrderMergeService;

	@Inject
	public BatchCreditTransferSupplierPaymentBankPayment(GeneralService generalService, InvoiceRepository invoiceRepo,
			InvoicePaymentCreateService invoicePaymentCreateService, InvoicePaymentRepository invoicePaymentRepository,
			BankOrderMergeService bankOrderMergeService) {
		super(generalService, invoiceRepo, invoicePaymentCreateService, invoicePaymentRepository);
		this.bankOrderMergeService = bankOrderMergeService;
	}

	@Override
	protected void process() {
		List<InvoicePayment> doneList = processInvoices(InvoiceRepository.OPERATION_TYPE_SUPPLIER_PURCHASE);

		if (!doneList.isEmpty()) {
			try {
				bankOrderMergeService.mergeFromInvoicePayments(doneList);
			} catch (Exception e) {
				TraceBackService.trace(e, IException.INVOICE_ORIGIN, batch.getId());
				LOG.error(e.getMessage());
			}
		}
	}

}
