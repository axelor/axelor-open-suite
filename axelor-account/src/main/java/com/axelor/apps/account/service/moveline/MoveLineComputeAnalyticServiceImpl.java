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
package com.axelor.apps.account.service.moveline;

import com.axelor.apps.account.db.Account;
import com.axelor.apps.account.db.AnalyticMoveLine;
import com.axelor.apps.account.db.Move;
import com.axelor.apps.account.db.MoveLine;
import com.axelor.apps.account.db.repo.AccountConfigRepository;
import com.axelor.apps.account.db.repo.AnalyticAccountRepository;
import com.axelor.apps.account.db.repo.AnalyticMoveLineRepository;
import com.axelor.apps.account.service.analytic.AnalyticMoveLineService;
import com.axelor.apps.account.service.analytic.AnalyticToolService;
import com.axelor.apps.account.service.app.AppAccountService;
import com.axelor.apps.account.service.config.AccountConfigService;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Company;
import com.axelor.utils.service.ListToolService;
import com.google.inject.Inject;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.collections.CollectionUtils;

public class MoveLineComputeAnalyticServiceImpl implements MoveLineComputeAnalyticService {

  protected AnalyticMoveLineService analyticMoveLineService;
  protected AccountConfigService accountConfigService;
  protected AnalyticAccountRepository analyticAccountRepository;
  protected ListToolService listToolService;
  protected AnalyticToolService analyticToolService;
  protected AppAccountService appAccountService;
  private final int RETURN_SCALE = 2;

  @Inject
  public MoveLineComputeAnalyticServiceImpl(
      AnalyticMoveLineService analyticMoveLineService,
      AccountConfigService accountConfigService,
      AnalyticAccountRepository analyticAccountRepository,
      ListToolService listToolService,
      AnalyticToolService analyticToolService,
      AppAccountService appAccountService) {
    this.analyticMoveLineService = analyticMoveLineService;
    this.accountConfigService = accountConfigService;
    this.analyticAccountRepository = analyticAccountRepository;
    this.listToolService = listToolService;
    this.analyticToolService = analyticToolService;
    this.appAccountService = appAccountService;
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
  public void computeAnalyticDistribution(MoveLine moveLine, Move move) throws AxelorException {
    if (move != null && this.checkManageAnalytic(move.getCompany())) {
      this.computeAnalyticDistribution(moveLine);
    }
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
  public MoveLine createAnalyticDistributionWithTemplate(MoveLine moveLine, Move move)
      throws AxelorException {
    if (this.checkManageAnalytic(move.getCompany())) {
      this.createAnalyticDistributionWithTemplate(moveLine);
    }

    return moveLine;
  }

  @Override
  public MoveLine selectDefaultDistributionTemplate(MoveLine moveLine) throws AxelorException {
    if (moveLine != null) {
      Account account = moveLine.getAccount();
      if (account != null
          && account.getAnalyticDistributionAuthorized()
          && account.getAnalyticDistributionTemplate() != null
          && accountConfigService
                  .getAccountConfig(account.getCompany())
                  .getAnalyticDistributionTypeSelect()
              == AccountConfigRepository.DISTRIBUTION_TYPE_PRODUCT) {
        moveLine.setAnalyticDistributionTemplate(
            moveLine.getAccount().getAnalyticDistributionTemplate());
      } else {
        moveLine.setAnalyticDistributionTemplate(null);
      }
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

  @Override
  public MoveLine clearAnalyticAccounting(MoveLine moveLine) {
    moveLine.setAxis1AnalyticAccount(null);
    moveLine.setAxis2AnalyticAccount(null);
    moveLine.setAxis3AnalyticAccount(null);
    moveLine.setAxis4AnalyticAccount(null);
    moveLine.setAxis5AnalyticAccount(null);

    this.clearAnalyticMoveLineList(moveLine);

    return moveLine;
  }

  @Override
  public MoveLine clearAnalyticAccountingIfEmpty(MoveLine moveLine) {
    if (moveLine.getAxis1AnalyticAccount() == null
        && moveLine.getAxis2AnalyticAccount() == null
        && moveLine.getAxis3AnalyticAccount() == null
        && moveLine.getAxis4AnalyticAccount() == null
        && moveLine.getAxis5AnalyticAccount() == null) {
      this.clearAnalyticMoveLineList(moveLine);
    }

    return moveLine;
  }

  protected void clearAnalyticMoveLineList(MoveLine moveLine) {
    if (!CollectionUtils.isEmpty(moveLine.getAnalyticMoveLineList())) {
      moveLine
          .getAnalyticMoveLineList()
          .forEach(analyticMoveLine -> analyticMoveLine.setMoveLine(null));
      moveLine.getAnalyticMoveLineList().clear();
    }
  }

  @Override
  public BigDecimal getAnalyticAmount(MoveLine moveLine, AnalyticMoveLine analyticMoveLine) {
    if (moveLine.getCredit().compareTo(BigDecimal.ZERO) > 0) {
      return analyticMoveLine
          .getPercentage()
          .multiply(moveLine.getCredit())
          .divide(new BigDecimal(100), RETURN_SCALE, RoundingMode.HALF_UP);
    } else if (moveLine.getDebit().compareTo(BigDecimal.ZERO) > 0) {
      return analyticMoveLine
          .getPercentage()
          .multiply(moveLine.getDebit())
          .divide(new BigDecimal(100), RETURN_SCALE, RoundingMode.HALF_UP);
    }
    return BigDecimal.ZERO;
  }

  @Override
  public boolean checkManageAnalytic(Company company) throws AxelorException {
    return company != null
        && appAccountService.getAppAccount().getManageAnalyticAccounting()
        && accountConfigService.getAccountConfig(company).getManageAnalyticAccounting();
  }
}
