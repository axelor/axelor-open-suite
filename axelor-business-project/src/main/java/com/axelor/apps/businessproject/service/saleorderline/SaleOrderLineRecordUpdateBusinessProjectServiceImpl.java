package com.axelor.apps.businessproject.service.saleorderline;

import com.axelor.apps.sale.db.SaleOrder;
import com.axelor.apps.sale.db.SaleOrderLine;
import com.axelor.apps.sale.helper.SaleOrderLineHelper;
import com.axelor.apps.stock.db.repo.StockMoveLineRepository;
import com.google.inject.Inject;
import java.time.LocalDate;
import java.util.Map;

public class SaleOrderLineRecordUpdateBusinessProjectServiceImpl
    implements SaleOrderLineRecordUpdateBusinessProjectService {

  protected final StockMoveLineRepository stockMoveLineRepository;

  @Inject()
  public SaleOrderLineRecordUpdateBusinessProjectServiceImpl(
      StockMoveLineRepository stockMoveLineRepository) {
    this.stockMoveLineRepository = stockMoveLineRepository;
  }

  @Override
  public void resetInvoicingMode(SaleOrderLine saleOrderLine) {
    saleOrderLine.setInvoicingModeSelect(null);
  }

  @Override
  public void setEstimatedDateValue(
      SaleOrderLine saleOrderLine, SaleOrder saleOrder, Map<String, Map<String, Object>> attrsMap) {
    SaleOrderLineHelper.addAttr(
            "estimatedShippingDate", "value", setEstimatedDateValue(saleOrderLine, saleOrder), attrsMap);
  }

  @Override
  public LocalDate setEstimatedDateValue(
          SaleOrderLine saleOrderLine, SaleOrder saleOrder) {
    LocalDate estimatedShippingDate = null;
    if (saleOrder != null || saleOrderLine.getDeliveryState() < 2) {
      estimatedShippingDate = saleOrder.getEstimatedShippingDate();
    } else {
      estimatedShippingDate = saleOrderLine.getEstimatedShippingDate();
    }
    return estimatedShippingDate;
  }

  @Override
  public void setProjectValue(SaleOrder saleOrder, Map<String, Map<String, Object>> attrsMap) {
    SaleOrderLineHelper.addAttr("project", "value", saleOrder.getProject(), attrsMap);
  }
}
