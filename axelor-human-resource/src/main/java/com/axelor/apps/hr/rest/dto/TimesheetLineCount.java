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
package com.axelor.apps.hr.rest.dto;

import com.axelor.utils.api.ResponseStructure;
import java.math.BigDecimal;

public class TimesheetLineCount extends ResponseStructure {

  private BigDecimal duration;
  private BigDecimal hoursDuration;
  private BigDecimal weeklyPlanningDuration;
  private BigDecimal weeklyPlanningHoursDuration;
  private BigDecimal leaveDuration;
  private BigDecimal leaveHoursDuration;
  private String leaveReason;

  public TimesheetLineCount(
      BigDecimal duration,
      BigDecimal hoursDuration,
      BigDecimal weeklyPlanningDuration,
      BigDecimal weeklyPlanningHoursDuration,
      BigDecimal leaveDuration,
      BigDecimal leaveHoursDuration,
      String leaveReason) {
    super(0);
    this.duration = duration;
    this.hoursDuration = hoursDuration;
    this.weeklyPlanningDuration = weeklyPlanningDuration;
    this.weeklyPlanningHoursDuration = weeklyPlanningHoursDuration;
    this.leaveDuration = leaveDuration;
    this.leaveHoursDuration = leaveHoursDuration;
    this.leaveReason = leaveReason;
  }

  public BigDecimal getDuration() {
    return duration;
  }

  public void setDuration(BigDecimal duration) {
    this.duration = duration;
  }

  public BigDecimal getHoursDuration() {
    return hoursDuration;
  }

  public void setHoursDuration(BigDecimal hoursDuration) {
    this.hoursDuration = hoursDuration;
  }

  public BigDecimal getWeeklyPlanningDuration() {
    return weeklyPlanningDuration;
  }

  public void setWeeklyPlanningDuration(BigDecimal weeklyPlanningDuration) {
    this.weeklyPlanningDuration = weeklyPlanningDuration;
  }

  public BigDecimal getWeeklyPlanningHoursDuration() {
    return weeklyPlanningHoursDuration;
  }

  public void setWeeklyPlanningHoursDuration(BigDecimal weeklyPlanningHoursDuration) {
    this.weeklyPlanningHoursDuration = weeklyPlanningHoursDuration;
  }

  public BigDecimal getLeaveDuration() {
    return leaveDuration;
  }

  public void setLeaveDuration(BigDecimal leaveDuration) {
    this.leaveDuration = leaveDuration;
  }

  public BigDecimal getLeaveHoursDuration() {
    return leaveHoursDuration;
  }

  public void setLeaveHoursDuration(BigDecimal leaveHoursDuration) {
    this.leaveHoursDuration = leaveHoursDuration;
  }

  public String getLeaveReason() {
    return leaveReason;
  }

  public void setLeaveReason(String leaveReason) {
    this.leaveReason = leaveReason;
  }
}
