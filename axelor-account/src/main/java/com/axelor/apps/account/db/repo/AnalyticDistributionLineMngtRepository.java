package com.axelor.apps.account.db.repo;

import com.axelor.apps.account.db.AnalyticDistributionLine;

public class AnalyticDistributionLineMngtRepository extends AnalyticDistributionLineRepository{
	@Override
	public AnalyticDistributionLine copy(AnalyticDistributionLine entity, boolean deep) {
		AnalyticDistributionLine copy = super.copy(entity, deep);
		copy.setMoveLine(null);
		copy.setInvoiceLine(null);
		return copy;
	}
}
