/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2022 Axelor (<http://axelor.com>).
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
import com.axelor.apps.account.db.repo.MoveRepository;
import com.axelor.exception.AxelorException;
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
  @Transactional(rollbackOn = {AxelorException.class, RuntimeException.class})
  public void simulate(Move move) throws AxelorException {
    moveValidateService.checkPreconditions(move);
    moveValidateService.freezeFieldsOnMoveLines(move);
    moveValidateService.completeMoveLines(move);
    move.setStatusSelect(MoveRepository.STATUS_SIMULATED);
    moveRepository.save(move);
  }
}
