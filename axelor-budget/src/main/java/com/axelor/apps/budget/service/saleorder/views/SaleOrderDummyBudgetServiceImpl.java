package com.axelor.apps.budget.service.saleorder.views;

import com.axelor.apps.account.service.config.AccountConfigService;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.service.CompanyService;
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.apps.sale.db.SaleOrder;
import com.axelor.apps.sale.service.app.AppSaleService;
import com.axelor.apps.sale.service.saleorder.SaleOrderVersionService;
import com.axelor.apps.sale.service.saleorder.views.SaleOrderDummyServiceImpl;
import com.google.inject.Inject;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class SaleOrderDummyBudgetServiceImpl extends SaleOrderDummyServiceImpl {

  protected CompanyService companyService;
  protected AccountConfigService accountConfigService;

  @Inject
  public SaleOrderDummyBudgetServiceImpl(
      AppBaseService appBaseService,
      AppSaleService appSaleService,
      SaleOrderVersionService saleOrderVersionService,
      CompanyService companyService,
      AccountConfigService accountConfigService) {
    super(appBaseService, appSaleService, saleOrderVersionService);
    this.companyService = companyService;
    this.accountConfigService = accountConfigService;
  }

  @Override
  public Map<String, Object> getOnNewDummies(SaleOrder saleOrder) throws AxelorException {
    Map<String, Object> dummies = super.getOnNewDummies(saleOrder);
    if (!appBaseService.isApp("budget")) {
      dummies.putAll(getEnableBudgetKey(saleOrder));
    }

    return dummies;
  }

  protected Map<String, Object> getEnableBudgetKey(SaleOrder saleOrder) throws AxelorException {
    Map<String, Object> dummies = new HashMap<>();
    Long companyId =
        Optional.of(saleOrder).map(SaleOrder::getCompany).map(Company::getId).orElse(null);
    Company company = companyService.getDefaultCompany(companyId);

    if (company != null) {
      dummies.put(
          "$enableBudgetKey", accountConfigService.getAccountConfig(company).getEnableBudgetKey());
    }

    return dummies;
  }
}
