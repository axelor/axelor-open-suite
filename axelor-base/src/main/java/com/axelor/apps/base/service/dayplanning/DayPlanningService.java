package com.axelor.apps.base.service.dayplanning;

import com.axelor.apps.base.db.DayPlanning;
import java.time.LocalDateTime;
import java.util.Optional;

public interface DayPlanningService {

  /**
   * This method will return a allowed start dateTime for dateT in the dayPlanning
   *
   * <p>If dateT is in one the two periods of the day planning, then it will return itself. If dateT
   * is not in any of the two periods but is before the start of one of them, it will return a
   * startDateTime at the localTime of the next period. If dateT is after the two periods of the
   * day, it will look for the next day.
   *
   * <p>The method will return a empty optional if we can't find any period to start. (Happens if
   * day planning exist but there is not period specified in it), also a null dayPlanning will be
   * considered as a allowed day for any hour and a call with such value will result a return a
   * Optional of dateT
   *
   * @param dayPlanning
   * @param dateT
   * @return Optional.empty if we can't find any period, else a Optional of dateTime
   */
  Optional<LocalDateTime> getAllowedStartDateTPeriodAt(
      DayPlanning dayPlanning, LocalDateTime dateT);

  /**
   * This method will compute the "void" (time where not there are no period) between startDateT and
   * endDateT
   *
   * @param startDateT
   * @param endDateT
   * @return the void duration in seconds
   */
  long computeVoidDurationBetween(
      DayPlanning dayPlanning, LocalDateTime startDateT, LocalDateTime endDateT);
}
