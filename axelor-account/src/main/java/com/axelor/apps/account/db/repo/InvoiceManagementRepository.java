package com.axelor.apps.account.db.repo;

import java.math.BigDecimal;

import com.axelor.apps.account.db.Invoice;
import com.axelor.apps.base.service.administration.GeneralService;

public class InvoiceManagementRepository extends InvoiceRepository {
	@Override
	public Invoice copy(Invoice entity, boolean deep) {
		
		entity.setStatusSelect(STATUS_DRAFT);
		entity.setInvoiceId(null);
		entity.setInvoiceDate(GeneralService.getTodayDate());
		entity.setDueDate(entity.getInvoiceDate());
		entity.setValidatedByUser(null);
		entity.setMove(null);
		entity.setOriginalInvoice(null);
		entity.setInTaxTotalRemaining(BigDecimal.ZERO);
		entity.setIrrecoverableStatusSelect(IRRECOVERABLE_STATUS_NOT_IRRECOUVRABLE);
		entity.setAmountRejected(BigDecimal.ZERO);
		entity.clearBatchSet();
		entity.setDebitNumber(null);
		entity.setDirectDebitManagement(null);
		entity.setDoubtfulCustomerOk(false);
		entity.setMove(null);
		entity.setEndOfCycleOk(false);
		entity.setInterbankCodeLine(null);
		entity.setPaymentMove(null);
		entity.clearRefundInvoiceList();
		entity.setRejectDate(null);
		entity.setOriginalInvoice(null);
		entity.setUsherPassageOk(false);
		entity.setAlreadyPrintedOk(false);
		entity.setCanceledPaymentSchedule(null);
		entity.setDirectDebitAmount(BigDecimal.ZERO);
		entity.setImportId(null);
		
		return super.copy(entity, deep);
	}
}
