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

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.apps.sale.db.SaleOrder;
import com.axelor.apps.sale.service.app.AppSaleService;
import com.axelor.apps.sale.service.saleorder.SaleOrderCheckServiceImpl;
import com.axelor.apps.stock.service.app.AppStockService;
import com.axelor.apps.supplychain.service.app.AppSupplychainService;
import com.google.inject.Inject;
import java.util.List;

public class SaleOrderCheckSupplychainServiceImpl extends SaleOrderCheckServiceImpl {

  protected AppSupplychainService appSupplychainService;
  protected AppStockService appStockService;
  protected AppSaleService appSaleService;
  protected final SaleOrderCheckBlockingSupplychainService saleOrderCheckBlockingSupplychainService;

  @Inject
  public SaleOrderCheckSupplychainServiceImpl(
      AppBaseService appBaseService,
      AppSupplychainService appSupplychainService,
      AppStockService appStockService,
      SaleOrderCheckBlockingSupplychainService saleOrderCheckBlockingSupplychainService,
      AppSaleService appSaleService) {
    super(appBaseService, appSaleService);
    this.appSupplychainService = appSupplychainService;
    this.appStockService = appStockService;
    this.saleOrderCheckBlockingSupplychainService = saleOrderCheckBlockingSupplychainService;
  }

  @Override
  public List<String> confirmCheckAlert(SaleOrder saleOrder) throws AxelorException {
    List<String> alertList = super.confirmCheckAlert(saleOrder);
    if (!appSupplychainService.isApp("supplychain")) {
      return alertList;
    }
    alertList.addAll(saleOrderCheckBlockingSupplychainService.checkBlocking(saleOrder));
    return alertList;
  }
}
