package com.axelor.apps.supplychain.service;

import com.axelor.apps.account.service.analytic.AnalyticAttrsService;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.Product;
import com.axelor.apps.sale.db.SaleOrder;
import com.axelor.apps.sale.db.SaleOrderLine;
import com.axelor.apps.sale.db.repo.SaleOrderRepository;
import com.axelor.apps.sale.service.saleorder.SaleOrderLineViewServiceImpl;
import com.axelor.apps.supplychain.db.SupplyChainConfig;
import com.axelor.apps.supplychain.model.AnalyticLineModel;
import com.axelor.apps.supplychain.service.analytic.AnalyticAttrsSupplychainService;
import com.axelor.auth.AuthUtils;
import com.google.inject.Inject;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class SaleOrderLineViewSupplychainServiceImpl extends SaleOrderLineViewServiceImpl
    implements SaleOrderLineViewSupplychainService {

  protected AnalyticAttrsService analyticAttrsService;
  protected AnalyticAttrsSupplychainService analyticAttrsSupplychainService;

  @Inject
  public SaleOrderLineViewSupplychainServiceImpl(
      AnalyticAttrsService analyticAttrsService,
      AnalyticAttrsSupplychainService analyticAttrsSupplychainService) {
    this.analyticAttrsService = analyticAttrsService;
    this.analyticAttrsSupplychainService = analyticAttrsSupplychainService;
  }

  @Override
  public Map<String, Map<String, Object>> getOnNewAttrs(
      SaleOrderLine saleOrderLine, SaleOrder saleOrder) throws AxelorException {
    Map<String, Map<String, Object>> attrs = super.getOnNewAttrs(saleOrderLine, saleOrder);
    attrs.putAll(hideSupplychainPanels(saleOrder));
    attrs.putAll(hideDeliveredQty(saleOrder));
    attrs.putAll(hideAllocatedQtyBtn(saleOrder, saleOrderLine));
    analyticAttrsService.addAnalyticAxisAttrs(saleOrder.getCompany(), null, attrs);
    attrs.putAll(setAnalyticDistributionPanelHidden(saleOrder, saleOrderLine));
    return attrs;
  }

  @Override
  public Map<String, Map<String, Object>> getOnLoadAttrs(
      SaleOrderLine saleOrderLine, SaleOrder saleOrder) throws AxelorException {
    Map<String, Map<String, Object>> attrs = super.getOnLoadAttrs(saleOrderLine, saleOrder);
    attrs.putAll(hideSupplychainPanels(saleOrder));
    attrs.putAll(hideDeliveredQty(saleOrder));
    attrs.putAll(hideAllocatedQtyBtn(saleOrder, saleOrderLine));
    attrs.putAll(hideReservedQty(saleOrder, saleOrderLine));
    analyticAttrsService.addAnalyticAxisAttrs(saleOrder.getCompany(), null, attrs);
    attrs.putAll(setAnalyticDistributionPanelHidden(saleOrder, saleOrderLine));
    return attrs;
  }

  @Override
  public Map<String, Map<String, Object>> getProductOnChangeAttrs(
      SaleOrderLine saleOrderLine, SaleOrder saleOrder) throws AxelorException {
    Map<String, Map<String, Object>> attrs =
        super.getProductOnChangeAttrs(saleOrderLine, saleOrder);
    attrs.putAll(hideDeliveryPanel(saleOrderLine));
    analyticAttrsService.addAnalyticAxisAttrs(saleOrder.getCompany(), null, attrs);
    attrs.putAll(setAnalyticDistributionPanelHidden(saleOrder, saleOrderLine));
    return attrs;
  }

  @Override
  public Map<String, Map<String, Object>> getSaleSupplySelectOnChangeAttrs(
      SaleOrderLine saleOrderLine, SaleOrder saleOrder) {
    Map<String, Map<String, Object>> attrs = new HashMap<>();
    return attrs;
  }

  protected Map<String, Map<String, Object>> hideSupplychainPanels(SaleOrder saleOrder) {
    Map<String, Map<String, Object>> attrs = new HashMap<>();
    int statusSelect = saleOrder.getStatusSelect();
    boolean hidePanels =
        statusSelect == SaleOrderRepository.STATUS_DRAFT_QUOTATION
            || statusSelect == SaleOrderRepository.STATUS_FINALIZED_QUOTATION;
    attrs.put("stockMoveLineOfSOPanel", Map.of(HIDDEN_ATTR, hidePanels));
    attrs.put("projectTaskListPanel", Map.of(HIDDEN_ATTR, hidePanels));
    attrs.put("invoicingFollowUpPanel", Map.of(HIDDEN_ATTR, hidePanels));
    return attrs;
  }

  protected Map<String, Map<String, Object>> hideDeliveryPanel(SaleOrderLine saleOrderLine) {
    Map<String, Map<String, Object>> attrs = new HashMap<>();
    String productTypeSelect =
        Optional.ofNullable(saleOrderLine.getProduct())
            .map(Product::getProductTypeSelect)
            .orElse("");
    boolean hidePanels = false;
    if (productTypeSelect.equals("storable")) {
      hidePanels =
          Optional.ofNullable(AuthUtils.getUser().getActiveCompany())
              .map(Company::getSupplyChainConfig)
              .map(SupplyChainConfig::getHasOutSmForStorableProduct)
              .orElse(false);
    }
    if (productTypeSelect.equals("service")) {
      hidePanels =
          Optional.ofNullable(AuthUtils.getUser().getActiveCompany())
              .map(Company::getSupplyChainConfig)
              .map(SupplyChainConfig::getHasOutSmForNonStorableProduct)
              .orElse(false);
    }
    attrs.put("deliveryPanel", Map.of(HIDDEN_ATTR, !hidePanels));
    return attrs;
  }

  protected Map<String, Map<String, Object>> hideDeliveredQty(SaleOrder saleOrder) {
    Map<String, Map<String, Object>> attrs = new HashMap<>();
    int statusSelect = saleOrder.getStatusSelect();
    attrs.put("deliveredQty", Map.of(HIDDEN_ATTR, statusSelect == 1 || statusSelect == 2));
    return attrs;
  }

  protected Map<String, Map<String, Object>> hideAllocatedQtyBtn(
      SaleOrder saleOrder, SaleOrderLine saleOrderLine) {
    Map<String, Map<String, Object>> attrs = new HashMap<>();
    String productTypeSelect =
        Optional.ofNullable(saleOrderLine.getProduct())
            .map(Product::getProductTypeSelect)
            .orElse("");
    int statusSelect = saleOrder.getStatusSelect();
    attrs.put(
        "updateAllocatedQtyBtn",
        Map.of(
            HIDDEN_ATTR,
            saleOrderLine.getId() == null
                || statusSelect != SaleOrderRepository.STATUS_ORDER_CONFIRMED
                || productTypeSelect.equals("service")));
    return attrs;
  }

  protected Map<String, Map<String, Object>> hideReservedQty(
      SaleOrder saleOrder, SaleOrderLine saleOrderLine) {
    Map<String, Map<String, Object>> attrs = new HashMap<>();
    String productTypeSelect =
        Optional.ofNullable(saleOrderLine.getProduct())
            .map(Product::getProductTypeSelect)
            .orElse("");
    int statusSelect = saleOrder.getStatusSelect();
    attrs.put(
        "reservedQty",
        Map.of(
            HIDDEN_ATTR,
            statusSelect != SaleOrderRepository.STATUS_ORDER_CONFIRMED
                || productTypeSelect.equals("service")));
    return attrs;
  }

  protected Map<String, Map<String, Object>> setAnalyticDistributionPanelHidden(
      SaleOrder saleOrder, SaleOrderLine saleOrderLine) throws AxelorException {
    Map<String, Map<String, Object>> attrs = new HashMap<>();
    AnalyticLineModel analyticLineModel = new AnalyticLineModel(saleOrderLine, saleOrder);
    analyticAttrsSupplychainService.addAnalyticDistributionPanelHiddenAttrs(
        analyticLineModel, attrs);
    return attrs;
  }
}
