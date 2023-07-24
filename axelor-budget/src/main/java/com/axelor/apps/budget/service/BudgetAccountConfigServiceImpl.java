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
import com.axelor.apps.account.db.AnalyticAxisByCompany;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.apps.budget.exception.BudgetExceptionMessage;
import com.axelor.i18n.I18n;
import com.google.inject.Inject;
import com.google.inject.servlet.RequestScoped;
import org.apache.commons.collections.CollectionUtils;

@RequestScoped
public class BudgetAccountConfigServiceImpl implements BudgetAccountConfigService {

  @Inject
  public BudgetAccountConfigServiceImpl() {}

  @Override
  public void checkBudgetKey(AccountConfig accountConfig) throws AxelorException {
    if (accountConfig.getEnableBudgetKey()
        && !CollectionUtils.isEmpty(accountConfig.getAnalyticAxisByCompanyList())) {
      for (AnalyticAxisByCompany analyticAxisByCompany :
          accountConfig.getAnalyticAxisByCompanyList()) {
        if (analyticAxisByCompany.getIncludeInBudgetKey()) {
          return;
        }
      }
      throw new AxelorException(
          TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
          I18n.get(BudgetExceptionMessage.ERROR_CONFIG_BUDGET_KEY));
    }
  }
}
