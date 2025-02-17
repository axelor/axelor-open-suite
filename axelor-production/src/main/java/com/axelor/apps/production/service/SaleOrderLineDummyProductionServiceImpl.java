package com.axelor.apps.production.service;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.service.ProductMultipleQtyService;
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.apps.production.db.ManufOrder;
import com.axelor.apps.production.db.repo.ManufOrderRepository;
import com.axelor.apps.sale.db.ProductionStatusSelect;
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

  protected static final String DUMMY_PRODUCTION_STATUS = "$productionStatus";

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

    dummyFields.putAll(getCanceledStatusSelect(statusSelectList));
    if (dummyFields.get(DUMMY_PRODUCTION_STATUS) != null) {
      return dummyFields;
    }

    dummyFields.putAll(getDraftStatusSelect(statusSelectList));
    if (dummyFields.get(DUMMY_PRODUCTION_STATUS) != null) {
      return dummyFields;
    }

    dummyFields.putAll(getAllStandByStatusSelect(statusSelectList));
    if (dummyFields.get(DUMMY_PRODUCTION_STATUS) != null) {
      return dummyFields;
    }

    dummyFields.putAll(getFinishedStatusSelect(statusSelectList));
    if (dummyFields.get(DUMMY_PRODUCTION_STATUS) != null) {
      return dummyFields;
    }

    dummyFields.putAll(getInProgressStatusSelect(statusSelectList));
    if (dummyFields.get(DUMMY_PRODUCTION_STATUS) != null) {
      return dummyFields;
    }

    dummyFields.putAll(getPlannedStatusSelect(statusSelectList));
    if (dummyFields.get(DUMMY_PRODUCTION_STATUS) != null) {
      return dummyFields;
    }

    dummyFields.putAll(getAnyStandByStatusSelect(statusSelectList));
    if (dummyFields.get(DUMMY_PRODUCTION_STATUS) != null) {
      return dummyFields;
    }

    dummyFields.putAll(getAnyDraftStatusSelect(statusSelectList));
    if (dummyFields.get(DUMMY_PRODUCTION_STATUS) != null) {
      return dummyFields;
    }

    return dummyFields;
  }

  protected Map<String, Object> getCanceledStatusSelect(List<Integer> statusSelectList) {
    Map<String, Object> dummyFields = new HashMap<>();
    if (statusSelectList.stream()
        .allMatch(status -> status == ManufOrderRepository.STATUS_CANCELED)) {
      dummyFields.put(
          DUMMY_PRODUCTION_STATUS, ProductionStatusSelect.PRODUCTION_STATUS_CANCELED.getValue());
    }
    return dummyFields;
  }

  protected Map<String, Object> getDraftStatusSelect(List<Integer> statusSelectList) {
    Map<String, Object> dummyFields = new HashMap<>();
    if (statusSelectList.stream().allMatch(status -> status == ManufOrderRepository.STATUS_DRAFT)) {
      dummyFields.put(
          DUMMY_PRODUCTION_STATUS, ProductionStatusSelect.PRODUCTION_STATUS_DRAFT.getValue());
    }
    return dummyFields;
  }

  protected Map<String, Object> getAllStandByStatusSelect(List<Integer> statusSelectList) {
    Map<String, Object> dummyFields = new HashMap<>();
    if (statusSelectList.stream()
        .allMatch(status -> status == ManufOrderRepository.STATUS_STANDBY)) {
      dummyFields.put(
          DUMMY_PRODUCTION_STATUS, ProductionStatusSelect.PRODUCTION_STATUS_CANCELED.getValue());
    }
    return dummyFields;
  }

  protected Map<String, Object> getFinishedStatusSelect(List<Integer> statusSelectList) {
    Map<String, Object> dummyFields = new HashMap<>();
    if (statusSelectList.stream()
        .allMatch(
            status ->
                status == ManufOrderRepository.STATUS_FINISHED
                    || status == ManufOrderRepository.STATUS_CANCELED
                    || status == ManufOrderRepository.STATUS_MERGED)) {
      dummyFields.put(
          DUMMY_PRODUCTION_STATUS, ProductionStatusSelect.PRODUCTION_STATUS_FINISHED.getValue());
    }
    return dummyFields;
  }

  protected Map<String, Object> getInProgressStatusSelect(List<Integer> statusSelectList) {
    Map<String, Object> dummyFields = new HashMap<>();
    if (statusSelectList.stream()
            .anyMatch(status -> status == ManufOrderRepository.STATUS_IN_PROGRESS)
        || statusSelectList.stream()
            .anyMatch(status -> status == ManufOrderRepository.STATUS_FINISHED)
        || statusSelectList.stream()
            .anyMatch(status -> status == ManufOrderRepository.STATUS_STANDBY)) {
      dummyFields.put(
          DUMMY_PRODUCTION_STATUS, ProductionStatusSelect.PRODUCTION_STATUS_IN_PROGRESS.getValue());
    }
    return dummyFields;
  }

  protected Map<String, Object> getPlannedStatusSelect(List<Integer> statusSelectList) {
    Map<String, Object> dummyFields = new HashMap<>();
    if (statusSelectList.stream()
        .anyMatch(status -> status == ManufOrderRepository.STATUS_PLANNED)) {
      dummyFields.put(
          DUMMY_PRODUCTION_STATUS, ProductionStatusSelect.PRODUCTION_STATUS_PLANNED.getValue());
    }
    return dummyFields;
  }

  protected Map<String, Object> getAnyStandByStatusSelect(List<Integer> statusSelectList) {
    Map<String, Object> dummyFields = new HashMap<>();
    if (statusSelectList.stream()
        .anyMatch(status -> status == ManufOrderRepository.STATUS_STANDBY)) {
      dummyFields.put(
          DUMMY_PRODUCTION_STATUS, ProductionStatusSelect.PRODUCTION_STATUS_STANDBY.getValue());
    }
    return dummyFields;
  }

  protected Map<String, Object> getAnyDraftStatusSelect(List<Integer> statusSelectList) {
    Map<String, Object> dummyFields = new HashMap<>();
    if (statusSelectList.stream().anyMatch(status -> status == ManufOrderRepository.STATUS_DRAFT)) {
      dummyFields.put(
          DUMMY_PRODUCTION_STATUS, ProductionStatusSelect.PRODUCTION_STATUS_DRAFT.getValue());
    }
    return dummyFields;
  }
}
