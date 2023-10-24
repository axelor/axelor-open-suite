/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2005-2023 Axelor (<http://axelor.com>).
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
package com.axelor.apps.base.service.dayplanning;

import com.axelor.apps.base.db.DayPlanning;
import java.time.LocalDateTime;
import java.util.Optional;

public interface DayPlanningService {

  /**
   * This method will return a allowed start dateTime for dateT in the dayPlanning
   *
   * <p>If dateT is in one the two periods of the day planning, then it will return itself. If dateT
   * is not in any of the two periods but is before the start of one of them, it will return a
   * startDateTime at the localTime of the next period. If dateT is after the two periods of the
   * day, it will look for the next day.
   *
   * <p>The method will return a empty optional if we can't find any period to start. (Happens if
   * day planning exist but there is not period specified in it), also a null dayPlanning will be
   * considered as a allowed day for any hour and a call with such value will result a return a
   * Optional of dateT
   *
   * @param dayPlanning
   * @param dateT
   * @return Optional.empty if we can't find any period, else a Optional of dateTime
   */
  Optional<LocalDateTime> getAllowedStartDateTPeriodAt(
      DayPlanning dayPlanning, LocalDateTime dateT);

  /**
   * This method will compute the "void" (time where not there are no period) between startDateT and
   * endDateT
   *
   * @param startDateT
   * @param endDateT
   * @return the void duration in seconds
   */
  long computeVoidDurationBetween(
      DayPlanning dayPlanning, LocalDateTime startDateT, LocalDateTime endDateT);
}
