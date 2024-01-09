package com.axelor.apps.helpdesk.service;

import com.axelor.apps.helpdesk.db.TicketStatus;

public interface TicketStatusService {

  TicketStatus findDefaultStatus();

  TicketStatus findOngoingStatus();

  TicketStatus findResolvedStatus();

  TicketStatus findClosedStatus();
}
