/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2005-2025 Axelor (<http://axelor.com>).
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
import com.axelor.apps.account.exception.AccountExceptionMessage;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.Partner;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.common.ObjectUtils;
import com.axelor.i18n.I18n;
import com.google.inject.Inject;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class AccountingSituationCheckServiceImpl implements AccountingSituationCheckService {

  @Inject
  public AccountingSituationCheckServiceImpl() {}

  @Override
  public void checkDuplicatedCompaniesInAccountingSituation(Partner partner)
      throws AxelorException {
    List<Company> duplicatedCompanyList = getDuplicatedCompanies(partner);
    if (!ObjectUtils.isEmpty(duplicatedCompanyList)) {
      throw new AxelorException(
          partner,
          TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
          I18n.get(AccountExceptionMessage.PARTNER_MULTIPLE_ACCOUNTING_SITUATION_ON_COMPANIES),
          duplicatedCompanyList.stream().map(Company::getName).collect(Collectors.joining(", ")));
    }
  }

  @Override
  public List<Company> getDuplicatedCompanies(Partner partner) {
    if (partner == null
        || ObjectUtils.isEmpty(partner.getAccountingSituationList())
        || partner.getAccountingSituationList().size() == 1) {
      return new ArrayList<>();
    }

    List<Company> duplicatedCompaniesList =
        partner.getAccountingSituationList().stream()
            .map(AccountingSituation::getCompany)
            .collect(Collectors.toList());
    return duplicatedCompaniesList.stream()
        .filter(i -> Collections.frequency(duplicatedCompaniesList, i) > 1)
        .distinct()
        .collect(Collectors.toList());
  }
}
