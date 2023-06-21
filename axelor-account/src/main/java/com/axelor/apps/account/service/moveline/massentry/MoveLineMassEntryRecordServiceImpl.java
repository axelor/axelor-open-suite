package com.axelor.apps.account.service.moveline.massentry;

import com.axelor.apps.account.db.AnalyticMoveLine;
import com.axelor.apps.account.db.Move;
import com.axelor.apps.account.db.MoveLine;
import com.axelor.apps.account.db.MoveLineMassEntry;
import com.axelor.apps.account.db.repo.AnalyticMoveLineRepository;
import com.axelor.apps.account.db.repo.JournalTypeRepository;
import com.axelor.apps.account.service.moveline.MoveLineRecordService;
import com.axelor.apps.account.util.TaxAccountToolService;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Company;
import com.axelor.common.ObjectUtils;
import com.axelor.inject.Beans;
import com.google.inject.Inject;
import java.math.BigDecimal;
import org.apache.commons.collections.CollectionUtils;

public class MoveLineMassEntryRecordServiceImpl implements MoveLineMassEntryRecordService {

  protected MoveLineMassEntryService moveLineMassEntryService;
  protected MoveLineRecordService moveLineRecordService;
  protected TaxAccountToolService taxAccountToolService;
  protected AnalyticMoveLineRepository analyticMoveLineRepository;

  @Inject
  public MoveLineMassEntryRecordServiceImpl(
      MoveLineMassEntryService moveLineMassEntryService,
      MoveLineRecordService moveLineRecordService,
      TaxAccountToolService taxAccountToolService,
      AnalyticMoveLineRepository analyticMoveLineRepository) {
    this.moveLineMassEntryService = moveLineMassEntryService;
    this.moveLineRecordService = moveLineRecordService;
    this.taxAccountToolService = taxAccountToolService;
    this.analyticMoveLineRepository = analyticMoveLineRepository;
  }

  @Override
  public void setCurrencyRate(Move move, MoveLineMassEntry moveLine) throws AxelorException {
    BigDecimal currencyRate = BigDecimal.ONE;

    currencyRate =
        moveLineMassEntryService.computeCurrentRate(
            currencyRate,
            move.getMoveLineMassEntryList(),
            move.getCurrency(),
            move.getCompanyCurrency(),
            moveLine.getTemporaryMoveNumber(),
            moveLine.getOriginDate());

    moveLine.setCurrencyRate(currencyRate);
  }

  @Override
  public void resetDebit(MoveLineMassEntry moveLine) {
    if (moveLine.getCredit().signum() != 0 && moveLine.getDebit().signum() != 0) {
      moveLine.setDebit(BigDecimal.ZERO);
    }
  }

  @Override
  public void setMovePfpValidatorUser(MoveLineMassEntry moveLine, Company company) {
    moveLine.setMovePfpValidatorUser(
        Beans.get(MoveLineMassEntryService.class)
            .getPfpValidatorUserForInTaxAccount(
                moveLine.getAccount(), company, moveLine.getPartner()));
  }

  @Override
  public void setCutOff(MoveLineMassEntry moveLine) {
    if (moveLine.getAccount() != null && !moveLine.getAccount().getManageCutOffPeriod()) {
      moveLine.setCutOffStartDate(null);
      moveLine.setCutOffEndDate(null);
    } else if (ObjectUtils.isEmpty(moveLine.getCutOffStartDate())
        && ObjectUtils.isEmpty(moveLine.getCutOffEndDate())
        && ObjectUtils.notEmpty(moveLine.getAccount())) {
      moveLine.setCutOffStartDate(moveLine.getDate());
      moveLine.setCutOffEndDate(moveLine.getDate());
    }
  }

  @Override
  public void refreshAccountInformation(MoveLine moveLine, Move move) throws AxelorException {
    moveLineRecordService.refreshAccountInformation(moveLine, move);

    if (ObjectUtils.isEmpty(moveLine.getAccount())) {
      moveLine.setVatSystemSelect(
          taxAccountToolService.calculateVatSystem(
              moveLine.getPartner(),
              move.getCompany(),
              null,
              (move.getJournal().getJournalType().getTechnicalTypeSelect()
                  == JournalTypeRepository.TECHNICAL_TYPE_SELECT_EXPENSE),
              (move.getJournal().getJournalType().getTechnicalTypeSelect()
                  == JournalTypeRepository.TECHNICAL_TYPE_SELECT_SALE)));
    }
  }

  @Override
  public void setAnalyticMoveLineMassEntryList(
      MoveLineMassEntry moveLineMassEntry, MoveLine moveLine) {
    moveLineMassEntry.clearAnalyticMoveLineMassEntryList();
    if (CollectionUtils.isNotEmpty(moveLine.getAnalyticMoveLineList())) {
      for (AnalyticMoveLine analyticMoveLine : moveLine.getAnalyticMoveLineList()) {
        AnalyticMoveLine copyAnalyticMoveLine =
            analyticMoveLineRepository.copy(analyticMoveLine, false);
        moveLineMassEntry.addAnalyticMoveLineMassEntryListItem(copyAnalyticMoveLine);
      }
    }
  }

  @Override
  public void setAnalyticMoveLineList(MoveLineMassEntry moveLineMassEntry, MoveLine moveLine) {
    moveLine.clearAnalyticMoveLineList();
    if (CollectionUtils.isNotEmpty(moveLineMassEntry.getAnalyticMoveLineMassEntryList())) {
      for (AnalyticMoveLine analyticMoveLine :
          moveLineMassEntry.getAnalyticMoveLineMassEntryList()) {
        AnalyticMoveLine copyAnalyticMoveLine =
            analyticMoveLineRepository.copy(analyticMoveLine, false);
        moveLine.addAnalyticMoveLineListItem(copyAnalyticMoveLine);
      }
    }
  }

  @Override
  public void setAnalyticMoveLineMassEntryList(MoveLineMassEntry moveLine) {
    moveLine.clearAnalyticMoveLineMassEntryList();
    if (ObjectUtils.notEmpty(moveLine.getAnalyticMoveLineList())) {
      moveLine
          .getAnalyticMoveLineList()
          .forEach(
              analyticMoveLine -> {
                moveLine.addAnalyticMoveLineMassEntryListItem(
                    analyticMoveLineRepository.copy(analyticMoveLine, false));
              });
    }
  }
}
