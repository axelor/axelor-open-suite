package com.axelor.apps.account.service.move.control;

import com.axelor.apps.account.db.Move;
import com.axelor.apps.account.db.MoveLine;
import com.axelor.apps.account.service.AccountService;
import com.axelor.apps.account.service.moveline.MoveLineToolService;
import com.axelor.exception.AxelorException;
import com.google.inject.Inject;
import java.util.List;
import java.util.Objects;
import org.apache.commons.collections.CollectionUtils;

public class MoveLineCheckServiceImpl implements MoveLineCheckService {

  protected MoveLineToolService moveLineToolService;
  protected AccountService accountService;

  @Inject
  public MoveLineCheckServiceImpl(
      MoveLineToolService moveLineToolService, AccountService accountService) {
    this.moveLineToolService = moveLineToolService;
    this.accountService = accountService;
  }

  @Override
  public void checkDates(Move move) throws AxelorException {
    if (!CollectionUtils.isEmpty(move.getMoveLineList())) {
      for (MoveLine moveline : move.getMoveLineList()) {
        moveLineToolService.checkDateInPeriod(move, moveline);
      }
    }
  }

  @Override
  public void checkAnalyticAccount(List<MoveLine> moveLineList) throws AxelorException {
    Objects.requireNonNull(moveLineList);
    for (MoveLine moveLine : moveLineList) {
      if (moveLine != null && moveLine.getAccount() != null) {
        accountService.checkAnalyticAxis(
            moveLine.getAccount(), moveLine.getAnalyticDistributionTemplate());
      }
    }
  }
}
