package com.axelor.apps.account.service.move.massentry;

import com.axelor.apps.account.db.Move;
import com.axelor.apps.account.db.MoveLineMassEntry;
import com.axelor.apps.account.db.repo.MoveRepository;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.util.Objects;

public class MassEntryMoveUpdateServiceImpl implements MassEntryMoveUpdateService {

  protected MoveRepository moveRepository;

  @Inject
  public MassEntryMoveUpdateServiceImpl(MoveRepository moveRepository) {
    this.moveRepository = moveRepository;
  }

  @Transactional(rollbackOn = {Exception.class})
  public void updateMassEntryMoveLines(Move move, Move generatedMove, int temporaryMoveNumber) {
    move = moveRepository.find(move.getId());
    move.getMoveLineMassEntryList().stream()
        .filter(ml -> Objects.equals(temporaryMoveNumber, ml.getTemporaryMoveNumber()))
        .forEach(
            ml -> {
              ml.setMoveStatusSelect(generatedMove.getStatusSelect());
              ml.setTemporaryMoveNumber(Math.toIntExact(generatedMove.getId()));
              ml.setIsGenerated(true);
            });

    moveRepository.save(move);
  }

  @Transactional(rollbackOn = {Exception.class})
  public void resetMassEntryMoveLinesStatus(Move move, int errorId) {
    move = moveRepository.find(move.getId());
    move.getMoveLineMassEntryList().stream()
        .filter(ml -> Objects.equals(errorId, ml.getTemporaryMoveNumber()))
        .forEach(
            ml -> {
              ml.setMoveStatusSelect(MoveRepository.STATUS_NEW);
            });

    moveRepository.save(move);
  }

  @Transactional(rollbackOn = {Exception.class})
  public void updateMassEntryMoveStatus(Move move) {
    move = moveRepository.find(move.getId());
    if (move.getMoveLineMassEntryList().stream().allMatch(MoveLineMassEntry::getIsGenerated)) {
      move.setMassEntryStatusSelect(MoveRepository.MASS_ENTRY_STATUS_VALIDATED);
      moveRepository.save(move);
    }
  }
}
