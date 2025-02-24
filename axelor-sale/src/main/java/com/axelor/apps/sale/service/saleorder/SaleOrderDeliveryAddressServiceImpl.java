package com.axelor.apps.sale.service.saleorder;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Address;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.apps.sale.db.SaleOrder;
import com.axelor.apps.sale.db.SaleOrderLine;
import com.axelor.apps.sale.exception.SaleExceptionMessage;
import com.axelor.common.StringUtils;
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
  public Address getDeliveryAddress(SaleOrder saleOrder, List<SaleOrderLine> saleOrderLineList) {
    return saleOrderLineList.stream()
        .map(SaleOrderLine::getDeliveryAddress)
        .filter(Objects::nonNull)
        .findFirst()
        .orElse(saleOrder.getDeliveryAddress());
  }

  @Override
  public void checkSaleOrderLinesDeliveryAddress(List<SaleOrderLine> saleOrderLineList)
      throws AxelorException {
    if (CollectionUtils.isEmpty(saleOrderLineList)) {
      return;
    }
    if (saleOrderLineList.stream()
            .map(SaleOrderLine::getDeliveryAddressStr)
            .filter(StringUtils::notBlank)
            .distinct()
            .count()
        > 1) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_INCONSISTENCY,
          SaleExceptionMessage.DELIVERY_ADDRESS_MUST_BE_SAME_FOR_ALL_LINES);
    }
  }
}
