/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2005-2024 Axelor (<http://axelor.com>).
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
package com.axelor.apps.helpdesk.web;

import com.axelor.apps.base.ResponseMessageType;
import com.axelor.apps.base.db.Timer;
import com.axelor.apps.base.db.repo.TimerRepository;
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.apps.base.service.exception.TraceBackService;
import com.axelor.apps.helpdesk.db.Ticket;
import com.axelor.apps.helpdesk.db.repo.TicketRepository;
import com.axelor.apps.helpdesk.exceptions.HelpdeskExceptionMessage;
import com.axelor.apps.helpdesk.service.TicketService;
import com.axelor.apps.helpdesk.service.TimerTicketService;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.axelor.utils.date.DateTool;
import com.axelor.utils.date.DurationTool;
import com.google.inject.Singleton;
import com.google.inject.persist.Transactional;
import java.math.BigDecimal;
import java.time.Duration;
import java.util.List;

@Singleton
public class TicketController {

  private static final String HIDDEN_ATTR = "hidden";

  /**
   * Ticket assign to the current user.
   *
   * @param request
   * @param response
   */
  public void assignToMeTicket(ActionRequest request, ActionResponse response) {
    try {
      Long id = (Long) request.getContext().get("id");
      List<?> ids = (List<?>) request.getContext().get("_ids");

      if (id == null && ids == null) {
        response.setAlert(I18n.get(HelpdeskExceptionMessage.SELECT_TICKETS));
      } else {
        Beans.get(TicketService.class).assignToMeTicket(id, ids);
        response.setReload(true);
      }
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  /**
   * Compute duration or endDateTime from startDateTime
   *
   * @param request
   * @param response
   */
  public void computeFromStartDateTime(ActionRequest request, ActionResponse response) {
    try {
      Ticket ticket = request.getContext().asType(Ticket.class);

      if (ticket.getStartDateT() != null) {
        if (ticket.getDuration() != null && ticket.getDuration() != 0) {
          response.setValue(
              "endDateT", DateTool.plusSeconds(ticket.getStartDateT(), ticket.getDuration()));

        } else if (ticket.getEndDateT() != null
            && ticket.getEndDateT().isAfter(ticket.getStartDateT())) {
          Duration duration =
              DurationTool.computeDuration(ticket.getStartDateT(), ticket.getEndDateT());
          response.setValue("duration", DurationTool.getSecondsDuration(duration));
        }
      }
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  /**
   * Compute startDateTime or endDateTime from duration
   *
   * @param request
   * @param response
   */
  public void computeFromDuration(ActionRequest request, ActionResponse response) {
    try {
      Ticket ticket = request.getContext().asType(Ticket.class);

      if (ticket.getDuration() != null) {
        if (ticket.getStartDateT() != null) {
          response.setValue(
              "endDateT", DateTool.plusSeconds(ticket.getStartDateT(), ticket.getDuration()));

        } else if (ticket.getEndDateT() != null) {
          response.setValue(
              "startDateT", DateTool.minusSeconds(ticket.getEndDateT(), ticket.getDuration()));
        }
      }
    } catch (Exception e) {
      TraceBackService.trace(e);
    }
  }

  /**
   * Compute duration or startDateTime from endDateTime
   *
   * @param request
   * @param response
   */
  public void computeFromEndDateTime(ActionRequest request, ActionResponse response) {
    try {
      Ticket ticket = request.getContext().asType(Ticket.class);

      if (ticket.getEndDateT() != null) {

        if (ticket.getStartDateT() != null
            && ticket.getStartDateT().isBefore(ticket.getEndDateT())) {
          Duration duration =
              DurationTool.computeDuration(ticket.getStartDateT(), ticket.getEndDateT());
          response.setValue("duration", DurationTool.getSecondsDuration(duration));

        } else if (ticket.getDuration() != null) {
          response.setValue(
              "startDateT", DateTool.minusSeconds(ticket.getEndDateT(), ticket.getDuration()));
        }
      }
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  public void manageTimerButtons(ActionRequest request, ActionResponse response) {
    try {
      Ticket ticket = request.getContext().asType(Ticket.class);
      TimerTicketService service = Beans.get(TimerTicketService.class);

      Timer timer = service.find(ticket);

      response.setAttr(
          "startTimerBtn",
          HIDDEN_ATTR,
          timer == null
              || timer.getStatusSelect() == TimerRepository.TIMER_STARTED
              || ticket.getStatusSelect() != TicketRepository.STATUS_IN_PROGRESS);
      response.setAttr(
          "stopTimerBtn",
          HIDDEN_ATTR,
          timer == null
              || timer.getStatusSelect() != TimerRepository.TIMER_STARTED
              || ticket.getStatusSelect() != TicketRepository.STATUS_IN_PROGRESS);
      response.setAttr(
          "cancelTimerBtn",
          HIDDEN_ATTR,
          timer == null
              || timer.getTimerHistoryList().isEmpty()
              || timer.getStatusSelect().equals(TimerRepository.TIMER_STOPPED)
              || ticket.getStatusSelect() != TicketRepository.STATUS_IN_PROGRESS);
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  public void computeTotalTimerDuration(ActionRequest request, ActionResponse response) {
    try {
      Ticket ticket = request.getContext().asType(Ticket.class);
      if (ticket.getId() != null) {
        Duration duration = Beans.get(TimerTicketService.class).compute(ticket);
        response.setValue("$_totalTimerDuration", duration.toMinutes() / 60F);
      }
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  public void startTimer(ActionRequest request, ActionResponse response) {
    try {
      Ticket ticket = request.getContext().asType(Ticket.class);
      Beans.get(TimerTicketService.class)
          .start(ticket, Beans.get(AppBaseService.class).getTodayDateTime().toLocalDateTime());
    } catch (Exception e) {
      TraceBackService.trace(response, e, ResponseMessageType.ERROR);
    }
  }

  public void stopTimer(ActionRequest request, ActionResponse response) {
    try {
      Ticket ticket = request.getContext().asType(Ticket.class);
      Beans.get(TimerTicketService.class)
          .stop(ticket, Beans.get(AppBaseService.class).getTodayDateTime().toLocalDateTime());
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  public void cancelTimer(ActionRequest request, ActionResponse response) {
    try {
      Ticket ticket = request.getContext().asType(Ticket.class);
      Beans.get(TimerTicketService.class).cancel(ticket);
      response.setReload(true);
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  public void computeRealDuration(ActionRequest request, ActionResponse response) {
    try {
      Ticket ticket = request.getContext().asType(Ticket.class);
      if (ticket.getId() != null && ticket.getRealTotalDuration().compareTo(BigDecimal.ZERO) == 0) {
        response.setValue(
            "realTotalDuration",
            Beans.get(TimerTicketService.class).compute(ticket).toMinutes() / 60F);
      }

    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  @Transactional
  public void timerStateOn(ActionRequest request, ActionResponse response) {
    try {
      TicketRepository ticketRepo = Beans.get(TicketRepository.class);
      Ticket ticket = request.getContext().asType(Ticket.class);
      ticket = ticketRepo.find(ticket.getId());
      ticket.setTimerState(true);
      ticketRepo.save(ticket);
      response.setReload(true);
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  @Transactional
  public void timerStateOff(ActionRequest request, ActionResponse response) {
    try {
      TicketRepository ticketRepo = Beans.get(TicketRepository.class);
      Ticket ticket = request.getContext().asType(Ticket.class);
      ticket = ticketRepo.find(ticket.getId());
      ticket.setTimerState(false);
      ticketRepo.save(ticket);
      response.setReload(true);
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }
}
