/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2020 Axelor (<http://axelor.com>).
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

import static java.time.temporal.ChronoUnit.DAYS;

import java.lang.invoke.MethodHandles;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DateTool {

  private DateTool() {
    throw new IllegalStateException("Utility class");
  }

  private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  public static long daysBetween(LocalDate date1, LocalDate date2, boolean days360) {

    long days = 0;

    if (days360) {
      days = date1.isBefore(date2) ? days360Between(date1, date2) : -days360Between(date2, date1);
    } else {
      days = daysBetween(date1, date2);
    }

    LOG.debug(
        "Number of days between {} - {} (month of 30 days ? {}) : {}", date1, date2, days360, days);
    return days;
  }

  private static long daysBetween(LocalDate date1, LocalDate date2) {

    if (date2.isBefore(date1)) {
      return DAYS.between(date1, date2) - 1;
    } else {
      return DAYS.between(date1, date2) + 1;
    }
  }

  private static int days360Between(LocalDate startDate, LocalDate endDate) {

    int nbDayOfFirstMonth = 0;
    int nbDayOfOthersMonths = 0;
    int nbDayOfLastMonth = 0;

    LocalDate start = startDate;

    if (endDate.getMonthValue() != startDate.getMonthValue()
        || endDate.getYear() != startDate.getYear()) {

      // First month :: if the startDate is not the last day of the month
      if (!startDate.isEqual(startDate.withDayOfMonth(startDate.lengthOfMonth()))) {
        nbDayOfFirstMonth = 30 - startDate.getDayOfMonth();
      }

      // The startDate is included
      nbDayOfFirstMonth = nbDayOfFirstMonth + 1;

      // Months between the first one and the last one
      LocalDate date1 = startDate.plusMonths(1).withDayOfMonth(1);
      while (endDate.getMonthValue() != date1.getMonthValue()
          || endDate.getYear() != date1.getYear()) {

        nbDayOfOthersMonths = nbDayOfOthersMonths + 30;
        date1 = date1.plusMonths(1);
      }

      // Last Month
      start = endDate.withDayOfMonth(1);
    }

    if (endDate.isEqual(endDate.withDayOfMonth(endDate.lengthOfMonth()))) {
      nbDayOfLastMonth = 30 - start.getDayOfMonth();
    } else {
      nbDayOfLastMonth = endDate.getDayOfMonth() - start.getDayOfMonth();
    }

    // The endDate is included
    nbDayOfLastMonth = nbDayOfLastMonth + 1;

    return nbDayOfFirstMonth + nbDayOfOthersMonths + nbDayOfLastMonth;
  }

  public static int days360MonthsBetween(LocalDate startDate, LocalDate endDate) {

    if (startDate.isBefore(endDate)) {
      return days360Between(startDate, endDate) / 30;
    } else {
      return -days360Between(endDate, startDate) / 30;
    }
  }

  public static boolean isProrata(
      LocalDate dateFrame1, LocalDate dateFrame2, LocalDate date1, LocalDate date2) {

    if (date2 == null && (date1.isBefore(dateFrame2) || date1.isEqual(dateFrame2))) {
      return true;
    } else if (date2 == null) {
      return false;
    }

    return ((date1.isAfter(dateFrame1) || date1.isEqual(dateFrame1))
            && (date1.isBefore(dateFrame2) || date1.isEqual(dateFrame2)))
        || ((date2.isAfter(dateFrame1) || date2.isEqual(dateFrame1))
            && (date2.isBefore(dateFrame2) || date2.isEqual(dateFrame2)))
        || (date1.isBefore(dateFrame1) && date2.isAfter(dateFrame2));
  }

  public static boolean isBetween(LocalDate dateFrame1, LocalDate dateFrame2, LocalDate date) {

    return (dateFrame2 == null && (date.isAfter(dateFrame1) || date.isEqual(dateFrame1)))
        || (dateFrame2 != null
            && (date.isAfter(dateFrame1) || date.isEqual(dateFrame1))
            && (date.isBefore(dateFrame2) || date.isEqual(dateFrame2)));
  }

  /**
   * Computes the date of the next occurrence of an event according to the following calculation:
   * deletes as many times as possible the frequency in month to the targeted date while being
   * greater than the start date.
   *
   * @param startDate The start date
   * @param goalDate The targeted date
   * @param frequencyInMonth Number of months depicting the frequency of the event
   */
  public static LocalDate nextOccurency(
      LocalDate startDate, LocalDate goalDate, int frequencyInMonth) {

    if (!checkValidInputs(startDate, goalDate, frequencyInMonth)) {
      return null;

    } else if (startDate.isAfter(goalDate)) {
      return goalDate;
    }

    return minusMonths(
        goalDate,
        days360MonthsBetween(startDate.plusDays(1), goalDate.minusDays(1))
            / frequencyInMonth
            * frequencyInMonth);
  }

  /**
   * Computes the date of the next occurrence of an event according to the following calculation:
   * deletes as many times as possible the frequency in month to the targeted date while being
   * greater than or equal to the start date.
   *
   * @param startDate The start date
   * @param goalDate The targeted date
   * @param frequencyInMonth Number of months depicting the frequency of the event
   */
  public LocalDate nextOccurencyStartDateIncluded(
      LocalDate startDate, LocalDate goalDate, int frequencyInMonth) {

    if (!checkValidInputs(startDate, goalDate, frequencyInMonth)) {
      return null;

    } else if (startDate.isAfter(goalDate)) {
      return goalDate;
    }

    return minusMonths(
        goalDate,
        days360MonthsBetween(startDate, goalDate.minusDays(1))
            / frequencyInMonth
            * frequencyInMonth);
  }

  /**
   * Computes the date of the last occurrence of an event according to the following calculation:
   * adds as many times as possible the frequency in month to the start date while being less than
   * or equal to the end date.
   *
   * @param startDate
   * @param endDate
   * @param frequencyInMonth Number of months depicting the frequency of the event
   */
  public static LocalDate lastOccurency(
      LocalDate startDate, LocalDate endDate, int frequencyInMonth) {

    if (!checkValidInputs(startDate, endDate, frequencyInMonth)) {
      return null;

    } else if (startDate.isAfter(endDate)) {
      return null;

    } else {
      return plusMonths(
          startDate,
          days360MonthsBetween(startDate, endDate) / frequencyInMonth * frequencyInMonth);
    }
  }

  /**
   * Checks that the frequency is not equal to zero and that the start and end dates are not null.
   *
   * @param startDate
   * @param endDate
   * @param frequencyInMonth
   * @return
   */
  private static boolean checkValidInputs(
      LocalDate startDate, LocalDate endDate, int frequencyInMonth) {
    if (frequencyInMonth == 0) {
      LOG.debug("The frequency should not be zero.");
      return false;
    } else if (startDate == null) {
      LOG.debug("The start date should not be null.");
      return false;
    } else if (endDate == null) {
      LOG.debug("The end date should not be null.");
      return false;
    }
    return true;
  }

  public static LocalDate minusMonths(LocalDate date, int nbMonths) {

    return date.plusDays(1).minusMonths(nbMonths).minusDays(1);
  }

  public static LocalDate plusMonths(LocalDate date, int nbMonths) {

    return date.plusDays(1).plusMonths(nbMonths).minusDays(1);
  }

  public static LocalDateTime plusSeconds(LocalDateTime datetime, long duration) {

    return datetime.plusSeconds(duration);
  }

  public static LocalDateTime minusSeconds(LocalDateTime datetime, long duration) {

    return datetime.minusSeconds(duration);
  }

  /**
   * Checks if a date is in a specific period.
   *
   * @param date The date to check
   * @param dayBegin The start day of the period
   * @param monthBegin The start month of the period
   * @param dayEnd The end day of the period
   * @param monthEnd The start month of the period
   * @return
   */
  public static boolean dateInPeriod(
      LocalDate date, int dayBegin, int monthBegin, int dayEnd, int monthEnd) {

    if (monthBegin > monthEnd) {
      return (date.getMonthValue() == monthBegin && date.getDayOfMonth() >= dayBegin)
          || (date.getMonthValue() > monthBegin)
          || (date.getMonthValue() < monthEnd)
          || (date.getMonthValue() == monthEnd && date.getDayOfMonth() <= dayEnd);

    } else if (monthBegin == monthEnd) {
      return (date.getMonthValue() == monthBegin
          && date.getDayOfMonth() >= dayBegin
          && date.getDayOfMonth() <= dayEnd);

    } else {
      return (date.getMonthValue() == monthBegin && date.getDayOfMonth() >= dayBegin)
          || (date.getMonthValue() > monthBegin && date.getMonthValue() < monthEnd)
          || (date.getMonthValue() == monthEnd && date.getDayOfMonth() <= dayEnd);
    }
  }

  public static LocalDate toLocalDate(Date date) {

    Instant instant = date.toInstant();

    return instant.atZone(ZoneId.systemDefault()).toLocalDate();
  }

  public static LocalDateTime toLocalDateT(Date date) {

    Instant instant = date.toInstant();

    return instant.atZone(ZoneId.systemDefault()).toLocalDateTime();
  }

  public static Date toDate(LocalDate date) {

    return Date.from(date.atStartOfDay(ZoneId.systemDefault()).toInstant());
  }

  /**
   * Returns the maximum of two dates. A null date is treated as being less than any non-null date.
   */
  public static LocalDateTime max(LocalDateTime d1, LocalDateTime d2) {
    if (d1 == null && d2 == null) return null;
    if (d1 == null) return d2;
    if (d2 == null) return d1;
    return (d1.isAfter(d2)) ? d1 : d2;
  }
}
