package com.axelor.apps.account.service.moveline.massentry;

import com.axelor.apps.account.db.Move;
import com.axelor.apps.account.db.MoveLineMassEntry;
import com.axelor.apps.account.service.moveline.MoveLineComputeAnalyticService;
import com.axelor.apps.account.service.moveline.MoveLineToolService;
import com.axelor.apps.base.db.Period;
import com.axelor.apps.base.db.repo.YearRepository;
import com.axelor.apps.base.service.PeriodService;
import com.axelor.exception.AxelorException;
import com.axelor.inject.Beans;
import java.time.LocalDate;

public class MassEntryVerificationServiceImpl implements MassEntryVerificationService {

  public void checkAndReplaceDateInMoveLineMassEntry(
      MoveLineMassEntry moveLineMassEntry, LocalDate newDate, Move move) throws AxelorException {
    if (!moveLineMassEntry.getDate().equals(newDate)) {
      moveLineMassEntry.setDate(newDate);
      moveLineMassEntry.setOriginDate(newDate);

      if (Beans.get(MoveLineComputeAnalyticService.class).checkManageAnalytic(move.getCompany())) {
        moveLineMassEntry.setAnalyticMoveLineList(
            Beans.get(MoveLineComputeAnalyticService.class)
                .computeAnalyticDistribution(moveLineMassEntry)
                .getAnalyticMoveLineList());
        Period period = null;
        if (move.getDate() != null && move.getCompany() != null) {
          period =
              Beans.get(PeriodService.class)
                  .getActivePeriod(move.getDate(), move.getCompany(), YearRepository.TYPE_FISCAL);
          move.setPeriod(period);
        }
        Beans.get(MoveLineToolService.class).checkDateInPeriod(move, moveLineMassEntry);
      }
    }
  }
}
