package com.axelor.apps.production.service;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.service.ProductMultipleQtyService;
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.apps.production.db.ManufOrder;
import com.axelor.apps.production.db.repo.ManufOrderRepository;
import com.axelor.apps.sale.db.SaleOrder;
import com.axelor.apps.sale.db.SaleOrderLine;
import com.axelor.apps.sale.service.app.AppSaleService;
import com.axelor.apps.sale.service.saleorderline.SaleOrderLineDiscountService;
import com.axelor.apps.stock.db.repo.StockMoveLineRepository;
import com.axelor.apps.supplychain.service.app.AppSupplychainService;
import com.axelor.apps.supplychain.service.saleorderline.SaleOrderLineDummySupplychainServiceImpl;
import com.axelor.apps.supplychain.service.saleorderline.SaleOrderLineServiceSupplyChain;
import com.google.inject.Inject;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.apache.commons.collections.CollectionUtils;

public class SaleOrderLineDummyProductionServiceImpl
    extends SaleOrderLineDummySupplychainServiceImpl {

  protected final String DUMMY_PRODUCTION_STATUS = "$productionStatus";
  protected final String PRODUCTION_STATUS_FINISHED = "FINISHED";
  protected final String PRODUCTION_STATUS_STANDBY = "STANDBY";
  protected final String PRODUCTION_STATUS_IN_PROGRESS = "IN_PROGRESS";
  protected final String PRODUCTION_STATUS_PLANNED = "PLANNED";
  protected final String PRODUCTION_STATUS_CANCELED = "CANCELED";
  protected final String PRODUCTION_STATUS_DRAFT = "DRAFT";

  @Inject
  public SaleOrderLineDummyProductionServiceImpl(
      AppBaseService appBaseService,
      SaleOrderLineDiscountService saleOrderLineDiscountService,
      ProductMultipleQtyService productMultipleQtyService,
      AppSaleService appSaleService,
      StockMoveLineRepository stockMoveLineRepository,
      AppSupplychainService appSupplychainService,
      SaleOrderLineServiceSupplyChain saleOrderLineServiceSupplyChain) {
    super(
        appBaseService,
        saleOrderLineDiscountService,
        productMultipleQtyService,
        appSaleService,
        stockMoveLineRepository,
        appSupplychainService,
        saleOrderLineServiceSupplyChain);
  }

  @Override
  public Map<String, Object> getOnLoadDummies(SaleOrderLine saleOrderLine, SaleOrder saleOrder)
      throws AxelorException {
    Map<String, Object> dummyFields = super.getOnLoadDummies(saleOrderLine, saleOrder);
    dummyFields.putAll(initProductionInformation(saleOrderLine));
    return dummyFields;
  }

  protected Map<String, Object> initProductionInformation(SaleOrderLine saleOrderLine) {
    Map<String, Object> dummyFields = new HashMap<>();

    List<ManufOrder> manufOrderList = saleOrderLine.getManufOrderList();
    if (CollectionUtils.isEmpty(manufOrderList)) {
      return dummyFields;
    }
    List<Integer> statusSelectList =
        manufOrderList.stream().map(ManufOrder::getStatusSelect).collect(Collectors.toList());
    if (statusSelectList.stream()
        .allMatch(status -> status == ManufOrderRepository.STATUS_CANCELED)) {
      dummyFields.put(DUMMY_PRODUCTION_STATUS, PRODUCTION_STATUS_CANCELED);
      return dummyFields;
    }
    if (statusSelectList.stream().allMatch(status -> status == ManufOrderRepository.STATUS_DRAFT)) {
      dummyFields.put(DUMMY_PRODUCTION_STATUS, PRODUCTION_STATUS_DRAFT);
      return dummyFields;
    }
    if (statusSelectList.stream()
        .allMatch(status -> status == ManufOrderRepository.STATUS_STANDBY)) {
      dummyFields.put(DUMMY_PRODUCTION_STATUS, PRODUCTION_STATUS_STANDBY);
      return dummyFields;
    }
    if (statusSelectList.stream()
        .allMatch(
            status ->
                status == ManufOrderRepository.STATUS_FINISHED
                    || status == ManufOrderRepository.STATUS_CANCELED
                    || status == ManufOrderRepository.STATUS_MERGED)) {
      dummyFields.put(DUMMY_PRODUCTION_STATUS, PRODUCTION_STATUS_FINISHED);
      return dummyFields;
    }

    if (statusSelectList.stream()
            .anyMatch(status -> status == ManufOrderRepository.STATUS_IN_PROGRESS)
        || statusSelectList.stream()
            .anyMatch(status -> status == ManufOrderRepository.STATUS_FINISHED)
        || statusSelectList.stream()
            .anyMatch(status -> status == ManufOrderRepository.STATUS_STANDBY)) {
      dummyFields.put(DUMMY_PRODUCTION_STATUS, PRODUCTION_STATUS_IN_PROGRESS);
      return dummyFields;
    }

    if (statusSelectList.stream()
        .anyMatch(status -> status == ManufOrderRepository.STATUS_PLANNED)) {
      dummyFields.put(DUMMY_PRODUCTION_STATUS, PRODUCTION_STATUS_PLANNED);
      return dummyFields;
    }
    if (statusSelectList.stream()
        .anyMatch(status -> status == ManufOrderRepository.STATUS_STANDBY)) {
      dummyFields.put(DUMMY_PRODUCTION_STATUS, PRODUCTION_STATUS_STANDBY);
      return dummyFields;
    }
    if (statusSelectList.stream().anyMatch(status -> status == ManufOrderRepository.STATUS_DRAFT)) {
      dummyFields.put(DUMMY_PRODUCTION_STATUS, PRODUCTION_STATUS_DRAFT);
      return dummyFields;
    }

    return dummyFields;
  }
}
