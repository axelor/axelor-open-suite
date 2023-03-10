package com.axelor.apps.account.service.moveline.massentry;

import com.axelor.apps.account.db.Move;
import com.axelor.apps.account.db.MoveLineMassEntry;
import com.axelor.apps.account.db.PaymentMode;
import com.axelor.apps.account.service.moveline.MoveLineComputeAnalyticService;
import com.axelor.apps.account.service.moveline.MoveLineToolService;
import com.axelor.apps.base.db.Period;
import com.axelor.apps.base.db.repo.YearRepository;
import com.axelor.apps.base.service.PeriodService;
import com.axelor.exception.AxelorException;
import com.google.inject.Inject;
import java.lang.invoke.MethodHandles;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MassEntryVerificationServiceImpl implements MassEntryVerificationService {

  private final Logger log = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  protected MoveLineComputeAnalyticService moveLineComputeAnalyticService;
  protected PeriodService periodService;
  protected MoveLineToolService moveLineToolService;

  @Inject
  public MassEntryVerificationServiceImpl(
      MoveLineComputeAnalyticService moveLineComputeAnalyticService,
      PeriodService periodService,
      MoveLineToolService moveLineToolService) {
    this.moveLineComputeAnalyticService = moveLineComputeAnalyticService;
    this.periodService = periodService;
    this.moveLineToolService = moveLineToolService;
  }

  public void checkAndReplaceDateInMoveLineMassEntry(
      MoveLineMassEntry moveLineMassEntry, LocalDate newDate, Move move) throws AxelorException {
    if (!moveLineMassEntry.getDate().equals(newDate)) {
      moveLineMassEntry.setDate(newDate);

      if (moveLineComputeAnalyticService.checkManageAnalytic(move.getCompany())) {
        moveLineMassEntry.setAnalyticMoveLineList(
            moveLineComputeAnalyticService
                .computeAnalyticDistribution(moveLineMassEntry)
                .getAnalyticMoveLineList());
        Period period = null;
        if (move.getDate() != null && move.getCompany() != null) {
          period =
              periodService.getActivePeriod(
                  move.getDate(), move.getCompany(), YearRepository.TYPE_FISCAL);
          move.setPeriod(period);
        }
        moveLineToolService.checkDateInPeriod(move, moveLineMassEntry);
      }
    }
  }

  public void checkAndReplaceOriginDateInMoveLineMassEntry(
      MoveLineMassEntry moveLineMassEntry, LocalDate newDate) {
    if (newDate != null && !newDate.equals(moveLineMassEntry.getOriginDate())) {
      moveLineMassEntry.setOriginDate(newDate);
    }
  }

  public void checkAndReplaceOriginInMoveLineMassEntry(
      MoveLineMassEntry moveLineMassEntry, String newOrigin) {
    if (!newOrigin.equals(moveLineMassEntry.getOrigin())) {
      moveLineMassEntry.setOrigin(newOrigin);
    }
  }

  public void checkAndReplaceMoveDescriptionInMoveLineMassEntry(
      MoveLineMassEntry moveLineMassEntry, String newMoveDescription) {
    if (!newMoveDescription.equals(moveLineMassEntry.getMoveDescription())) {
      this.checkAndReplaceDescriptionInMoveLineMassEntry(moveLineMassEntry, newMoveDescription);
      moveLineMassEntry.setMoveDescription(newMoveDescription);
    }
  }

  public void checkAndReplaceDescriptionInMoveLineMassEntry(
      MoveLineMassEntry moveLineMassEntry, String newDescription) {
    if (moveLineMassEntry.getMoveDescription().equals(moveLineMassEntry.getDescription())) {
      moveLineMassEntry.setDescription(newDescription);
    }
  }

  public void checkAndReplaceMovePaymentModeInMoveLineMassEntry(
      MoveLineMassEntry moveLineMassEntry, PaymentMode newMovePaymentMode) {
    if (!newMovePaymentMode.equals(moveLineMassEntry.getMovePaymentMode())) {
      moveLineMassEntry.setMovePaymentMode(newMovePaymentMode);
    }
  }

  public void checkAndReplaceCurrencyRateInMoveLineMassEntry(
      MoveLineMassEntry moveLineMassEntry, BigDecimal newCurrencyRate) {
    if (!newCurrencyRate.equals(moveLineMassEntry.getCurrencyRate())) {
      moveLineMassEntry.setCurrencyRate(newCurrencyRate);
    }
  }

  public void checkDateInAllMoveLineMassEntry(List<MoveLineMassEntry> moveLineMassEntryList) {
    // TODO add control for MoveDate
    // need to verify if one line have a different date
    // need to verify if period is not closed or exist
  }

  public void checkCurrencyRateInAllMoveLineMassEntry(
      List<MoveLineMassEntry> moveLineMassEntryList) {
    // TODO add control for currencyRate
    // need to verify if currencyRate is not 0,00
  }

  public void checkOriginDateInAllMoveLineMassEntry(List<MoveLineMassEntry> moveLineMassEntryList) {
    // TODO add control for OriginDate
    // need to verify if one line on same temporaryMoveNumber have a different originDate
  }

  public void checkOriginInAllMoveLineMassEntry(List<MoveLineMassEntry> moveLineMassEntryList) {
    // TODO add control for Origin
    // need to verify if we have duplicates Origin on same Journal/Period/Partner
  }
}
