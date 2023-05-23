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
package com.axelor.apps.account.service.analytic;

import com.axelor.apps.account.db.Account;
import com.axelor.apps.account.db.AnalyticAccount;
import com.axelor.apps.account.db.AnalyticAxis;
import com.axelor.apps.account.db.AnalyticAxisByCompany;
import com.axelor.apps.account.db.repo.AccountAnalyticRulesRepository;
import com.axelor.apps.account.db.repo.AccountConfigRepository;
import com.axelor.apps.account.db.repo.AnalyticAccountRepository;
import com.axelor.apps.account.service.config.AccountConfigService;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Company;
import com.axelor.common.ObjectUtils;
import com.axelor.utils.StringTool;
import com.google.inject.persist.Transactional;
import java.util.List;
import java.util.stream.Collectors;
import javax.inject.Inject;
import org.apache.commons.collections.CollectionUtils;

public class AnalyticAccountServiceImpl implements AnalyticAccountService {

  protected AnalyticAccountRepository analyticAccountRepository;
  protected AccountAnalyticRulesRepository accountAnalyticRulesRepository;
  protected AccountConfigRepository accountConfigRepository;
  protected AccountConfigService accountConfigService;

  @Inject
  public AnalyticAccountServiceImpl(
      AnalyticAccountRepository analyticAccountRepository,
      AccountAnalyticRulesRepository accountAnalyticRulesRepository,
      AccountConfigRepository accountConfigRepository,
      AccountConfigService accountConfigService) {
    this.analyticAccountRepository = analyticAccountRepository;
    this.accountAnalyticRulesRepository = accountAnalyticRulesRepository;
    this.accountConfigRepository = accountConfigRepository;
    this.accountConfigService = accountConfigService;
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

  @Override
  public boolean checkChildrenAccount(Company company, List<AnalyticAccount> childrenList) {
    return !CollectionUtils.isEmpty(childrenList)
        && childrenList.stream()
            .anyMatch(it -> it.getCompany() != null && !it.getCompany().equals(company));
  }

  @Override
  public String getParentDomain(AnalyticAccount analyticAccount) {
    if (analyticAccount != null
        && analyticAccount.getAnalyticAxis() != null
        && analyticAccount.getAnalyticLevel() != null) {
      Integer level = analyticAccount.getAnalyticLevel().getNbr() + 1;
      String domain =
          "self.analyticLevel.nbr = "
              + level
              + " AND self.analyticAxis.id = "
              + analyticAccount.getAnalyticAxis().getId();
      if (analyticAccount.getCompany() != null) {
        domain = domain.concat(" AND self.company.id = " + analyticAccount.getCompany().getId());
      } else {
        domain = domain.concat(" AND self.company IS NULL");
      }
      return domain;
    }
    return null;
  }

  @Override
  public String getAnalyticAccountDomain(
      Company company, AnalyticAxis analyticAxis, Account account) throws AxelorException {
    String domain = "null";

    if (company != null) {
      domain =
          "(self.company is null OR self.company.id = "
              + company.getId()
              + ") AND self.analyticAxis.id ";
      if (analyticAxis != null) {
        domain += "= " + analyticAxis.getId();
      } else {
        String analyticAxisIdList = "0";
        List<AnalyticAxisByCompany> analyticAxisByCompanyList =
            accountConfigService.getAccountConfig(company).getAnalyticAxisByCompanyList();
        if (ObjectUtils.notEmpty(analyticAxisByCompanyList)) {
          analyticAxisIdList =
              StringTool.getIdListString(
                  analyticAxisByCompanyList.stream()
                      .map(it -> it.getAnalyticAxis())
                      .collect(Collectors.toList()));
        }

        domain += "in (" + analyticAxisIdList + ")";
      }

      if (account != null) {
        List<AnalyticAccount> analyticAccountList =
            accountAnalyticRulesRepository.findAnalyticAccountByAccounts(account);
        if (!CollectionUtils.isEmpty(analyticAccountList)) {
          domain += " AND self.id in (";
          String idList = StringTool.getIdListString(analyticAccountList);
          domain += idList + ")";
        }
      }
    }

    return domain;
  }
}
