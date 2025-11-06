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

import com.axelor.apps.sale.db.SaleOrder;
import com.axelor.apps.supplychain.exception.SupplychainExceptionMessage;
import com.axelor.apps.supplychain.service.app.AppSupplychainService;
import com.axelor.i18n.I18n;
import com.google.inject.Inject;
import java.util.ArrayList;
import java.util.List;

public class SaleOrderCheckBlockingSupplychainServiceImpl
    implements SaleOrderCheckBlockingSupplychainService {

  protected final SaleOrderBlockingSupplychainService saleOrderBlockingSupplychainService;
  protected final AppSupplychainService appSupplychainService;

  @Inject
  public SaleOrderCheckBlockingSupplychainServiceImpl(
      SaleOrderBlockingSupplychainService saleOrderBlockingSupplychainService,
      AppSupplychainService appSupplychainService) {
    this.saleOrderBlockingSupplychainService = saleOrderBlockingSupplychainService;
    this.appSupplychainService = appSupplychainService;
  }

  @Override
  public List<String> checkBlocking(SaleOrder saleOrder) {
    List<String> alertList = new ArrayList<>();
    if (saleOrderBlockingSupplychainService.hasOnGoingBlocking(saleOrder)
        && appSupplychainService.getAppSupplychain().getCustomerStockMoveGenerationAuto()) {
      alertList.add(I18n.get(SupplychainExceptionMessage.SALE_ORDER_LINES_CANNOT_DELIVER));
    }
    return alertList;
  }
}
