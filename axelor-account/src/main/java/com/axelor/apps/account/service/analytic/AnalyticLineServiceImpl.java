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
package com.axelor.apps.account.service.analytic;

import com.axelor.apps.account.db.Account;
import com.axelor.apps.account.db.AccountConfig;
import com.axelor.apps.account.db.AnalyticAccount;
import com.axelor.apps.account.db.AnalyticAxis;
import com.axelor.apps.account.db.AnalyticAxisByCompany;
import com.axelor.apps.account.db.AnalyticJournal;
import com.axelor.apps.account.db.AnalyticMoveLine;
import com.axelor.apps.account.db.MoveLine;
import com.axelor.apps.account.db.repo.AnalyticAccountRepository;
import com.axelor.apps.account.db.repo.AnalyticLine;
import com.axelor.apps.account.service.AccountService;
import com.axelor.apps.account.service.config.AccountConfigService;
import com.axelor.apps.account.service.moveline.MoveLineComputeAnalyticService;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.Currency;
import com.axelor.apps.base.service.CurrencyScaleService;
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.common.ObjectUtils;
import com.axelor.utils.helpers.ListHelper;
import com.google.inject.Inject;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.apache.commons.collections.CollectionUtils;

public class AnalyticLineServiceImpl implements AnalyticLineService {

  protected AccountConfigService accountConfigService;
  protected AppBaseService appBaseService;
  protected AnalyticToolService analyticToolService;
  protected AnalyticAccountRepository analyticAccountRepository;
  protected AccountService accountService;
  protected MoveLineComputeAnalyticService moveLineComputeAnalyticService;
  protected CurrencyScaleService currencyScaleService;

  @Inject
  public AnalyticLineServiceImpl(
      AccountConfigService accountConfigService,
      AppBaseService appBaseService,
      AnalyticToolService analyticToolService,
      AnalyticAccountRepository analyticAccountRepository,
      AccountService accountService,
      MoveLineComputeAnalyticService moveLineComputeAnalyticService,
      CurrencyScaleService currencyScaleService) {
    this.accountConfigService = accountConfigService;
    this.appBaseService = appBaseService;
    this.analyticToolService = analyticToolService;
    this.analyticAccountRepository = analyticAccountRepository;
    this.accountService = accountService;
    this.moveLineComputeAnalyticService = moveLineComputeAnalyticService;
    this.currencyScaleService = currencyScaleService;
  }

  @Override
  public AnalyticJournal getAnalyticJournal(AnalyticLine analyticLine) throws AxelorException {
    if (analyticLine.getAccount() != null && analyticLine.getAccount().getCompany() != null) {
      return accountConfigService
          .getAccountConfig(analyticLine.getAccount().getCompany())
          .getAnalyticJournal();
    }
    return null;
  }

  @Override
  public Currency getCompanyCurrency(AnalyticLine analyticLine) {
    return Optional.of(analyticLine)
        .map(AnalyticLine::getAccount)
        .map(Account::getCompany)
        .map(Company::getCurrency)
        .orElse(null);
  }

  @Override
  public LocalDate getDate(AnalyticLine analyticLine) {
    if (analyticLine instanceof MoveLine) {
      MoveLine line = (MoveLine) analyticLine;
      if (line.getDate() != null) {
        return line.getDate();
      }
    }
    if (analyticLine.getAccount() != null && analyticLine.getAccount().getCompany() != null) {
      return appBaseService.getTodayDate(analyticLine.getAccount().getCompany());
    }
    return appBaseService.getTodayDate(null);
  }

  @Override
  public List<Long> getAxisDomains(AnalyticLine line, Company company, int position)
      throws AxelorException {
    List<Long> analyticAccountListByAxis = new ArrayList<>();

    if (analyticToolService.isPositionUnderAnalyticAxisSelect(company, position)) {

      AnalyticAxis analyticAxis =
          accountConfigService.getAccountConfig(company).getAnalyticAxisByCompanyList().stream()
              .filter(it -> it.getSequence() == position)
              .findFirst()
              .stream()
              .map(AnalyticAxisByCompany::getAnalyticAxis)
              .findFirst()
              .orElse(null);

      analyticAccountListByAxis = getAnalyticAccountsByAxis(line, analyticAxis);
    }
    return analyticAccountListByAxis;
  }

