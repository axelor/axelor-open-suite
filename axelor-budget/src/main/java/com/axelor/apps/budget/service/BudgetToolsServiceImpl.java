/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2005-2023 Axelor (<http://axelor.com>).
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
package com.axelor.apps.budget.service;

import com.axelor.apps.account.db.AccountConfig;
import com.axelor.apps.account.db.Move;
import com.axelor.apps.account.db.repo.MoveRepository;
import com.axelor.apps.account.service.config.AccountConfigService;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Company;
import com.axelor.auth.AuthUtils;
import com.axelor.auth.db.Role;
import com.axelor.auth.db.User;
import com.google.inject.Inject;
import com.google.inject.servlet.RequestScoped;
import org.apache.commons.collections.CollectionUtils;

@RequestScoped
public class BudgetToolsServiceImpl implements BudgetToolsService {

  protected AccountConfigService accountConfigService;

  @Inject
  public BudgetToolsServiceImpl(AccountConfigService accountConfigService) {
    this.accountConfigService = accountConfigService;
  }

  @Override
  public boolean checkBudgetKeyAndRole(Company company, User user) throws AxelorException {
    if (company != null && user != null) {
      AccountConfig accountConfig = accountConfigService.getAccountConfig(company);
      if (!accountConfig.getEnableBudgetKey()
          || CollectionUtils.isEmpty(accountConfig.getBudgetDistributionRoleList())) {
        return true;
      }
      for (Role role : user.getRoles()) {
        if (accountConfig.getBudgetDistributionRoleList().contains(role)) {
          return true;
        }
      }
      for (Role role : user.getGroup().getRoles()) {
        if (accountConfig.getBudgetDistributionRoleList().contains(role)) {
          return true;
        }
      }
    }
    return false;
  }

  @Override
  public boolean checkBudgetKeyAndRoleForMove(Move move) throws AxelorException {
    if (move != null) {
      return !(checkBudgetKeyAndRole(move.getCompany(), AuthUtils.getUser()))
          || move.getStatusSelect() == MoveRepository.STATUS_ACCOUNTED
          || move.getStatusSelect() == MoveRepository.STATUS_CANCELED;
    }
    return false;
  }
}
