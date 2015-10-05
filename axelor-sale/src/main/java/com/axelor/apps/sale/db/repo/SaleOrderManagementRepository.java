package com.axelor.apps.sale.db.repo;

import javax.persistence.PersistenceException;

import com.axelor.apps.base.service.administration.GeneralService;
import com.axelor.apps.sale.db.ISaleOrder;
import com.axelor.apps.sale.db.SaleOrder;
import com.axelor.apps.sale.service.SaleOrderService;
import com.axelor.db.JPA;
import com.axelor.inject.Beans;
import com.google.common.base.Strings;
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
			
			if((saleOrder.getSaleOrderSeq() == null || Strings.isNullOrEmpty(saleOrder.getSaleOrderSeq())) && !saleOrder.getTemplate()){
				saleOrder.setSaleOrderSeq(Beans.get(SaleOrderService.class).getSequence(saleOrder.getCompany()));
			}
			if(!Strings.isNullOrEmpty(saleOrder.getSaleOrderSeq())){
				saleOrder.setFullName(saleOrder.getSaleOrderSeq()+"-"+saleOrder.getClientPartner().getName());
			}
			else{
				saleOrder.setFullName(saleOrder.getClientPartner().getName());
			}
			return JPA.save(saleOrder);
		} catch (Exception e) {
			throw new PersistenceException(e.getLocalizedMessage());
		}
	}
}
