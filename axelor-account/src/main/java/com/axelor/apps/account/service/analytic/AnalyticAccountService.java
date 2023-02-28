/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2023 Axelor (<http://axelor.com>).
 *
 * This program is free software: you can redistribute it and/or  modify
 * it under the terms of the GNU Affero General Public License, version 3,
 * as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.axelor.apps.account.service.analytic;

import com.axelor.apps.account.db.Account;
import com.axelor.apps.account.db.AnalyticAccount;
import com.axelor.apps.account.db.AnalyticDistributionLine;
import com.axelor.apps.account.db.AnalyticDistributionTemplate;
import com.axelor.apps.base.db.Company;
import java.util.List;

public interface AnalyticAccountService {

  void toggleStatusSelect(AnalyticAccount analyticAccount);

  boolean checkChildrenAccount(Company company, List<AnalyticAccount> childrenList);

  String getParentDomain(AnalyticAccount analyticAccount);

  String getAnalyticAccountDomain(
      AnalyticDistributionTemplate analyticDistributionTemplate,
      AnalyticDistributionLine analyticDistributionLine,
      Account account);
}
