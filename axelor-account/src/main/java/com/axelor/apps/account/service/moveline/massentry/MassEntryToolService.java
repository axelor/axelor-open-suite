package com.axelor.apps.account.service.moveline.massentry;

import com.axelor.apps.account.db.Journal;
import com.axelor.apps.account.db.Move;
import com.axelor.apps.account.db.MoveLine;
import com.axelor.apps.account.db.MoveLineMassEntry;
import java.util.List;

public interface MassEntryToolService {

  void clearMoveLineMassEntryListAndAddNewLines(Move move, Integer temporaryMoveNumber);

  void sortMoveLinesMassEntryByTemporaryNumber(Move move);

  List<MoveLineMassEntry> convertMoveLinesIntoMoveLineMassEntry(
      Move move, List<MoveLine> moveLines, Integer temporaryMoveNumber);

  MoveLineMassEntry convertMoveLineIntoMoveLineMassEntry(
      Move move, MoveLine moveLine, Integer temporaryMoveNumber);

  List<MoveLineMassEntry> getEditedMoveLineMassEntry(List<MoveLineMassEntry> moveLineMassEntryList);

  void setPaymentModeOnMoveLineMassEntry(
      MoveLineMassEntry moveLineMassEntry, Integer technicalTypeSelect);

  List<Move> createMoveListFromMassEntryList(Move move);

  Integer getMaxTemporaryMoveNumber(List<MoveLineMassEntry> moveLineMassEntryList);

  void setNewStatusSelectOnMassEntryLines(Move move, Integer newStatusSelect);

  boolean verifyJournalAuthorizeNewMove(
      List<MoveLineMassEntry> moveLineMassEntryList, Journal journal);
}
