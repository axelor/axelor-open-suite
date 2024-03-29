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
package com.axelor.apps.intervention.service.planning;

import java.time.LocalDateTime;
import java.time.LocalTime;
import javax.annotation.Nonnull;

public class DayPlanningPeriod {
  private final LocalTime start;
  private final LocalTime end;

  public DayPlanningPeriod(LocalTime start, LocalTime end) {
    this.start = start;
    this.end = end;
  }

  public LocalTime getStart() {
    return start;
  }

  public LocalTime getEnd() {
    return end;
  }

  public boolean include(@Nonnull LocalDateTime dateTime) {
    return dateTime.toLocalTime().compareTo(start) >= 0
        && dateTime.toLocalTime().compareTo(end) <= 0;
  }
}
