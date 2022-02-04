package com.axelor.apps.account.service.move;

import com.axelor.apps.account.db.Move;
import com.axelor.apps.account.db.repo.MoveRepository;
import com.axelor.exception.AxelorException;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;

public class MoveSimulateServiceImpl implements MoveSimulateService {

  protected MoveValidateService moveValidateService;
  protected MoveRepository moveRepository;

  @Inject
  public MoveSimulateServiceImpl(
      MoveValidateService moveValidateService, MoveRepository moveRepository) {
    this.moveValidateService = moveValidateService;
    this.moveRepository = moveRepository;
  }

  @Override
  @Transactional(rollbackOn = {AxelorException.class, RuntimeException.class})
  public void simulate(Move move) throws AxelorException {
    moveValidateService.checkPreconditions(move);
    move.setStatusSelect(MoveRepository.STATUS_SIMULATED);
    moveRepository.save(move);
  }
}
