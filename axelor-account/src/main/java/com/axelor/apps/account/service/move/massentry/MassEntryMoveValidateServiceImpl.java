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
import com.axelor.apps.account.db.repo.MoveRepository;
import com.axelor.apps.base.service.exception.TraceBackService;
import com.axelor.common.ObjectUtils;
import com.axelor.db.JPA;
import com.google.inject.Inject;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class MassEntryMoveValidateServiceImpl implements MassEntryMoveValidateService {

  protected MoveRepository moveRepository;
  protected MassEntryToolService massEntryToolService;
  protected MassEntryMoveCreateService massEntryMoveCreateService;
  protected MassEntryMoveUpdateService massEntryMoveUpdateService;

  @Inject
  public MassEntryMoveValidateServiceImpl(
      MoveRepository moveRepository,
      MassEntryToolService massEntryToolService,
      MassEntryMoveCreateService massEntryMoveCreateService,
      MassEntryMoveUpdateService massEntryMoveUpdateService) {
    this.moveRepository = moveRepository;
    this.massEntryToolService = massEntryToolService;
    this.massEntryMoveCreateService = massEntryMoveCreateService;
    this.massEntryMoveUpdateService = massEntryMoveUpdateService;
  }

  @Override
  public Map<List<Long>, String> validateMassEntryMove(Move move) {
    Map<List<Long>, String> resultMap = new HashMap<>();
    String errors = "";
    List<Long> moveIdList = new ArrayList<>();
    move = moveRepository.find(move.getId());
    Long massEntryMove = move.getId();

    if (massEntryToolService.verifyJournalAuthorizeNewMove(
            move.getMoveLineMassEntryList(), move.getJournal())
        && ObjectUtils.notEmpty(move.getMoveLineMassEntryList())) {
      List<Integer> uniqueIdList =
          move.getMoveLineMassEntryList().stream()
              .filter(ml -> !ml.getIsGenerated())
              .map(MoveLineMassEntry::getTemporaryMoveNumber)
              .distinct()
              .sorted()
              .collect(Collectors.toList());
      for (Integer x : uniqueIdList) {
        Move element = massEntryMoveCreateService.createMoveFromMassEntryList(move, x);
        Map.Entry<Move, Integer> moveMap =
            this.validateMove(element, massEntryMove).entrySet().iterator().next();

        if (moveMap.getKey() != null) {
          moveIdList.add(moveMap.getKey().getId());
        }

        if (moveMap.getValue() != null) {
          if (!errors.isEmpty()) {
            errors = errors.concat(", ");
          }
          errors = errors.concat(moveMap.getValue().toString());
        }

        JPA.clear();
      }
    }
    massEntryMoveUpdateService.updateMassEntryMoveStatus(move.getId());
    resultMap.put(moveIdList, errors);

    return resultMap;
  }

  protected Map<Move, Integer> validateMove(Move move, Long massEntryMoveId) {
    Map<Move, Integer> resultMap = new HashMap<>();
    int moveTemporaryMoveNumber = Integer.parseInt(move.getReference());
    try {
      move.setReference(null);

      Move generatedMove = massEntryMoveCreateService.generateMassEntryMove(move);
      massEntryMoveUpdateService.updateMassEntryMoveLines(
          massEntryMoveId, generatedMove, moveTemporaryMoveNumber);
      resultMap.put(generatedMove, null);

    } catch (Exception e) {
      TraceBackService.trace(e);
      resultMap.put(null, moveTemporaryMoveNumber);
      massEntryMoveUpdateService.resetMassEntryMoveLinesStatus(
          massEntryMoveId, moveTemporaryMoveNumber);
    }

    return resultMap;
  }
}
