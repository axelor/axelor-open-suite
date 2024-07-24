package com.axelor.apps.supplychain.service;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Product;
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.apps.sale.db.SaleOrder;
import com.axelor.apps.sale.db.SaleOrderLine;
import com.axelor.apps.sale.db.repo.SaleOrderRepository;
import com.axelor.apps.sale.service.saleorder.SaleOrderLineDiscountService;
import com.axelor.apps.sale.service.saleorder.SaleOrderLineDummyServiceImpl;
import com.axelor.apps.stock.db.StockLocation;
import com.axelor.apps.stock.db.repo.StockMoveLineRepository;
import com.axelor.apps.supplychain.service.app.AppSupplychainService;
import com.google.inject.Inject;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

public class SaleOrderLineDummySupplychainServiceImpl extends SaleOrderLineDummyServiceImpl {

  protected StockMoveLineRepository stockMoveLineRepository;
  protected AppSupplychainService appSupplychainService;
  protected SaleOrderLineServiceSupplyChain saleOrderLineServiceSupplyChain;

  @Inject
  public SaleOrderLineDummySupplychainServiceImpl(
      AppBaseService appBaseService,
      SaleOrderLineDiscountService saleOrderLineDiscountService,
      StockMoveLineRepository stockMoveLineRepository,
      AppSupplychainService appSupplychainService,
      SaleOrderLineServiceSupplyChain saleOrderLineServiceSupplyChain) {
    super(appBaseService, saleOrderLineDiscountService);
    this.stockMoveLineRepository = stockMoveLineRepository;
    this.appSupplychainService = appSupplychainService;
    this.saleOrderLineServiceSupplyChain = saleOrderLineServiceSupplyChain;
  }

  @Override
  public Map<String, Object> getOnNewDummies(SaleOrderLine saleOrderLine, SaleOrder saleOrder) {
    Map<String, Object> dummyFields = super.getOnNewDummies(saleOrderLine, saleOrder);
    dummyFields.putAll(fillCtx());
    return dummyFields;
  }

  @Override
  public Map<String, Object> getOnLoadDummies(SaleOrderLine saleOrderLine, SaleOrder saleOrder)
      throws AxelorException {
    Map<String, Object> dummyFields = super.getOnLoadDummies(saleOrderLine, saleOrder);
    dummyFields.putAll(initAvailabilityRequest(saleOrderLine));
    dummyFields.putAll(fillAvailableAndAllocatedStock(saleOrder, saleOrderLine));
    dummyFields.putAll(fillCtx());
    return dummyFields;
  }

  @Override
  public Map<String, Object> getOnProductChangeDummies(
      SaleOrderLine saleOrderLine, SaleOrder saleOrder) throws AxelorException {
    Map<String, Object> dummyFields = super.getOnProductChangeDummies(saleOrderLine, saleOrder);
    dummyFields.putAll(fillAvailableAndAllocatedStock(saleOrder, saleOrderLine));
    return dummyFields;
  }

  protected Map<String, Object> initAvailabilityRequest(SaleOrderLine saleOrderLine) {
    Map<String, Object> dummyFields = new HashMap<>();
    if (!appSupplychainService.isApp("supplychain")) {
      return dummyFields;
    }
    Long lineId = saleOrderLine.getId();
    long stockMoveCount =
        stockMoveLineRepository
            .all()
            .filter(
                "self.saleOrderLine.id = :saleOrderLineId AND self.stockMove.availabilityRequest = TRUE AND self.stockMove.statusSelect = 2")
            .bind("saleOrderLineId", lineId)
            .count();
    dummyFields.put("$availabilityRequest", lineId != null && stockMoveCount > 0);
    return dummyFields;
  }

  protected Map<String, Object> fillAvailableAndAllocatedStock(
      SaleOrder saleOrder, SaleOrderLine saleOrderLine) {
    Map<String, Object> dummyFields = new HashMap<>();
    if (!appSupplychainService.isApp("supplychain")) {
      return dummyFields;
    }

    Product product = saleOrderLine.getProduct();
    StockLocation stockLocation = saleOrder.getStockLocation();

    if (product == null || stockLocation == null) {
      return dummyFields;
    }
    BigDecimal availableStock =
        saleOrderLineServiceSupplyChain.getAvailableStock(saleOrder, saleOrderLine);
    BigDecimal allocatedStock =
        saleOrderLineServiceSupplyChain.getAllocatedStock(saleOrder, saleOrderLine);
    dummyFields.put("$availableStock", availableStock);
    dummyFields.put("$allocatedStock", allocatedStock);
    dummyFields.put("$totalStock", availableStock.add(allocatedStock));
    return dummyFields;
  }

  @Override
  protected Map<String, Object> initReadonlyDummy(
      SaleOrder saleOrder, SaleOrderLine saleOrderLine) {
    Map<String, Object> dummyFields = new HashMap<>();
    int statusSelect = saleOrder.getStatusSelect();
    BigDecimal exTaxTotal = saleOrderLine.getExTaxTotal();
    BigDecimal amountInvoiced = saleOrderLine.getAmountInvoiced();

    boolean availabilityRequest =
        (boolean) initAvailabilityRequest(saleOrderLine).get("$availabilityRequest");
    dummyFields.put(
        "$isReadOnly",
        statusSelect != SaleOrderRepository.STATUS_DRAFT_QUOTATION
            && (!saleOrder.getOrderBeingEdited()
                || (exTaxTotal != null
                    && exTaxTotal.signum() != 0
                    && amountInvoiced.compareTo(exTaxTotal) == 0)
                || availabilityRequest));
    return dummyFields;
  }

  protected Map<String, Object> fillCtx() {
    Map<String, Object> dummyFields = new HashMap<>();
    dummyFields.put("$_xFillProductAvailableQty", true);
    return dummyFields;
  }
}
