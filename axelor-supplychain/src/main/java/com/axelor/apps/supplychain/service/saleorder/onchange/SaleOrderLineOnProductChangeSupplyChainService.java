package com.axelor.apps.supplychain.service.saleorder.onchange;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.supplychain.db.FreightCarrierPricing;
import java.util.Set;

public interface SaleOrderLineOnProductChangeSupplyChainService {

  void applyPricing(Set<FreightCarrierPricing> freightCarrierPricingSet) throws AxelorException;
}
