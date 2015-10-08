package com.axelor.apps.account.service;

import java.math.BigDecimal;

import com.axelor.apps.account.db.AnalyticDistributionLine;
import com.axelor.rpc.Context;

public interface AnalyticDistributionLineService {
	public BigDecimal chooseComputeWay(Context context, AnalyticDistributionLine analyticDistributionLine);
	public BigDecimal computeAmount(AnalyticDistributionLine analyticDistributionLine);
}
