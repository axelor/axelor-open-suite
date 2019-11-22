/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2019 Axelor (<http://axelor.com>).
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
package com.axelor.apps.bankpayment.service.move;

import com.axelor.apps.account.db.MoveLine;
import com.axelor.apps.account.db.repo.MoveLineRepository;
import com.axelor.apps.account.db.repo.MoveRepository;
import com.axelor.apps.account.service.AccountCustomerService;
import com.axelor.apps.account.service.AccountingSituationService;
import com.axelor.apps.account.service.ReconcileService;
import com.axelor.apps.account.service.move.MoveRemoveService;
import com.axelor.apps.bankpayment.exception.IExceptionMessage;
import com.axelor.apps.tool.service.ArchivingToolService;
import com.axelor.exception.AxelorException;
import com.axelor.i18n.I18n;
import com.google.inject.Inject;
import java.math.BigDecimal;

public class MoveRemoveServiceBankPaymentImpl extends MoveRemoveService {

  @Inject
  public MoveRemoveServiceBankPaymentImpl(
      MoveRepository moveRepo,
      MoveLineRepository moveLineRepo,
      ArchivingToolService archivingToolService,
      ReconcileService reconcileService,
      AccountingSituationService accountingSituationService,
      AccountCustomerService accountCustomerService) {
    super(
        moveRepo,
        moveLineRepo,
        archivingToolService,
        reconcileService,
        accountingSituationService,
        accountCustomerService);
  }

  @Override
  protected String checkDaybookMoveLine(MoveLine moveLine) throws AxelorException {
    String errorMessage = super.checkDaybookMoveLine(moveLine);

    if (moveLine.getBankReconciledAmount().compareTo(BigDecimal.ZERO) > 0) {
      errorMessage +=
          String.format(
              I18n.get(
                  IExceptionMessage.MOVE_LINE_ARCHIVE_NOT_OK_BECAUSE_OF_BANK_RECONCILIATION_AMOUNT),
              moveLine.getName());
    }
    return errorMessage;
  }
}
