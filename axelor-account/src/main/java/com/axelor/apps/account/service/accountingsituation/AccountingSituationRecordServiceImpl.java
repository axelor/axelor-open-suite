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
package com.axelor.apps.account.service.accountingsituation;

import com.axelor.apps.account.db.AccountingSituation;
import com.axelor.apps.account.service.config.AccountConfigService;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.Partner;
import com.axelor.apps.base.db.repo.CompanyRepository;
import com.axelor.apps.base.service.CompanyService;
import com.axelor.auth.AuthUtils;
import com.axelor.auth.db.User;
import com.axelor.common.ObjectUtils;
import com.google.inject.Inject;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class AccountingSituationRecordServiceImpl implements AccountingSituationRecordService {

  protected CompanyRepository companyRepository;
  protected AccountConfigService accountConfigService;
  protected CompanyService companyService;

  @Inject
  public AccountingSituationRecordServiceImpl(
      CompanyRepository companyRepository,
      AccountConfigService accountConfigService,
      CompanyService companyService) {
    this.companyRepository = companyRepository;
    this.accountConfigService = accountConfigService;
    this.companyService = companyService;
  }

  @Override
  public void setDefaultCompany(AccountingSituation accountingSituation, Partner partner) {
    Company company = Optional.of(AuthUtils.getUser()).map(User::getActiveCompany).orElse(null);

    if (company == null) {
      company = companyService.getDefaultCompany(null);
    }

    Company finalCompany = company;
    if (company != null && partner != null) {
      List<AccountingSituation> accountingSituationList = partner.getAccountingSituationList();
      if (ObjectUtils.isEmpty(accountingSituationList)
          || accountingSituationList.stream()
              .noneMatch(as -> Objects.equals(finalCompany, as.getCompany()))) {
        accountingSituation.setCompany(finalCompany);
      }
    }
  }
}
