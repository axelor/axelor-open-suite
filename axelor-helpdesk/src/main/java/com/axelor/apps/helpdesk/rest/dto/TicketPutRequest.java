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
