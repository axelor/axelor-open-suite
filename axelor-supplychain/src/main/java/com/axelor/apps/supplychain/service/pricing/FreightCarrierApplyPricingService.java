package com.axelor.apps.supplychain.service.pricing;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.supplychain.db.FreightCarrierPricing;
import java.util.Set;

public interface FreightCarrierApplyPricingService {

  void applyPricing(Set<FreightCarrierPricing> freightCarrierPricingSet) throws AxelorException;
}
