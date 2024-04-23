package com.axelor.apps.contract.service;

import com.axelor.apps.contract.db.ContractLine;
import com.axelor.apps.supplychain.model.AnalyticLineModel;

public interface AnalyticLineModelFromContractService {
  void copyAnalyticsDataFromContractLine(
      ContractLine contractLine, AnalyticLineModel analyticLineModel);
}
