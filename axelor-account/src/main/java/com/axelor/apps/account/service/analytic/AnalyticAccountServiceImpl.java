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

import com.axelor.apps.account.db.AnalyticAccount;
import com.axelor.apps.account.db.repo.AnalyticAccountRepository;
import com.google.inject.persist.Transactional;
import javax.inject.Inject;

public class AnalyticAccountServiceImpl implements AnalyticAccountService {

  protected AnalyticAccountRepository analyticAccountRepository;

  @Inject
  public AnalyticAccountServiceImpl(AnalyticAccountRepository analyticAccountRepository) {
    this.analyticAccountRepository = analyticAccountRepository;
  }

  @Override
  @Transactional
  public void toggleStatusSelect(AnalyticAccount analyticAccount) {
    if (analyticAccount != null) {
      if (analyticAccount.getStatusSelect() == AnalyticAccountRepository.STATUS_INACTIVE) {
        analyticAccount = activate(analyticAccount);
      } else {
        analyticAccount = desactivate(analyticAccount);
      }
      analyticAccountRepository.save(analyticAccount);
    }
  }

  protected AnalyticAccount activate(AnalyticAccount analyticAccount) {
    analyticAccount.setStatusSelect(AnalyticAccountRepository.STATUS_ACTIVE);
    return analyticAccount;
  }

  protected AnalyticAccount desactivate(AnalyticAccount analyticAccount) {
    analyticAccount.setStatusSelect(AnalyticAccountRepository.STATUS_INACTIVE);
    return analyticAccount;
  }
}
