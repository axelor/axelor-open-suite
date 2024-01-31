package com.axelor.apps.base.interfaces;

import java.time.LocalDate;

/** A date interval, e.g. a model that has a "from" date and a "to" date. */
public interface LocalDateInterval {

  LocalDate getFromDate();

  LocalDate getToDate();

  Long getId();
}
