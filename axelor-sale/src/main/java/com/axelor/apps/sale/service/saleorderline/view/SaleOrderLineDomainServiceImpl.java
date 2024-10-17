package com.axelor.apps.sale.service.saleorderline.view;

import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.apps.sale.db.SaleOrder;
import com.axelor.apps.sale.db.SaleOrderLine;
import com.axelor.apps.sale.service.app.AppSaleService;
import com.axelor.meta.loader.ModuleManager;
import com.google.inject.Inject;
import org.apache.commons.collections.CollectionUtils;

public class SaleOrderLineDomainServiceImpl implements SaleOrderLineDomainService {

  protected AppBaseService appBaseService;
  protected AppSaleService appSaleService;

  @Inject
  public SaleOrderLineDomainServiceImpl(
      AppBaseService appBaseService, AppSaleService appSaleService) {
    this.appBaseService = appBaseService;
    this.appSaleService = appSaleService;
  }

  @Override
  public String computeProductDomain(
      SaleOrderLine saleOrderLine, SaleOrder saleOrder, boolean isSubLine) {
    String domain =
        "self.isModel = false"
            + " and (self.startDate = null or self.startDate <= :__date__)"
            + " and (self.endDate = null or self.endDate > :__date__)"
            + " and self.dtype = 'Product'";

    if (appBaseService.getAppBase().getCompanySpecificProductFieldsSet() != null
        && appBaseService.getAppBase().getCompanySpecificProductFieldsSet().stream()
            .anyMatch(it -> "sellable".equals(it.getName()))
        && saleOrder != null
        && saleOrder.getCompany() != null) {
      domain +=
          " and (SELECT sellable "
              + "FROM ProductCompany productCompany "
              + "WHERE productCompany.product.id = self.id "
              + "AND productCompany.company.id = "
              + saleOrder.getCompany().getId()
              + ") IS TRUE ";
    } else {
      domain += " and self.sellable = true ";
    }

    if (appSaleService.getAppSale().getEnableSalesProductByTradName()
        && saleOrder != null
        && saleOrder.getTradingName() != null
        && saleOrder.getCompany() != null
        && !CollectionUtils.isEmpty(saleOrder.getCompany().getTradingNameList())) {
      domain +=
          " AND " + saleOrder.getTradingName().getId() + " member of self.tradingNameSellerSet";
    }

    // The standard way to do this would be to override the method in HR module.
    // But here, we have to do this because overriding a sale service in hr module will prevent the
    // override in supplychain, business-project, and business production module.
    if (ModuleManager.isInstalled("axelor-human-resource")) {
      domain += " AND self.expense = false OR self.expense IS NULL";
    }

    return domain;
  }
}
