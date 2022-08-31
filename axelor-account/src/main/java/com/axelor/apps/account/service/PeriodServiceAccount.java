/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2022 Axelor (<http://axelor.com>).
 *
 * This program is free software: you can redistribute it and/or  modify
 * it under the terms of the GNU Affero General Public License, version 3,
 * as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.axelor.apps.account.service;

import com.axelor.apps.account.db.Move;
import com.axelor.apps.base.db.Period;
import com.axelor.auth.db.User;
import com.axelor.db.Query;
import com.axelor.exception.AxelorException;
import com.axelor.meta.CallMethod;

public interface PeriodServiceAccount {

  public Query<Move> getMoveListToValidateQuery(Period period);

  @CallMethod
  public boolean isManageClosedPeriod(Period period, User user) throws AxelorException;

  @CallMethod
  public boolean isTemporarilyClosurePeriodManage(Period period, User user) throws AxelorException;

  @CallMethod
  public boolean isAuthorizedToAccountOnPeriod(Period period, User user) throws AxelorException;
}
