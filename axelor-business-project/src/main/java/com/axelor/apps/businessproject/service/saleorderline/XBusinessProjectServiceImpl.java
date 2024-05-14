package com.axelor.apps.businessproject.service.saleorderline;

import com.axelor.apps.project.service.app.AppProjectService;
import com.axelor.apps.sale.db.SaleOrder;
import com.axelor.apps.sale.db.SaleOrderLine;
import com.axelor.apps.sale.service.app.AppSaleService;
import com.axelor.apps.supplychain.service.saleOrderLine.XSupplychainServiceImpl;
import com.axelor.auth.AuthUtils;
import com.google.common.base.Strings;
import com.google.inject.Inject;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class XBusinessProjectServiceImpl extends XSupplychainServiceImpl {

  protected final AppProjectService appProjectService;

  @Inject
  public XBusinessProjectServiceImpl(
      AppSaleService appSaleService, AppProjectService appProjectService) {
    super(appSaleService);
    this.appProjectService = appProjectService;
  }

  @Override
  public void resetInvoicingMode(SaleOrderLine saleOrderLine) {
    saleOrderLine.setInvoicingModeSelect(null);
  }

  @Override
  public void setProjectTitle(Map<String, Map<String, Object>> attrsMap) {
    Map<String, String> map = new HashMap<>();
    if (appProjectService.isApp("project")
        && !Strings.isNullOrEmpty(appProjectService.getAppProject().getProjectLabel())) {
      String projectLabel = appProjectService.getAppProject().getProjectLabel();
      this.addAttr("project", "title", projectLabel, attrsMap);
      this.addAttr("projectPanel", "title", projectLabel, attrsMap);
    }
  }

  @Override
  public void showDeliveryPanel(
      SaleOrderLine saleOrderLine, Map<String, Map<String, Object>> attrsMap) {
    if (Objects.equals(saleOrderLine.getProduct().getProductTypeSelect(), "storable")) {
      this.addAttr(
          "deliveryPanel",
          "hidden",
          !AuthUtils.getUser()
              .getActiveCompany()
              .getSupplyChainConfig()
              .getHasOutSmForStorableProduct(),
          attrsMap);
    }
    if (Objects.equals(saleOrderLine.getProduct().getProductTypeSelect(), "service")) {
      this.addAttr(
          "deliveryPanel",
          "hidden",
          !AuthUtils.getUser()
              .getActiveCompany()
              .getSupplyChainConfig()
              .getHasOutSmForNonStorableProduct(),
          attrsMap);
    }
  }

  @Override
  public void setEstimatedDateValue(
      SaleOrderLine saleOrderLine, SaleOrder saleOrder, Map<String, Map<String, Object>> attrsMap) {
    if (saleOrder != null || saleOrderLine.getDeliveryState() < 2) {
      this.addAttr(
          "estimatedShippingDate", "value", saleOrder.getEstimatedShippingDate(), attrsMap);
    } else {
      this.addAttr(
          "estimatedShippingDate", "value", saleOrderLine.getEstimatedShippingDate(), attrsMap);
    }
  }

  @Override
  public void setProjectValue(SaleOrder saleOrder, Map<String, Map<String, Object>> attrsMap) {
    this.addAttr("project", "value", saleOrder.getProject(), attrsMap);
  }
}
