package com.axelor.apps.sale.service.saleorder;

import com.axelor.apps.sale.db.SaleOrder;
import com.axelor.auth.db.User;
import com.axelor.team.db.Team;

public interface SaleOrderUserService {
  User getUser(SaleOrder saleOrder);

  Team getTeam(SaleOrder saleOrder);
}
