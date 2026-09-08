/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2005-2026 Axelor (<http://axelor.com>).
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
package com.axelor.apps.account.service.loan;

import com.axelor.apps.account.db.LoanManagementConfig;
import com.axelor.apps.account.db.repo.LoanManagementConfigRepository;
import com.axelor.apps.base.db.Company;
import jakarta.inject.Inject;
import java.util.List;

public class LoanManagementConfigServiceImpl implements LoanManagementConfigService {

  protected LoanManagementConfigRepository loanManagementConfigRepository;

  @Inject
  public LoanManagementConfigServiceImpl(
      LoanManagementConfigRepository loanManagementConfigRepository) {
    this.loanManagementConfigRepository = loanManagementConfigRepository;
  }

  @Override
  public LoanManagementConfig getDefaultLoanManagementConfig(Company company) {
    if (company == null) {
      return null;
    }
    List<LoanManagementConfig> configs =
        loanManagementConfigRepository
            .all()
            .filter("self.company = :company")
            .bind("company", company)
            .fetch(2);
    return configs.size() == 1 ? configs.get(0) : null;
  }
}
