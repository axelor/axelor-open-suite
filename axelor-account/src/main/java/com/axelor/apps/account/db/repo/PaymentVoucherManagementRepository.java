package com.axelor.apps.account.db.repo;

import com.axelor.apps.account.db.PaymentVoucher;
import com.axelor.apps.base.service.administration.GeneralService;
import com.google.inject.Inject;

public class PaymentVoucherManagementRepository extends
		PaymentVoucherRepository {

	@Inject
	protected GeneralService generalService;

	@Override
	public PaymentVoucher copy(PaymentVoucher entity, boolean deep) {

		PaymentVoucher copy = super.copy(entity, deep);

		copy.setStatusSelect(STATUS_DRAFT);
		copy.setRef(null);
		copy.setPaymentDateTime(generalService.getTodayDateTime());
		copy.clearPaymentInvoiceList();
		copy.clearPaymentInvoiceToPayList();
		copy.setGeneratedMove(null);
		copy.setBankCardTransactionNumber(null);
		copy.clearBatchSet();
		copy.setImportId(null);
		copy.setPayboxAmountPaid(null);
		copy.setPayboxPaidOk(false);
		copy.setReceiptNo(null);
		copy.setRemainingAmount(null);
		copy.setRemainingAllocatedAmount(null);
		copy.setToSaveEmailOk(false);
		copy.setDefaultEmailOk(false);
		copy.setEmail(null);

		return copy;
	}
}
