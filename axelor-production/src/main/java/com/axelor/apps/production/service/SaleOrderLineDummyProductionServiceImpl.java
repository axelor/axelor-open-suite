package com.axelor.apps.production.service;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.service.ProductMultipleQtyService;
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.apps.production.db.ManufOrder;
import com.axelor.apps.production.db.repo.ManufOrderRepository;
import com.axelor.apps.production.enums.ProductionStatusSelect;
import com.axelor.apps.sale.db.SaleOrder;
import com.axelor.apps.sale.db.SaleOrderLine;
import com.axelor.apps.sale.service.app.AppSaleService;
import com.axelor.apps.sale.service.saleorderline.SaleOrderLineDiscountService;
import com.axelor.apps.stock.db.repo.StockMoveLineRepository;
import com.axelor.apps.supplychain.service.app.AppSupplychainService;
import com.axelor.apps.supplychain.service.saleorderline.SaleOrderLineDummySupplychainServiceImpl;
import com.axelor.apps.supplychain.service.saleorderline.SaleOrderLineServiceSupplyChain;
import com.google.inject.Inject;
import java.util.List;
import java.util.Map;
import java.util.Optional;
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
    initProductionInformation(saleOrderLine)
        .ifPresent(s -> dummyFields.put(DUMMY_PRODUCTION_STATUS, s.getValue()));
    return dummyFields;
  }

  protected Optional<ProductionStatusSelect> initProductionInformation(
      SaleOrderLine saleOrderLine) {

    List<ManufOrder> manufOrderList = saleOrderLine.getManufOrderList();
    if (CollectionUtils.isEmpty(manufOrderList)) {
      return Optional.empty();
    }
    List<Integer> statusSelectList =
        manufOrderList.stream().map(ManufOrder::getStatusSelect).collect(Collectors.toList());
    if (statusSelectList.stream()
        .allMatch(status -> status == ManufOrderRepository.STATUS_CANCELED)) {
      return Optional.of(ProductionStatusSelect.PRODUCTION_STATUS_CANCELED);
    }

    if (statusSelectList.stream().allMatch(status -> status == ManufOrderRepository.STATUS_DRAFT)) {
      return Optional.of(ProductionStatusSelect.PRODUCTION_STATUS_DRAFT);
    }

    if (statusSelectList.stream()
        .allMatch(status -> status == ManufOrderRepository.STATUS_STANDBY)) {
      return Optional.of(ProductionStatusSelect.PRODUCTION_STATUS_STANDBY);
    }

    if (statusSelectList.stream()
        .allMatch(
            status ->
                status == ManufOrderRepository.STATUS_FINISHED
                    || status == ManufOrderRepository.STATUS_CANCELED
                    || status == ManufOrderRepository.STATUS_MERGED)) {
      return Optional.of(ProductionStatusSelect.PRODUCTION_STATUS_FINISHED);
    }

    if (statusSelectList.stream()
            .anyMatch(status -> status == ManufOrderRepository.STATUS_IN_PROGRESS)
        || statusSelectList.stream()
            .anyMatch(status -> status == ManufOrderRepository.STATUS_FINISHED)
        || statusSelectList.stream()
            .anyMatch(status -> status == ManufOrderRepository.STATUS_STANDBY)) {
      return Optional.of(ProductionStatusSelect.PRODUCTION_STATUS_IN_PROGRESS);
    }

    if (statusSelectList.stream()
        .anyMatch(status -> status == ManufOrderRepository.STATUS_PLANNED)) {
      return Optional.of(ProductionStatusSelect.PRODUCTION_STATUS_PLANNED);
    }

    if (statusSelectList.stream()
        .anyMatch(status -> status == ManufOrderRepository.STATUS_STANDBY)) {
      return Optional.of(ProductionStatusSelect.PRODUCTION_STATUS_STANDBY);
    }

    if (statusSelectList.stream().anyMatch(status -> status == ManufOrderRepository.STATUS_DRAFT)) {
      return Optional.of(ProductionStatusSelect.PRODUCTION_STATUS_DRAFT);
    }

    return Optional.empty();
  }
}
