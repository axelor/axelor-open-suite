package com.axelor.apps.base.service;

import com.axelor.apps.base.interfaces.PricedOrder;

public class PricedOrderServiceImpl implements PricedOrderService {

  @Override
  public String getPartnerDomain(PricedOrder pricedOrder, String domain) {
    StringBuilder newDomain = new StringBuilder(domain);
    if (pricedOrder != null) {
      if (pricedOrder.getCurrency() != null) {
        newDomain.append(
            String.format(" AND self.currency.id = %d", pricedOrder.getCurrency().getId()));
      }
      if (pricedOrder.getPriceList() != null) {
        newDomain.append(
            String.format(
                " AND %d IN self.salePartnerPriceList.priceListSet.id",
                pricedOrder.getPriceList().getId()));
      }
      if (pricedOrder.getFiscalPosition() != null) {
        newDomain.append(
            String.format(
                " AND self.fiscalPosition.id = %s", pricedOrder.getFiscalPosition().getId()));
      }
    }
    return newDomain.toString();
  }
}
