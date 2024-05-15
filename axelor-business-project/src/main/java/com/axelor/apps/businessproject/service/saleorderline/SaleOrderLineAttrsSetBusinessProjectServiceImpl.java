package com.axelor.apps.businessproject.service.saleorderline;

import com.axelor.apps.project.service.app.AppProjectService;
import com.axelor.apps.sale.db.SaleOrderLine;
import com.axelor.apps.sale.helper.SaleOrderLineHelper;
import com.axelor.apps.sale.service.app.AppSaleService;
import com.axelor.apps.supplychain.service.saleOrderLine.SaleOrderLineAttrsSetSupplychainServiceImpl;
import com.axelor.auth.AuthUtils;
import com.google.common.base.Strings;
import com.google.inject.Inject;
import java.util.Map;

public class SaleOrderLineAttrsSetBusinessProjectServiceImpl
    extends SaleOrderLineAttrsSetSupplychainServiceImpl
    implements SaleOrderLineAttrsSetBusinessProjectService {

  protected final AppProjectService appProjectService;

  @Inject
  public SaleOrderLineAttrsSetBusinessProjectServiceImpl(
      AppSaleService appSaleService, AppProjectService appProjectService) {
    super(appSaleService);
    this.appProjectService = appProjectService;
  }

  @Override
  public void setProjectTitle(Map<String, Map<String, Object>> attrsMap) {
    if (appProjectService.isApp("project")
        && !Strings.isNullOrEmpty(appProjectService.getAppProject().getProjectLabel())) {
      String projectLabel = appProjectService.getAppProject().getProjectLabel();
      SaleOrderLineHelper.addAttr("project", "title", projectLabel, attrsMap);
      SaleOrderLineHelper.addAttr("projectPanel", "title", projectLabel, attrsMap);
    }
  }

  @Override
  public void showDeliveryPanel(
      SaleOrderLine saleOrderLine, Map<String, Map<String, Object>> attrsMap) {
    if (saleOrderLine != null
        && saleOrderLine.getProduct() != null
        && AuthUtils.getUser() != null
        && AuthUtils.getUser().getActiveCompany() != null
        && AuthUtils.getUser().getActiveCompany().getSupplyChainConfig() != null) {
      if (saleOrderLine.getProduct().getProductTypeSelect().equals("storable")) {
        SaleOrderLineHelper.addAttr(
            "deliveryPanel",
            "hidden",
            !AuthUtils.getUser()
                .getActiveCompany()
                .getSupplyChainConfig()
                .getHasOutSmForStorableProduct(),
            attrsMap);
      }
      if (saleOrderLine.getProduct().getProductTypeSelect().equals("service")) {
        SaleOrderLineHelper.addAttr(
            "deliveryPanel",
            "hidden",
            !AuthUtils.getUser()
                .getActiveCompany()
                .getSupplyChainConfig()
                .getHasOutSmForNonStorableProduct(),
            attrsMap);
      }
    }
  }
}
