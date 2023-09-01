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
package com.axelor.apps.helpdesk.rest.service;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.apps.helpdesk.db.Ticket;
import com.axelor.apps.helpdesk.db.repo.TicketRepository;
import com.axelor.apps.helpdesk.exceptions.HelpdeskExceptionMessage;
import com.axelor.apps.helpdesk.service.TicketService;
import com.axelor.apps.helpdesk.service.TimerTicketService;
import com.axelor.i18n.I18n;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;

public class TicketUpdateRestServiceImpl implements TicketUpdateRestService {

  protected TicketService ticketService;
  protected TimerTicketService timerTicketService;
  protected TicketRepository ticketRepository;

  @Inject
  public TicketUpdateRestServiceImpl(
      TicketService ticketService,
      TimerTicketService timerTicketService,
      TicketRepository ticketRepository) {
    this.ticketService = ticketService;
    this.timerTicketService = timerTicketService;
    this.ticketRepository = ticketRepository;
  }

  @Override
  @Transactional(rollbackOn = {Exception.class})
  public Ticket updateTicketStatus(Ticket ticket, String targetStatus, LocalDateTime dateTime)
      throws AxelorException {

    if (ticket.getStatusSelect() == TicketRepository.STATUS_NEW) {
      if (Objects.equals(targetStatus, TicketUpdateRestService.TICKET_START_STATUS)) {
        return handleStart(ticket, dateTime);
      } else {
        throw new AxelorException(
            TraceBackRepository.CATEGORY_INCONSISTENCY,
            I18n.get(HelpdeskExceptionMessage.UPDATE_TICKET_WORKFLOW));
      }
    }

    if (ticket.getStatusSelect() == TicketRepository.STATUS_IN_PROGRESS) {
      if (Objects.equals(targetStatus, TicketUpdateRestService.TICKET_START_STATUS)) {
        return handleStart(ticket, dateTime);
      } else if (Objects.equals(targetStatus, TicketUpdateRestService.TICKET_PAUSE_STATUS)) {
        return handlePause(ticket, dateTime);
      } else if (Objects.equals(targetStatus, TicketUpdateRestService.TICKET_RESET_STATUS)) {
        return handleCancel(ticket);
      } else if (Objects.equals(targetStatus, TicketUpdateRestService.TICKET_STOP_STATUS)) {
        return handleStop(ticket, dateTime);
      } else if (Objects.equals(targetStatus, TicketUpdateRestService.TICKET_CLOSE_STATUS)) {
        return handleClose(ticket, dateTime);
      } else {
        throw new AxelorException(
            TraceBackRepository.CATEGORY_INCONSISTENCY,
            I18n.get(HelpdeskExceptionMessage.UPDATE_TICKET_WORKFLOW));
      }
    }

    if (ticket.getStatusSelect() == TicketRepository.STATUS_RESOLVED) {
      if (Objects.equals(targetStatus, TicketUpdateRestService.TICKET_CLOSE_STATUS)) {
        return handleClose(ticket, dateTime);
      } else {
        throw new AxelorException(
            TraceBackRepository.CATEGORY_INCONSISTENCY,
            I18n.get(HelpdeskExceptionMessage.UPDATE_TICKET_WORKFLOW));
      }
    }

    throw new AxelorException(
        TraceBackRepository.CATEGORY_INCONSISTENCY,
        I18n.get(HelpdeskExceptionMessage.UPDATE_TICKET_WORKFLOW));
  }

  @Transactional(rollbackOn = {Exception.class})
  public Ticket handleStart(Ticket ticket, LocalDateTime startDateT) throws AxelorException {
    timerTicketService.start(ticket, startDateT);
    ticket.setTimerState(true);

    if (ticket.getStatusSelect() == TicketRepository.STATUS_NEW) {
      ticket.setStartDateT(startDateT);

      if (ticket.getDuration() != null && ticket.getDuration() != 0) {
        ticket.setEndDateT(ticketService.computeEndDate(ticket));
      } else if (ticket.getEndDateT() != null
          && ticket.getEndDateT().isAfter(ticket.getStartDateT())) {
        ticket.setDuration(ticketService.computeDuration(ticket));
      }

      ticket.setStatusSelect(TicketRepository.STATUS_IN_PROGRESS);
    }

    return ticketRepository.save(ticket);
  }

  @Transactional(rollbackOn = {Exception.class})
  public Ticket handlePause(Ticket ticket, LocalDateTime pauseDateT) throws AxelorException {
    timerTicketService.stop(ticket, pauseDateT);
    ticket.setTimerState(false);

    return ticketRepository.save(ticket);
  }

  @Transactional(rollbackOn = {Exception.class})
  public Ticket handleCancel(Ticket ticket) throws AxelorException {
    timerTicketService.cancel(ticket);

    return ticket;
  }

  @Transactional(rollbackOn = {Exception.class})
  public Ticket handleStop(Ticket ticket, LocalDateTime endDateT) throws AxelorException {
    timerTicketService.stop(ticket, endDateT);
    ticket.setTimerState(false);

    if (BigDecimal.ZERO.compareTo(ticket.getRealTotalDuration()) == 0) {
      ticket.setRealTotalDuration(
          BigDecimal.valueOf(timerTicketService.compute(ticket).toMinutes() / 60F));
    }

    ticket.setEndDateT(endDateT);

    if (ticket.getStartDateT() != null && ticket.getStartDateT().isBefore(ticket.getEndDateT())) {
      ticket.setDuration(ticketService.computeDuration(ticket));
    } else if (ticket.getDuration() != null && ticket.getDuration() != 0) {
      ticket.setStartDateT(ticketService.computeStartDate(ticket));
    }

    ticket.setStatusSelect(TicketRepository.STATUS_RESOLVED);
    ticket.setProgressSelect(100);

    return ticketRepository.save(ticket);
  }

  @Transactional(rollbackOn = {Exception.class})
  public Ticket handleClose(Ticket ticket, LocalDateTime endDateT) throws AxelorException {
    timerTicketService.stop(ticket, endDateT);
    ticket.setTimerState(false);

    if (BigDecimal.ZERO.compareTo(ticket.getRealTotalDuration()) == 0) {
      ticket.setRealTotalDuration(
          BigDecimal.valueOf(timerTicketService.compute(ticket).toMinutes() / 60F));
    }

    ticket.setStatusSelect(TicketRepository.STATUS_CLOSED);

    return ticketRepository.save(ticket);
  }
}
