package com.axelor.apps.sale.db.repo;

import javax.persistence.PersistenceException;

import com.axelor.apps.base.service.administration.GeneralService;
import com.axelor.apps.sale.db.ISaleOrder;
import com.axelor.apps.sale.db.SaleOrder;
import com.axelor.apps.sale.service.SaleOrderService;
import com.axelor.db.JPA;
import com.axelor.inject.Beans;
import com.google.inject.Inject;

public class SaleOrderManagementRepository extends SaleOrderRepository {

	@Inject
	protected GeneralService generalService;

	@Override
	public SaleOrder copy(SaleOrder entity, boolean deep) {

		SaleOrder copy = super.copy(entity, deep);

		copy.setStatusSelect(ISaleOrder.STATUS_DRAFT);
		copy.setSaleOrderSeq(null);
		copy.clearBatchSet();
		copy.setImportId(null);
		copy.setCreationDate(generalService.getTodayDate());
		copy.setConfirmationDate(null);
		copy.setConfirmedByUser(null);
		copy.setOrderDate(null);
		copy.setOrderNumber(null);
		copy.setVersionNumber(1);

		return copy;
	}

	@Override
	public SaleOrder save(SaleOrder saleOrder) {
		try {
//			saleOrder = super.save(saleOrder);
			Beans.get(SaleOrderService.class).setDraftSequence(saleOrder);

			return JPA.save(saleOrder);
		} catch (Exception e) {
			throw new PersistenceException(e.getLocalizedMessage());
		}
	}
}
