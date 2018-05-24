package com.axelor.apps.contract.service;

import com.axelor.apps.contract.db.Contract;
import com.axelor.db.Query;

public interface BatchContractState {
    Query<Contract> prepare();
    Contract process(Contract contract) throws Exception;
}
