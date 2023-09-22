/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2005-2023 Axelor (<http://axelor.com>).
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
package com.axelor.apps.budget.service.move;

import com.axelor.apps.account.db.Move;
import com.axelor.apps.account.db.repo.MoveRepository;
import com.axelor.apps.account.service.PeriodServiceAccount;
import com.axelor.apps.account.service.app.AppAccountService;
import com.axelor.apps.account.service.move.MoveCounterPartService;
import com.axelor.apps.account.service.move.MoveCutOffService;
import com.axelor.apps.account.service.move.MoveInvoiceTermService;
import com.axelor.apps.account.service.move.MoveLineControlService;
import com.axelor.apps.account.service.move.MoveToolService;
import com.axelor.apps.account.service.move.attributes.MoveAttrsService;
import com.axelor.apps.account.service.move.control.MoveCheckService;
import com.axelor.apps.account.service.move.massentry.MassEntryService;
import com.axelor.apps.account.service.move.massentry.MassEntryVerificationService;
import com.axelor.apps.account.service.move.record.MoveDefaultService;
import com.axelor.apps.account.service.move.record.MoveGroupServiceImpl;
import com.axelor.apps.account.service.move.record.MoveRecordSetService;
import com.axelor.apps.account.service.move.record.MoveRecordUpdateService;
import com.axelor.apps.account.service.moveline.MoveLineTaxService;
import com.axelor.apps.account.service.moveline.massentry.MoveLineMassEntryRecordService;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.service.PeriodService;
import com.axelor.inject.Beans;
import com.google.inject.Inject;

public class MoveRecordBudgetServiceImpl extends MoveGroupServiceImpl {

  protected MoveBudgetService moveBudgetService;

  @Inject
  public MoveRecordBudgetServiceImpl(
      MoveDefaultService moveDefaultService,
      MoveAttrsService moveAttrsService,
      PeriodServiceAccount periodAccountService,
      MoveCheckService moveCheckService,
      MoveCutOffService moveCutOffService,
      MoveRecordUpdateService moveRecordUpdateService,
      MoveRecordSetService moveRecordSetService,
      MoveToolService moveToolService,
      MoveInvoiceTermService moveInvoiceTermService,
      MoveCounterPartService moveCounterPartService,
      MoveLineControlService moveLineControlService,
      MoveLineTaxService moveLineTaxService,
      PeriodService periodService,
      MoveRepository moveRepository,
      AppAccountService appAccountService,
      MassEntryService massEntryService,
      MassEntryVerificationService massEntryVerificationService,
      MoveLineMassEntryRecordService moveLineMassEntryRecordService,
      MoveBudgetService moveBudgetService) {
    super(
        moveDefaultService,
        moveAttrsService,
        periodAccountService,
        moveCheckService,
        moveCutOffService,
        moveRecordUpdateService,
        moveRecordSetService,
        moveToolService,
        moveInvoiceTermService,
        moveCounterPartService,
        moveLineControlService,
        moveLineTaxService,
        periodService,
        moveRepository,
        appAccountService,
        massEntryService,
        massEntryVerificationService,
        moveLineMassEntryRecordService);
    this.moveBudgetService = moveBudgetService;
  }

  @Override
  public void checkBeforeSave(Move move) throws AxelorException {

    super.checkBeforeSave(move);

    Beans.get(MoveBudgetService.class).getBudgetExceedAlert(move);
  }
}
