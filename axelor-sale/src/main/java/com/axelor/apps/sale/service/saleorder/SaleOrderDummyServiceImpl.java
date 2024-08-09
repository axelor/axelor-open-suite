package com.axelor.apps.sale.service.saleorder;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.apps.sale.db.LoyaltyAccount;
import com.axelor.apps.sale.db.SaleOrder;
import com.axelor.apps.sale.service.app.AppSaleService;
import com.axelor.apps.sale.service.loyalty.LoyaltyAccountService;
import com.axelor.studio.db.AppBase;
import com.google.inject.Inject;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class SaleOrderDummyServiceImpl implements SaleOrderDummyService {
  protected AppBaseService appBaseService;
  protected AppSaleService appSaleService;
  protected SaleOrderVersionService saleOrderVersionService;
  protected LoyaltyAccountService loyaltyAccountService;

  @Inject
  public SaleOrderDummyServiceImpl(
      AppBaseService appBaseService,
      AppSaleService appSaleService,
      SaleOrderVersionService saleOrderVersionService,
      LoyaltyAccountService loyaltyAccountService) {
    this.appBaseService = appBaseService;
    this.appSaleService = appSaleService;
    this.saleOrderVersionService = saleOrderVersionService;
    this.loyaltyAccountService = loyaltyAccountService;
  }

  @Override
  public Map<String, Object> getOnNewDummies(SaleOrder saleOrder) throws AxelorException {
    Map<String, Object> dummies = new HashMap<>();
    dummies.putAll(getTradingManagementConfig());
    dummies.putAll(getSaveActualVersion());
    dummies.putAll(getLastVersion(saleOrder));
    return dummies;
  }

  @Override
  public Map<String, Object> getOnLoadDummies(SaleOrder saleOrder) throws AxelorException {
    Map<String, Object> dummies = new HashMap<>();
    if (appSaleService.getAppSale().getEnableLoyalty()) {
      dummies.putAll(getLoyaltyAccountDummies(saleOrder));
    }
    return dummies;
  }

  protected Map<String, Object> getTradingManagementConfig() {
    Map<String, Object> dummies = new HashMap<>();
    AppBase appBase = appBaseService.getAppBase();
    dummies.put("$enableTradingNamesManagement", appBase.getEnableTradingNamesManagement());
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

  protected Map<String, Object> getLoyaltyAccountDummies(SaleOrder saleOrder) {
    Map<String, Object> dummies = new HashMap<>();
    Optional<LoyaltyAccount> loyaltyAccount =
        loyaltyAccountService.getLoyaltyAccount(
            saleOrder.getClientPartner(), saleOrder.getCompany(), saleOrder.getTradingName());
    dummies.put(
        "$loyaltyPoints",
        loyaltyAccount
            .map(LoyaltyAccount::getPointsBalance)
            .map(points -> points.setScale(0, RoundingMode.HALF_UP))
            .orElse(null));
    return dummies;
  }
}