  @Override
  public List<Long> getAnalyticAccountsByAxis(AnalyticLine line, AnalyticAxis analyticAxis) {
    List<Long> analyticAccountListByRules = new ArrayList<>();
    List<Long> analyticAccountListByAxis =
        analyticAccountRepository.findByAnalyticAxis(analyticAxis).fetch().stream()
            .map(AnalyticAccount::getId)
            .collect(Collectors.toList());

    if (line.getAccount() != null) {
      List<Long> analyticAccountIdList = accountService.getAnalyticAccountsIds(line.getAccount());
      if (CollectionUtils.isNotEmpty(analyticAccountIdList)) {
        for (Long analyticAccountId : analyticAccountIdList) {
          analyticAccountListByRules.add(analyticAccountId);
        }
        if (!CollectionUtils.isEmpty(analyticAccountListByRules)) {
          analyticAccountListByAxis =
              ListHelper.intersection(analyticAccountListByAxis, analyticAccountListByRules);
        }
      }
    }
    return analyticAccountListByAxis;
  }

  @Override
  public boolean isAxisRequired(AnalyticLine line, Company company, int position)
      throws AxelorException {
    Account account = line.getAccount();
    if (!analyticToolService.isManageAnalytic(company)
        || !analyticToolService.isPositionUnderAnalyticAxisSelect(company, position)
        || account == null
        || !account.getAnalyticDistributionAuthorized()) {
      return false;
    }

    if (analyticToolService.isFreeAnalyticDistribution(company)) {
      AccountConfig accountConfig = accountConfigService.getAccountConfig(company);
      List<AnalyticAxisByCompany> analyticAxisByCompanyList =
          accountConfig.getAnalyticAxisByCompanyList();
      if (ObjectUtils.isEmpty(analyticAxisByCompanyList)
          || analyticAxisByCompanyList.size() < position) {
        return false;
      }
      return Optional.ofNullable(analyticAxisByCompanyList.get(position - 1))
          .map(AnalyticAxisByCompany::getIsRequired)
          .orElse(false);

    } else {
      return account.getAnalyticDistributionRequiredOnMoveLines()
          && line.getAnalyticDistributionTemplate() == null;
    }
  }

  @Override
  public AnalyticLine checkAnalyticLineForAxis(AnalyticLine line) {
    if (analyticToolService.isAnalyticAxisFilled(
        line.getAxis1AnalyticAccount(), line.getAnalyticMoveLineList())) {
      line.setAxis1AnalyticAccount(null);
    }
    if (analyticToolService.isAnalyticAxisFilled(
        line.getAxis2AnalyticAccount(), line.getAnalyticMoveLineList())) {
      line.setAxis2AnalyticAccount(null);
    }
    if (analyticToolService.isAnalyticAxisFilled(
        line.getAxis3AnalyticAccount(), line.getAnalyticMoveLineList())) {
      line.setAxis3AnalyticAccount(null);
    }
    if (analyticToolService.isAnalyticAxisFilled(
        line.getAxis4AnalyticAccount(), line.getAnalyticMoveLineList())) {
      line.setAxis4AnalyticAccount(null);
    }
    if (analyticToolService.isAnalyticAxisFilled(
        line.getAxis5AnalyticAccount(), line.getAnalyticMoveLineList())) {
      line.setAxis5AnalyticAccount(null);
    }
    return line;
  }

