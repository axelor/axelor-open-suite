/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2019 Axelor (<http://axelor.com>).
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
package com.axelor.apps.tool.date;

import java.time.Duration;
import java.time.LocalDateTime;

public class DurationTool {

  private DurationTool() {
    throw new IllegalStateException("Utility class");
  }

  public static Duration computeDuration(LocalDateTime startDateTime, LocalDateTime endDateTime) {
    return Duration.between(startDateTime, endDateTime);
  }

  public static long getDaysDuration(Duration duration) {
    return duration.toDays();
  }

  public static long getHoursDuration(Duration duration) {
    return duration.toHours();
  }

  public static long getMinutesDuration(Duration duration) {
    return duration.toMinutes();
  }

  public static long getSecondsDuration(Duration duration) {

    return duration.getSeconds();
  }
}
