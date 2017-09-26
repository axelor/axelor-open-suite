package com.axelor.apps.bankpayment.service.batch;

import com.axelor.apps.account.db.repo.PaymentScheduleRepository;

public class BatchDirectDebitMonthlyPaymentSchedule extends BatchDirectDebitPaymentSchedule {

	@Override
	protected void process() {
		processPaymentScheduleLines(PaymentScheduleRepository.TYPE_MONTHLY);
	}

}
