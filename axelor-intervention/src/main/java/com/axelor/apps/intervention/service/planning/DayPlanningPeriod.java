package com.axelor.apps.intervention.service.planning;

import java.time.LocalDateTime;
import java.time.LocalTime;
import javax.annotation.Nonnull;

public class DayPlanningPeriod {
  private final LocalTime start;
  private final LocalTime end;

  public DayPlanningPeriod(LocalTime start, LocalTime end) {
    this.start = start;
    this.end = end;
  }

  public LocalTime getStart() {
    return start;
  }

  public LocalTime getEnd() {
    return end;
  }

  public boolean include(@Nonnull LocalDateTime dateTime) {
    return dateTime.toLocalTime().compareTo(start) >= 0
        && dateTime.toLocalTime().compareTo(end) <= 0;
  }
}
