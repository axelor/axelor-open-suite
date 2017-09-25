package com.axelor.apps.account.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import com.axelor.apps.account.db.Move;
import com.axelor.apps.account.db.PaymentSchedule;
import com.axelor.apps.account.db.PaymentScheduleLine;
import com.axelor.exception.AxelorException;

public interface PaymentScheduleLineService {

	PaymentScheduleLine createPaymentScheduleLine(PaymentSchedule paymentSchedule, BigDecimal inTaxAmount,
			int scheduleLineSeq, LocalDate scheduleDate);

	List<PaymentScheduleLine> createPaymentScheduleLines(PaymentSchedule paymentSchedule);

	/**
	 * Create a payment move for a payment schedule line.
	 * 
	 * @param paymentScheduleLine
	 * @return
	 * @throws AxelorException
	 */
	Move createPaymentMove(PaymentScheduleLine paymentScheduleLine) throws AxelorException;

}
