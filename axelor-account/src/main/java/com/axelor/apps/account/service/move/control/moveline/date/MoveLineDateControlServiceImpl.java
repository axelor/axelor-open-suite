package com.axelor.apps.account.service.move.control.moveline.date;

import com.axelor.apps.account.db.Move;
import com.axelor.apps.account.db.MoveLine;
import com.axelor.apps.account.exception.IExceptionMessage;
import com.axelor.exception.AxelorException;
import com.axelor.exception.db.repo.TraceBackRepository;
import com.axelor.i18n.I18n;

public class MoveLineDateControlServiceImpl implements MoveLineDateControlService {

  @Override
  public void checkDateInPeriod(MoveLine moveLine) throws AxelorException {

    Move move = moveLine.getMove();

    if (move != null
        && move.getPeriod() != null
        && moveLine != null
        && moveLine.getDate() != null
        && (moveLine.getDate().isBefore(move.getPeriod().getFromDate())
            || moveLine.getDate().isAfter(move.getPeriod().getToDate()))) {
      if (move.getCurrency() != null
          && move.getCurrency().getSymbol() != null
          && moveLine.getAccount() != null) {
        throw new AxelorException(
            moveLine,
            TraceBackRepository.CATEGORY_MISSING_FIELD,
            I18n.get(IExceptionMessage.DATE_NOT_IN_PERIOD_MOVE),
            moveLine.getCurrencyAmount(),
            move.getCurrency().getSymbol(),
            moveLine.getAccount().getCode());
      } else {
        throw new AxelorException(
            moveLine,
            TraceBackRepository.CATEGORY_MISSING_FIELD,
            I18n.get(IExceptionMessage.DATE_NOT_IN_PERIOD_MOVE_WITHOUT_ACCOUNT));
      }
    }
  }
}
