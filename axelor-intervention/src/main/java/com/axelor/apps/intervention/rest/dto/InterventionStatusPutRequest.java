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
package com.axelor.apps.intervention.rest.dto;

import com.axelor.utils.api.RequestStructure;
import java.time.LocalDateTime;
import javax.validation.constraints.NotNull;

public class InterventionStatusPutRequest extends RequestStructure {

  @NotNull private Integer toStatus;

  private LocalDateTime dateTime;

  private Long plannedDuration;

  private Long plannedTechnicianUserId;

  public Integer getToStatus() {
    return toStatus;
  }

  public void setToStatus(Integer toStatus) {
    this.toStatus = toStatus;
  }

  public LocalDateTime getDateTime() {
    return dateTime;
  }

  public void setDateTime(LocalDateTime dateTime) {
    this.dateTime = dateTime;
  }

  public Long getPlannedDuration() {
    return plannedDuration;
  }

  public void setPlannedDuration(Long plannedDuration) {
    this.plannedDuration = plannedDuration;
  }

  public Long getPlannedTechnicianUserId() {
    return plannedTechnicianUserId;
  }

  public void setPlannedTechnicianUserId(Long plannedTechnicianUserId) {
    this.plannedTechnicianUserId = plannedTechnicianUserId;
  }
}
