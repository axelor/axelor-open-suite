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
