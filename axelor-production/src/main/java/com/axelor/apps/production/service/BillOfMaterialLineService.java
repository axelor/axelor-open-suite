package com.axelor.apps.production.service;

import java.math.BigDecimal;

import com.axelor.apps.base.db.Product;
import com.axelor.apps.production.db.BillOfMaterial;
import com.axelor.apps.production.db.BillOfMaterialLine;

public interface BillOfMaterialLineService {

	
	BillOfMaterialLine newBillOfMaterial(Product product, BillOfMaterial billOfMaterial, BigDecimal qty, Integer priority, boolean hasNoManageStock);
	
	BillOfMaterialLine createFromRawMaterial(long productId, int priority);
}
