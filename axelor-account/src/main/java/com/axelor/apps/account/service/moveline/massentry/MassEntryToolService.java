package com.axelor.apps.account.service.moveline.massentry;

import com.axelor.apps.account.db.Move;
import com.axelor.apps.account.db.MoveLine;
import com.axelor.apps.account.db.MoveLineMassEntry;
import java.time.LocalDate;
import java.util.List;

public interface MassEntryToolService {

  void clearMoveLineMassEntryListAndAddNewLines(Move move, Integer temporaryMoveNumber);

  void sortMoveLinesMassEntryByTemporaryNumber(Move move);

  List<MoveLineMassEntry> convertMoveLinesIntoMoveLineMassEntry(
      Move move, List<MoveLine> moveLines, Integer temporaryMoveNumber);

  MoveLineMassEntry convertMoveLineIntoMoveLineMassEntry(
      Move move, MoveLine moveLine, Integer temporaryMoveNumber);

  void checkAndReplaceDateInAllMoveLineMassEntry(
      List<MoveLineMassEntry> moveLineMassEntryList, LocalDate newDate);
}
