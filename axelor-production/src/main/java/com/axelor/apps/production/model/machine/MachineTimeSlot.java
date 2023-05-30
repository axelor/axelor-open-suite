package com.axelor.apps.production.model.machine;

import java.time.LocalDateTime;
import java.util.Objects;

public class MachineTimeSlot {

  private final LocalDateTime startDateT;
  private final LocalDateTime endDateT;

  public MachineTimeSlot(LocalDateTime startDateT, LocalDateTime endDateT) {
    this.startDateT = Objects.requireNonNull(startDateT);
    this.endDateT = Objects.requireNonNull(endDateT);
  }

  public LocalDateTime getStartDateT() {
    return startDateT;
  }

  public LocalDateTime getEndDateT() {
    return endDateT;
  }
}
