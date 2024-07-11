package com.axelor.apps.supplychain.service;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.apps.sale.db.SaleOrder;
import com.axelor.apps.sale.db.SaleOrderLine;
import com.axelor.apps.sale.service.saleorder.SaleOrderLineDiscountService;
import com.axelor.apps.sale.service.saleorder.SaleOrderLineDummyServiceImpl;
import com.axelor.apps.stock.db.repo.StockMoveLineRepository;
import com.axelor.apps.stock.db.repo.StockMoveRepository;
import com.google.inject.Inject;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

public class SaleOrderLineDummySupplychainServiceImpl extends SaleOrderLineDummyServiceImpl {

  protected SaleOrderLineServiceSupplyChain saleOrderLineServiceSupplyChain;
  protected StockMoveLineRepository stockMoveLineRepository;

  @Inject
  public SaleOrderLineDummySupplychainServiceImpl(
      AppBaseService appBaseService,
      SaleOrderLineDiscountService saleOrderLineDiscountService,
      SaleOrderLineServiceSupplyChain saleOrderLineServiceSupplyChain,
      StockMoveLineRepository stockMoveLineRepository) {
    super(appBaseService, saleOrderLineDiscountService);
    this.saleOrderLineServiceSupplyChain = saleOrderLineServiceSupplyChain;
    this.stockMoveLineRepository = stockMoveLineRepository;
  }

  @Override
  public Map<String, Object> getOnNewDummies(SaleOrderLine saleOrderLine, SaleOrder saleOrder) {
    Map<String, Object> dummyFields = super.getOnNewDummies(saleOrderLine, saleOrder);

    if (appBaseService.isApp("supplychain")) {
      dummyFields.putAll(fillCtx());
    }

    return dummyFields;
  }

  @Override
  public Map<String, Object> getOnLoadDummies(SaleOrderLine saleOrderLine, SaleOrder saleOrder)
      throws AxelorException {
    Map<String, Object> dummyFields = super.getOnLoadDummies(saleOrderLine, saleOrder);

    if (appBaseService.isApp("supplychain")) {
      dummyFields.putAll(fillAvailableAndAllocatedStock(saleOrder, saleOrderLine));
      dummyFields.putAll(fillCtx());
      dummyFields.putAll(fillAvailabilityRequest(saleOrderLine));
    }

    return dummyFields;
  }

  protected Map<String, Object> fillAvailableAndAllocatedStock(
      SaleOrder saleOrder, SaleOrderLine saleOrderLine) {
    Map<String, Object> dummyFields = new HashMap<>();

    if (saleOrderLine.getProduct() != null && saleOrder.getStockLocation() != null) {
      BigDecimal availableStock =
          saleOrderLineServiceSupplyChain.getAvailableStock(saleOrder, saleOrderLine);
      BigDecimal allocatedStock =
          saleOrderLineServiceSupplyChain.getAllocatedStock(saleOrder, saleOrderLine);
      dummyFields.put("$availableStock", availableStock);
      dummyFields.put("$allocatedStock", allocatedStock);
      dummyFields.put("$totalStock", availableStock.add(allocatedStock));
    }

    return dummyFields;
  }

  protected Map<String, Object> fillCtx() {
    Map<String, Object> dummyFields = new HashMap<>();
    dummyFields.put("$_xFillProductAvailableQty", true);

    return dummyFields;
  }

  protected Map<String, Object> fillAvailabilityRequest(SaleOrderLine saleOrderLine) {
    Map<String, Object> dummyFields = new HashMap<>();
    boolean availabilityRequest = saleOrderLine.getId() != null;
    if (availabilityRequest) {
      availabilityRequest =
          stockMoveLineRepository
                  .all()
                  .filter(
                      String.format(
                          "self.saleOrderLine.id = %s AND self.stockMove.availabilityRequest = TRUE AND self.stockMove.statusSelect = %s",
                          saleOrderLine.getId(), StockMoveRepository.STATUS_PLANNED))
                  .count()
              > 0;
    }

    dummyFields.put("$availabiltyRequest", availabilityRequest);

    return dummyFields;
  }
}
