/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2005-2024 Axelor (<http://axelor.com>).
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
package com.axelor.apps.account.service;

import com.axelor.apps.account.db.Move;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Period;
import com.axelor.auth.db.User;
import com.axelor.db.Query;
import com.axelor.meta.CallMethod;

public interface PeriodServiceAccount {

  public Query<Move> getMoveListByPeriodAndStatusQuery(Period period, int status);

  @CallMethod
  public boolean isManageClosedPeriod(Period period, User user) throws AxelorException;

  @CallMethod
  public boolean isTemporarilyClosurePeriodManage(Period period, User user) throws AxelorException;

  @CallMethod
  public boolean isAuthorizedToAccountOnPeriod(Period period, User user) throws AxelorException;

  @CallMethod
  public boolean isAuthorizedToAccountOnPeriod(Move move, User user) throws AxelorException;
}
