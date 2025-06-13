package com.axelor.apps.supplychain.service.pricing;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.sale.db.SaleOrder;
import com.axelor.apps.stock.db.FreightCarrierMode;
import com.axelor.apps.supplychain.db.FreightCarrierPricing;
import java.util.List;
import java.util.Set;

public interface FreightCarrierPricingService {

  String computeFreightCarrierPricing(
      List<FreightCarrierPricing> freightCarrierPricingList, Long saleOrderId)
      throws AxelorException;

  Set<FreightCarrierPricing> getFreightCarrierPricingSet(Long shipmentModeId, Long saleOrderId);

  FreightCarrierPricing createFreightCarrierPricing(
      FreightCarrierMode freightCarrierMode, SaleOrder saleOrder);

  String notifyEstimatedShippingDateUpdate(SaleOrder saleOrder);
}
