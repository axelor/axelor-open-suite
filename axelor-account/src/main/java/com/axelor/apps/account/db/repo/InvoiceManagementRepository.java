package com.axelor.apps.account.db.repo;

import com.axelor.apps.account.db.Invoice;

public class InvoiceManagementRepository extends InvoiceRepository {
	@Override
	public Invoice copy(Invoice entity, boolean deep) {
		entity.setStatusSelect(1);
		entity.setInvoiceId(null);
		entity.setInvoiceDate(null);
		entity.setDueDate(null);
		entity.setValidatedByUser(null);
		entity.setMove(null);
		return super.copy(entity, deep);
	}
}
