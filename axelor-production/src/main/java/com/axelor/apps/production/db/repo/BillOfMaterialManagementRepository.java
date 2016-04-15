package com.axelor.apps.production.db.repo;

import com.axelor.apps.production.db.BillOfMaterial;

public class BillOfMaterialManagementRepository extends BillOfMaterialRepository {
	
	@Override
	public BillOfMaterial save(BillOfMaterial billOfMaterial){
		
		if (billOfMaterial.getVersionNumber() > 1){
			billOfMaterial.setFullName( billOfMaterial.getName() + " - v" + String.valueOf(billOfMaterial.getVersionNumber()) );
		}
		else{
			billOfMaterial.setFullName( billOfMaterial.getName() );
		}
		
		
		return super.save(billOfMaterial);
	}

}
