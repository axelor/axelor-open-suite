package com.axelor.apps.helpdesk.rest.service;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.helpdesk.db.Ticket;
import java.time.LocalDateTime;

public interface TicketUpdateRestService {

  String TICKET_START_STATUS = "start";
  String TICKET_PAUSE_STATUS = "pause";
  String TICKET_STOP_STATUS = "stop";
  String TICKET_RESET_STATUS = "reset";
  String TICKET_CLOSE_STATUS = "validate";

  Ticket updateTicketStatus(Ticket ticket, String targetStatus, LocalDateTime dateTime)
      throws AxelorException;
}
