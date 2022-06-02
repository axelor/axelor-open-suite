package com.axelor.apps.base.service;

import com.axelor.apps.base.interfaces.PricedOrder;

public interface PricedOrderService {
  /**
   * Generate domain when changing client partner and priced order line list is not empty.
   *
   * @param pricedOrder
   * @param domain
   * @return
   */
  String getPartnerDomain(PricedOrder pricedOrder, String domain);
}
