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
        Move element = massEntryMoveCreateService.createMoveFromMassEntryList(massEntryMove, x);
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
