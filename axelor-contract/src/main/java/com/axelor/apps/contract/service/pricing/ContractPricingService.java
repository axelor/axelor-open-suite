package com.axelor.apps.contract.service.pricing;

import com.axelor.apps.base.db.Pricing;
import com.axelor.apps.contract.db.ContractLine;

public interface ContractPricingService {
  boolean isReadonly(Pricing pricing, ContractLine contractLine);
}
