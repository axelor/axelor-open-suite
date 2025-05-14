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
package com.axelor.apps.account.service.moveline;

import com.axelor.apps.account.db.Move;
import com.axelor.apps.account.db.MoveLine;
import com.axelor.apps.account.db.TaxLine;
import com.axelor.apps.account.db.repo.MoveRepository;
import com.axelor.apps.account.exception.AccountExceptionMessage;
import com.axelor.apps.account.service.AccountService;
import com.axelor.apps.account.service.TaxAccountService;
import com.axelor.apps.account.service.analytic.AnalyticDistributionTemplateService;
import com.axelor.apps.account.service.analytic.AnalyticMoveLineService;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.common.ObjectUtils;
import com.axelor.i18n.I18n;
import com.google.inject.Inject;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import org.apache.commons.collections.CollectionUtils;

public class MoveLineCheckServiceImpl implements MoveLineCheckService {
  protected AccountService accountService;
  protected AnalyticMoveLineService analyticMoveLineService;
  protected AnalyticDistributionTemplateService analyticDistributionTemplateService;
  protected MoveLineToolService moveLineToolService;
  protected TaxAccountService taxAccountService;

  @Inject
  public MoveLineCheckServiceImpl(
      AccountService accountService,
      AnalyticMoveLineService analyticMoveLineService,
      AnalyticDistributionTemplateService analyticDistributionTemplateService,
      MoveLineToolService moveLineToolService,
      TaxAccountService taxAccountService) {
    this.accountService = accountService;
    this.analyticMoveLineService = analyticMoveLineService;
    this.analyticDistributionTemplateService = analyticDistributionTemplateService;
    this.moveLineToolService = moveLineToolService;
    this.taxAccountService = taxAccountService;
  }

  @Override
  public void checkAnalyticByTemplate(MoveLine moveLine) throws AxelorException {
    if (moveLine.getAnalyticDistributionTemplate() != null) {
      analyticMoveLineService.validateLines(
          moveLine.getAnalyticDistributionTemplate().getAnalyticDistributionLineList());

      checkAnalyticMoveLinesPercentage(moveLine);

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

  @Override
  public void checkAnalyticMoveLinesPercentage(MoveLine moveLine) throws AxelorException {
    if (!analyticMoveLineService.validateAnalyticMoveLines(moveLine.getAnalyticMoveLineList())) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_INCONSISTENCY,
          I18n.get(AccountExceptionMessage.INVALID_ANALYTIC_MOVE_LINE));
    }
  }

  public void nonDeductibleTaxAuthorized(Move move, MoveLine moveLine) throws AxelorException {
    int technicalType = Optional.of(move.getFunctionalOriginSelect()).orElse(0);
    if (technicalType != MoveRepository.FUNCTIONAL_ORIGIN_PURCHASE) {
      this.checkMoveLineTaxes(moveLine.getTaxLineSet());
    }
  }

  public void checkMoveLineTaxes(Set<TaxLine> taxLineSet) throws AxelorException {
    if (ObjectUtils.notEmpty(taxLineSet) && taxAccountService.isNonDeductibleTaxesSet(taxLineSet)) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_INCONSISTENCY,
          I18n.get(AccountExceptionMessage.MOVE_LINE_WITH_NON_DEDUCTIBLE_TAX_NOT_AUTHORIZED));
    }
  }
}
