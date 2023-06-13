package com.axelor.apps.helpdesk.rest.dto;

import com.axelor.apps.helpdesk.db.Ticket;
import com.axelor.utils.api.ResponseStructure;

public class TicketResponse extends ResponseStructure {

  private final Long id;
  private final String ticketSeq;
  private final Integer statusSelect;
  private final String startDateT;
  private final String endDateT;
  private final Long duration;

  public TicketResponse(Ticket ticket) {
    super(ticket.getVersion());
    this.id = ticket.getId();
    this.ticketSeq = ticket.getTicketSeq();
    this.statusSelect = ticket.getStatusSelect();
    this.startDateT = ticket.getStartDateT().toString();
    this.endDateT = ticket.getEndDateT().toString();
    this.duration = ticket.getDuration();
  }

  public Long getId() {
    return id;
  }

  public String getTicketSeq() {
    return ticketSeq;
  }

  public Integer getStatusSelect() {
    return statusSelect;
  }

  public String getStartDateT() {
    return startDateT;
  }

  public String getEndDateT() {
    return endDateT;
  }

  public Long getDuration() {
    return duration;
  }
}
