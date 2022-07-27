package com.axelor.apps.account.db.repo.listener;

import com.axelor.apps.account.db.AnalyticMoveLine;
import com.axelor.apps.account.db.repo.MoveRepository;
import com.axelor.apps.account.service.move.control.accounting.moveline.analytic.analyticline.MoveAccountingMoveLineAnalyticLineControlService;
import com.axelor.exception.AxelorException;
import com.axelor.inject.Beans;
import java.lang.invoke.MethodHandles;
import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AnalyticMoveLineListener {

  private final Logger log = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  @PrePersist
  @PreUpdate
  public void beforeSave(AnalyticMoveLine analyticMoveLine) throws AxelorException {

    log.debug("Applying pre-save operations on analyticMoveLine {}", analyticMoveLine);

    if (analyticMoveLine.getMoveLine() != null
        && analyticMoveLine.getMoveLine().getMove() != null
        && (analyticMoveLine.getMoveLine().getMove().getStatusSelect()
                == MoveRepository.STATUS_ACCOUNTED
            || analyticMoveLine.getMoveLine().getMove().getStatusSelect()
                == MoveRepository.STATUS_SIMULATED
            || analyticMoveLine.getMoveLine().getMove().getStatusSelect()
                == MoveRepository.STATUS_DAYBOOK)) {

      Beans.get(MoveAccountingMoveLineAnalyticLineControlService.class)
          .checkInactiveAnalyticAccount(analyticMoveLine);
      Beans.get(MoveAccountingMoveLineAnalyticLineControlService.class)
          .checkInactiveAnalyticJournal(analyticMoveLine);
    }

    log.debug("Applied pre-save operations");
  }
}
