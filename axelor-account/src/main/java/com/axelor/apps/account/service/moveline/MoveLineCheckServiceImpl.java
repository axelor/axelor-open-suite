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

import com.axelor.apps.account.db.Move;
import com.axelor.apps.account.db.MoveLine;
import com.axelor.apps.account.exception.AccountExceptionMessage;
import com.axelor.apps.account.service.AccountService;
import com.axelor.apps.account.service.analytic.AnalyticDistributionTemplateService;
import com.axelor.apps.account.service.analytic.AnalyticMoveLineService;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.i18n.I18n;
import com.google.inject.Inject;
import java.util.List;
import java.util.Objects;
import org.apache.commons.collections.CollectionUtils;

public class MoveLineCheckServiceImpl implements MoveLineCheckService {
  protected AccountService accountService;
  protected AnalyticMoveLineService analyticMoveLineService;
  protected AnalyticDistributionTemplateService analyticDistributionTemplateService;
  protected MoveLineToolService moveLineToolService;

  @Inject
  public MoveLineCheckServiceImpl(
      AccountService accountService,
      AnalyticMoveLineService analyticMoveLineService,
      AnalyticDistributionTemplateService analyticDistributionTemplateService,
      MoveLineToolService moveLineToolService) {
    this.accountService = accountService;
    this.analyticMoveLineService = analyticMoveLineService;
    this.analyticDistributionTemplateService = analyticDistributionTemplateService;
    this.moveLineToolService = moveLineToolService;
  }

  @Override
  public void checkAnalyticByTemplate(MoveLine moveLine) throws AxelorException {
    if (moveLine.getAnalyticDistributionTemplate() != null) {
      analyticMoveLineService.validateLines(
          moveLine.getAnalyticDistributionTemplate().getAnalyticDistributionLineList());

      if (!analyticMoveLineService.validateAnalyticMoveLines(moveLine.getAnalyticMoveLineList())) {
        throw new AxelorException(
            TraceBackRepository.CATEGORY_INCONSISTENCY,
            I18n.get(AccountExceptionMessage.INVALID_ANALYTIC_MOVE_LINE));
      }

      analyticDistributionTemplateService.validateTemplatePercentages(
          moveLine.getAnalyticDistributionTemplate());
    }
  }

  @Override
  public void checkAnalyticAxes(MoveLine moveLine) throws AxelorException {
    if (moveLine.getAccount() != null) {
      accountService.checkAnalyticAxis(
          moveLine.getAccount(),
          moveLine.getAnalyticDistributionTemplate(),
          moveLine.getAccount().getAnalyticDistributionRequiredOnMoveLines(),
          false);
    }
  }

  @Override
  public void checkDebitCredit(MoveLine moveLine) throws AxelorException {
    if (moveLine.getCredit().signum() == 0 && moveLine.getDebit().signum() == 0) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_INCONSISTENCY,
          I18n.get(AccountExceptionMessage.MOVE_LINE_NO_DEBIT_CREDIT));
    }

    if (moveLine.getCredit().signum() < 0 || moveLine.getDebit().signum() < 0) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_INCONSISTENCY,
          I18n.get(AccountExceptionMessage.MOVE_LINE_NEGATIVE_DEBIT_CREDIT));
    }
  }

  @Override
  public void checkDates(Move move) throws AxelorException {
    if (!CollectionUtils.isEmpty(move.getMoveLineList())) {
      for (MoveLine moveline : move.getMoveLineList()) {
        moveLineToolService.checkDateInPeriod(move, moveline);
      }
    }
  }

  @Override
  public void checkAnalyticAccount(List<MoveLine> moveLineList) throws AxelorException {
    Objects.requireNonNull(moveLineList);
    for (MoveLine moveLine : moveLineList) {
      if (moveLine != null && moveLine.getAccount() != null) {
        accountService.checkAnalyticAxis(
            moveLine.getAccount(),
            moveLine.getAnalyticDistributionTemplate(),
            moveLine.getAccount().getAnalyticDistributionRequiredOnMoveLines(),
            false);
      }
    }
  }
}
