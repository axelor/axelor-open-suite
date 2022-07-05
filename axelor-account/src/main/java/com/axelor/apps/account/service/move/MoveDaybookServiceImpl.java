package com.axelor.apps.account.service.move;

import com.axelor.apps.account.db.Move;
import com.axelor.apps.account.db.repo.MoveRepository;
import com.axelor.exception.AxelorException;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;

public class MoveDaybookServiceImpl implements MoveDaybookService {

  protected MoveValidateService moveValidateService;
  protected MoveRepository moveRepository;

  @Inject
  public MoveDaybookServiceImpl(
      MoveValidateService moveValidateService, MoveRepository moveRepository) {
    this.moveValidateService = moveValidateService;
    this.moveRepository = moveRepository;
  }

  @Override
  @Transactional(rollbackOn = {AxelorException.class, RuntimeException.class})
  public void daybook(Move move) throws AxelorException {
    moveValidateService.checkPreconditions(move);
    move.setStatusSelect(MoveRepository.STATUS_DAYBOOK);
    moveRepository.save(move);
  }
}
