package com.axelor.apps.helpdesk.rest.dto;

import com.axelor.apps.helpdesk.db.Ticket;
import com.axelor.utils.api.ResponseStructure;
import java.time.LocalDateTime;

public class TicketResponse extends ResponseStructure {

  private final Long id;
  private final String ticketSeq;
  private final Integer statusSelect;
  private final LocalDateTime startDateT;
  private final LocalDateTime endDateT;
  private final Long duration;

  public TicketResponse(Ticket ticket) {
    super(ticket.getVersion());
    this.id = ticket.getId();
    this.ticketSeq = ticket.getTicketSeq();
    this.statusSelect = ticket.getStatusSelect();
    this.startDateT = ticket.getStartDateT();
    this.endDateT = ticket.getEndDateT();
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

  public LocalDateTime getStartDateT() {
    return startDateT;
  }

  public LocalDateTime getEndDateT() {
    return endDateT;
  }

  public Long getDuration() {
    return duration;
  }
}
