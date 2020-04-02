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

  private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  public static long daysBetween(LocalDate date1, LocalDate date2, boolean days360) {

    long days = 0;

    if (days360) {

      if (date1.isBefore(date2)) {
        days = days360Between(date1, date2);
      } else {
        days = -days360Between(date2, date1);
      }
    } else {
      days = daysBetween(date1, date2);
    }

    LOG.debug(
        "Nombre de jour entre {} - {} (mois de 30 jours ? {}) : {}",
        new Object[] {date1, date2, days360, days});

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

    if (((date1.isAfter(dateFrame1) || date1.isEqual(dateFrame1))
            && (date1.isBefore(dateFrame2) || date1.isEqual(dateFrame2)))
        || ((date2.isAfter(dateFrame1) || date2.isEqual(dateFrame1))
            && (date2.isBefore(dateFrame2) || date2.isEqual(dateFrame2)))
        || (date1.isBefore(dateFrame1) && date2.isAfter(dateFrame2))) {

      return true;
    }

    return false;
  }

  public static boolean isBetween(LocalDate dateFrame1, LocalDate dateFrame2, LocalDate date) {

    if (dateFrame2 == null && (date.isAfter(dateFrame1) || date.isEqual(dateFrame1))) {
      return true;
    } else if (dateFrame2 != null
        && (date.isAfter(dateFrame1) || date.isEqual(dateFrame1))
        && (date.isBefore(dateFrame2) || date.isEqual(dateFrame2))) {
      return true;
    } else {
      return false;
    }
  }

  /**
   * Calculer la date de la prochaine occurence d'un évènement suivant le calcul suivant : Supprimer
   * autant de fois que possible la fréquence en mois à la date visée tout en étant supérieure à la
   * date de début
   *
   * @param startDate La date de début
   * @param goalDate La date visée
   * @param frequencyInMonth Nombre de mois représentant la fréquence de l'évènement
   */
  public static LocalDate nextOccurency(
      LocalDate startDate, LocalDate goalDate, int frequencyInMonth) {

    if (frequencyInMonth == 0) {

      LOG.debug("La fréquence ne doit pas etre égale à 0.");

      return null;

    } else {

      if (startDate == null && goalDate == null) {
        return null;
      } else {
        if (startDate.isAfter(goalDate)) {
          return goalDate;
        }
        return minusMonths(
            goalDate,
            days360MonthsBetween(startDate.plusDays(1), goalDate.minusDays(1))
                / frequencyInMonth
                * frequencyInMonth);
      }
    }
  }

  /**
   * Calculer la date de la prochaine occurence d'un évènement suivant le calcul suivant : Supprimer
   * autant de fois que possible la fréquence en mois à la date visée tout en étant supérieure ou
   * égale à la date de début
   *
   * @param startDate La date de début
   * @param goalDate La date visée
   * @param frequencyInMonth Nombre de mois représentant la fréquence de l'évènement
   */
  public LocalDate nextOccurencyStartDateIncluded(
      LocalDate startDate, LocalDate goalDate, int frequencyInMonth) {

    if (frequencyInMonth == 0) {

      LOG.debug("La fréquence ne doit pas etre égale à 0.");

      return null;

    } else {

      if (startDate == null && goalDate == null) {
        return null;
      } else {
        if (startDate.isAfter(goalDate)) {
          return goalDate;
        }
        return minusMonths(
            goalDate,
            days360MonthsBetween(startDate, goalDate.minusDays(1))
                / frequencyInMonth
                * frequencyInMonth);
      }
    }
  }

  /**
   * Calculer la date de la dernière occurence d'un évènement suivant le calcul suivant : Ajouter
   * autant de fois que possible la fréquence en mois à la date de début tout en étant inférieure ou
   * égale à la date de fin
   *
   * @param startDate La date de début
   * @param endDate La date de fin
   * @param frequencyInMonth Nombre de mois représentant la fréquence de l'évènement
   */
  public static LocalDate lastOccurency(
      LocalDate startDate, LocalDate endDate, int frequencyInMonth) {

    if (frequencyInMonth == 0) {

      LOG.debug("La fréquence ne doit pas etre égale à 0.");
      return null;

    } else {

      if ((startDate == null && endDate == null) || startDate.isAfter(endDate)) {
        return null;
      } else {
        return plusMonths(
            startDate,
            days360MonthsBetween(startDate, endDate) / frequencyInMonth * frequencyInMonth);
      }
    }
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
   * Procédure permettant de tester si aujourd'hui nous sommes dans une période particulière
   *
   * @param date La date à tester
   * @param dayBegin Le jour du début de la période
   * @param monthBegin Le mois de début de la période
   * @param dayEnd Le jour de fin de la période
   * @param monthEnd Le mois de fin de la période
   * @return Sommes-nous dans la période?
   */
  public static boolean dateInPeriod(
      LocalDate date, int dayBegin, int monthBegin, int dayEnd, int monthEnd) {

    if (monthBegin > monthEnd) {

      if ((date.getMonthValue() == monthBegin && date.getDayOfMonth() >= dayBegin)
          || (date.getMonthValue() > monthBegin)
          || (date.getMonthValue() < monthEnd)
          || (date.getMonthValue() == monthEnd && date.getDayOfMonth() <= dayEnd)) {
        return true;
      } else {
        return false;
      }
    } else if (monthBegin == monthEnd) {

      if ((date.getMonthValue() == monthBegin
          && date.getDayOfMonth() >= dayBegin
          && date.getDayOfMonth() <= dayEnd)) {
        return true;
      } else {
        return false;
      }
    } else {

      if ((date.getMonthValue() == monthBegin && date.getDayOfMonth() >= dayBegin)
          || (date.getMonthValue() > monthBegin && date.getMonthValue() < monthEnd)
          || (date.getMonthValue() == monthEnd && date.getDayOfMonth() <= dayEnd)) {
        return true;
      } else {
        return false;
      }
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
}
