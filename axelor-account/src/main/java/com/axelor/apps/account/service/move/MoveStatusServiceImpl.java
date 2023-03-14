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
package com.axelor.apps.account.service.move;

import com.axelor.apps.account.db.Move;
import com.axelor.apps.account.db.MoveLine;
import com.axelor.apps.account.db.repo.MoveRepository;
import com.axelor.apps.account.service.moveline.MoveLineService;
import com.google.inject.Inject;

public class MoveStatusServiceImpl implements MoveStatusService {
  protected MoveLineService moveLineService;

  @Inject
  public MoveStatusServiceImpl(MoveLineService moveLineService) {
    this.moveLineService = moveLineService;
  }

  @Override
  public void update(Move move, int statusSelect) {
    if (statusSelect == MoveRepository.STATUS_SIMULATED
        || statusSelect == MoveRepository.STATUS_ACCOUNTED
        || statusSelect == MoveRepository.STATUS_DAYBOOK) {
      this.applyCutOffDates(move);
    }
    move.setStatusSelect(statusSelect);
  }

  @Override
  public void applyCutOffDates(Move move) {
    for (MoveLine moveLine : move.getMoveLineList()) {
      if (moveLine.getAccount().getManageCutOffPeriod()
          && moveLine.getAccount().getHasAutomaticApplicationAccountingDate()
          && moveLine.getCutOffStartDate() == null
          && moveLine.getCutOffEndDate() == null) {
        moveLine.setIsCutOffGenerated(true);
        moveLineService.applyCutOffDates(moveLine, move, move.getDate(), move.getDate());
      }
    }
  }
}
