package com.axelor.apps.supplychain.db.repo;

import com.axelor.apps.account.db.AnalyticDistributionLine;
import com.axelor.apps.account.db.repo.AnalyticDistributionLineMngtRepository;

public class AnalyticDistributionLineSupplychainRepository extends AnalyticDistributionLineMngtRepository{
	@Override
	public AnalyticDistributionLine copy(AnalyticDistributionLine entity, boolean deep) {
		AnalyticDistributionLine copy = super.copy(entity, deep);
		copy.setPurchaseOrderLine(null);
		copy.setSaleOrderLine(null);
		return copy;
	}
}
