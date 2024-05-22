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
  protected final SaleOrderLineInitialValuesBusinessProjectService
      saleOrderLineInitialValuesBusinessProjectService;

  @Inject()
  public SaleOrderLineRecordUpdateBusinessProjectServiceImpl(
      StockMoveLineRepository stockMoveLineRepository,
      SaleOrderLineInitialValuesBusinessProjectService
          saleOrderLineInitialValuesBusinessProjectService) {
    this.stockMoveLineRepository = stockMoveLineRepository;
    this.saleOrderLineInitialValuesBusinessProjectService =
        saleOrderLineInitialValuesBusinessProjectService;
  }

  @Override
  public void resetInvoicingMode(SaleOrderLine saleOrderLine) {
    saleOrderLine.setInvoicingModeSelect(null);
  }

  @Override
  public void setEstimatedDateValue(
      SaleOrderLine saleOrderLine, SaleOrder saleOrder, Map<String, Map<String, Object>> attrsMap) {
    SaleOrderLineHelper.addAttr(
        "estimatedShippingDate",
        "value",
        saleOrderLineInitialValuesBusinessProjectService.setEstimatedDateValue(
            saleOrderLine, saleOrder),
        attrsMap);
  }

  @Override
  public void setProjectValue(SaleOrder saleOrder, Map<String, Map<String, Object>> attrsMap) {
    SaleOrderLineHelper.addAttr("project", "value", saleOrder.getProject(), attrsMap);
  }
}
