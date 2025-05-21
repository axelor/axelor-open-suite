package com.axelor.apps.account.service.move.massentry;

import com.axelor.apps.account.db.Move;

public interface MassEntryMoveUpdateService {

  void updateMassEntryMoveLines(Move move, Move generatedMove, int temporaryMoveNumber);

  void resetMassEntryMoveLinesStatus(Move move, int errorId);

  void updateMassEntryMoveStatus(Move move);
}
