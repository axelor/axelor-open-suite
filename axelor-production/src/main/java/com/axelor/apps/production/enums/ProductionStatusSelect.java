package com.axelor.apps.production.enums;

import com.axelor.db.ValueEnum;
import java.util.Objects;

public enum ProductionStatusSelect implements ValueEnum<String> {
  PRODUCTION_STATUS_FINISHED("FINISHED"),
  PRODUCTION_STATUS_STANDBY("STANDBY"),
  PRODUCTION_STATUS_IN_PROGRESS("IN_PROGRESS"),
  PRODUCTION_STATUS_PLANNED("PLANNED"),
  PRODUCTION_STATUS_CANCELED("CANCELED"),
  PRODUCTION_STATUS_DRAFT("DRAFT");

  private final String value;

  ProductionStatusSelect(String value) {
    this.value = Objects.requireNonNull(value);
  }

  @Override
  public String getValue() {
    return value;
  }
}
