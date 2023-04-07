package com.axelor.apps.account.service.move.control;

import com.axelor.apps.account.db.Move;
import com.axelor.apps.account.db.MoveLine;
import com.axelor.apps.account.db.repo.MoveRepository;
import com.axelor.apps.account.exception.AccountExceptionMessage;
import com.axelor.apps.account.service.app.AppAccountService;
import com.axelor.apps.account.service.move.MoveToolService;
import com.axelor.apps.base.exceptions.BaseExceptionMessage;
import com.axelor.apps.base.service.PeriodService;
import com.axelor.exception.AxelorException;
import com.axelor.exception.db.repo.TraceBackRepository;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.google.inject.Inject;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import org.apache.commons.collections.CollectionUtils;

public class MoveCheckServiceImpl implements MoveCheckService {

  protected MoveRepository moveRepository;
  protected MoveToolService moveToolService;
  protected PeriodService periodService;
  protected AppAccountService appAccountService;
  protected MoveLineCheckService moveLineCheckService;

  @Inject
  public MoveCheckServiceImpl(
      MoveRepository moveRepository,
      MoveToolService moveToolService,
      PeriodService periodService,
      AppAccountService appAccountService,
      MoveLineCheckService moveLineCheckService) {
    this.moveRepository = moveRepository;
    this.moveToolService = moveToolService;
    this.periodService = periodService;
    this.appAccountService = appAccountService;
    this.moveLineCheckService = moveLineCheckService;
  }

  @Override
  public boolean checkRelatedCutoffMoves(Move move) {
    Objects.requireNonNull(move);

    if (appAccountService.getAppAccount().getManageCutOffPeriod()) {
      if (move.getId() != null) {
        return moveRepository
                .all()
                .filter("self.cutOffOriginMove = :id")
                .bind("id", move.getId())
                .count()
            > 0;
      }
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

  @Override
  public void checkPeriodPermission(Move move) throws AxelorException {
    if (Beans.get(PeriodService.class).isClosedPeriod(move.getPeriod())) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
          I18n.get(BaseExceptionMessage.PERIOD_CLOSED_AND_NO_PERMISSIONS));
    }
  }

  @Override
  public void checkDates(Move move) throws AxelorException {
    Objects.requireNonNull(move);

    moveLineCheckService.checkDates(move);
  }

  @Override
  public void checkRemovedLines(Move move) throws AxelorException {
    Objects.requireNonNull(move);
    if (move.getId() == null) {
      return;
    }

    Move moveDB = moveRepository.find(move.getId());

    List<String> moveLineReconciledAndRemovedNameList = new ArrayList<>();
    for (MoveLine moveLineBD : moveDB.getMoveLineList()) {
      if (!move.getMoveLineList().contains(moveLineBD)) {
        if (moveLineBD.getReconcileGroup() != null) {
          moveLineReconciledAndRemovedNameList.add(moveLineBD.getName());
        }
      }
    }
    if (moveLineReconciledAndRemovedNameList != null
        && !moveLineReconciledAndRemovedNameList.isEmpty()) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_INCONSISTENCY,
          String.format(
              I18n.get(AccountExceptionMessage.MOVE_LINE_RECONCILE_LINE_CANNOT_BE_REMOVED),
              moveLineReconciledAndRemovedNameList.toString()));
    }
  }

  @Override
  public void checkAnalyticAccount(Move move) throws AxelorException {
    Objects.requireNonNull(move);
    if (move != null && CollectionUtils.isNotEmpty(move.getMoveLineList())) {
      moveLineCheckService.checkAnalyticAccount(move.getMoveLineList());
    }
  }
}
