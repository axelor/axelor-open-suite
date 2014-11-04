package com.axelor.apps.sale.db.repo;

import com.axelor.apps.sale.db.SaleOrder;

public class SaleOrderManagementRepositroy extends SaleOrderRepository {
 @Override
public SaleOrder copy(SaleOrder entity, boolean deep) {
	entity.setStatusSelect(1);
	entity.setSaleOrderSeq(null);
	return super.copy(entity, deep);
}
}
