package com.axelor.apps.supplychain.service;

import com.axelor.apps.account.service.analytic.AnalyticAttrsService;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Product;
import com.axelor.apps.base.db.repo.ProductRepository;
import com.axelor.apps.sale.db.SaleOrder;
import com.axelor.apps.sale.db.SaleOrderLine;
import com.axelor.apps.sale.db.repo.SaleOrderRepository;
import com.axelor.apps.supplychain.model.AnalyticLineModel;
import com.axelor.apps.supplychain.service.analytic.AnalyticAttrsSupplychainService;
import com.axelor.apps.supplychain.service.app.AppSupplychainService;
import com.google.inject.Inject;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class SaleOrderLineViewSupplychainServiceImpl
    implements SaleOrderLineViewSupplychainService {

  protected AppSupplychainService appSupplychainService;
  protected AnalyticAttrsService analyticAttrsService;
  protected AnalyticAttrsSupplychainService analyticAttrsSupplychainService;

  public static final String HIDDEN_ATTR = "hidden";
  public static final String TITLE_ATTR = "title";
  public static final String SCALE_ATTR = "scale";
  public static final String SELECTION_IN_ATTR = "selection-in";
  public static final String READONLY_ATTR = "readonly";

  @Inject
  public SaleOrderLineViewSupplychainServiceImpl(
      AppSupplychainService appSupplychainService,
      AnalyticAttrsService analyticAttrsService,
      AnalyticAttrsSupplychainService analyticAttrsSupplychainService) {
    this.appSupplychainService = appSupplychainService;
    this.analyticAttrsService = analyticAttrsService;
    this.analyticAttrsSupplychainService = analyticAttrsSupplychainService;
  }

  @Override
  public Map<String, Map<String, Object>> getOnNewAttrs(
      SaleOrderLine saleOrderLine, SaleOrder saleOrder) throws AxelorException {
    Map<String, Map<String, Object>> attrs = new HashMap<>();

    if (appSupplychainService.isApp("supplychain")) {
      attrs.putAll(hideUpdateAllocatedQtyButton(saleOrder, saleOrderLine));
      attrs.putAll(readonlyRequestedReservedQty(saleOrder));
      attrs.putAll(hideSupplychainPanel(saleOrder));
      attrs.putAll(manageAnalytic(saleOrder, saleOrderLine));
    }

    return attrs;
  }

  @Override
  public Map<String, Map<String, Object>> getOnLoadAttrs(
      SaleOrderLine saleOrderLine, SaleOrder saleOrder) throws AxelorException {
    Map<String, Map<String, Object>> attrs = new HashMap<>();

    if (appSupplychainService.isApp("supplychain")) {
      attrs.putAll(hideUpdateAllocatedQtyButton(saleOrder, saleOrderLine));
      attrs.putAll(readonlyRequestedReservedQty(saleOrder));
      attrs.putAll(hideSupplychainPanel(saleOrder));
      attrs.putAll(manageAnalytic(saleOrder, saleOrderLine));
      attrs.putAll(hideReservedQty(saleOrder, saleOrderLine));
    }

    return attrs;
  }

  protected Map<String, Map<String, Object>> hideUpdateAllocatedQtyButton(
      SaleOrder saleOrder, SaleOrderLine saleOrderLine) {
    Map<String, Map<String, Object>> attrs = new HashMap<>();
    Boolean isHidden =
        saleOrder.getId() == null
            || saleOrder.getStatusSelect() != SaleOrderRepository.STATUS_ORDER_CONFIRMED
            || ProductRepository.PRODUCT_TYPE_SERVICE.equals(
                Optional.of(saleOrderLine)
                    .map(SaleOrderLine::getProduct)
                    .map(Product::getProductTypeSelect)
                    .orElse(""));
    attrs.put("updateAllocatedQtyBtn", Map.of(HIDDEN_ATTR, isHidden));
    return attrs;
  }

  protected Map<String, Map<String, Object>> readonlyRequestedReservedQty(SaleOrder saleOrder) {
    Map<String, Map<String, Object>> attrs = new HashMap<>();
    if (appSupplychainService.getAppSupplychain() != null
        && appSupplychainService.getAppSupplychain().getManageStockReservation()) {
      attrs.put(
          "requestedReservedQty",
          Map.of(
              READONLY_ATTR,
              saleOrder.getStatusSelect() > SaleOrderRepository.STATUS_FINALIZED_QUOTATION));
    }
    return attrs;
  }

  protected Map<String, Map<String, Object>> hideSupplychainPanel(SaleOrder saleOrder) {
    Map<String, Map<String, Object>> attrs = new HashMap<>();
    Boolean isHidden =
        Arrays.asList(
                SaleOrderRepository.STATUS_DRAFT_QUOTATION,
                SaleOrderRepository.STATUS_FINALIZED_QUOTATION)
            .contains(saleOrder.getStatusSelect());
    attrs.put("stockMoveLineOfSOPanel", Map.of(HIDDEN_ATTR, isHidden));
    attrs.put("projectTaskListPanel", Map.of(HIDDEN_ATTR, isHidden));
    attrs.put("invoicingFollowUpPanel", Map.of(HIDDEN_ATTR, isHidden));
    return attrs;
  }

  protected Map<String, Map<String, Object>> manageAnalytic(
      SaleOrder saleOrder, SaleOrderLine saleOrderLine) throws AxelorException {
    Map<String, Map<String, Object>> attrs = new HashMap<>();

    analyticAttrsService.addAnalyticAxisAttrs(saleOrder.getCompany(), null, attrs);
    AnalyticLineModel analyticLineModel = new AnalyticLineModel(saleOrderLine, saleOrder);
    analyticAttrsSupplychainService.addAnalyticDistributionPanelHiddenAttrs(
        analyticLineModel, attrs);

    return attrs;
  }

  protected Map<String, Map<String, Object>> hideReservedQty(
      SaleOrder saleOrder, SaleOrderLine saleOrderLine) {
    Map<String, Map<String, Object>> attrs = new HashMap<>();
    Boolean isHidden =
        saleOrder.getStatusSelect() != SaleOrderRepository.STATUS_ORDER_CONFIRMED
            || ProductRepository.PRODUCT_TYPE_SERVICE.equals(
                Optional.of(saleOrderLine)
                    .map(SaleOrderLine::getProduct)
                    .map(Product::getProductTypeSelect)
                    .orElse(""));
    attrs.put("reservedQty", Map.of(HIDDEN_ATTR, isHidden));
    return attrs;
  }
}
