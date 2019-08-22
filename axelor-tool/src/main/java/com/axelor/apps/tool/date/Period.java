/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2019 Axelor (<http://axelor.com>).
 *
 * This program is free software: you can redistribute it and/or  modify
 * it under the terms of the GNU Affero General Public License, version 3,
 * as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.axelor.apps.tool.date;

import com.axelor.apps.tool.exception.IExceptionMessage;
import com.axelor.i18n.I18n;
import com.google.inject.Inject;
import java.time.LocalDate;

/**
 * A period is composed of a start date, an end date and a boolean attribute to determine if the
 * year is fixed to 360 days.
 *
 * <p>Seems to be unused since there is another object Period in base module.
 */
@Deprecated
public class Period {

  private LocalDate from;
  private LocalDate to;
  private boolean days360;

  @Inject
  public Period() {}

  public LocalDate getFrom() {
    return from;
  }

  public void setFrom(LocalDate from) {
    this.from = from;
  }

  public LocalDate getTo() {
    return to;
  }

  public void setTo(LocalDate to) {
    this.to = to;
  }

  public boolean isDays360() {
    return days360;
  }

  public void setDays360(boolean days360) {
    this.days360 = days360;
  }

  public Period(boolean days360) {
    this.days360 = days360;
  }

  public Period(LocalDate from, LocalDate to, boolean days360) {

    this.from = from;
    this.to = to;
    this.days360 = days360;
  }

  public Period(LocalDate from, LocalDate to) {

    this.from = from;
    this.to = to;
    this.days360 = false;
  }

  public Period(Period p) {

    this.from = p.getFrom();
    this.to = p.getTo();
    this.days360 = p.isDays360();
  }

  public long getDays() {
    return DateTool.daysBetween(this.from, this.to, this.days360);
  }

  public long getMonths() {

    if (this.days360) {
      return DateTool.days360MonthsBetween(this.from, this.to);
    } else {
      return java.time.Period.between(this.from, this.to).getMonths();
    }
  }

  public Period prorata(Period period) {
    return prorata(period.getFrom(), period.getTo());
  }

  public Period prorata(LocalDate date1, LocalDate date2) {

    Period p = null;

    if (DateTool.isProrata(this.from, this.to, date1, date2)) {

      p = new Period(this);

      if (date1.isAfter(this.from)) {
        p.setFrom(date1);
      }

      if (date2 != null && date2.isBefore(this.to)) {
        p.setTo(date2);
      }
    }

    return p;
  }

  public boolean isProrata(Period period) {
    return DateTool.isProrata(this.from, this.to, period.getFrom(), period.getTo());
  }

  public boolean fromBetween(LocalDate date1, LocalDate date2) {

    return DateTool.isBetween(date1, date2, this.from);
  }

  public boolean toBetween(LocalDate date1, LocalDate date2) {

    return DateTool.isBetween(date1, date2, this.to);
  }

  public boolean contains(LocalDate date) {
    return DateTool.isBetween(this.from, this.to, date);
  }

  public boolean isNotNull() {
    return this.getFrom() != null && this.getTo() != null;
  }

  @Override
  public boolean equals(Object obj) {

    if (obj == this) {
      return true;
    }

    if (obj instanceof Period) {

      Period period = (Period) obj;
      return this.from.equals(period.getFrom())
          && this.to.equals(period.getTo())
          && this.days360 == period.isDays360();
    }

    return false;
  }

  @Override
  public int hashCode() {

    if (days360) {
      return from.hashCode() ^ to.hashCode();
    } else {
      return from.hashCode() ^ to.hashCode() * -1;
    }
  }

  @Override
  public String toString() {
    return this.from
        + " - "
        + this.to
        + "("
        + I18n.get(IExceptionMessage.PERIOD_1)
        + " :"
        + this.days360
        + ")";
  }
}
