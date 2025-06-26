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
package com.axelor.apps.supplychain.service.saleorder;

import com.axelor.apps.account.service.app.AppAccountService;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.apps.sale.db.SaleOrder;
import com.axelor.apps.sale.service.app.AppSaleService;
import com.axelor.apps.sale.service.saleorder.SaleOrderVersionService;
import com.axelor.apps.sale.service.saleorder.views.SaleOrderDummyServiceImpl;
import com.axelor.apps.supplychain.service.AccountingSituationSupplychainService;
import com.google.inject.Inject;
import java.util.HashMap;
import java.util.Map;

public class SaleOrderDummySupplychainServiceImpl extends SaleOrderDummyServiceImpl {

  protected final AccountingSituationSupplychainService accountingSituationSupplychainService;
  protected final AppAccountService appAccountService;

  @Inject
  public SaleOrderDummySupplychainServiceImpl(
      AppBaseService appBaseService,
      AppSaleService appSaleService,
      SaleOrderVersionService saleOrderVersionService,
      AccountingSituationSupplychainService accountingSituationSupplychainService,
      AppAccountService appAccountService) {
    super(appBaseService, appSaleService, saleOrderVersionService);
    this.accountingSituationSupplychainService = accountingSituationSupplychainService;
    this.appAccountService = appAccountService;
  }

  @Override
  public Map<String, Object> getOnLoadSplitDummies(SaleOrder saleOrder) throws AxelorException {
    Map<String, Object> dummies = super.getOnLoadSplitDummies(saleOrder);
    dummies.putAll(fillIsUsedCreditExceeded(saleOrder));
    return dummies;
  }

  protected Map<String, Object> fillIsUsedCreditExceeded(SaleOrder saleOrder)
      throws AxelorException {
    Map<String, Object> dummies = new HashMap<>();
    if (!appAccountService.getAppAccount().getManageCustomerCredit()) {
      return dummies;
    }
    dummies.put(
        "$isUsedCreditExceeded",
        accountingSituationSupplychainService.isUsedCreditExceeded(saleOrder));
    return dummies;
  }
}
