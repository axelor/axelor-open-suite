package com.axelor.apps.account.service.move.massentry;

import com.axelor.apps.account.db.Move;
import com.axelor.apps.account.db.MoveLineMassEntry;
import com.axelor.apps.base.AxelorException;
import java.util.List;

public interface MassEntryMoveCreateService {

  Move generateMassEntryMove(Move move) throws AxelorException;

  Move createMassEntryMove(Move move) throws AxelorException;

  void accoutingMassEntryMove(Move newMove, int statusSelect, boolean authorizeSimulatedMove)
      throws AxelorException;

  List<Move> createMoveListFromMassEntryList(Move parentMove);

  Move createMoveFromMassEntryList(Move parentMove, int temporaryMoveNumber);

  Integer getMaxTemporaryMoveNumber(List<MoveLineMassEntry> moveLineList);
}
