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
package com.axelor.apps.bankpayment.service.move;

import com.axelor.apps.account.db.MoveLineMassEntry;
import com.axelor.apps.account.db.repo.MoveRepository;
import com.axelor.apps.account.service.app.AppAccountService;
import com.axelor.apps.account.service.move.MoveToolService;
import com.axelor.apps.account.service.move.massentry.MassEntryMoveCreateService;
import com.axelor.apps.account.service.move.massentry.MassEntryServiceImpl;
import com.axelor.apps.account.service.move.massentry.MassEntryToolService;
import com.axelor.apps.account.service.move.massentry.MassEntryVerificationService;
import com.axelor.apps.account.service.moveline.massentry.MoveLineMassEntryService;
import com.axelor.apps.account.service.moveline.massentry.MoveLineMassEntryToolService;
import com.google.inject.Inject;

public class MassEntryServiceBankPaymentImpl extends MassEntryServiceImpl {

  @Inject
  public MassEntryServiceBankPaymentImpl(
      MassEntryToolService massEntryToolService,
      MassEntryVerificationService massEntryVerificationService,
      MoveToolService moveToolService,
      MoveLineMassEntryService moveLineMassEntryService,
      MassEntryMoveCreateService massEntryMoveCreateService,
      AppAccountService appAccountService,
      MoveRepository moveRepository,
      MoveLineMassEntryToolService moveLineMassEntryToolService) {
    super(
        massEntryToolService,
        massEntryVerificationService,
        moveToolService,
        moveLineMassEntryService,
        massEntryMoveCreateService,
        appAccountService,
        moveRepository,
        moveLineMassEntryToolService);
  }

  @Override
  protected void setMoveLineMassEntry(MoveLineMassEntry inputLine, MoveLineMassEntry moveLine) {
    inputLine.setInterbankCodeLine(moveLine.getInterbankCodeLine());
    super.setMoveLineMassEntry(inputLine, moveLine);
  }
}
