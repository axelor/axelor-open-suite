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
package com.axelor.apps.account.service.move;

import com.axelor.apps.account.db.Move;
import com.axelor.apps.account.db.repo.MoveRepository;
import com.axelor.apps.base.AxelorException;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.util.List;

public class MoveSimulateServiceImpl implements MoveSimulateService {

  protected MoveValidateService moveValidateService;
  protected MoveRepository moveRepository;

  @Inject
  public MoveSimulateServiceImpl(
      MoveValidateService moveValidateService, MoveRepository moveRepository) {
    this.moveValidateService = moveValidateService;
    this.moveRepository = moveRepository;
  }

  @Transactional(rollbackOn = {Exception.class})
  public void simulateMultiple(List<? extends Move> moveList) throws AxelorException {
    if (moveList == null) {
      return;
    }

    for (Move move : moveList) {
      this.simulate(move);
    }
  }

  @Override
  @Transactional(rollbackOn = {Exception.class})
  public void simulate(Move move) throws AxelorException {
    moveValidateService.checkPreconditions(move);
    moveValidateService.freezeFieldsOnMoveLines(move);
    moveValidateService.completeMoveLines(move);
    move.setStatusSelect(MoveRepository.STATUS_SIMULATED);
    moveRepository.save(move);
  }
}
