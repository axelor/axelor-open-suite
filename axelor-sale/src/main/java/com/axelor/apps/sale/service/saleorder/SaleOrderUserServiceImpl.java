/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2005-2024 Axelor (<http://axelor.com>).
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
package com.axelor.apps.sale.service.saleorder;

import com.axelor.apps.base.db.Partner;
import com.axelor.apps.base.service.user.UserService;
import com.axelor.apps.sale.db.SaleOrder;
import com.axelor.apps.sale.service.app.AppSaleService;
import com.axelor.auth.AuthUtils;
import com.axelor.auth.db.User;
import com.axelor.studio.db.AppSale;
import com.axelor.studio.db.repo.AppSaleRepository;
import com.axelor.team.db.Team;
import com.google.inject.Inject;

public class SaleOrderUserServiceImpl implements SaleOrderUserService {

  protected AppSaleService appSaleService;
  protected UserService userService;

  @Inject
  public SaleOrderUserServiceImpl(AppSaleService appSaleService, UserService userService) {
    this.appSaleService = appSaleService;
    this.userService = userService;
  }

  @Override
  public User getUser(SaleOrder saleOrder) {
    User user = null;
    AppSale appSale = appSaleService.getAppSale();
    int salesPersonSelect = appSale.getSalespersonSelect();
    Partner clientPartner = saleOrder.getClientPartner();
    if (salesPersonSelect == AppSaleRepository.APP_SALE_CURRENT_LOGIN_USER) {
      user = AuthUtils.getUser();
    }
    if (salesPersonSelect == AppSaleRepository.APP_SALE_USER_ASSIGNED_TO_CUSTOMER
        && clientPartner != null) {
      user = clientPartner.getUser();
    }
    return user;
  }

  @Override
  public Team getTeam(SaleOrder saleOrder) {
    Team team = null;
    AppSale appSale = appSaleService.getAppSale();
    int salesPersonSelect = appSale.getSalespersonSelect();
    Partner clientPartner = saleOrder.getClientPartner();
    if (salesPersonSelect == AppSaleRepository.APP_SALE_CURRENT_LOGIN_USER) {
      team = userService.getUserActiveTeam();
    }
    if (salesPersonSelect == AppSaleRepository.APP_SALE_USER_ASSIGNED_TO_CUSTOMER
        && clientPartner != null) {
      team = clientPartner.getTeam();
    }
    return team;
  }
}
