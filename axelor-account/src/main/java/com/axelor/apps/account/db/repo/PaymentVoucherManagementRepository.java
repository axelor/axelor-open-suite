/**
 * Axelor Business Solutions
 *
 * Copyright (C) 2016 Axelor (<http://axelor.com>).
 *
 * This program is free software: you can redistribute it and/or  modify
 * it under the terms of the GNU Affero General Public License, version 3,
 * as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
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
