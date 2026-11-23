/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2005-2026 Axelor (<http://axelor.com>).
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
package com.axelor.apps.helpdesk.service;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.service.publicHoliday.PublicHolidayService;
import com.axelor.apps.base.service.weeklyplanning.WeeklyPlanningService;
import com.axelor.apps.helpdesk.db.Sla;
import com.axelor.apps.helpdesk.db.Ticket;
import com.axelor.apps.helpdesk.db.TicketStatus;
import com.axelor.apps.helpdesk.db.repo.SlaRepository;
import com.axelor.apps.helpdesk.service.app.AppHelpdeskService;
import com.axelor.auth.AuthUtils;
import com.axelor.auth.db.User;
import com.axelor.studio.db.AppHelpdesk;
import com.axelor.team.db.Team;
import com.axelor.utils.helpers.date.DurationHelper;
import com.axelor.utils.helpers.date.LocalDateHelper;
import com.google.inject.Inject;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

public class TicketServiceImpl implements TicketService {

  protected AppHelpdeskService appHelpdeskService;

  protected SlaRepository slaRepo;

  protected PublicHolidayService publicHolidayService;

  protected WeeklyPlanningService weeklyPlanningService;

  private LocalDateTime toDate;

  @Inject
  public TicketServiceImpl(
      AppHelpdeskService appHelpdeskService,
      SlaRepository slaRepo,
      PublicHolidayService publicHolidayService,
      WeeklyPlanningService weeklyPlanningService) {
    this.appHelpdeskService = appHelpdeskService;
    this.slaRepo = slaRepo;
    this.publicHolidayService = publicHolidayService;
    this.weeklyPlanningService = weeklyPlanningService;
  }

  /**
   * Finds the SLA policy of the ticket and sets it on the ticket.
   *
   * <p>An SLA policy matches the ticket when its team, its ticket type and its minimum priority are
   * empty or match the ticket, and when the ticket status is still below its stage to reach. When
   * several policies match, the first one in the order of {@link #getSlaComparator()} is applied.
   * Returns null, and removes the SLA policy of the ticket, when no policy matches.
   */
  @Override
  public Sla computeSLA(Ticket ticket) {

    AppHelpdesk helpdesk = appHelpdeskService.getHelpdeskApp();
    Sla sla = null;

    if (helpdesk.getIsSla()) {
      int ticketStatusPriority =
          Optional.ofNullable(ticket.getTicketStatus()).map(TicketStatus::getPriority).orElse(0);

      sla =
          getMatchingSlaList(ticket).stream()
              .filter(
                  candidate ->
                      candidate.getReachStageTicketStatus() != null
                          && ticketStatusPriority
                              < candidate.getReachStageTicketStatus().getPriority())
              .min(getSlaComparator())
              .orElse(null);
    }
    ticket.setSlaPolicy(sla);
    return sla;
  }

  /**
   * SLA policies whose team, ticket type and minimum priority are empty or match the ticket. A
   * minimum priority applies to the tickets of this priority and above.
   */
  protected List<Sla> getMatchingSlaList(Ticket ticket) {
    return slaRepo
        .all()
        .filter(
            "(self.team IS NULL OR self.team = :team) "
                + "AND (self.ticketType IS NULL OR self.ticketType = :ticketType) "
                + "AND (self.prioritySelect IS NULL OR self.prioritySelect <= :prioritySelect)")
        .bind("team", getSlaTeam(ticket))
        .bind("ticketType", ticket.getTicketType())
        .bind("prioritySelect", ticket.getPrioritySelect())
        .fetch();
  }

  /** Team compared to the team of the SLA policies: the active team of the assigned user. */
  protected Team getSlaTeam(Ticket ticket) {
    return Optional.ofNullable(ticket.getAssignedToUser()).map(User::getActiveTeam).orElse(null);
  }

  /**
   * Order of the matching SLA policies, the first one being applied: a policy with a team before a
   * policy for any team, then a policy with a ticket type before a policy for any type, then the
   * highest minimum priority (an empty one counts as the lowest), then the closest stage to reach,
   * then the smallest id. The compared policies must have a stage to reach.
   */
  protected Comparator<Sla> getSlaComparator() {
    return Comparator.comparing((Sla sla) -> sla.getTeam() == null)
        .thenComparing(sla -> sla.getTicketType() == null)
        // getPrioritySelect() returns 0 for an empty minimum priority, below every priority
        .thenComparing(Sla::getPrioritySelect, Comparator.<Integer>reverseOrder())
        .thenComparingInt(sla -> sla.getReachStageTicketStatus().getPriority())
        .thenComparing(Sla::getId, Comparator.nullsLast(Comparator.<Long>naturalOrder()));
  }

  @Override
  public void computeSLAAndDeadLine(Ticket ticket) throws AxelorException {
    if (!appHelpdeskService.getHelpdeskApp().getIsSla()) {
      return;
    }

    Sla sla = computeSLA(ticket);

    if (sla != null) {
      this.computeDeadLine(ticket, sla);
    } else {
      ticket.setDeadlineDateT(null);
    }
  }

  /**
   * Calculate deadline date & time of the ticket.
   *
   * @param ticket
   * @param sla
   * @throws AxelorException
   */
  protected void computeDeadLine(Ticket ticket, Sla sla) throws AxelorException {

    // Check assignedUserCompany -> Current user company -> else null
    Company company =
        Optional.ofNullable(ticket.getAssignedToUser())
            .map(User::getActiveCompany)
            .orElse(
                Optional.ofNullable(AuthUtils.getUser()).map(User::getActiveCompany).orElse(null));

    if (sla.getIsWorkingDays() && company != null && company.getWeeklyPlanning() != null) {
      if (sla.getDays() > 0) {
        LocalDateTime fromDate = ticket.getStartDateT().plusDays(1);
        this.calculateWorkingDays(fromDate, company, sla.getDays());
        ticket.setDeadlineDateT(toDate.plusHours(sla.getHours()));
      } else {
        ticket.setDeadlineDateT(ticket.getStartDateT().plusHours(sla.getHours()));
      }
    } else {
      this.calculateAllDays(ticket, sla);
    }
  }

  /**
   * Calculate deadline date & time based on all days if workingDays field in SLA is not checked.
   *
   * @param ticket
   * @param sla
   */
  protected void calculateAllDays(Ticket ticket, Sla sla) {
    LocalDateTime localDateTime = ticket.getStartDateT().plusDays(sla.getDays());
    localDateTime = localDateTime.plusHours(sla.getHours());
    ticket.setDeadlineDateT(localDateTime);
  }

  /**
   * Calculate deadline date & time based on only working days if workingDays field in SLA is
   * checked.
   *
   * @param fromDate
   * @param employee
   * @param days
   * @throws AxelorException
   */
  protected void calculateWorkingDays(LocalDateTime fromDate, Company company, int days)
      throws AxelorException {

    if (weeklyPlanningService.getWorkingDayValueInDays(
                company.getWeeklyPlanning(), fromDate.toLocalDate())
            == 0
        || publicHolidayService.checkPublicHolidayDay(
            fromDate.toLocalDate(), company.getPublicHolidayEventsPlanning())) {

      fromDate = fromDate.plusDays(1);
      this.calculateWorkingDays(fromDate, company, days);

    } else {
      toDate = fromDate;
      days--;
      if (days != 0) {
        fromDate = fromDate.plusDays(1);
        this.calculateWorkingDays(fromDate, company, days);
      }
    }
  }

  /** Check if SLA is completed or not. */
  @Override
  public void checkSLAcompleted(Ticket ticket) {

    if (ticket.getSlaPolicy() != null) {
      LocalDateTime currentDate = LocalDateTime.now();
      LocalDateTime deadlineDateT = ticket.getDeadlineDateT();

      if (ticket.getTicketStatus() != null
          && ticket.getSlaPolicy().getReachStageTicketStatus() != null) {
        ticket.setIsSlaCompleted(
            ticket.getTicketStatus().getPriority()
                    >= ticket.getSlaPolicy().getReachStageTicketStatus().getPriority()
                && (currentDate.isBefore(deadlineDateT) || currentDate.isEqual(deadlineDateT)));
      }
    }
  }

  @Override
  public Long computeDuration(Ticket ticket) {
    if (ticket.getStartDateT() != null
        && ticket.getEndDateT() != null
        && ticket.getEndDateT().isAfter(ticket.getStartDateT())) {
      Duration duration =
          DurationHelper.computeDuration(ticket.getStartDateT(), ticket.getEndDateT());
      return DurationHelper.getSecondsDuration(duration);
    }

    return ticket.getDuration();
  }

  @Override
  public LocalDateTime computeEndDate(Ticket ticket) {
    if (ticket.getStartDateT() != null
        && ticket.getDuration() != null
        && ticket.getDuration() != 0) {
      return LocalDateHelper.plusSeconds(ticket.getStartDateT(), ticket.getDuration());
    }

    return ticket.getEndDateT();
  }

  @Override
  public LocalDateTime computeStartDate(Ticket ticket) {
    if (ticket.getEndDateT() != null && ticket.getDuration() != null && ticket.getDuration() != 0) {
      return LocalDateHelper.minusSeconds(ticket.getEndDateT(), ticket.getDuration());
    }

    return ticket.getStartDateT();
  }
}
