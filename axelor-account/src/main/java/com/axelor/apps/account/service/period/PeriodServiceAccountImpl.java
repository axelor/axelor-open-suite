/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2005-2025 Axelor (<http://axelor.com>).
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package com.axelor.apps.account.service.period;

import com.axelor.apps.account.db.AccountConfig;
import com.axelor.apps.account.db.Move;
import com.axelor.apps.account.db.repo.MoveRepository;
import com.axelor.apps.account.service.config.AccountConfigService;
import com.axelor.apps.account.service.move.MoveRemoveService;
import com.axelor.apps.account.service.move.MoveValidateService;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Period;
import com.axelor.apps.base.db.repo.PeriodRepository;
import com.axelor.apps.base.db.repo.YearRepository;
import com.axelor.apps.base.service.AdjustHistoryService;
import com.axelor.apps.base.service.PeriodServiceImpl;
import com.axelor.apps.base.service.user.UserRoleToolService;
import com.axelor.auth.AuthUtils;
import com.axelor.auth.db.User;
import com.axelor.db.Query;
import com.google.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class PeriodServiceAccountImpl extends PeriodServiceImpl implements PeriodServiceAccount {

  protected MoveValidateService moveValidateService;
  protected MoveRepository moveRepository;
  protected AccountConfigService accountConfigService;
  protected MoveRemoveService moveRemoveService;
  protected PeriodCheckService periodCheckService;

  @Inject
  public PeriodServiceAccountImpl(
      PeriodRepository periodRepo,
      AdjustHistoryService adjustHistoryService,
      MoveValidateService moveValidateService,
      MoveRepository moveRepository,
      AccountConfigService accountConfigService,
      MoveRemoveService moveRemoveService,
      PeriodCheckService periodCheckService) {
    super(periodRepo, adjustHistoryService);
    this.moveValidateService = moveValidateService;
    this.moveRepository = moveRepository;
    this.accountConfigService = accountConfigService;
    this.moveRemoveService = moveRemoveService;
    this.periodCheckService = periodCheckService;
  }

  @Override
  public void close(Period period) throws AxelorException {
    if (period.getYear().getTypeSelect() == YearRepository.TYPE_FISCAL) {
      moveValidateService.accountingMultiple(
          getMoveListByPeriodAndStatusQuery(period, MoveRepository.STATUS_DAYBOOK));
      period = periodRepo.find(period.getId());
    }
    moveRemoveService.deleteMultiple(
        getMoveListByPeriodAndStatusQuery(period, MoveRepository.STATUS_NEW).fetch());

    period = periodRepo.find(period.getId());

    super.close(period);
  }

  public Query<Move> getMoveListByPeriodAndStatusQuery(Period period, int status) {
    return moveRepository
        .all()
        .filter(
            "self.period.id = ?1 AND self.statusSelect = ?2 AND (self.archived = false OR self.archived is null)",
            period.getId(),
            status)
        .order("date")
        .order("id");
  }

  public boolean isManageClosedPeriod(Period period, User user) throws AxelorException {
    if (period != null && period.getYear().getCompany() != null && user != null) {
      AccountConfig accountConfig =
          accountConfigService.getAccountConfig(period.getYear().getCompany());

      return UserRoleToolService.checkUserRolesPermissionExcludingEmpty(
          user, accountConfig.getClosureAuthorizedRoleList());
    }
    return false;
  }

  public boolean isTemporarilyClosurePeriodManage(Period period, User user) throws AxelorException {
    if (period != null && period.getYear().getCompany() != null && user != null) {
      AccountConfig accountConfig =
          accountConfigService.getAccountConfig(period.getYear().getCompany());

      return UserRoleToolService.checkUserRolesPermissionExcludingEmpty(
          user, accountConfig.getTemporaryClosureAuthorizedRoleList());
    }
    return false;
  }

  @Override
  public boolean isClosedPeriod(Period period) throws AxelorException {
    User user = AuthUtils.getUser();

    return super.isClosedPeriod(period)
        && !periodCheckService.isAuthorizedToAccountOnPeriod(period, user);
  }
}
