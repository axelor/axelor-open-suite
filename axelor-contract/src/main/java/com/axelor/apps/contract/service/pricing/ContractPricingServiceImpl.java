package com.axelor.apps.contract.service.pricing;

import com.axelor.apps.base.db.Pricing;
import com.axelor.apps.contract.db.ContractLine;
import com.axelor.apps.contract.db.repo.ContractLineRepository;
import com.axelor.rpc.Context;
import com.google.inject.Inject;
import java.util.ArrayList;
import java.util.List;

public class ContractPricingServiceImpl implements ContractPricingService {
  protected ContractLineRepository contractLineRepository;

  @Inject
  public ContractPricingServiceImpl(ContractLineRepository contractLineRepository) {
    this.contractLineRepository = contractLineRepository;
  }

  @Override
  public boolean isReadonly(Context context) {
    Context parent = context.getParent();
    Pricing pricing = context.asType(Pricing.class);
    List<ContractLine> contractLineList = new ArrayList<>();

    if (pricing != null && pricing.getId() != null) {
      contractLineList =
          contractLineRepository
              .all()
              .filter("self.pricing = :pricing")
              .bind("pricing", pricing)
              .fetch();
    }

    // Pricing not used by any contract line
    if (contractLineList.isEmpty()) {
      return false;
    }

    // Pricing used and opened by one contract line
    if (parent != null
        && contractLineList.size() == 1
        && parent.getContextClass().equals(ContractLine.class)
        && contractLineList.get(0).getId() == parent.get("id")) {
      return false;
    }

    // Pricing used by several contract lines
    if (contractLineList.size() > 1) {
      return true;
    }

    // Pricing opened by a new contract line (non-saved)
    if (contractLineList.size() == 1 && parent != null && parent.get("id") == null) {
      return true;
    }

    return false;
  }
}
