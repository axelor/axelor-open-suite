package com.axelor.apps.account.service.move.massentry;

import com.axelor.apps.account.db.Move;

public interface MassEntryMoveUpdateService {

  void updateMassEntryMoveLines(Long moveId, Move generatedMove, int temporaryMoveNumber);

  void resetMassEntryMoveLinesStatus(Long moveId, int errorId);

  void updateMassEntryMoveStatus(Long moveId);
}
