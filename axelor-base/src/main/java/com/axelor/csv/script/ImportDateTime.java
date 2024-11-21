/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2005-2024 Axelor (<http://axelor.com>).
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
package com.axelor.csv.script;

import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.auth.AuthUtils;
import com.axelor.auth.db.User;
import com.axelor.meta.CallMethod;
import com.google.common.base.Strings;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.inject.Inject;

public class ImportDateTime {
  public static final String PREFIX_PATTERN = "((\\+|\\-|\\=)?[0-9]{1,%s}%s)";
  public static final String ISO_DATE_PATTERN_REGEX = "[0-9]{4}-[0-9]{2}-[0-9]{2}";
  public static final String HOUR_MINUTE_SECOND_REGEX = "[0-9]{2}:[0-9]{2}:[0-9]{2}";
  public static final Pattern FOUR_DIGITS_PATTERN = Pattern.compile("[0-9]{1,4}");
  public static final Pattern TWO_DIGITS_PATTERN = Pattern.compile("[0-9]{1,2}");
  public static final String TODAY_KEYWORD = "TODAY";
  public static final String NOW_KEYWORD = "NOW";

  protected AppBaseService appBaseService;

  @Inject
  public ImportDateTime(AppBaseService appBaseService) {
    this.appBaseService = appBaseService;
  }

  @CallMethod
  public String importDate(String inputDate) {
    String patDate = getDatePattern();
    if (Strings.isNullOrEmpty(inputDate) || !inputDate.matches(patDate)) {
      return null;
    }

    List<String> dates = Arrays.asList(inputDate.split("\\["));
    inputDate = computeInputDate(dates.get(0));
    if (dates.size() > 1) {
      LocalDateTime localDate =
          LocalDate.parse(inputDate, DateTimeFormatter.ISO_DATE).atStartOfDay();
      localDate = updateDateElements(localDate, dates.get(1));
      return localDate.toString();
    } else {
      return inputDate;
    }
  }

  public String importDateTime(String inputDateTime) {
    String dateTimePattern = getDateTimePattern();
    if (Strings.isNullOrEmpty(inputDateTime) || !inputDateTime.matches(dateTimePattern)) {
      return null;
    }
    List<String> timeList = Arrays.asList(inputDateTime.split("\\["));
    inputDateTime =
        timeList.get(0).equals(NOW_KEYWORD) ? LocalDateTime.now().toString() : timeList.get(0);
    if (timeList.size() > 1) {
      LocalDateTime datetime = LocalDateTime.parse(inputDateTime, DateTimeFormatter.ISO_DATE_TIME);
      datetime = updateDateTimeElements(datetime, timeList.get(1));
      return datetime.toString();
    } else {
      return inputDateTime;
    }
  }

  public LocalDateTime updateYear(LocalDateTime datetime, String year) {
    if (!Strings.isNullOrEmpty(year)) {
      Matcher matcher = FOUR_DIGITS_PATTERN.matcher(year);
      if (matcher.find()) {
        Long years = Long.parseLong(matcher.group());
        if (year.startsWith("+")) {
          datetime = datetime.plusYears(years);
        } else if (year.startsWith("-")) {
          datetime = datetime.minusYears(years);
        } else if (year.startsWith("=")) {
          datetime = datetime.withYear(years.intValue());
        } else {
          datetime = datetime.withYear(years.intValue());
        }
      }
    }
    return datetime;
  }

  public LocalDateTime updateMonth(LocalDateTime datetime, String month) {
    if (!Strings.isNullOrEmpty(month)) {
      Matcher matcher = TWO_DIGITS_PATTERN.matcher(month);
      if (matcher.find()) {
        Long months = Long.parseLong(matcher.group());
        if (month.startsWith("+")) {
          datetime = datetime.plusMonths(months);
        } else if (month.startsWith("-")) {
          datetime = datetime.minusMonths(months);
        } else if (month.startsWith("=")) {
          datetime = datetime.withMonth(months.intValue());
        } else {
          datetime = datetime.withMonth(months.intValue());
        }
      }
    }
    return datetime;
  }

  public LocalDateTime updateDay(LocalDateTime datetime, String day) {
    if (!Strings.isNullOrEmpty(day)) {
      Matcher matcher = TWO_DIGITS_PATTERN.matcher(day);
      if (matcher.find()) {
        Long days = Long.parseLong(matcher.group());
        if (day.startsWith("+")) {
          datetime = datetime.plusDays(days);
        } else if (day.startsWith("-")) {
          datetime = datetime.minusDays(days);
        } else if (day.startsWith("=")) {
          datetime = datetime.withDayOfMonth(days.intValue());
        } else {
          if (days > datetime.toLocalDate().lengthOfMonth()) {
            days = Long.valueOf(datetime.toLocalDate().lengthOfMonth());
          }
          datetime = datetime.withDayOfMonth(days.intValue());
        }
      }
    }
    return datetime;
  }

  public LocalDateTime updateHour(LocalDateTime datetime, String hour) {
    if (!Strings.isNullOrEmpty(hour)) {
      Matcher matcher = TWO_DIGITS_PATTERN.matcher(hour);
      if (matcher.find()) {
        Long hours = Long.parseLong(matcher.group());
        if (hour.startsWith("+")) {
          datetime = datetime.plusHours(hours);
        } else if (hour.startsWith("-")) {
          datetime = datetime.minusHours(hours);
        } else {
          datetime = datetime.withHour(hours.intValue());
        }
      }
    }
    return datetime;
  }

  public LocalDateTime updateMinute(LocalDateTime datetime, String minute) {
    if (!Strings.isNullOrEmpty(minute)) {
      Matcher matcher = TWO_DIGITS_PATTERN.matcher(minute);
      if (matcher.find()) {
        Long minutes = Long.parseLong(matcher.group());
        if (minute.startsWith("+")) {
          datetime = datetime.plusMinutes(minutes);
        } else if (minute.startsWith("-")) {
          datetime = datetime.minusMinutes(minutes);
        } else {
          datetime = datetime.withMinute(minutes.intValue());
        }
      }
    }
    return datetime;
  }

  public LocalDateTime updateSecond(LocalDateTime datetime, String second) {
    if (!Strings.isNullOrEmpty(second)) {
      Matcher matcher = TWO_DIGITS_PATTERN.matcher(second);
      if (matcher.find()) {
        Long seconds = Long.parseLong(matcher.group());
        if (second.startsWith("+")) {
          datetime = datetime.plusSeconds(seconds);
        } else if (second.startsWith("-")) {
          datetime = datetime.minusSeconds(seconds);
        } else {
          datetime = datetime.withSecond(seconds.intValue());
        }
      }
    }
    return datetime;
  }

  protected LocalDateTime updateDateTimeElements(LocalDateTime datetime, String time) {
    datetime = updateDateElements(datetime, time);
    datetime = checkAndUpdateTwoDigitsDateElement(time, datetime, "H");
    datetime = checkAndUpdateTwoDigitsDateElement(time, datetime, "m");
    datetime = checkAndUpdateTwoDigitsDateElement(time, datetime, "s");
    return datetime;
  }

  protected LocalDateTime updateDateElements(LocalDateTime localDate, String date) {
    localDate = checkAndUpdateYear(date, localDate);
    localDate = checkAndUpdateTwoDigitsDateElement(date, localDate, "M");
    localDate = checkAndUpdateTwoDigitsDateElement(date, localDate, "d");
    return localDate;
  }

  protected LocalDateTime checkAndUpdateYear(String time, LocalDateTime datetime) {
    Matcher matcher = Pattern.compile(String.format(PREFIX_PATTERN, 4, "y")).matcher(time);
    if (!matcher.find()) {
      return datetime;
    }
    return updateYear(datetime, matcher.group());
  }

  protected LocalDateTime checkAndUpdateTwoDigitsDateElement(
      String time, LocalDateTime datetime, String letter) {
    Matcher matcher = Pattern.compile(String.format(PREFIX_PATTERN, 2, letter)).matcher(time);
    if (!matcher.find()) {
      return datetime;
    }

    String result = matcher.group();
    LocalDateTime resultDate;
    switch (letter) {
      case "M":
        resultDate = updateMonth(datetime, result);
        break;
      case "d":
        resultDate = updateDay(datetime, result);
        break;
      case "H":
        resultDate = updateHour(datetime, result);
        break;
      case "m":
        resultDate = updateMinute(datetime, result);
        break;
      case "s":
        resultDate = updateSecond(datetime, result);
        break;
      default:
        resultDate = datetime;
    }
    return resultDate;
  }

  protected String computeInputDate(String inputDate) {
    if (inputDate.equals(TODAY_KEYWORD)) {
      return appBaseService
          .getTodayDate(
              Optional.ofNullable(AuthUtils.getUser()).map(User::getActiveCompany).orElse(null))
          .toString();
    }
    return inputDate;
  }

  protected String getDatePattern() {
    return "("
        + ISO_DATE_PATTERN_REGEX
        + "|"
        + TODAY_KEYWORD
        + ")(\\[("
        + String.format(PREFIX_PATTERN, 4, "y")
        + "?"
        + String.format(PREFIX_PATTERN, 2, "M")
        + "?"
        + String.format(PREFIX_PATTERN, 2, "d")
        + "?"
        + ")\\])?";
  }

  protected String getDateTimePattern() {
    return "("
        + ISO_DATE_PATTERN_REGEX
        + " "
        + HOUR_MINUTE_SECOND_REGEX
        + "|"
        + NOW_KEYWORD
        + ")(\\[("
        + String.format(PREFIX_PATTERN, 4, "y")
        + "?"
        + String.format(PREFIX_PATTERN, 2, "M")
        + "?"
        + String.format(PREFIX_PATTERN, 2, "d")
        + "?"
        + String.format(PREFIX_PATTERN, 2, "H")
        + "?"
        + String.format(PREFIX_PATTERN, 2, "m")
        + "?"
        + String.format(PREFIX_PATTERN, 2, "s")
        + "?"
        + ")\\])?";
  }
}
