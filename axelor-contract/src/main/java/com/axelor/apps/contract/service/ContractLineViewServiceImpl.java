package com.axelor.apps.contract.service;

import com.axelor.apps.contract.db.Contract;
import com.axelor.apps.contract.db.ContractVersion;

public class ContractLineViewServiceImpl implements ContractLineViewService {
  @Override
  public boolean hideIsToRevaluate(Contract contract, ContractVersion contractVersion) {
    boolean hideIsToRevaluate = false;
    if (contract != null) {
      hideIsToRevaluate = !contract.getIsToRevaluate();
    }

    if (contractVersion != null) {
      Contract contract1 = contractVersion.getContract();
      hideIsToRevaluate = contract1 != null && !contract1.getIsToRevaluate();
    }
    return hideIsToRevaluate;
  }
}
