package com.axelor.apps.account.service.move;

import com.axelor.apps.account.db.Move;
import com.axelor.apps.account.db.repo.MoveRepository;
import com.axelor.exception.AxelorException;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;

public class MoveSimulateServiceImpl implements MoveSimulateService {

  protected MoveValidateService moveValidateService;

  @Inject
  public MoveSimulateServiceImpl(MoveValidateService moveValidateService) {
    this.moveValidateService = moveValidateService;
  }

  @Override
  @Transactional
  public void simulate(Move move) throws AxelorException {
    moveValidateService.checkPreconditions(move);
    move.setStatusSelect(MoveRepository.STATUS_SIMULATED);
  }
}
