package com.axelor.apps.supplychain.service.saleOrderLine;

import com.axelor.apps.sale.db.SaleOrder;
import com.axelor.apps.sale.db.SaleOrderLine;
import com.axelor.apps.sale.service.app.AppSaleService;
import com.axelor.apps.sale.service.saleorderline.XServiceImpl;
import com.google.inject.Inject;
import java.math.BigDecimal;
import java.util.Map;
import java.util.Objects;

public class XSupplychainServiceImpl extends XServiceImpl {

  @Inject
  protected XSupplychainServiceImpl(AppSaleService appSaleService) {
    super(appSaleService);
  }

  public void setIsReadOnlyValue(
      SaleOrder saleOrder, SaleOrderLine saleOrderLine, Map<String, Map<String, Object>> attrsMap) {
    this.addAttr(
        "$isReadOnly",
        "value",
        (saleOrder.getStatusSelect() == 2 || saleOrder.getStatusSelect() == 3)
                && (!saleOrder.getOrderBeingEdited()
                    || (saleOrderLine.getExTaxTotal() != null
                        && saleOrderLine.getExTaxTotal().compareTo(BigDecimal.ZERO) == 0
                        && saleOrderLine
                                .getAmountInvoiced()
                                .compareTo(saleOrderLine.getExTaxTotal())
                            == 0))
            || saleOrder.getStatusSelect() == 4,
        attrsMap);
  }

  public void hideUpdateAllocatedQtyBtn(
      SaleOrder saleOrder, SaleOrderLine saleOrderLine, Map<String, Map<String, Object>> attrsMap) {
    this.addAttr(
        "updateAllocatedQtyBtn",
        "hidden",
        saleOrderLine.getId() == null
            || saleOrder.getStatusSelect() != 3
            || Objects.equals(saleOrderLine.getProduct().getProductTypeSelect(), "service"),
        attrsMap);
  }

  public void setRequestedReservedQtyTOReadOnly(
      SaleOrder saleOrder, Map<String, Map<String, Object>> attrsMap) {
    this.addAttr("requestedReservedQty", "readonly", saleOrder.getStatusSelect() > 2, attrsMap);
  }

  public void updateRequestedReservedQty(
      SaleOrderLine saleOrderLine, Map<String, Map<String, Object>> attrsMap) {
    if (saleOrderLine.getRequestedReservedQty().compareTo(saleOrderLine.getQty()) > 0
        || saleOrderLine.getIsQtyRequested()) {
      this.addAttr(
          "requestedReservedQty", "value", BigDecimal.ZERO.max(saleOrderLine.getQty()), attrsMap);
    }
  }
}
