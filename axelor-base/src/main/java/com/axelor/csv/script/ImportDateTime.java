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
package com.axelor.csv.script;

import com.axelor.meta.CallMethod;
import com.google.common.base.Strings;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ImportDateTime {
  String pat = "((\\+|\\-)?[0-9]{1,%s}%s)";
  String dt = "[0-9]{4}-[0-9]{2}-[0-9]{2}";
  Pattern patternYear = Pattern.compile("[0-9]{1,4}");
  Pattern patternMonth = Pattern.compile("[0-9]{1,2}");

  public LocalDateTime updateYear(LocalDateTime datetime, String year) {
    if (!Strings.isNullOrEmpty(year)) {
      Matcher matcher = patternYear.matcher(year);
      if (matcher.find()) {
        Long years = Long.parseLong(matcher.group());
        if (year.startsWith("+")) datetime = datetime.plusYears(years);
        else if (year.startsWith("-")) datetime = datetime.minusYears(years);
        else datetime = datetime.withYear(years.intValue());
      }
    }
    return datetime;
  }

  public LocalDateTime updateMonth(LocalDateTime datetime, String month) {
    if (!Strings.isNullOrEmpty(month)) {
      Matcher matcher = patternMonth.matcher(month);
      if (matcher.find()) {
        Long months = Long.parseLong(matcher.group());
        if (month.startsWith("+")) datetime = datetime.plusMonths(months);
        else if (month.startsWith("-")) datetime = datetime.minusMonths(months);
        else datetime = datetime.withMonth(months.intValue());
      }
    }
    return datetime;
  }

  public LocalDateTime updateDay(LocalDateTime datetime, String day) {
    if (!Strings.isNullOrEmpty(day)) {
      Matcher matcher = patternMonth.matcher(day);
      if (matcher.find()) {
        Long days = Long.parseLong(matcher.group());
        if (day.startsWith("+")) datetime = datetime.plusDays(days);
        else if (day.startsWith("-")) datetime = datetime.minusDays(days);
        else {
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
      Matcher matcher = patternMonth.matcher(hour);
      if (matcher.find()) {
        Long hours = Long.parseLong(matcher.group());
        if (hour.startsWith("+")) datetime = datetime.plusHours(hours);
        else if (hour.startsWith("-")) datetime = datetime.minusHours(hours);
        else datetime = datetime.withHour(hours.intValue());
      }
    }
    return datetime;
  }

  public LocalDateTime updateMinute(LocalDateTime datetime, String minute) {
    if (!Strings.isNullOrEmpty(minute)) {
      Matcher matcher = patternMonth.matcher(minute);
      if (matcher.find()) {
        Long minutes = Long.parseLong(matcher.group());
        if (minute.startsWith("+")) datetime = datetime.plusMinutes(minutes);
        else if (minute.startsWith("-")) datetime = datetime.minusMinutes(minutes);
        else datetime = datetime.withMinute(minutes.intValue());
      }
    }
    return datetime;
  }

  public LocalDateTime updateSecond(LocalDateTime datetime, String second) {
    if (!Strings.isNullOrEmpty(second)) {
      Matcher matcher = patternMonth.matcher(second);
      if (matcher.find()) {
        Long seconds = Long.parseLong(matcher.group());
        if (second.startsWith("+")) datetime = datetime.plusSeconds(seconds);
        else if (second.startsWith("-")) datetime = datetime.minusSeconds(seconds);
        else datetime = datetime.withSecond(seconds.intValue());
      }
    }
    return datetime;
  }

  @CallMethod
  public String importDate(String inputDate) {

    String patDate =
        "("
            + dt
            + "|TODAY)(\\[("
            + String.format(pat, 4, "y")
            + "?"
            + String.format(pat, 2, "M")
            + "?"
            + String.format(pat, 2, "d")
            + "?"
            + ")\\])?";
    try {
      if (!Strings.isNullOrEmpty(inputDate) && inputDate.matches(patDate)) {
        List<String> dates = Arrays.asList(inputDate.split("\\["));
        inputDate = dates.get(0).equals("TODAY") ? LocalDate.now().toString() : dates.get(0);
        if (dates.size() > 1) {
          LocalDateTime localDate =
              LocalDate.parse(inputDate, DateTimeFormatter.ISO_DATE).atStartOfDay();
          Matcher matcher = Pattern.compile(String.format(pat, 4, "y")).matcher(dates.get(1));
          if (matcher.find()) localDate = updateYear(localDate, matcher.group());
          matcher = Pattern.compile(String.format(pat, 2, "M")).matcher(dates.get(1));
          if (matcher.find()) localDate = updateMonth(localDate, matcher.group());
          matcher = Pattern.compile(String.format(pat, 2, "d")).matcher(dates.get(1));
          if (matcher.find()) localDate = updateDay(localDate, matcher.group());
          return localDate.toString();
        } else return inputDate;
      } else return null;
    } catch (Exception e) {
      return null;
    }
  }

  public String importDateTime(String inputDateTime) {
    String tm = "[0-9]{2}:[0-9]{2}:[0-9]{2}";
    String patTime =
        "("
            + dt
            + " "
            + tm
            + "|NOW)(\\[("
            + String.format(pat, 4, "y")
            + "?"
            + String.format(pat, 2, "M")
            + "?"
            + String.format(pat, 2, "d")
            + "?"
            + String.format(pat, 2, "H")
            + "?"
            + String.format(pat, 2, "m")
            + "?"
            + String.format(pat, 2, "s")
            + "?"
            + ")\\])?";
    try {
      if (!Strings.isNullOrEmpty(inputDateTime) && inputDateTime.matches(patTime)) {
        List<String> timeList = Arrays.asList(inputDateTime.split("\\["));
        inputDateTime =
            timeList.get(0).equals("NOW") ? LocalDateTime.now().toString() : timeList.get(0);
        if (timeList.size() > 1) {
          LocalDateTime datetime =
              LocalDateTime.parse(inputDateTime, DateTimeFormatter.ISO_DATE_TIME);
          Matcher matcher = Pattern.compile(String.format(pat, 4, "y")).matcher(timeList.get(1));
          if (matcher.find()) datetime = updateYear(datetime, matcher.group());
          matcher = Pattern.compile(String.format(pat, 2, "M")).matcher(timeList.get(1));
          if (matcher.find()) datetime = updateMonth(datetime, matcher.group());
          matcher = Pattern.compile(String.format(pat, 2, "d")).matcher(timeList.get(1));
          if (matcher.find()) datetime = updateDay(datetime, matcher.group());
          matcher = Pattern.compile(String.format(pat, 2, "H")).matcher(timeList.get(1));
          if (matcher.find()) datetime = updateHour(datetime, matcher.group());
          matcher = Pattern.compile(String.format(pat, 2, "m")).matcher(timeList.get(1));
          if (matcher.find()) datetime = updateMinute(datetime, matcher.group());
          matcher = Pattern.compile(String.format(pat, 2, "s")).matcher(timeList.get(1));
          if (matcher.find()) datetime = updateSecond(datetime, matcher.group());
          return datetime.toString();
        }
        return inputDateTime.replace(" ", "T");
      }
      return null;
    } catch (Exception e) {
      return null;
    }
  }
}
