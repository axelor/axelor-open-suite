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
import java.time.LocalDate;
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
    if (newDate != null && !moveLineMassEntry.getOriginDate().equals(newDate)) {
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
      moveLineMassEntry.setMoveDescription(newMoveDescription);
    }
  }

  public void checkAndReplaceMovePaymentModeInMoveLineMassEntry(
      MoveLineMassEntry moveLineMassEntry, PaymentMode newMovePaymentMode) {
    if (!newMovePaymentMode.equals(moveLineMassEntry.getMovePaymentMode())) {
      moveLineMassEntry.setMovePaymentMode(newMovePaymentMode);
    }
  }
}
