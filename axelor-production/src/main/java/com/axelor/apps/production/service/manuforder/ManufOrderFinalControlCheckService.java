/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2005-2026 Axelor (<http://axelor.com>).
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
package com.axelor.apps.production.service.manuforder;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.production.db.ManufOrder;
import com.axelor.apps.production.db.OperationOrder;

public interface ManufOrderFinalControlCheckService {

  int getCheckSelect(ManufOrder manufOrder);

  /**
   * @return the reason preventing a clean finish, null when the manufacturing order can finish.
   */
  String getFinishIssueMessage(ManufOrder manufOrder);

  /** Only for the last operation to finish, null for the other operations. */
  String getFinishIssueMessage(OperationOrder operationOrder);

  /**
   * Blocking mode: throws when the final control is missing or non-compliant. Warning mode: traces
   * the issue on the manufacturing order and notifies the current user.
   */
  void checkBeforeFinish(ManufOrder manufOrder) throws AxelorException;
}
