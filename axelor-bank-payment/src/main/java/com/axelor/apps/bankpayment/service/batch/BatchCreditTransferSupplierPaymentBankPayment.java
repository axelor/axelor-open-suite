package com.axelor.apps.bankpayment.service.batch;

import java.util.ArrayList;
import java.util.List;

import com.axelor.apps.account.db.InvoicePayment;
import com.axelor.apps.account.db.repo.InvoicePaymentRepository;
import com.axelor.apps.account.db.repo.InvoiceRepository;
import com.axelor.apps.account.service.batch.BatchCreditTransferSupplierPayment;
import com.axelor.apps.account.service.invoice.InvoiceService;
import com.axelor.apps.account.service.payment.invoice.payment.InvoicePaymentCreateService;
import com.axelor.apps.account.service.payment.invoice.payment.InvoicePaymentValidateService;
import com.axelor.apps.bankpayment.db.BankOrder;
import com.axelor.apps.bankpayment.service.bankorder.BankOrderMergeService;
import com.axelor.apps.base.service.administration.GeneralService;
import com.axelor.exception.AxelorException;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;

public class BatchCreditTransferSupplierPaymentBankPayment extends BatchCreditTransferSupplierPayment {

	protected final BankOrderMergeService bankOrderMergeService;

	@Inject
	public BatchCreditTransferSupplierPaymentBankPayment(GeneralService generalService, InvoiceRepository invoiceRepo,
			InvoicePaymentCreateService invoicePaymentCreateService,
			InvoicePaymentValidateService invoicePaymentValidateService,
			InvoicePaymentRepository invoicePaymentRepository, BankOrderMergeService bankOrderMergeService) {
		super(generalService, invoiceRepo, invoicePaymentCreateService, invoicePaymentValidateService,
				invoicePaymentRepository);
		this.bankOrderMergeService = bankOrderMergeService;
	}

	@Override
	@Transactional(rollbackOn = { AxelorException.class, Exception.class })
	protected void postProcess(List<InvoicePayment> invoicePaymentList) throws AxelorException {
		List<BankOrder> bankOrderList = new ArrayList<>();

		for (InvoicePayment invoicePayment : invoicePaymentList) {
			BankOrder bankOrder = invoicePayment.getBankOrder();
			if (bankOrder != null) {
				bankOrderList.add(bankOrder);
			}
		}

		if (bankOrderList.size() > 1) {
			bankOrderMergeService.mergeBankOrderList(bankOrderList);
		}
	}

}
