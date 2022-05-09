/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2022 Axelor (<http://axelor.com>).
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
package com.axelor.apps.base.service;

import com.axelor.apps.account.db.AccountManagement;
import com.axelor.apps.account.db.repo.AccountManagementRepository;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.repo.CompanyRepository;
import com.axelor.inject.Beans;
import java.util.ArrayList;
import java.util.List;

public class PaymentModeServiceImpl implements PaymentModeService {

  @Override
  public List<AccountManagement> getAccountManagementDefaults() {
    List<AccountManagement> accountManagementList = new ArrayList<>();
    List<Company> companyList = Beans.get(CompanyRepository.class).all().fetch();
    for (Company company : companyList) {
      AccountManagement accountManagement = new AccountManagement();
      accountManagement.setCompany(company);
      accountManagement.setTypeSelect(AccountManagementRepository.TYPE_PAYMENT);
      accountManagementList.add(accountManagement);
    }
    return accountManagementList;
  }
}
