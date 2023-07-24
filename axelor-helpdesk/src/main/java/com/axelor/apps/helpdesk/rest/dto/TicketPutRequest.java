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
package com.axelor.apps.helpdesk.rest.dto;

import com.axelor.apps.helpdesk.rest.service.TicketUpdateRestService;
import com.axelor.utils.api.RequestStructure;
import java.time.LocalDateTime;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;

public class TicketPutRequest extends RequestStructure {

  @NotNull private LocalDateTime dateTime;

  @NotNull
  @Pattern(
      regexp =
          TicketUpdateRestService.TICKET_START_STATUS
              + "|"
              + TicketUpdateRestService.TICKET_PAUSE_STATUS
              + "|"
              + TicketUpdateRestService.TICKET_RESET_STATUS
              + "|"
              + TicketUpdateRestService.TICKET_STOP_STATUS
              + "|"
              + TicketUpdateRestService.TICKET_CLOSE_STATUS,
      flags = Pattern.Flag.CASE_INSENSITIVE)
  private String targetStatus;

  public LocalDateTime getDateTime() {
    return dateTime;
  }

  public void setDateTime(LocalDateTime dateTime) {
    this.dateTime = dateTime;
  }

  public String getTargetStatus() {
    return targetStatus;
  }

  public void setTargetStatus(String targetStatus) {
    this.targetStatus = targetStatus;
  }
}
