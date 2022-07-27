package com.axelor.apps.account.service.move.control.period;

import com.axelor.apps.account.db.Move;
import com.axelor.apps.account.exception.IExceptionMessage;
import com.axelor.apps.account.service.PeriodServiceAccount;
import com.axelor.apps.base.db.repo.PeriodRepository;
import com.axelor.auth.AuthUtils;
import com.axelor.exception.AxelorException;
import com.axelor.exception.db.repo.TraceBackRepository;
import com.axelor.i18n.I18n;
import com.google.inject.Inject;
import java.lang.invoke.MethodHandles;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MovePeriodControlServiceImpl implements MovePeriodControlService {
  private final Logger log = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
  protected PeriodServiceAccount periodServiceAccount;

  @Inject
  public MovePeriodControlServiceImpl(PeriodServiceAccount periodServiceAccount) {
    this.periodServiceAccount = periodServiceAccount;
  }

  @Override
  public void checkClosedPeriod(Move move) throws AxelorException {

    log.debug("Checking closed period of move {}. Period = {}", move, move.getPeriod());
    if (move.getPeriod().getStatusSelect() == PeriodRepository.STATUS_CLOSED
        && !move.getAutoYearClosureMove()) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
          I18n.get(IExceptionMessage.MOVE_VALIDATION_FISCAL_PERIOD_CLOSED));
    }
  }

  @Override
  public void checkAuthorizationOnClosedPeriod(Move move) throws AxelorException {
    log.debug("Checking authorization on move {} on period {}", move, move.getPeriod());
    if (!periodServiceAccount.isAuthorizedToAccountOnPeriod(
        move.getPeriod(), AuthUtils.getUser())) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
          I18n.get(IExceptionMessage.MOVE_PERIOD_IS_CLOSED));
    }
  }
}
