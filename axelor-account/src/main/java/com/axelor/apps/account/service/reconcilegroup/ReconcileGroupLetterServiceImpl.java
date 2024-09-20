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
package com.axelor.apps.account.service.reconcilegroup;

import com.axelor.apps.account.db.MoveLine;
import com.axelor.apps.account.db.ReconcileGroup;
import com.axelor.apps.account.db.repo.MoveLineRepository;
import com.axelor.apps.account.service.moveline.MoveLineService;
import com.axelor.apps.base.AxelorException;
import com.google.inject.Inject;
import java.util.List;

public class ReconcileGroupLetterServiceImpl implements ReconcileGroupLetterService {

  protected MoveLineRepository moveLineRepository;
  protected MoveLineService moveLineService;

  @Inject
  public ReconcileGroupLetterServiceImpl(
      MoveLineRepository moveLineRepository, MoveLineService moveLineService) {
    this.moveLineRepository = moveLineRepository;
    this.moveLineService = moveLineService;
  }

  @Override
  public void letter(ReconcileGroup reconcileGroup) throws AxelorException {

    List<MoveLine> moveLines =
        moveLineRepository
            .all()
            .filter("self.reconcileGroup = :reconcileGroup")
            .bind("reconcileGroup", reconcileGroup)
            .fetch();
    moveLineService.reconcileMoveLines(moveLines);
  }
}
