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
package com.axelor.apps.production.service;

import com.axelor.apps.production.db.WorkCenter;
import com.axelor.apps.production.db.WorkCenterGroup;
import com.axelor.exception.AxelorException;
import java.math.BigDecimal;

public interface WorkCenterService {

  public Long getDurationFromWorkCenter(WorkCenter workCenter);

  public BigDecimal getMinCapacityPerCycleFromWorkCenter(WorkCenter workCenter);

  public BigDecimal getMaxCapacityPerCycleFromWorkCenter(WorkCenter workCenter);

  /**
   * Returns work center with min sequence in {@link workCenterGroup}. Can return null if the work
   * center group is null, else if the work center group has no work centers, throws an exception.
   */
  public WorkCenter getMainWorkCenterFromGroup(WorkCenterGroup workCenterGroup)
      throws AxelorException;
}
