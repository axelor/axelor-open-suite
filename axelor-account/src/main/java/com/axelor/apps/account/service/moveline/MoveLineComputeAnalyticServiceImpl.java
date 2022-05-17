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
package com.axelor.apps.account.service.moveline;

import com.axelor.apps.account.db.AnalyticAccount;
import com.axelor.apps.account.db.AnalyticAxis;
import com.axelor.apps.account.db.AnalyticAxisByCompany;
import com.axelor.apps.account.db.AnalyticMoveLine;
import com.axelor.apps.account.db.Move;
import com.axelor.apps.account.db.MoveLine;
import com.axelor.apps.account.db.repo.AccountAnalyticRulesRepository;
import com.axelor.apps.account.db.repo.AccountConfigRepository;
import com.axelor.apps.account.db.repo.AnalyticAccountRepository;
import com.axelor.apps.account.db.repo.AnalyticMoveLineRepository;
import com.axelor.apps.account.service.analytic.AnalyticMoveLineService;
import com.axelor.apps.account.service.config.AccountConfigService;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.tool.service.ListToolService;
import com.axelor.exception.AxelorException;
import com.google.inject.Inject;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.collections.CollectionUtils;

public class MoveLineComputeAnalyticServiceImpl implements MoveLineComputeAnalyticService {

  protected AnalyticMoveLineService analyticMoveLineService;
  protected AccountConfigService accountConfigService;
  protected AnalyticAccountRepository analyticAccountRepository;
  protected AccountAnalyticRulesRepository accountAnalyticRulesRepository;
  protected ListToolService listToolService;

  @Inject
  public MoveLineComputeAnalyticServiceImpl(
      AnalyticMoveLineService analyticMoveLineService,
      AccountConfigService accountConfigService,
      AnalyticAccountRepository analyticAccountRepository,
      AccountAnalyticRulesRepository accountAnalyticRulesRepository,
      ListToolService listToolService) {
    this.analyticMoveLineService = analyticMoveLineService;
    this.accountConfigService = accountConfigService;
    this.analyticAccountRepository = analyticAccountRepository;
    this.accountAnalyticRulesRepository = accountAnalyticRulesRepository;
    this.listToolService = listToolService;
  }

  @Override
  public MoveLine computeAnalyticDistribution(MoveLine moveLine) {

    List<AnalyticMoveLine> analyticMoveLineList = moveLine.getAnalyticMoveLineList();

    if ((analyticMoveLineList == null || analyticMoveLineList.isEmpty())) {
      createAnalyticDistributionWithTemplate(moveLine);
    } else {
      LocalDate date = moveLine.getDate();
      BigDecimal amount = moveLine.getDebit().add(moveLine.getCredit());
      for (AnalyticMoveLine analyticMoveLine : analyticMoveLineList) {
        analyticMoveLineService.updateAnalyticMoveLine(analyticMoveLine, amount, date);
      }
    }
    updateAccountTypeOnAnalytic(moveLine, analyticMoveLineList);

    return moveLine;
  }

  @Override
  public void updateAccountTypeOnAnalytic(
      MoveLine moveLine, List<AnalyticMoveLine> analyticMoveLineList) {

    if ((analyticMoveLineList == null || analyticMoveLineList.isEmpty())) {
      return;
    }

    for (AnalyticMoveLine analyticMoveLine : analyticMoveLineList) {
      if (moveLine.getAccount() != null) {
        analyticMoveLine.setAccount(moveLine.getAccount());
        analyticMoveLine.setAccountType(moveLine.getAccount().getAccountType());
      }
    }
  }

  @Override
  public void generateAnalyticMoveLines(MoveLine moveLine) {

    List<AnalyticMoveLine> analyticMoveLineList =
        analyticMoveLineService.generateLines(
            moveLine.getAnalyticDistributionTemplate(),
            moveLine.getDebit().add(moveLine.getCredit()),
            AnalyticMoveLineRepository.STATUS_REAL_ACCOUNTING,
            moveLine.getDate());

    analyticMoveLineList.stream().forEach(moveLine::addAnalyticMoveLineListItem);
  }

  @Override
  public MoveLine createAnalyticDistributionWithTemplate(MoveLine moveLine) {

    List<AnalyticMoveLine> analyticMoveLineList =
        analyticMoveLineService.generateLines(
            moveLine.getAnalyticDistributionTemplate(),
            moveLine.getDebit().add(moveLine.getCredit()),
            AnalyticMoveLineRepository.STATUS_REAL_ACCOUNTING,
            moveLine.getDate());

    if (moveLine.getAnalyticMoveLineList() == null) {
      moveLine.setAnalyticMoveLineList(new ArrayList<>());
    } else {
      moveLine.getAnalyticMoveLineList().clear();
    }
    for (AnalyticMoveLine analyticMoveLine : analyticMoveLineList) {
      moveLine.addAnalyticMoveLineListItem(analyticMoveLine);
    }
    return moveLine;
  }

