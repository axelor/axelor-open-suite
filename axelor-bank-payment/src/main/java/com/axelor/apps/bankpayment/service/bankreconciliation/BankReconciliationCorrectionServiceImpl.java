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
package com.axelor.apps.bankpayment.service.bankreconciliation;

import com.axelor.apps.account.db.MoveLine;
import com.axelor.apps.account.db.repo.MoveLineRepository;
import com.axelor.apps.bankpayment.db.BankReconciliation;
import com.axelor.apps.bankpayment.db.repo.BankReconciliationRepository;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.repo.PeriodRepository;
import com.axelor.apps.base.service.DateService;
import com.axelor.auth.db.User;
import com.axelor.i18n.I18n;
import com.google.inject.Inject;
import java.time.LocalDateTime;
import java.util.List;

public class BankReconciliationCorrectionServiceImpl
    implements BankReconciliationCorrectionService {

  protected MoveLineRepository moveLineRepository;
  protected DateService dateService;
  protected BankReconciliationQueryService bankReconciliationQueryService;

  @Inject
  public BankReconciliationCorrectionServiceImpl(
      MoveLineRepository moveLineRepository,
      DateService dateService,
      BankReconciliationQueryService bankReconciliationQueryService) {
    this.moveLineRepository = moveLineRepository;
    this.dateService = dateService;
    this.bankReconciliationQueryService = bankReconciliationQueryService;
  }

  @Override
  public boolean getIsCorrectButtonHidden(BankReconciliation bankReconciliation)
      throws AxelorException {
    String onClosedPeriodClause =
        " AND self.move.period.statusSelect IN ("
            + PeriodRepository.STATUS_CLOSED
            + ","
            + PeriodRepository.STATUS_CLOSURE_IN_PROGRESS
            + ")";
    List<MoveLine> authorizedMoveLinesOnClosedPeriod =
        moveLineRepository
            .all()
            .filter(bankReconciliationQueryService.getRequestMoveLines() + onClosedPeriodClause)
            .bind(bankReconciliationQueryService.getBindRequestMoveLine(bankReconciliation))
            .fetch();
    boolean haveMoveLineOnClosedPeriod = !authorizedMoveLinesOnClosedPeriod.isEmpty();
    return bankReconciliation.getStatusSelect() != BankReconciliationRepository.STATUS_VALIDATED
        || haveMoveLineOnClosedPeriod;
  }

  @Override
  public String getCorrectedLabel(LocalDateTime correctedDateTime, User correctedUser)
      throws AxelorException {
    String space = " ";
    StringBuilder correctedLabel = new StringBuilder();
    correctedLabel.append(I18n.get("Reconciliation corrected at"));
    correctedLabel.append(space);
    correctedLabel.append(correctedDateTime.format(dateService.getDateTimeFormat()));
    correctedLabel.append(space);
    correctedLabel.append(I18n.get("by"));
    correctedLabel.append(space);
    correctedLabel.append(correctedUser.getFullName());
    return correctedLabel.toString();
  }

  @Override
  public void correct(BankReconciliation bankReconciliation, User user) {
    bankReconciliation.setStatusSelect(BankReconciliationRepository.STATUS_UNDER_CORRECTION);
    bankReconciliation.setHasBeenCorrected(true);
    bankReconciliation.setCorrectedDateTime(LocalDateTime.now());
    bankReconciliation.setCorrectedUser(user);
  }
}
