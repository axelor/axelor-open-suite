package com.axelor.apps.contract.service;

import com.axelor.apps.contract.db.Contract;
import com.axelor.rpc.Context;

public interface ContractLineContextToolService {
  Contract getContract(Context context);
}
