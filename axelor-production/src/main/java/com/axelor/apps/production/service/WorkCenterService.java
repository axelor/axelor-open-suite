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
package com.axelor.apps.production.service;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.production.db.WorkCenter;
import com.axelor.apps.production.db.WorkCenterGroup;
import java.math.BigDecimal;

public interface WorkCenterService {

  public long getMachineDurationFromWorkCenter(WorkCenter workCenter);

  public long getHumanDurationFromWorkCenter(WorkCenter workCenter);

  public BigDecimal getMinCapacityPerCycleFromWorkCenter(WorkCenter workCenter);

  public BigDecimal getMaxCapacityPerCycleFromWorkCenter(WorkCenter workCenter);

  /**
   * Returns work center with min sequence in {@link workCenterGroup}. Can return null if the work
   * center group is null, else if the work center group has no work centers, throws an exception.
   */
  public WorkCenter getMainWorkCenterFromGroup(WorkCenterGroup workCenterGroup)
      throws AxelorException;
}
