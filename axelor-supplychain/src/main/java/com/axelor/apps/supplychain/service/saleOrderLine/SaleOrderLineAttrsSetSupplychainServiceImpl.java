package com.axelor.apps.supplychain.service.saleOrderLine;

import com.axelor.apps.sale.db.SaleOrder;
import com.axelor.apps.sale.db.SaleOrderLine;
import com.axelor.apps.sale.helper.SaleOrderLineHelper;
import com.axelor.apps.sale.service.app.AppSaleService;
import com.axelor.apps.sale.service.saleorderline.SaleOrderLineAttrsSetServiceImpl;
import com.google.inject.Inject;
import java.math.BigDecimal;
import java.util.Map;
import java.util.Objects;

public class SaleOrderLineAttrsSetSupplychainServiceImpl extends SaleOrderLineAttrsSetServiceImpl
    implements SaleOrderLineAttrsSetSupplychainService {

  @Inject
  protected SaleOrderLineAttrsSetSupplychainServiceImpl(AppSaleService appSaleService) {
    super(appSaleService);
  }

  @Override
  public void setIsReadOnlyValue(
      SaleOrder saleOrder, SaleOrderLine saleOrderLine, Map<String, Map<String, Object>> attrsMap) {
    SaleOrderLineHelper.addAttr(
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

  @Override
  public void setRequestedReservedQtyToReadOnly(
      SaleOrder saleOrder, Map<String, Map<String, Object>> attrsMap) {
    SaleOrderLineHelper.addAttr(
        "requestedReservedQty", "readonly", saleOrder.getStatusSelect() > 2, attrsMap);
  }

  @Override
  public void hideUpdateAllocatedQtyBtn(
      SaleOrder saleOrder, SaleOrderLine saleOrderLine, Map<String, Map<String, Object>> attrsMap) {
    SaleOrderLineHelper.addAttr(
        "updateAllocatedQtyBtn",
        "hidden",
        saleOrderLine.getId() == null
            || saleOrder.getStatusSelect() != 3
            || Objects.equals(saleOrderLine.getProduct().getProductTypeSelect(), "service"),
        attrsMap);
  }
}
