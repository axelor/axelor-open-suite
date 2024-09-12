package com.axelor.apps.sale.service.saleorder;

import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.apps.sale.db.SaleOrder;
import com.axelor.apps.sale.db.SaleOrderLine;
import com.axelor.apps.sale.db.repo.SaleOrderRepository;
import com.axelor.apps.sale.service.app.AppSaleService;
import com.axelor.apps.sale.service.event.SaleOrderViewDummy;
import com.axelor.event.Event;
import com.axelor.studio.db.AppBase;
import com.google.inject.Inject;
import java.util.HashMap;
import java.util.Map;

public class SaleOrderDummyServiceImpl implements SaleOrderDummyService {
  protected AppBaseService appBaseService;
  protected AppSaleService appSaleService;
  protected SaleOrderVersionService saleOrderVersionService;
  protected Event<SaleOrderViewDummy> saleOrderViewDummyEvent;

  @Inject
  public SaleOrderDummyServiceImpl(
      AppBaseService appBaseService,
      AppSaleService appSaleService,
      SaleOrderVersionService saleOrderVersionService,
      Event<SaleOrderViewDummy> saleOrderViewDummyEvent) {
    this.appBaseService = appBaseService;
    this.appSaleService = appSaleService;
    this.saleOrderVersionService = saleOrderVersionService;
    this.saleOrderViewDummyEvent = saleOrderViewDummyEvent;
  }

  @Override
  public Map<String, Object> fireDummies(SaleOrder saleOrder) {
    SaleOrderViewDummy saleOrderViewDummy = new SaleOrderViewDummy(saleOrder);
    saleOrderViewDummyEvent.fire(saleOrderViewDummy);
    return saleOrderViewDummy.getSaleOrderMap();
  }

  @Override
  public Map<String, Object> getDummies(SaleOrder saleOrder) {
    Map<String, Object> dummies = new HashMap<>();
    dummies.putAll(getTradingManagementConfig());
    dummies.putAll(getDiscountsNeedReview(saleOrder));
    dummies.putAll(getElementStartDate());
    dummies.putAll(getSaveActualVersion());
    dummies.putAll(getLastVersion(saleOrder));
    return dummies;
  }

  protected Map<String, Object> getTradingManagementConfig() {
    Map<String, Object> dummies = new HashMap<>();
    AppBase appBase = appBaseService.getAppBase();
    dummies.put("$enableTradingNamesManagement", appBase.getEnableTradingNamesManagement());
    return dummies;
  }

  protected Map<String, Object> getDiscountsNeedReview(SaleOrder saleOrder) {
    Map<String, Object> dummies = new HashMap<>();
    boolean discountsNeedReview =
        (saleOrder.getStatusSelect() == SaleOrderRepository.STATUS_FINALIZED_QUOTATION
                || (saleOrder.getOrderBeingEdited()
                    && saleOrder.getStatusSelect() == SaleOrderRepository.STATUS_ORDER_CONFIRMED))
            && saleOrder.getSaleOrderLineList().stream()
                .anyMatch(SaleOrderLine::getDiscountsNeedReview);
    dummies.put("$discountsNeedReview", discountsNeedReview);
    return dummies;
  }

  protected Map<String, Object> getElementStartDate() {
    Map<String, Object> dummies = new HashMap<>();
    dummies.put("$_elementStartDate", appBaseService.getTodayDateTime());
    return dummies;
  }

  protected Map<String, Object> getLastVersion(SaleOrder saleOrder) {
    Map<String, Object> dummies = new HashMap<>();
    Integer versionNumber = saleOrder.getVersionNumber() - 1;
    versionNumber =
        saleOrderVersionService.getCorrectedVersionNumber(
            saleOrder.getVersionNumber(), versionNumber);
    dummies.put("$previousVersionNumber", versionNumber);
    dummies.put(
        "$versionDateTime", saleOrderVersionService.getVersionDateTime(saleOrder, versionNumber));
    return dummies;
  }

  protected Map<String, Object> getSaveActualVersion() {
    Map<String, Object> dummies = new HashMap<>();
    dummies.put("$saveActualVersion", true);
    return dummies;
  }
}
