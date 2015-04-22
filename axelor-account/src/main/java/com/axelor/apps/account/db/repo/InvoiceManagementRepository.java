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
		switch (entity.getPaymentCondition().getTypeSelect()) {
		case 1:
			copy.setDueDate(copy.getInvoiceDate().plusDays(entity.getPaymentCondition().getPaymentTime()));
			break;
		
		case 2:
			copy.setDueDate(copy.getInvoiceDate().dayOfMonth().withMaximumValue().plusDays(entity.getPaymentCondition().getPaymentTime()));
			break;
		case 3:
			copy.setDueDate(copy.getInvoiceDate().plusDays(entity.getPaymentCondition().getPaymentTime()).dayOfMonth().withMaximumValue());
			break;
		case 4:
			copy.setDueDate(copy.getInvoiceDate().plusDays(entity.getPaymentCondition().getPaymentTime()).dayOfMonth().withMaximumValue().plusDays(entity.getPaymentCondition().getDaySelect()));
			break;

		default:
			copy.setDueDate(copy.getInvoiceDate());
			break;
		}
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
