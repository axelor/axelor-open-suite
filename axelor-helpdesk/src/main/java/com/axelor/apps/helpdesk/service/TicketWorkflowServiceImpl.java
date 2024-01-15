package com.axelor.apps.helpdesk.service;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.apps.helpdesk.db.Ticket;
import com.axelor.apps.helpdesk.db.TicketStatus;
import com.axelor.apps.helpdesk.db.repo.TicketRepository;
import com.axelor.apps.helpdesk.exceptions.HelpdeskExceptionMessage;
import com.axelor.i18n.I18n;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.util.Objects;
import java.util.Optional;

public class TicketWorkflowServiceImpl implements TicketWorkflowService {

  protected TicketStatusService ticketStatusService;
  protected TicketRepository ticketRepository;

  @Inject
  public TicketWorkflowServiceImpl(
      TicketStatusService ticketStatusService, TicketRepository ticketRepository) {
    this.ticketStatusService = ticketStatusService;
    this.ticketRepository = ticketRepository;
  }

  @Override
  @Transactional(rollbackOn = {Exception.class})
  public void startTicket(Ticket ticket) throws AxelorException {
    Objects.requireNonNull(ticket);

    TicketStatus ticketStatus =
        Optional.ofNullable(ticketStatusService.findOngoingStatus())
            .orElseThrow(
                () ->
                    new AxelorException(
                        TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
                        I18n.get(HelpdeskExceptionMessage.ON_GOING_TICKET_STATUS_DONT_EXIST)));

    ticket.setTicketStatus(ticketStatus);
    ticketRepository.save(ticket);
  }

  @Override
  @Transactional(rollbackOn = {Exception.class})
  public void resolveTicket(Ticket ticket) throws AxelorException {
    Objects.requireNonNull(ticket);

    TicketStatus ticketStatus =
        Optional.ofNullable(ticketStatusService.findResolvedStatus())
            .orElseThrow(
                () ->
                    new AxelorException(
                        TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
                        I18n.get(HelpdeskExceptionMessage.RESOLVED_TICKET_STATUS_DONT_EXIST)));

    ticket.setTicketStatus(ticketStatus);
    ticket.setProgressSelect(100);
    ticketRepository.save(ticket);
  }

  @Override
  @Transactional(rollbackOn = {Exception.class})
  public void closeTicket(Ticket ticket) throws AxelorException {
    Objects.requireNonNull(ticket);

    TicketStatus ticketStatus =
        Optional.ofNullable(ticketStatusService.findClosedStatus())
            .orElseThrow(
                () ->
                    new AxelorException(
                        TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
                        I18n.get(HelpdeskExceptionMessage.CLOSED_TICKET_STATUS_DONT_EXIST)));

    ticket.setTicketStatus(ticketStatus);
    ticketRepository.save(ticket);
  }

  @Override
  public void openTicket(Ticket ticket) throws AxelorException {
    Objects.requireNonNull(ticket);

    TicketStatus ticketStatus =
        Optional.ofNullable(ticketStatusService.findDefaultStatus())
            .orElseThrow(
                () ->
                    new AxelorException(
                        TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
                        I18n.get(HelpdeskExceptionMessage.DEFAULT_TICKET_STATUS_DONT_EXIST)));

    ticket.setTicketStatus(ticketStatus);
  }
}
