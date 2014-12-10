package com.axelor.apps.account.db.repo;

import java.math.BigDecimal;

import com.axelor.apps.account.db.Invoice;
import com.axelor.apps.base.service.administration.GeneralService;

public class InvoiceManagementRepository extends InvoiceRepository {
	@Override
	public Invoice copy(Invoice entity, boolean deep) {
		
		Invoice copy = super.copy(entity, deep);
		
		copy.setStatusSelect(STATUS_DRAFT);
		copy.setInvoiceId(null);
		copy.setInvoiceDate(GeneralService.getTodayDate());
		copy.setDueDate(entity.getInvoiceDate());
		copy.setValidatedByUser(null);
		copy.setMove(null);
		copy.setOriginalInvoice(null);
		copy.setInTaxTotalRemaining(BigDecimal.ZERO);
		copy.setIrrecoverableStatusSelect(IRRECOVERABLE_STATUS_NOT_IRRECOUVRABLE);
		copy.setAmountRejected(BigDecimal.ZERO);
		copy.clearBatchSet();
		copy.setDebitNumber(null);
		copy.setDirectDebitManagement(null);
		copy.setDoubtfulCustomerOk(false);
		copy.setMove(null);
		copy.setEndOfCycleOk(false);
		copy.setInterbankCodeLine(null);
		copy.setPaymentMove(null);
		copy.clearRefundInvoiceList();
		copy.setRejectDate(null);
		copy.setOriginalInvoice(null);
		copy.setUsherPassageOk(false);
		copy.setAlreadyPrintedOk(false);
		copy.setCanceledPaymentSchedule(null);
		copy.setDirectDebitAmount(BigDecimal.ZERO);
		copy.setImportId(null);
		
		return copy;
	}
}
