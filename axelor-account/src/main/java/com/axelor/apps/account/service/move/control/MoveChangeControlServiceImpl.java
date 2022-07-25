package com.axelor.apps.account.service.move.control;

import com.axelor.apps.account.db.Move;
import com.axelor.apps.account.db.MoveLine;
import com.axelor.apps.account.db.repo.MoveRepository;
import com.axelor.apps.account.exception.IExceptionMessage;
import com.axelor.exception.AxelorException;
import com.axelor.exception.db.repo.TraceBackRepository;
import com.axelor.i18n.I18n;
import com.google.inject.Inject;
import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MoveChangeControlServiceImpl implements MoveChangeControlService {
  private final Logger log = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
  protected MoveRepository moveRepository;

  @Inject
  public MoveChangeControlServiceImpl(MoveRepository moveRepository) {
    this.moveRepository = moveRepository;
  }

  @Override
  public void checkIllegalRemoval(Move move) throws AxelorException {

    log.debug("Checking illegal removal in move {}", move);
    checkRemoveLines(move);
  }

  protected void checkRemoveLines(Move move) throws AxelorException {

    if (move.getId() == null) {
      return;
    }

    Move moveDB = moveRepository.find(move.getId());
    List<String> moveLineReconciledAndRemovedNameList = new ArrayList<>();
    if (moveDB.getMoveLineList() == null) {
      return;
    }
    for (MoveLine moveLineDB : moveDB.getMoveLineList()) {
      if (!move.getMoveLineList().contains(moveLineDB) && moveLineDB.getReconcileGroup() != null) {
        moveLineReconciledAndRemovedNameList.add(moveLineDB.getName());
      }
    }
    if (!moveLineReconciledAndRemovedNameList.isEmpty()) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_INCONSISTENCY,
          String.format(
              I18n.get(IExceptionMessage.MOVE_LINE_RECONCILE_LINE_CANNOT_BE_REMOVED),
              moveLineReconciledAndRemovedNameList.toString()));
    }
  }
}