  @Override
  public MoveLine selectDefaultDistributionTemplate(MoveLine moveLine) throws AxelorException {
    if (moveLine != null && moveLine.getAccount() != null) {
      if (moveLine.getAccount().getAnalyticDistributionAuthorized()
          && moveLine.getAccount().getAnalyticDistributionTemplate() != null
          && accountConfigService
                  .getAccountConfig(moveLine.getAccount().getCompany())
                  .getAnalyticDistributionTypeSelect()
              == AccountConfigRepository.DISTRIBUTION_TYPE_PRODUCT) {
        moveLine.setAnalyticDistributionTemplate(
            moveLine.getAccount().getAnalyticDistributionTemplate());
      }
    } else {
      moveLine.setAnalyticDistributionTemplate(null);
    }
    List<AnalyticMoveLine> analyticMoveLineList = moveLine.getAnalyticMoveLineList();
    if (analyticMoveLineList != null) {
      analyticMoveLineList.clear();
    } else {
      moveLine.setAnalyticMoveLineList(new ArrayList<AnalyticMoveLine>());
    }
    moveLine = computeAnalyticDistribution(moveLine);
    return moveLine;
  }

  @Override
  public boolean compareNbrOfAnalyticAxisSelect(int position, Move move) throws AxelorException {
    return move != null
        && move.getCompany() != null
        && position
            <= accountConfigService
                .getAccountConfig(move.getCompany())
                .getNbrOfAnalyticAxisSelect();
  }

  @Override
  public List<Long> setAxisDomains(MoveLine moveLine, Move move, int position)
      throws AxelorException {
    List<Long> analyticAccountListByAxis = new ArrayList<Long>();
    List<Long> analyticAccountListByRules = new ArrayList<Long>();

    AnalyticAxis analyticAxis = new AnalyticAxis();

    if (compareNbrOfAnalyticAxisSelect(position, move)) {

      for (AnalyticAxisByCompany axis :
          accountConfigService.getAccountConfig(move.getCompany()).getAnalyticAxisByCompanyList()) {
        if (axis.getOrderSelect() == position) {
          analyticAxis = axis.getAnalyticAxis();
        }
      }

      for (AnalyticAccount analyticAccount :
          analyticAccountRepository.findByAnalyticAxis(analyticAxis).fetch()) {
        analyticAccountListByAxis.add(analyticAccount.getId());
      }
      if (moveLine.getAccount() != null) {
        List<AnalyticAccount> analyticAccountList =
            accountAnalyticRulesRepository.findAnalyticAccountByAccounts(moveLine.getAccount());
        if (!analyticAccountList.isEmpty()) {
          for (AnalyticAccount analyticAccount : analyticAccountList) {
            analyticAccountListByRules.add(analyticAccount.getId());
          }
          if (!CollectionUtils.isEmpty(analyticAccountListByRules)) {
            analyticAccountListByAxis =
                listToolService.intersection(analyticAccountListByAxis, analyticAccountListByRules);
          }
        }
      }
    }
    return analyticAccountListByAxis;
  }

  @Override
  public MoveLine analyzeMoveLine(MoveLine moveLine, Company company) throws AxelorException {
    if (moveLine != null) {

      if (moveLine.getAnalyticMoveLineList() == null) {
        moveLine.setAnalyticMoveLineList(new ArrayList<>());
      } else {
        moveLine.getAnalyticMoveLineList().clear();
      }

      AnalyticMoveLine analyticMoveLine = null;

      if (moveLine.getAxis1AnalyticAccount() != null) {
        analyticMoveLine =
            analyticMoveLineService.computeAnalyticMoveLine(
                moveLine, company, moveLine.getAxis1AnalyticAccount());
        moveLine.addAnalyticMoveLineListItem(analyticMoveLine);
      }
      if (moveLine.getAxis2AnalyticAccount() != null) {
        analyticMoveLine =
            analyticMoveLineService.computeAnalyticMoveLine(
                moveLine, company, moveLine.getAxis2AnalyticAccount());
        moveLine.addAnalyticMoveLineListItem(analyticMoveLine);
      }
      if (moveLine.getAxis3AnalyticAccount() != null) {
        analyticMoveLine =
            analyticMoveLineService.computeAnalyticMoveLine(
                moveLine, company, moveLine.getAxis3AnalyticAccount());
        moveLine.addAnalyticMoveLineListItem(analyticMoveLine);
      }
      if (moveLine.getAxis4AnalyticAccount() != null) {
        analyticMoveLine =
            analyticMoveLineService.computeAnalyticMoveLine(
                moveLine, company, moveLine.getAxis4AnalyticAccount());
        moveLine.addAnalyticMoveLineListItem(analyticMoveLine);
      }
      if (moveLine.getAxis5AnalyticAccount() != null) {
        analyticMoveLine =
            analyticMoveLineService.computeAnalyticMoveLine(
                moveLine, company, moveLine.getAxis5AnalyticAccount());
        moveLine.addAnalyticMoveLineListItem(analyticMoveLine);
      }
    }
    return moveLine;
  }
}
