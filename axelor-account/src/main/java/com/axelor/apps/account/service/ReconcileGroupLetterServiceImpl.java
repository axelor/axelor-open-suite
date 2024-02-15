package com.axelor.apps.account.service;

import com.axelor.apps.account.db.MoveLine;
import com.axelor.apps.account.db.ReconcileGroup;
import com.axelor.apps.account.db.repo.MoveLineRepository;
import com.axelor.apps.account.service.moveline.MoveLineService;
import com.axelor.apps.base.AxelorException;
import com.google.inject.Inject;
import java.util.List;

public class ReconcileGroupLetterServiceImpl implements ReconcileGroupLetterService {

  protected MoveLineRepository moveLineRepository;
  protected MoveLineService moveLineService;

  @Inject
  public ReconcileGroupLetterServiceImpl(
      MoveLineRepository moveLineRepository, MoveLineService moveLineService) {
    this.moveLineRepository = moveLineRepository;
    this.moveLineService = moveLineService;
  }

  @Override
  public void letter(ReconcileGroup reconcileGroup) throws AxelorException {

    List<MoveLine> moveLines =
        moveLineRepository
            .all()
            .filter("self.reconcileGroup = :reconcileGroup")
            .bind("reconcileGroup", reconcileGroup)
            .fetch();
    moveLineService.reconcileMoveLines(moveLines);
  }
}
