package com.axelor.apps.account.service.move.massentry;

import com.axelor.apps.account.db.Journal;
import com.axelor.apps.account.db.Move;
import com.axelor.apps.account.db.MoveLine;
import com.axelor.apps.account.db.MoveLineMassEntry;
import com.axelor.apps.account.db.repo.MoveRepository;
import com.axelor.apps.base.db.repo.YearRepository;
import com.axelor.apps.base.service.PeriodService;
import com.axelor.common.ObjectUtils;
import com.google.inject.Inject;
import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MassEntryToolServiceImpl implements MassEntryToolService {

  private final Logger log = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  protected PeriodService periodService;

  @Inject
  public MassEntryToolServiceImpl(PeriodService periodService) {
    this.periodService = periodService;
  }

  @Override
  public void clearMoveLineMassEntryListAndAddNewLines(Move move, Integer temporaryMoveNumber) {
    List<MoveLineMassEntry> moveLineMassEntryList =
        new ArrayList<>(move.getMoveLineMassEntryList());
    for (MoveLineMassEntry moveLineMassEntry : moveLineMassEntryList) {
      if (Objects.equals(moveLineMassEntry.getTemporaryMoveNumber(), temporaryMoveNumber)) {
        move.getMoveLineMassEntryList().remove(moveLineMassEntry);
      }
    }

    moveLineMassEntryList =
        convertMoveLinesIntoMoveLineMassEntry(move, move.getMoveLineList(), temporaryMoveNumber);
    if (moveLineMassEntryList.size() > 0) {
      for (MoveLineMassEntry moveLineMassEntry : moveLineMassEntryList) {
        move.getMoveLineMassEntryList().add(moveLineMassEntry);
      }
    }
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
      moveLineMassEntry.setCounter(moveLine.getCounter());
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

  @Override
  public List<MoveLineMassEntry> getEditedMoveLineMassEntry(
      List<MoveLineMassEntry> moveLineMassEntryList) {
    List<MoveLineMassEntry> resultList = new ArrayList<>();

    for (MoveLineMassEntry moveLineMassEntry : moveLineMassEntryList) {
      if (moveLineMassEntry.getIsEdited()) {
        resultList.add(moveLineMassEntry);
      }
    }
    return resultList;
  }

  @Override
  public List<Move> createMoveListFromMassEntryList(Move move) {
    int numberOfDifferentMovesToCheck = 0;
    List<Move> moveList = new ArrayList<>();
    Move moveToCheck;
    boolean firstMove = true;

    numberOfDifferentMovesToCheck = this.getMaxTemporaryMoveNumber(move.getMoveLineMassEntryList());

    for (int i = 1; i <= numberOfDifferentMovesToCheck; i++) {
      moveToCheck = new Move();
      moveToCheck.setJournal(move.getJournal());
      moveToCheck.setCompany(move.getCompany());
      moveToCheck.setCurrency(move.getCurrency());
      moveToCheck.setCompanyBankDetails(move.getCompanyBankDetails());

      for (MoveLineMassEntry moveLineMassEntry : move.getMoveLineMassEntryList()) {
        if (moveLineMassEntry.getTemporaryMoveNumber() == i) {
          if (firstMove) {
            if (moveLineMassEntry.getDate() != null && move.getCompany() != null) {
              moveToCheck.setPeriod(
                  periodService.getPeriod(
                      moveLineMassEntry.getDate(), move.getCompany(), YearRepository.TYPE_FISCAL));
            }
            moveToCheck.setReference(moveLineMassEntry.getTemporaryMoveNumber().toString());
            moveToCheck.setDate(moveLineMassEntry.getDate());
            moveToCheck.setPartner(moveLineMassEntry.getPartner());
            moveToCheck.setOrigin(moveLineMassEntry.getOrigin());
            moveToCheck.setStatusSelect(moveLineMassEntry.getMoveStatusSelect());
            moveToCheck.setOriginDate(moveLineMassEntry.getOriginDate());
            moveToCheck.setDescription(moveLineMassEntry.getDescription());
            firstMove = false;
          }
          moveLineMassEntry.setMove(moveToCheck);
          moveLineMassEntry.setFieldsErrorList(null);
          moveToCheck.addMoveLineListItem(moveLineMassEntry);
          moveToCheck.addMoveLineMassEntryListItem(moveLineMassEntry);
        }
      }
      moveList.add(moveToCheck);
    }

    return moveList;
  }

  @Override
  public Integer getMaxTemporaryMoveNumber(List<MoveLineMassEntry> moveLineMassEntryList) {
    int max = 0;

    for (MoveLineMassEntry moveLine : moveLineMassEntryList) {
      if (moveLine.getTemporaryMoveNumber() > max) {
        max = moveLine.getTemporaryMoveNumber();
      }
    }

    return max;
  }

  @Override
  public void setNewStatusSelectOnMassEntryLines(Move move, Integer newStatusSelect) {
    if (ObjectUtils.notEmpty(move.getMoveLineMassEntryList())) {
      for (MoveLineMassEntry moveLineMassEntry : move.getMoveLineMassEntryList()) {
        moveLineMassEntry.setMoveStatusSelect(newStatusSelect);
      }
    }
  }

  @Override
  public boolean verifyJournalAuthorizeNewMove(
      List<MoveLineMassEntry> moveLineMassEntryList, Journal journal) {
    if (!journal.getAllowAccountingNewOnMassEntry()) {
      for (MoveLineMassEntry moveLineMassEntry : moveLineMassEntryList) {
        if (moveLineMassEntry.getMoveStatusSelect().equals(MoveRepository.STATUS_NEW)) {
          return false;
        }
      }
    }
    return true;
  }
}
