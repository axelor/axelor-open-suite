package com.axelor.apps.businessproject.service.saleorderline;

import com.axelor.apps.sale.db.SaleOrder;
import com.axelor.apps.sale.db.SaleOrderLine;
import com.axelor.apps.sale.helper.SaleOrderLineHelper;
import com.axelor.apps.stock.db.repo.StockMoveLineRepository;
import com.google.inject.Inject;
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
    if (saleOrder != null || saleOrderLine.getDeliveryState() < 2) {
      SaleOrderLineHelper.addAttr(
          "estimatedShippingDate", "value", saleOrder.getEstimatedShippingDate(), attrsMap);
    } else {
      SaleOrderLineHelper.addAttr(
          "estimatedShippingDate", "value", saleOrderLine.getEstimatedShippingDate(), attrsMap);
    }
  }

  @Override
  public void setProjectValue(SaleOrder saleOrder, Map<String, Map<String, Object>> attrsMap) {
    SaleOrderLineHelper.addAttr("project", "value", saleOrder.getProject(), attrsMap);
  }

  @Override
  public void setAvailabilityRequestValue(
      SaleOrderLine saleOrderLine, Map<String, Map<String, Object>> attrsMap) {
    SaleOrderLineHelper.addAttr(
        "$availabiltyRequest",
        "value",
        "id &amp;&amp; __repo__(StockMoveLine).all().filter('self.saleOrderLine.id = ? AND self.stockMove.availabilityRequest = TRUE AND self.stockMove.statusSelect = 2', __self__?.id).count() > 0",
        attrsMap);
  }
}
