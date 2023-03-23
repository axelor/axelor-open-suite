package com.axelor.apps.account.service.move.massentry;

import com.axelor.apps.account.db.Journal;
import com.axelor.apps.account.db.Move;
import com.axelor.apps.account.db.MoveLine;
import com.axelor.apps.account.db.MoveLineMassEntry;
import java.util.List;

public interface MassEntryToolService {

  void clearMoveLineMassEntryListAndAddNewLines(
      Move parentMove, Move childMove, Integer temporaryMoveNumber);

  void sortMoveLinesMassEntryByTemporaryNumber(Move move);

  List<MoveLineMassEntry> convertMoveLinesIntoMoveLineMassEntry(
      Move move, List<MoveLine> moveLineList, Integer temporaryMoveNumber);

  MoveLineMassEntry convertMoveLineIntoMoveLineMassEntry(
      Move move, MoveLine moveLine, Integer temporaryMoveNumber);

  List<MoveLineMassEntry> getEditedMoveLineMassEntry(List<MoveLineMassEntry> moveLineList);

  List<Move> createMoveListFromMassEntryList(Move parentMove);

  Move createMoveFromMassEntryList(Move parentMove, int temporaryMoveNumber);

  Integer getMaxTemporaryMoveNumber(List<MoveLineMassEntry> moveLineList);

  void setNewStatusSelectOnMassEntryLines(Move move, Integer newStatusSelect);

  boolean verifyJournalAuthorizeNewMove(List<MoveLineMassEntry> moveLineList, Journal journal);
}