  @Override
  public AnalyticLine setAnalyticAccount(AnalyticLine analyticLine, Company company)
      throws AxelorException {
    if (CollectionUtils.isEmpty(analyticLine.getAnalyticMoveLineList()) || company == null) {
      this.resetAxisAnalyticAccount(analyticLine);
      return analyticLine;
    }

    for (AnalyticAxisByCompany analyticAxisByCompany :
        accountConfigService.getAccountConfig(company).getAnalyticAxisByCompanyList()) {
      if (checkAnalyticAxisPercentage(analyticLine, analyticAxisByCompany.getAnalyticAxis())) {
        AnalyticMoveLine analyticMoveLine =
            getAnalyticMoveLines(analyticLine, analyticAxisByCompany.getAnalyticAxis()).get(0);
        this.setAxisAccount(analyticLine, analyticAxisByCompany, analyticMoveLine);
      } else {
        this.setAxisAccount(analyticLine, analyticAxisByCompany, null);
      }
    }

    return analyticLine;
  }

  @Override
  public boolean checkAnalyticLinesByAxis(AnalyticLine analyticLine, int position, Company company)
      throws AxelorException {
    if (CollectionUtils.isEmpty(analyticLine.getAnalyticMoveLineList()) || company == null) {
      return false;
    }

    List<AnalyticAxisByCompany> analyticAxisByCompany =
        accountConfigService.getAccountConfig(company).getAnalyticAxisByCompanyList();
    if (ObjectUtils.notEmpty(analyticAxisByCompany)
        && analyticToolService.isPositionUnderAnalyticAxisSelect(company, position)) {
      return analyticToolService.isAxisAccountSumValidated(
          analyticLine.getAnalyticMoveLineList(),
          analyticAxisByCompany.get(position - 1).getAnalyticAxis());
    }

    return false;
  }

  protected boolean checkAnalyticAxisPercentage(
      AnalyticLine analyticLine, AnalyticAxis analyticAxis) {
    return getAnalyticMoveLines(analyticLine, analyticAxis).size() == 1
        && getAnalyticMoveLineOnAxis(analyticLine, analyticAxis).count() == 1;
  }

  protected List<AnalyticMoveLine> getAnalyticMoveLines(
      AnalyticLine analyticLine, AnalyticAxis analyticAxis) {
    return getAnalyticMoveLineOnAxis(analyticLine, analyticAxis)
        .filter(it -> it.getPercentage().compareTo(new BigDecimal(100)) == 0)
        .collect(Collectors.toList());
  }

  protected Stream<AnalyticMoveLine> getAnalyticMoveLineOnAxis(
      AnalyticLine analyticLine, AnalyticAxis analyticAxis) {
    if (analyticAxis != null) {
      return analyticLine.getAnalyticMoveLineList().stream()
          .filter(it -> analyticAxis.equals(it.getAnalyticAxis()));
    }

    return Stream.empty();
  }

  protected void setAxisAccount(
      AnalyticLine analyticLine,
      AnalyticAxisByCompany analyticAxisByCompany,
      AnalyticMoveLine analyticMoveLine) {
    AnalyticAccount analyticAccount = null;
    if (analyticMoveLine != null) {
      analyticAccount = analyticMoveLine.getAnalyticAccount();
    }

    switch (analyticAxisByCompany.getSequence()) {
      case 1:
        analyticLine.setAxis1AnalyticAccount(analyticAccount);
        break;
      case 2:
        analyticLine.setAxis2AnalyticAccount(analyticAccount);
        break;
      case 3:
        analyticLine.setAxis3AnalyticAccount(analyticAccount);
        break;
      case 4:
        analyticLine.setAxis4AnalyticAccount(analyticAccount);
        break;
      case 5:
        analyticLine.setAxis5AnalyticAccount(analyticAccount);
        break;
      default:
        break;
    }
  }

  protected void resetAxisAnalyticAccount(AnalyticLine analyticLine) {
    analyticLine.setAxis1AnalyticAccount(null);
    analyticLine.setAxis2AnalyticAccount(null);
    analyticLine.setAxis3AnalyticAccount(null);
    analyticLine.setAxis4AnalyticAccount(null);
    analyticLine.setAxis5AnalyticAccount(null);
  }
}
