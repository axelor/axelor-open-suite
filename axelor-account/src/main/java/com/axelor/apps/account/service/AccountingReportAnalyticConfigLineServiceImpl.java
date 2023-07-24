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
package com.axelor.apps.account.service;

import com.axelor.apps.account.db.AnalyticLevel;
import com.axelor.apps.account.db.repo.AnalyticLevelRepository;
import com.google.inject.Inject;

public class AccountingReportAnalyticConfigLineServiceImpl
    implements AccountingReportAnalyticConfigLineService {
  protected AnalyticLevelRepository analyticLevelRepo;

  @Inject
  public AccountingReportAnalyticConfigLineServiceImpl(AnalyticLevelRepository analyticLevelRepo) {
    this.analyticLevelRepo = analyticLevelRepo;
  }

  @Override
  public boolean getIsNotValidRuleLevel(int ruleLevel) {
    int maxLevel =
        analyticLevelRepo.all().fetch().stream()
            .map(AnalyticLevel::getNbr)
            .max(Integer::compareTo)
            .orElse(1);

    return ruleLevel > maxLevel;
  }
}
