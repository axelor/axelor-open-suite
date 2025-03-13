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
package com.axelor.apps.hr.service;

import com.axelor.apps.account.db.MoveLine;
import com.axelor.apps.account.db.repo.MoveLineRepository;
import com.axelor.apps.account.db.repo.MoveRepository;
import com.axelor.apps.account.service.TaxAccountService;
import com.axelor.apps.account.service.TaxPaymentMoveLineService;
import com.axelor.apps.account.service.moveline.MoveLineCheckService;
import com.axelor.apps.account.service.moveline.MoveLineCreateService;
import com.axelor.apps.account.service.moveline.MoveLineTaxServiceImpl;
import com.axelor.apps.account.service.moveline.MoveLineToolService;
import com.axelor.apps.account.util.TaxAccountToolService;
import com.axelor.apps.base.service.app.AppBaseService;
import com.google.inject.Inject;

public class MoveLineTaxHRServiceImpl extends MoveLineTaxServiceImpl {

  @Inject
  public MoveLineTaxHRServiceImpl(
      MoveLineRepository moveLineRepository,
      TaxPaymentMoveLineService taxPaymentMoveLineService,
      AppBaseService appBaseService,
      MoveLineCreateService moveLineCreateService,
      MoveRepository moveRepository,
      TaxAccountToolService taxAccountToolService,
      MoveLineToolService moveLineToolService,
      TaxAccountService taxAccountService,
      MoveLineCheckService moveLineCheckService) {
    super(
        moveLineRepository,
        taxPaymentMoveLineService,
        appBaseService,
        moveLineCreateService,
        moveRepository,
        taxAccountToolService,
        moveLineToolService,
        taxAccountService,
        moveLineCheckService);
  }

  @Override
  public boolean isMoveLineTaxAccountRequired(MoveLine moveLine, int functionalOriginSelect) {

    if (moveLine.getMove() == null) {
      return super.isMoveLineTaxAccountRequired(moveLine, functionalOriginSelect);
    }

    return moveLine.getMove().getExpense() == null
        && super.isMoveLineTaxAccountRequired(moveLine, functionalOriginSelect);
  }
}
