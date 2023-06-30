package com.axelor.apps.contract.service;

import com.axelor.apps.contract.db.Contract;
import com.axelor.apps.contract.db.ContractVersion;

public interface ContractLineViewService {
  boolean hideIsToRevaluate(Contract contract, ContractVersion contractVersion);
}
