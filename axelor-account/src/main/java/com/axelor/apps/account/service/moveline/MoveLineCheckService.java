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
package com.axelor.apps.account.service.moveline;

import com.axelor.apps.account.db.Move;
import com.axelor.apps.account.db.MoveLine;
import com.axelor.apps.base.AxelorException;
import java.util.List;

public interface MoveLineCheckService {
  void checkAnalyticByTemplate(MoveLine moveLine) throws AxelorException;

  void checkAnalyticAxes(MoveLine moveLine) throws AxelorException;

  void checkDebitCredit(MoveLine moveLine) throws AxelorException;

  void checkDates(Move move) throws AxelorException;

  void checkAnalyticAccount(List<MoveLine> moveLineList) throws AxelorException;
}
