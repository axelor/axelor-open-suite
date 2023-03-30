package com.axelor.apps.account.service.move.massentry;

import com.axelor.apps.account.db.Journal;
import com.axelor.apps.account.db.Move;
import com.axelor.apps.account.db.MoveLine;
import com.axelor.apps.account.db.MoveLineMassEntry;
import com.axelor.apps.account.db.repo.MoveLineMassEntryRepository;
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
  public void clearMoveLineMassEntryListAndAddNewLines(
      Move parentMove, Move childMove, Integer temporaryMoveNumber) {
    List<MoveLineMassEntry> moveLineList = new ArrayList<>(parentMove.getMoveLineMassEntryList());
    for (MoveLineMassEntry moveLine : moveLineList) {
      if (Objects.equals(moveLine.getTemporaryMoveNumber(), temporaryMoveNumber)) {
        parentMove.removeMoveLineMassEntryListItem(moveLine);
      }
    }
    this.sortMoveLinesMassEntryByTemporaryNumber(parentMove);

    moveLineList =
        convertMoveLinesIntoMoveLineMassEntry(
            childMove, childMove.getMoveLineList(), temporaryMoveNumber);
    if (moveLineList.size() > 0) {
      for (MoveLineMassEntry moveLine : moveLineList) {
        parentMove.addMoveLineMassEntryListItem(moveLine);
      }
    }
  }

  @Override
  public void sortMoveLinesMassEntryByTemporaryNumber(Move move) {
    if (ObjectUtils.notEmpty(move.getMoveLineMassEntryList())) {
      move.getMoveLineMassEntryList()
          .sort(
              new Comparator<MoveLineMassEntry>() {
                @Override
                public int compare(MoveLineMassEntry o1, MoveLineMassEntry o2) {
                  return o1.getTemporaryMoveNumber() - o2.getTemporaryMoveNumber();
                }
              });
    }
  }

  @Override
  public List<MoveLineMassEntry> convertMoveLinesIntoMoveLineMassEntry(
      Move move, List<MoveLine> moveLineList, Integer temporaryMoveNumber) {
    List<MoveLineMassEntry> massEntryList = new ArrayList<>();
    if (move != null && ObjectUtils.notEmpty(moveLineList)) {
      for (MoveLine moveLine : moveLineList) {
        massEntryList.add(
            this.convertMoveLineIntoMoveLineMassEntry(move, moveLine, temporaryMoveNumber));
      }
    }
    return massEntryList;
  }

  @Override
  public MoveLineMassEntry convertMoveLineIntoMoveLineMassEntry(
      Move move, MoveLine moveLine, Integer tempMoveNumber) {
    MoveLineMassEntry moveLineResult = new MoveLineMassEntry();
    if (move != null && moveLine != null) {
      moveLineResult.setInputAction(MoveLineMassEntryRepository.MASS_ENTRY_INPUT_ACTION_LINE);
      moveLineResult.setMovePaymentMode(move.getPaymentMode());
      moveLineResult.setMovePaymentCondition(move.getPaymentCondition());
      moveLineResult.setTemporaryMoveNumber(tempMoveNumber);
      moveLineResult.setMoveDescription(move.getDescription());
      moveLineResult.setMovePartnerBankDetails(move.getPartnerBankDetails());
      moveLineResult.setMoveStatusSelect(move.getStatusSelect());

      moveLineResult.setPartner(moveLine.getPartner());
      moveLineResult.setAccount(moveLine.getAccount());
      moveLineResult.setDate(moveLine.getDate());
      moveLineResult.setDueDate(moveLine.getDueDate());
      moveLineResult.setCutOffStartDate(moveLine.getCutOffStartDate());
      moveLineResult.setCutOffEndDate(moveLine.getCutOffEndDate());
      moveLineResult.setCounter(moveLine.getCounter());
      moveLineResult.setDebit(moveLine.getDebit());
      moveLineResult.setCredit(moveLine.getCredit());
      moveLineResult.setDescription(moveLine.getDescription());
      moveLineResult.setOrigin(moveLine.getOrigin());
      moveLineResult.setOriginDate(moveLine.getOriginDate());
      moveLineResult.setTaxLine(moveLine.getTaxLine());
      moveLineResult.setTaxLineBeforeReverse(moveLine.getTaxLineBeforeReverse());
      moveLineResult.setCurrencyAmount(moveLine.getCurrencyAmount());
      moveLineResult.setCurrencyRate(moveLine.getCurrencyRate());
      moveLineResult.setSourceTaxLine(moveLine.getSourceTaxLine());
      moveLineResult.setVatSystemSelect(moveLine.getVatSystemSelect());
      moveLineResult.setAnalyticDistributionTemplate(moveLine.getAnalyticDistributionTemplate());
      moveLineResult.setAxis1AnalyticAccount(moveLine.getAxis1AnalyticAccount());
      moveLineResult.setAxis2AnalyticAccount(moveLine.getAxis2AnalyticAccount());
      moveLineResult.setAxis3AnalyticAccount(moveLine.getAxis3AnalyticAccount());
      moveLineResult.setAxis4AnalyticAccount(moveLine.getAxis4AnalyticAccount());
      moveLineResult.setAxis5AnalyticAccount(moveLine.getAxis5AnalyticAccount());
      moveLineResult.setAnalyticMoveLineList(moveLine.getAnalyticMoveLineList());
    }

    return moveLineResult;
  }

  @Override
  public List<MoveLineMassEntry> getEditedMoveLineMassEntry(List<MoveLineMassEntry> moveLineList) {
    List<MoveLineMassEntry> resultList = new ArrayList<>();

    for (MoveLineMassEntry moveLine : moveLineList) {
      if (moveLine.getIsEdited() > MoveLineMassEntryRepository.MASS_ENTRY_IS_EDITED_NULL) {
        resultList.add(moveLine);
      }
    }
    return resultList;
  }

  @Override
  public List<Move> createMoveListFromMassEntryList(Move parentMove) {
    List<Move> moveList = new ArrayList<>();
    Move moveToAdd;

    for (int i = 1;
        i <= this.getMaxTemporaryMoveNumber(parentMove.getMoveLineMassEntryList());
        i++) {
      moveToAdd = this.createMoveFromMassEntryList(parentMove, i);
      moveList.add(moveToAdd);
    }

    return moveList;
  }

  @Override
  public Move createMoveFromMassEntryList(Move parentMove, int temporaryMoveNumber) {
    Move moveResult = new Move();
    boolean firstMoveLine = true;

    moveResult.setJournal(parentMove.getJournal());
    moveResult.setFunctionalOriginSelect(parentMove.getFunctionalOriginSelect());
    moveResult.setCompany(parentMove.getCompany());
    moveResult.setCurrency(parentMove.getCurrency());
    moveResult.setCompanyBankDetails(parentMove.getCompanyBankDetails());

    for (MoveLineMassEntry massEntryLine : parentMove.getMoveLineMassEntryList()) {
      if (massEntryLine.getTemporaryMoveNumber() == temporaryMoveNumber
          && massEntryLine.getInputAction()
              == MoveLineMassEntryRepository.MASS_ENTRY_INPUT_ACTION_LINE) {
        if (firstMoveLine) {
          if (massEntryLine.getDate() != null && moveResult.getCompany() != null) {
            moveResult.setPeriod(
                periodService.getPeriod(
                    massEntryLine.getDate(), moveResult.getCompany(), YearRepository.TYPE_FISCAL));
          }
          moveResult.setReference(massEntryLine.getTemporaryMoveNumber().toString());
          moveResult.setDate(massEntryLine.getDate());
          moveResult.setPartner(massEntryLine.getPartner());
          moveResult.setOrigin(massEntryLine.getOrigin());
          moveResult.setStatusSelect(massEntryLine.getMoveStatusSelect());
          moveResult.setOriginDate(massEntryLine.getOriginDate());
          moveResult.setDescription(massEntryLine.getMoveDescription());
          moveResult.setPaymentMode(massEntryLine.getMovePaymentMode());
          moveResult.setPaymentCondition(massEntryLine.getMovePaymentCondition());
          moveResult.setPartnerBankDetails(massEntryLine.getMovePartnerBankDetails());
          firstMoveLine = false;
        }
        massEntryLine.setMove(moveResult);
        massEntryLine.setFieldsErrorList(null);
        moveResult.addMoveLineListItem(massEntryLine);
        moveResult.addMoveLineMassEntryListItem(massEntryLine);
      }
    }

    return moveResult;
  }

  @Override
  public Integer getMaxTemporaryMoveNumber(List<MoveLineMassEntry> moveLineList) {
    int max = 0;

    for (MoveLineMassEntry moveLine : moveLineList) {
      if (moveLine.getTemporaryMoveNumber() > max) {
        max = moveLine.getTemporaryMoveNumber();
      }
    }

    return max;
  }

  @Override
  public void setNewStatusSelectOnMassEntryLines(Move move, Integer newStatusSelect) {
    if (ObjectUtils.notEmpty(move.getMoveLineMassEntryList())) {
      for (MoveLineMassEntry moveLine : move.getMoveLineMassEntryList()) {
        moveLine.setMoveStatusSelect(newStatusSelect);
      }
    }
  }

  @Override
  public boolean verifyJournalAuthorizeNewMove(
      List<MoveLineMassEntry> moveLineList, Journal journal) {
    if (!journal.getAllowAccountingNewOnMassEntry()) {
      for (MoveLineMassEntry moveLine : moveLineList) {
        if (moveLine.getMoveStatusSelect().equals(MoveRepository.STATUS_NEW)) {
          return false;
        }
      }
    }
    return true;
  }
}
