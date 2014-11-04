package com.axelor.apps.account.db.repo;

import org.joda.time.DateTime;

import com.axelor.apps.account.db.PaymentVoucher;

public class PaymentVoucherManagementRepository extends
		PaymentVoucherRepository {
	@Override
	public PaymentVoucher copy(PaymentVoucher entity, boolean deep) {
		entity.setStatusSelect(1);
		entity.setRef(null);
		entity.setPaymentDateTime(DateTime.now());
		entity.setPaymentInvoiceList(null);
		entity.setPaymentInvoiceToPayList(null);
		return super.copy(entity, deep);
	}
}
