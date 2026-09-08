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
package com.axelor.apps.account.service;

import com.axelor.apps.account.db.AccountManagement;
import com.axelor.apps.account.db.Tax;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.ProductFamily;
import com.axelor.utils.helpers.StringHelper;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import org.apache.commons.collections.CollectionUtils;

public class AccountManagementAttrsServiceImpl implements AccountManagementAttrsService {
  @Override
  public String getCompanyDomain(
      AccountManagement accountManagement, ProductFamily productFamily, Tax tax) {
    String domain = "(self.archived IS NULL OR self.archived = false)";

    List<AccountManagement> accountManagementList = null;
    if (productFamily != null) {
      accountManagementList = productFamily.getAccountManagementList();
    } else if (tax != null) {
      accountManagementList = tax.getAccountManagementList();
    }
    if (CollectionUtils.isEmpty(accountManagementList)) {
      return domain;
    }
    Long id = accountManagement != null ? accountManagement.getId() : null;
    List<Company> companyList =
        accountManagementList.stream()
            .filter(am -> id == null || !id.equals(am.getId()))
            .map(AccountManagement::getCompany)
            .filter(Objects::nonNull)
            .collect(Collectors.toList());

    if (!CollectionUtils.isEmpty(companyList)) {
      domain +=
          String.format(" AND self.id NOT IN (%s)", StringHelper.getIdListString(companyList));
    }
    return domain;
  }
}
