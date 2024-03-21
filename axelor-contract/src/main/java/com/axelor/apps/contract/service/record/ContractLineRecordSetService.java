package com.axelor.apps.contract.service.record;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.contract.db.ContractLine;
import com.axelor.apps.contract.model.AnalyticLineContractModel;

public interface ContractLineRecordSetService {

  void setCompanyExTaxTotal(
      AnalyticLineContractModel analyticLineContractModel, ContractLine contractLine)
      throws AxelorException;
}
