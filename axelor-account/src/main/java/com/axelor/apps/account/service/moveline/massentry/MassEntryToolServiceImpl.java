package com.axelor.apps.account.service.moveline.massentry;

import com.axelor.apps.account.db.Move;
import com.axelor.apps.account.db.MoveLine;
import com.axelor.apps.account.db.MoveLineMassEntry;
import com.axelor.common.ObjectUtils;
import java.lang.invoke.MethodHandles;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MassEntryToolServiceImpl implements MassEntryToolService {

  private final Logger log = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  @Override
  public void clearMoveLineMassEntryListAndAddNewLines(Move move, Integer temporaryMoveNumber) {
    move.getMoveLineMassEntryList()
        .removeIf(
            moveLineMassEntry ->
                Objects.equals(moveLineMassEntry.getTemporaryMoveNumber(), temporaryMoveNumber));
    move.getMoveLineMassEntryList()
        .addAll(
            convertMoveLinesIntoMoveLineMassEntry(
                move, move.getMoveLineList(), temporaryMoveNumber));
    sortMoveLinesMassEntryByTemporaryNumber(move);
  }

  @Override
  public void sortMoveLinesMassEntryByTemporaryNumber(Move move) {
    move.getMoveLineMassEntryList()
        .sort(
            new Comparator<MoveLineMassEntry>() {
              @Override
              public int compare(MoveLineMassEntry o1, MoveLineMassEntry o2) {
                return o1.getTemporaryMoveNumber() - o2.getTemporaryMoveNumber();
              }
            });
  }

  @Override
  public List<MoveLineMassEntry> convertMoveLinesIntoMoveLineMassEntry(
      Move move, List<MoveLine> moveLines, Integer temporaryMoveNumber) {
    List<MoveLineMassEntry> moveLineMassEntryList = new ArrayList<>();
    if (move != null && ObjectUtils.notEmpty(moveLines)) {
      for (MoveLine moveLine : moveLines) {
        moveLineMassEntryList.add(
            this.convertMoveLineIntoMoveLineMassEntry(move, moveLine, temporaryMoveNumber));
      }
    }
    return moveLineMassEntryList;
  }

  @Override
  public MoveLineMassEntry convertMoveLineIntoMoveLineMassEntry(
      Move move, MoveLine moveLine, Integer tempMoveNumber) {
    MoveLineMassEntry moveLineMassEntry = new MoveLineMassEntry();
    if (move != null && moveLine != null) {
      moveLineMassEntry.setInputAction(1);
      moveLineMassEntry.setMovePaymentMode(move.getPaymentMode());
      moveLineMassEntry.setMovePaymentCondition(move.getPaymentCondition());
      moveLineMassEntry.setTemporaryMoveNumber(tempMoveNumber);
      moveLineMassEntry.setMoveMassEntry(move);
      moveLineMassEntry.setMoveDescription(move.getDescription());
      moveLineMassEntry.setMovePartnerBankDetails(move.getPartnerBankDetails());
      moveLineMassEntry.setMoveStatusSelect(move.getStatusSelect());

      moveLineMassEntry.setMove(move);
      moveLineMassEntry.setPartner(moveLine.getPartner());
      moveLineMassEntry.setAccount(moveLine.getAccount());
      moveLineMassEntry.setDate(moveLine.getDate());
      moveLineMassEntry.setDueDate(moveLine.getDueDate());
      moveLineMassEntry.setCutOffStartDate(moveLine.getCutOffStartDate());
      moveLineMassEntry.setCutOffEndDate(moveLine.getCutOffEndDate());
      moveLineMassEntry.setCounter(moveLine.getCounter());
      moveLineMassEntry.setDebit(moveLine.getDebit());
      moveLineMassEntry.setCredit(moveLine.getCredit());
      moveLineMassEntry.setDescription(moveLine.getDescription());
      moveLineMassEntry.setOrigin(moveLine.getOrigin());
      moveLineMassEntry.setOriginDate(moveLine.getOriginDate());
      moveLineMassEntry.setTaxLine(moveLine.getTaxLine());
      moveLineMassEntry.setTaxLineBeforeReverse(moveLine.getTaxLineBeforeReverse());
      moveLineMassEntry.setCurrencyAmount(moveLine.getCurrencyAmount());
      moveLineMassEntry.setCurrencyRate(moveLine.getCurrencyRate());
      moveLineMassEntry.setSourceTaxLine(moveLine.getSourceTaxLine());

      // TODO Add new fields added on MoveLineMassEntry
      // TODO Add nedded fields from MoveLine
    }

    return moveLineMassEntry;
  }

  public void checkAndReplaceDateInAllMoveLineMassEntry(
      List<MoveLineMassEntry> moveLineMassEntryList, LocalDate newDate) {
    // TODO add check for date and propagate this date on other moveLineMassEntry lines
  }
}
