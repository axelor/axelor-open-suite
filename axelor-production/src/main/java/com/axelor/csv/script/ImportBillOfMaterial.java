package com.axelor.csv.script;

import java.util.Map;

import javax.inject.Inject;

import com.axelor.apps.production.db.BillOfMaterial;
import com.axelor.apps.production.db.repo.BillOfMaterialRepository;
import com.axelor.apps.production.service.BillOfMaterialService;
import com.axelor.apps.production.service.CostSheetService;
import com.axelor.exception.AxelorException;
import com.google.inject.persist.Transactional;

public class ImportBillOfMaterial {
	
	@Inject
	BillOfMaterialService billOfMaterialService;
	
	@Inject
	CostSheetService costSheetService;
	
	@Inject
	BillOfMaterialRepository bomRepo;
	
	@Transactional
	public Object computeCostPrice(Object bean, Map values) throws AxelorException{
		assert bean instanceof BillOfMaterial;
        BillOfMaterial bom = (BillOfMaterial) bean;
        bom = bomRepo.save(bom);
        costSheetService.computeCostPrice(bom);
        billOfMaterialService.updateProductCostPrice(bom);
		return bom;
	}
}
