/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2005-2025 Axelor (<http://axelor.com>).
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package com.axelor.apps.sale.service.saleorder.views;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.apps.sale.db.SaleOrder;
import com.axelor.apps.sale.service.app.AppSaleService;
import com.axelor.apps.sale.service.saleorder.SaleOrderVersionService;
import com.axelor.studio.db.AppBase;
import com.google.inject.Inject;
import java.util.HashMap;
import java.util.Map;

public class SaleOrderDummyServiceImpl implements SaleOrderDummyService {
  protected AppBaseService appBaseService;
  protected AppSaleService appSaleService;
  protected SaleOrderVersionService saleOrderVersionService;

  @Inject
  public SaleOrderDummyServiceImpl(
      AppBaseService appBaseService,
      AppSaleService appSaleService,
      SaleOrderVersionService saleOrderVersionService) {
    this.appBaseService = appBaseService;
    this.appSaleService = appSaleService;
    this.saleOrderVersionService = saleOrderVersionService;
  }

  @Override
  public Map<String, Object> getOnNewDummies(SaleOrder saleOrder) {
    Map<String, Object> dummies = new HashMap<>();
    dummies.putAll(getTradingManagementConfig());
    dummies.putAll(getSaveActualVersion());
    dummies.putAll(getLastVersion(saleOrder));
    return dummies;
  }

  @Override
  public Map<String, Object> getOnLoadSplitDummies(SaleOrder saleOrder) throws AxelorException {
    Map<String, Object> dummies = new HashMap<>();
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
}
