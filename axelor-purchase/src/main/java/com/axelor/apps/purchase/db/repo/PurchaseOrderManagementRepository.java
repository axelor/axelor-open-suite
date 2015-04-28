package com.axelor.apps.purchase.db.repo;

import com.axelor.apps.purchase.db.PurchaseOrder;

public class PurchaseOrderManagementRepository extends PurchaseOrderRepository {
 @Override
public PurchaseOrder copy(PurchaseOrder entity, boolean deep) {
	entity.setStatusSelect(1);
	entity.setPurchaseOrderSeq(null);
	return super.copy(entity, deep);
}
}
