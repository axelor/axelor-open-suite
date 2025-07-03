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
package com.axelor.apps.account.service.move.massentry;

import com.axelor.apps.account.db.Move;
import com.axelor.apps.account.db.MoveLineMassEntry;
import com.axelor.apps.account.db.repo.MoveLineMassEntryRepository;
import com.axelor.apps.account.db.repo.MoveRepository;
import com.axelor.common.ObjectUtils;
import com.axelor.db.Query;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class MassEntryMoveUpdateServiceImpl implements MassEntryMoveUpdateService {

  protected MoveLineMassEntryRepository moveLineMassEntryRepository;

  @Inject
  public MassEntryMoveUpdateServiceImpl(MoveLineMassEntryRepository moveLineMassEntryRepository) {
    this.moveLineMassEntryRepository = moveLineMassEntryRepository;
  }

  @Transactional(rollbackOn = {Exception.class})
  public void updateMassEntryMoveLines(Long moveId, Move generatedMove, int temporaryMoveNumber) {
    List<Long> moveLineIds = this.getMoveLineMassEntryIds(temporaryMoveNumber, moveId);

    Map<String, Object> updateMap = new HashMap<>();
    updateMap.put("moveStatusSelect", generatedMove.getStatusSelect());
    updateMap.put("temporaryMoveNumber", Math.toIntExact(generatedMove.getId()));
    updateMap.put("isGenerated", true);

    Query.of(MoveLineMassEntry.class).filter("self.id in :ids", moveLineIds).update(updateMap);
  }

  @Transactional(rollbackOn = {Exception.class})
  public void resetMassEntryMoveLinesStatus(Long moveId, int errorId) {
    List<Long> moveLineIds = this.getMoveLineMassEntryIds(errorId, moveId);

    Query.of(MoveLineMassEntry.class)
        .filter("self.id in :ids", moveLineIds)
        .update("moveStatusSelect", MoveRepository.STATUS_NEW);
  }

  @Transactional(rollbackOn = {Exception.class})
  public void updateMassEntryMoveStatus(Long moveId) {
    List<Long> moveLineIds =
        moveLineMassEntryRepository
            .all()
            .filter("self.moveMassEntry = ? AND self.isGenerated IS FALSE", moveId)
            .order("id")
            .select("id")
            .fetch(0, 0)
            .stream()
            .map(m -> (Long) m.get("id"))
            .collect(Collectors.toList());
    if (ObjectUtils.isEmpty(moveLineIds)) {
      Query.of(Move.class)
          .filter("self.id = :id", moveId)
          .update("massEntryStatusSelect", MoveRepository.MASS_ENTRY_STATUS_VALIDATED);
    }
  }

  protected List<Long> getMoveLineMassEntryIds(int temporaryMoveNumber, Long moveId) {
    return moveLineMassEntryRepository
        .all()
        .filter(
            "self.temporaryMoveNumber = ? AND self.moveMassEntry = ?", temporaryMoveNumber, moveId)
        .order("id")
        .select("id")
        .fetch(0, 0)
        .stream()
        .map(m -> (Long) m.get("id"))
        .collect(Collectors.toList());
  }
}
