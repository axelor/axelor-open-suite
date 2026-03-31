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
package com.axelor.apps.account.service.moveline;

import com.axelor.apps.account.db.AnalyticDistributionTemplate;
import com.axelor.apps.account.db.AnalyticMoveLine;
import com.axelor.apps.account.db.Move;
import com.axelor.apps.account.db.MoveLine;
import com.axelor.apps.account.db.repo.AnalyticMoveLineRepository;
import com.axelor.apps.account.service.analytic.AnalyticLineComputeService;
import com.axelor.apps.account.service.analytic.AnalyticMoveLineService;
import com.axelor.apps.account.service.analytic.AnalyticToolService;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.TradingName;
import com.axelor.apps.base.service.CurrencyScaleService;
import jakarta.inject.Inject;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.collections.CollectionUtils;

public class MoveLineComputeAnalyticServiceImpl implements MoveLineComputeAnalyticService {

  protected AnalyticMoveLineService analyticMoveLineService;
  protected AnalyticToolService analyticToolService;
  protected CurrencyScaleService currencyScaleService;
  protected AnalyticLineComputeService analyticLineComputeService;

  @Inject
  public MoveLineComputeAnalyticServiceImpl(
      AnalyticMoveLineService analyticMoveLineService,
      AnalyticToolService analyticToolService,
      CurrencyScaleService currencyScaleService,
      AnalyticLineComputeService analyticLineComputeService) {
    this.analyticMoveLineService = analyticMoveLineService;
    this.analyticToolService = analyticToolService;
    this.currencyScaleService = currencyScaleService;
    this.analyticLineComputeService = analyticLineComputeService;
  }

  @Override
  public MoveLine computeAnalyticDistribution(MoveLine moveLine) {
    analyticLineComputeService.computeAnalyticDistribution(
        moveLine, getScaledAmount(moveLine), moveLine.getDate());
    return moveLine;
  }

  @Override
  public void computeAnalyticDistribution(MoveLine moveLine, Move move) throws AxelorException {
    if (move != null && analyticToolService.isManageAnalytic(move.getCompany())) {
      this.computeAnalyticDistribution(moveLine);
    }
  }

  @Override
  public void updateAccountTypeOnAnalytic(
      MoveLine moveLine, List<AnalyticMoveLine> analyticMoveLineList) {
    analyticLineComputeService.updateAccountTypeOnAnalytic(moveLine, analyticMoveLineList);
  }

  @Override
  public void generateAnalyticMoveLines(MoveLine moveLine) {

    List<AnalyticMoveLine> analyticMoveLineList =
        analyticMoveLineService.generateLines(
            moveLine.getAnalyticDistributionTemplate(),
            getScaledAmount(moveLine),
            AnalyticMoveLineRepository.STATUS_REAL_ACCOUNTING,
            moveLine.getDate());

    analyticMoveLineList.stream().forEach(moveLine::addAnalyticMoveLineListItem);
  }

  @Override
  public MoveLine createAnalyticDistributionWithTemplate(MoveLine moveLine) {
    analyticLineComputeService.createAnalyticDistributionWithTemplate(
        moveLine, getScaledAmount(moveLine), moveLine.getDate());
    return moveLine;
  }

  @Override
  public MoveLine createAnalyticDistributionWithTemplate(MoveLine moveLine, Move move)
      throws AxelorException {
    if (analyticToolService.isManageAnalytic(move.getCompany())) {
      this.createAnalyticDistributionWithTemplate(moveLine);
    }

    return moveLine;
  }

  @Override
  public MoveLine selectDefaultDistributionTemplate(MoveLine moveLine, Move move)
      throws AxelorException {
    if (moveLine != null) {
      moveLine.setAnalyticDistributionTemplate(
          getDistributionTemplate(move, moveLine, move.getTradingName()));
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

  protected AnalyticDistributionTemplate getDistributionTemplate(
      Move move, MoveLine moveLine, TradingName tradingName) throws AxelorException {
    if (move == null
        || moveLine == null
        || moveLine.getAccount() == null
        || !moveLine.getAccount().getAnalyticDistributionAuthorized()) {
      return null;
    }

    return analyticMoveLineService.getAnalyticDistributionTemplate(
        moveLine.getPartner(), null, move.getCompany(), tradingName, moveLine.getAccount(), false);
  }

  @Override
  public MoveLine analyzeMoveLine(MoveLine moveLine, Company company) throws AxelorException {
    analyticLineComputeService.analyzeAnalyticLine(
        moveLine, company, getScaledAmount(moveLine), moveLine.getDate());
    return moveLine;
  }

  @Override
  public MoveLine clearAnalyticAccounting(MoveLine moveLine) {
    clearAnalyticMoveLineList(moveLine);
    analyticLineComputeService.clearAnalyticAccounting(moveLine);
    return moveLine;
  }

  @Override
  public MoveLine clearAnalyticAccountingIfEmpty(MoveLine moveLine) {
    if (moveLine.getAxis1AnalyticAccount() == null
        && moveLine.getAxis2AnalyticAccount() == null
        && moveLine.getAxis3AnalyticAccount() == null
        && moveLine.getAxis4AnalyticAccount() == null
        && moveLine.getAxis5AnalyticAccount() == null) {
      clearAnalyticMoveLineList(moveLine);
    }
    analyticLineComputeService.clearAnalyticAccountingIfEmpty(moveLine);
    return moveLine;
  }

  protected void clearAnalyticMoveLineList(MoveLine moveLine) {
    if (!CollectionUtils.isEmpty(moveLine.getAnalyticMoveLineList())) {
      moveLine
          .getAnalyticMoveLineList()
          .forEach(analyticMoveLine -> analyticMoveLine.setMoveLine(null));
    }
  }

  protected BigDecimal getScaledAmount(MoveLine moveLine) {
    return currencyScaleService.getCompanyScaledValue(
        moveLine, moveLine.getDebit().add(moveLine.getCredit()));
  }
}
