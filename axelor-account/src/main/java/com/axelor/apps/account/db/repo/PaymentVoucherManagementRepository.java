package com.axelor.apps.account.db.repo;

import com.axelor.apps.account.db.PaymentVoucher;
import com.axelor.apps.base.service.administration.GeneralService;

public class PaymentVoucherManagementRepository extends
		PaymentVoucherRepository {
	@Override
	public PaymentVoucher copy(PaymentVoucher entity, boolean deep) {
		
		PaymentVoucher copy = super.copy(entity, deep);
		
		copy.setStatusSelect(STATUS_DRAFT);
		copy.setRef(null);
		copy.setPaymentDateTime(GeneralService.getTodayDateTime());
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
