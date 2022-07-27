package com.axelor.apps.account.service.move.control.moveline;

import com.axelor.apps.account.db.MoveLine;
import com.axelor.apps.account.db.repo.MoveRepository;
import com.axelor.apps.account.exception.IExceptionMessage;
import com.axelor.exception.AxelorException;
import com.axelor.exception.db.repo.TraceBackRepository;
import com.axelor.i18n.I18n;
import java.lang.invoke.MethodHandles;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MoveLineChangeControlServiceImpl implements MoveLineChangeControlService {

  private final Logger log = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  @Override
  public void checkIllegalRemoval(MoveLine moveLine) throws AxelorException {

    log.debug("Checking illegal removal of moveLine {}", moveLine);

    if (moveLine.getMove() != null) {

      if (moveLine.getReconcileGroup() != null) {
        throw new AxelorException(
            TraceBackRepository.CATEGORY_INCONSISTENCY,
            String.format(
                I18n.get(IExceptionMessage.MOVE_LINE_RECONCILE_LINE_CANNOT_BE_REMOVED),
                moveLine.getName()));
      }

      if (!moveLine.getMove().getStatusSelect().equals(MoveRepository.STATUS_NEW)
          && !moveLine.getMove().getStatusSelect().equals(MoveRepository.STATUS_SIMULATED)) {
        throw new AxelorException(
            TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
            I18n.get(IExceptionMessage.MOVE_REMOVE_NOT_OK),
            moveLine.getMove().getReference());
      }
    }
  }
}
