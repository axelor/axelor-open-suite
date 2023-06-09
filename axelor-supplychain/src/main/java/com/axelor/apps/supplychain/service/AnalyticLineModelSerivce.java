package com.axelor.apps.supplychain.service;

import com.axelor.apps.account.db.InvoiceLine;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.supplychain.model.AnalyticLineModel;

public interface AnalyticLineModelSerivce {
  boolean analyzeAnalyticLineModel(AnalyticLineModel analyticLineModel, Company company)
      throws AxelorException;

  AnalyticLineModel getAndComputeAnalyticDistribution(AnalyticLineModel analyticLineModel)
      throws AxelorException;

  AnalyticLineModel computeAnalyticDistribution(AnalyticLineModel analyticLineModel);

  AnalyticLineModel createAnalyticDistributionWithTemplate(AnalyticLineModel analyticLineModel);

  void setInvoiceLineAnalyticInfo(AnalyticLineModel analyticLineModel, InvoiceLine invoiceLine);
}
