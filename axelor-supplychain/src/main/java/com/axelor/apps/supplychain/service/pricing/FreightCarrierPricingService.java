package com.axelor.apps.supplychain.service.pricing;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.supplychain.db.FreightCarrierPricing;
import java.util.List;
import java.util.Set;

public interface FreightCarrierPricingService {

  void computeFreightCarrierPricing(
      List<FreightCarrierPricing> freightCarrierPricingList, Long saleOrderId)
      throws AxelorException;

  Set<FreightCarrierPricing> getFreightCarrierPricingSet(Long shipmentModeId, Long saleOrderId);
}
