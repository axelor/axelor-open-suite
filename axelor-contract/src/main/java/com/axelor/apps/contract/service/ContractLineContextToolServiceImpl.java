package com.axelor.apps.contract.service;

import com.axelor.apps.contract.db.Contract;
import com.axelor.apps.contract.db.ContractVersion;
import com.axelor.rpc.Context;

public class ContractLineContextToolServiceImpl implements ContractLineContextToolService {

  @Override
  public Contract getContract(Context context) {
    Context parentContext = context.getParent();

    // Classic contract line
    if (parentContext != null && ContractVersion.class.equals(parentContext.getContextClass())) {
      Context parentParentContext = parentContext.getParent();
      if (parentParentContext != null
          && Contract.class.equals(parentParentContext.getContextClass())) {
        return parentParentContext.asType(Contract.class);
      }
    }

    // Additional line
    if (parentContext != null && Contract.class.equals(parentContext.getContextClass())) {
      return parentContext.asType(Contract.class);
    }

    return null;
  }
}
