package com.axelor.apps.sale.service.saleorder;

import com.axelor.apps.base.db.Address;
import com.axelor.apps.sale.db.SaleOrder;
import com.axelor.apps.sale.db.SaleOrderLine;
import java.util.List;
import java.util.Objects;
import org.apache.commons.collections.CollectionUtils;

public class SaleOrderDeliveryAddressServiceImpl implements SaleOrderDeliveryAddressService {

  @Override
  public List<SaleOrderLine> updateSaleOrderLinesDeliveryAddress(SaleOrder saleOrder) {
    List<SaleOrderLine> saleOrderLineList = saleOrder.getSaleOrderLineList();
    if (CollectionUtils.isEmpty(saleOrderLineList)) {
      return saleOrderLineList;
    }
    for (SaleOrderLine saleOrderLine : saleOrderLineList) {
      updateSaleOrderLineDeliveryAddress(saleOrder, saleOrderLine);
    }
    return saleOrderLineList;
  }

  protected void updateSaleOrderLineDeliveryAddress(
      SaleOrder saleOrder, SaleOrderLine saleOrderLine) {
    saleOrderLine.setDeliveryAddress(saleOrder.getDeliveryAddress());
    saleOrderLine.setDeliveryAddressStr(saleOrder.getDeliveryAddressStr());
  }

  @Override
  public Address getDeliveryAddress(SaleOrder saleOrder) {
    return saleOrder.getSaleOrderLineList().stream()
        .map(SaleOrderLine::getDeliveryAddress)
        .filter(Objects::nonNull)
        .findFirst()
        .orElse(saleOrder.getDeliveryAddress());
  }
}
