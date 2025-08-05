package com.axelor.apps.base.service.publicHoliday;

import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.EventsPlanning;
import com.axelor.apps.base.db.EventsPlanningLine;
import com.axelor.apps.base.db.WeeklyPlanning;
import com.axelor.apps.base.db.repo.EventsPlanningLineRepository;
import com.axelor.apps.base.service.weeklyplanning.WeeklyPlanningService;
import com.axelor.common.ObjectUtils;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Objects;

public class PublicHolidayServiceImpl implements PublicHolidayService {

  protected WeeklyPlanningService weeklyPlanningService;
  protected EventsPlanningLineRepository eventsPlanningLineRepo;

  @Inject
  public PublicHolidayServiceImpl(
      WeeklyPlanningService weeklyPlanningService,
      EventsPlanningLineRepository eventsPlanningLineRepo) {

    this.weeklyPlanningService = weeklyPlanningService;
    this.eventsPlanningLineRepo = eventsPlanningLineRepo;
  }

  @Override
  public BigDecimal computePublicHolidayDays(
      LocalDate fromDate,
      LocalDate toDate,
      WeeklyPlanning weeklyPlanning,
      EventsPlanning publicHolidayPlanning) {
    BigDecimal publicHolidayDays = BigDecimal.ZERO;

    List<EventsPlanningLine> publicHolidayDayList =
        eventsPlanningLineRepo
            .all()
            .filter(
                "self.eventsPlanning = ?1 AND self.date BETWEEN ?2 AND ?3",
                publicHolidayPlanning,
                fromDate,
                toDate)
            .fetch();
    for (EventsPlanningLine publicHolidayDay : publicHolidayDayList) {
      publicHolidayDays =
          publicHolidayDays.add(
              BigDecimal.valueOf(
                  weeklyPlanningService.getWorkingDayValueInDays(
                      weeklyPlanning, publicHolidayDay.getDate())));
    }
    return publicHolidayDays;
  }

  /**
   * Returns true if the given date is a public holiday in the given public holiday events planning.
   *
   * @param date
   * @param publicHolidayEventsPlanning
   * @return
   */
  @Override
  public boolean checkPublicHolidayDay(LocalDate date, EventsPlanning publicHolidayEventsPlanning) {

    if (publicHolidayEventsPlanning == null) {
      return false;
    }

    List<EventsPlanningLine> publicHolidayDayList =
        getPublicHolidayList(date, publicHolidayEventsPlanning);
    return ObjectUtils.notEmpty(publicHolidayDayList);
  }

  @Override
  public List<EventsPlanningLine> getPublicHolidayList(
      LocalDate date, EventsPlanning publicHolidayEventsPlanning) {
    return eventsPlanningLineRepo
        .all()
        .filter("self.eventsPlanning = ?1 AND self.date = ?2", publicHolidayEventsPlanning, date)
        .fetch();
  }

  @Transactional(rollbackOn = Exception.class)
  @Override
  public void generateEventsPlanningLines(
      EventsPlanning eventsPlanning, LocalDate startDate, LocalDate endDate, String description) {
    for (LocalDate date = startDate; !date.isAfter(endDate); date = date.plusDays(1)) {
      EventsPlanningLine eventsPlanningLine = new EventsPlanningLine();
      eventsPlanningLine.setYear(date.getYear());
      eventsPlanningLine.setDate(date);
      eventsPlanningLine.setDescription(description);
      eventsPlanningLine.setEventsPlanning(eventsPlanning);
      eventsPlanningLineRepo.save(eventsPlanningLine);
    }
  }

  @Override
  public LocalDate getFreeDay(LocalDate date, Company company) {
    Objects.requireNonNull(date);
    Objects.requireNonNull(company);

    var publicHolidayEventsPlanning = company.getPublicHolidayEventsPlanning();
    var weeklyPlanning = company.getWeeklyPlanning();

    if (publicHolidayEventsPlanning == null && weeklyPlanning == null) {
      return date;
    }

    if (!checkPublicHolidayDay(date, publicHolidayEventsPlanning)
        && weeklyPlanningService.isWorkingDay(weeklyPlanning, date)) {
      return date;
    }

    return getFreeDay(date.plusDays(1), company);
  }
}
