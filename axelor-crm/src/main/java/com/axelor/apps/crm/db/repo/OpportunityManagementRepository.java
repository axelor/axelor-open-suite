package com.axelor.apps.crm.db.repo;

import com.axelor.apps.crm.db.Opportunity;

public class OpportunityManagementRepository extends OpportunityRepository {
	@Override
	public Opportunity copy(Opportunity entity, boolean deep) {
		entity.setSalesStageSelect(1);
		return super.copy(entity, deep);
	}
}
