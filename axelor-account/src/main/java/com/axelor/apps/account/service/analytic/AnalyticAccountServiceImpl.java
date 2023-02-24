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
import com.axelor.apps.account.db.repo.AccountAnalyticRulesRepository;
import com.axelor.apps.account.db.repo.AccountConfigRepository;
import com.axelor.apps.account.db.repo.AnalyticAccountRepository;
import com.axelor.apps.base.db.Company;
import com.axelor.common.ObjectUtils;
import com.google.common.base.Joiner;
import com.google.inject.persist.Transactional;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import javax.inject.Inject;
import org.apache.commons.collections.CollectionUtils;

public class AnalyticAccountServiceImpl implements AnalyticAccountService {

  protected AnalyticAccountRepository analyticAccountRepository;
  protected AccountAnalyticRulesRepository accountAnalyticRulesRepository;
  protected AccountConfigRepository accountConfigRepository;

  @Inject
  public AnalyticAccountServiceImpl(
      AnalyticAccountRepository analyticAccountRepository,
      AccountAnalyticRulesRepository accountAnalyticRulesRepository,
      AccountConfigRepository accountConfigRepository) {
    this.analyticAccountRepository = analyticAccountRepository;
    this.accountAnalyticRulesRepository = accountAnalyticRulesRepository;
    this.accountConfigRepository = accountConfigRepository;
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
      AnalyticDistributionTemplate analyticDistributionTemplate,
      AnalyticDistributionLine analyticDistributionLine,
      Account account) {
    String domain = "null";
    List<Long> analyticAccountIdList = new ArrayList<>();

    if (analyticDistributionTemplate != null && analyticDistributionTemplate.getCompany() != null) {
      domain =
          "(self.company is null OR self.company.id = "
              + analyticDistributionTemplate.getCompany().getId()
              + ") AND self.analyticAxis.id ";
      if (analyticDistributionLine.getAnalyticAxis() != null) {
        domain += "= " + analyticDistributionLine.getAnalyticAxis().getId();
      } else {
        domain +=
            "in ("
                + accountConfigRepository.findByCompany(analyticDistributionTemplate.getCompany())
                    .getAnalyticAxisByCompanyList().stream()
                    .map(it -> it.getAnalyticAxis().getId().toString())
                    .collect(Collectors.toList())
                + ")";
      }

      if (ObjectUtils.notEmpty(account)) {
        List<AnalyticAccount> analyticAccountList =
            accountAnalyticRulesRepository.findAnalyticAccountByAccounts(account);
        if (CollectionUtils.isNotEmpty(analyticAccountList)) {
          for (AnalyticAccount analyticAccount : analyticAccountList) {
            analyticAccountIdList.add(analyticAccount.getId());
          }

          domain += " AND self.id in (";
          String idList = Joiner.on(",").join(analyticAccountIdList);
          domain += idList + ")";
        } else {
          domain += " AND self.id in (0)";
        }
      }
    }

    return domain;
  }
}
