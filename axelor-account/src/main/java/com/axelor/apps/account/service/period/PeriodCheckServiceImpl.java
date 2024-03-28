package com.axelor.apps.account.service.period;

import com.axelor.apps.account.db.AccountConfig;
import com.axelor.apps.account.db.Move;
import com.axelor.apps.account.db.repo.MoveRepository;
import com.axelor.apps.account.service.config.AccountConfigService;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Period;
import com.axelor.apps.base.db.repo.PeriodRepository;
import com.axelor.apps.base.service.user.UserRoleToolService;
import com.axelor.auth.db.User;
import com.google.inject.Inject;

public class PeriodCheckServiceImpl implements PeriodCheckService {

  protected AccountConfigService accountConfigService;

  @Inject
  public PeriodCheckServiceImpl(AccountConfigService accountConfigService) {
    this.accountConfigService = accountConfigService;
  }

  @Override
  public boolean isAuthorizedToAccountOnPeriod(Period period, User user) throws AxelorException {
    if (period != null && period.getYear().getCompany() != null && user != null) {
      if (period.getStatusSelect() == PeriodRepository.STATUS_CLOSED) {
        return false;
      }
      if (period.getStatusSelect() == PeriodRepository.STATUS_TEMPORARILY_CLOSED) {
        AccountConfig accountConfig =
            accountConfigService.getAccountConfig(period.getYear().getCompany());
        return UserRoleToolService.checkUserRolesPermissionExcludingEmpty(
            user, accountConfig.getMoveOnTempClosureAuthorizedRoleList());
      }
      return true;
    }
    return false;
  }

  @Override
  public boolean isAuthorizedToAccountOnPeriod(Move move, User user) throws AxelorException {
    if (move.getCompany() == null
        || move.getFunctionalOriginSelect() == MoveRepository.FUNCTIONAL_ORIGIN_OPENING
        || move.getFunctionalOriginSelect() == MoveRepository.FUNCTIONAL_ORIGIN_CLOSURE) {
      return true;
    }

    return isAuthorizedToAccountOnPeriod(move.getPeriod(), user);
  }
}
