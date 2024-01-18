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
package com.axelor.apps.bankpayment.service.move;

import com.axelor.apps.account.db.MoveLine;
import com.axelor.apps.account.db.repo.MoveLineRepository;
import com.axelor.apps.account.db.repo.MoveRepository;
import com.axelor.apps.account.service.AccountCustomerService;
import com.axelor.apps.account.service.AccountingSituationService;
import com.axelor.apps.account.service.ReconcileService;
import com.axelor.apps.account.service.move.MoveRemoveServiceImpl;
import com.axelor.apps.bankpayment.db.BankStatementLineAFB120;
import com.axelor.apps.bankpayment.db.repo.BankStatementLineAFB120Repository;
import com.axelor.apps.bankpayment.exception.BankPaymentExceptionMessage;
import com.axelor.apps.bankpayment.service.app.AppBankPaymentService;
import com.axelor.apps.base.AxelorException;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.axelor.utils.service.ArchivingToolService;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.math.BigDecimal;
import java.util.List;

public class MoveRemoveServiceBankPaymentImpl extends MoveRemoveServiceImpl {

  protected BankStatementLineAFB120Repository bankStatementLineAFB120Repository;

  @Inject
  public MoveRemoveServiceBankPaymentImpl(
      MoveRepository moveRepo,
      MoveLineRepository moveLineRepo,
      ArchivingToolService archivingToolService,
      ReconcileService reconcileService,
      AccountingSituationService accountingSituationService,
      AccountCustomerService accountCustomerService,
      BankStatementLineAFB120Repository bankStatementLineAFB120Repository) {
    super(
        moveRepo,
        moveLineRepo,
        archivingToolService,
        reconcileService,
        accountingSituationService,
        accountCustomerService);
    this.bankStatementLineAFB120Repository = bankStatementLineAFB120Repository;
  }

  @Override
  public String checkMoveLineBeforeRemove(MoveLine moveLine) throws AxelorException {
    removeMoveLineFromBankStatements(moveLine);
    String errorMessage = super.checkMoveLineBeforeRemove(moveLine);

    if (Beans.get(AppBankPaymentService.class).isApp("bank-payment")
        && moveLine.getBankReconciledAmount().compareTo(BigDecimal.ZERO) > 0) {
      errorMessage +=
          String.format(
              I18n.get(
                  BankPaymentExceptionMessage
                      .MOVE_LINE_ARCHIVE_NOT_OK_BECAUSE_OF_BANK_RECONCILIATION_AMOUNT),
              moveLine.getName());
    }
    return errorMessage;
  }

  @Transactional
  protected void removeMoveLineFromBankStatements(MoveLine moveLine) {
    List<BankStatementLineAFB120> bankStatementLineAFB120List =
        bankStatementLineAFB120Repository.all().filter("self.moveLine = ?1", moveLine).fetch();
    for (BankStatementLineAFB120 bankStatementLineAFB120 : bankStatementLineAFB120List) {
      bankStatementLineAFB120.setMoveLine(null);
      bankStatementLineAFB120Repository.save(bankStatementLineAFB120);
    }
  }
}
