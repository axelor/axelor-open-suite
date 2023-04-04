package com.axelor.apps.account.service.move.control;

import com.axelor.apps.account.db.Move;
import com.axelor.apps.account.db.repo.MoveRepository;
import com.axelor.apps.account.service.move.MoveToolService;
import com.axelor.apps.base.service.PeriodService;
import com.axelor.exception.AxelorException;
import com.google.inject.Inject;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class MoveCheckServiceImpl implements MoveCheckService {

  protected MoveRepository moveRepository;
  protected MoveToolService moveToolService;
  protected PeriodService periodService;

  @Inject
  public MoveCheckServiceImpl(
      MoveRepository moveRepository, MoveToolService moveToolService, PeriodService periodService) {
    this.moveRepository = moveRepository;
    this.moveToolService = moveToolService;
    this.periodService = periodService;
  }

  @Override
  public boolean checkRelatedCutoffMoves(Move move) {
    Objects.requireNonNull(move);

    if (move.getId() != null) {
      return moveRepository
              .all()
              .filter("self.cutOffOriginMove = :id")
              .bind("id", move.getId())
              .count()
          > 0;
    }
    return false;
  }

  @Override
  public Map<String, Object> checkPeriodAndStatus(Move move) throws AxelorException {

    Objects.requireNonNull(move);
    HashMap<String, Object> resultMap = new HashMap<>();

    resultMap.put("$simulatedPeriodClosed", moveToolService.isSimulatedMovePeriodClosed(move));
    resultMap.put("$periodClosed", periodService.isClosedPeriod(move.getPeriod()));
    return resultMap;
  }
}
