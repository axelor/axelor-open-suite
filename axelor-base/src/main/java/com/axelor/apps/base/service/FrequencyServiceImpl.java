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
package com.axelor.apps.base.service;

import com.axelor.apps.base.db.Frequency;
import com.axelor.apps.base.db.repo.FrequencyRepository;
import com.axelor.apps.tool.date.DateTool;
import com.axelor.i18n.I18n;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class FrequencyServiceImpl implements FrequencyService {

  @Override
  public String computeSummary(Frequency frequency) {
    StringBuilder summary = new StringBuilder();

    // Frequency
    if (frequency.getTypeSelect() != null) {
      if (frequency.getTypeSelect().equals(FrequencyRepository.TYPE_EVERY_N_WEEKS)) {
        if (frequency.getEveryNWeeks() == 1) {
          summary.append(I18n.get("Every week"));
        } else {
          summary
              .append(I18n.get("Every"))
              .append(" ")
              .append(frequency.getEveryNWeeks())
              .append(" ")
              .append(I18n.get("weeks"));
        }
        summary.append("~ ");
        summary.append(
            I18n.get("THE_SINGULAR", "THE_PLURAL", 2)); // force plural for other languages
      } else if (frequency.getTypeSelect().equals(FrequencyRepository.TYPE_MONTH_DAYS)) {
        summary.append(I18n.get("Every")).append(" ");
        if (frequency.getFirst()) {
          summary.append(I18n.get("first"));
        }
        if (frequency.getSecond()) {
          if (frequency.getFirst()) {
            summary.append(", ");
          }
          summary.append(I18n.get("second"));
        }
        if (frequency.getThird()) {
          if (frequency.getFirst() || frequency.getSecond()) {
            summary.append(", ");
          }
          summary.append(I18n.get("third"));
        }
        if (frequency.getFourth()) {
          if (frequency.getFirst() || frequency.getSecond() || frequency.getThird()) {
            summary.append(", ");
          }
          summary.append(I18n.get("fourth"));
        }
        if (frequency.getLast()) {
          if (frequency.getFirst()
              || frequency.getSecond()
              || frequency.getThird()
              || frequency.getFourth()) {
            summary.append(", ");
          }
          summary.append(I18n.get("last"));
        }

        int lastCommaIndex = summary.lastIndexOf(",");
        if (lastCommaIndex != -1) {
          summary.replace(lastCommaIndex, lastCommaIndex + 1, " " + I18n.get("and"));
        }
      }
    }

    summary.append(" ");

    // Days
    if (frequency.getMonday()
        && frequency.getTuesday()
        && frequency.getWednesday()
        && frequency.getThursday()
        && frequency.getFriday()
        && !(frequency.getSaturday() || frequency.getSunday())) {
      summary.append(I18n.get("weekdays"));
    } else if (frequency.getSaturday()
        && frequency.getSunday()
        && !(frequency.getMonday()
            || frequency.getTuesday()
            || frequency.getWednesday()
            || frequency.getThursday()
            || frequency.getFriday())) {
      summary.append(I18n.get("weekends"));
    } else if (frequency.getMonday()
        && frequency.getTuesday()
        && frequency.getWednesday()
        && frequency.getThursday()
        && frequency.getFriday()
        && frequency.getSaturday()
        && frequency.getSunday()) {
      summary.append(I18n.get("days"));
    } else {
      if (frequency.getMonday()) {
        summary.append(I18n.get("Monday", "Mondays", 2)); // force plural for other languages
      }
      if (frequency.getTuesday()) {
        if (frequency.getMonday()) {
          summary.append(", ");
        }
        summary.append(I18n.get("Tuesday", "Tuesdays", 2));
      }
      if (frequency.getWednesday()) {
        if (frequency.getMonday() || frequency.getTuesday()) {
          summary.append(", ");
        }
        summary.append(I18n.get("Wednesday", "Wednesdays", 2));
      }
      if (frequency.getThursday()) {
        if (frequency.getMonday() || frequency.getTuesday() || frequency.getWednesday()) {
          summary.append(", ");
        }
        summary.append(I18n.get("Thursday", "Thursdays", 2));
      }
      if (frequency.getFriday()) {
        if (frequency.getMonday()
            || frequency.getTuesday()
            || frequency.getWednesday()
            || frequency.getThursday()) {
          summary.append(", ");
        }
        summary.append(I18n.get("Friday", "Fridays", 2));
      }
      if (frequency.getSaturday()) {
        if (frequency.getMonday()
            || frequency.getTuesday()
            || frequency.getWednesday()
            || frequency.getThursday()
            || frequency.getFriday()) {
          summary.append(", ");
        }
        summary.append(I18n.get("Saturday", "Saturdays", 2));
      }
      if (frequency.getSunday()) {
        if (frequency.getMonday()
            || frequency.getTuesday()
            || frequency.getWednesday()
            || frequency.getThursday()
            || frequency.getFriday()
            || frequency.getSaturday()) {
          summary.append(", ");
        }
        summary.append(I18n.get("Sunday", "Sundays", 2));
      }

      int lastCommaIndex = summary.lastIndexOf(",");
      if (lastCommaIndex != -1) {
        summary.replace(lastCommaIndex, lastCommaIndex + 1, " " + I18n.get("and"));
      }
    }

    summary.append(" ").append(I18n.get("of")).append(" ");

    // Months
    if (frequency.getJanuary()
        && frequency.getFebruary()
        && frequency.getMarch()
        && frequency.getApril()
        && frequency.getMay()
        && frequency.getJune()
        && frequency.getJuly()
        && frequency.getAugust()
        && frequency.getSeptember()
        && frequency.getOctober()
        && frequency.getNovember()
        && frequency.getDecember()) {
      summary.append(I18n.get("each month"));
    } else {
      if (frequency.getJanuary()) {
        summary.append(I18n.get("January"));
      }
      if (frequency.getFebruary()) {
        if (frequency.getJanuary()) {
          summary.append(", ");
        }
        summary.append(I18n.get("February"));
      }
      if (frequency.getMarch()) {
        if (frequency.getJanuary() || frequency.getFebruary()) {
          summary.append(", ");
        }
        summary.append(I18n.get("March"));
      }
      if (frequency.getApril()) {
        if (frequency.getJanuary() || frequency.getFebruary() || frequency.getMarch()) {
          summary.append(", ");
        }
        summary.append(I18n.get("April"));
      }
      if (frequency.getMay()) {
        if (frequency.getJanuary()
            || frequency.getFebruary()
            || frequency.getMarch()
            || frequency.getApril()) {
          summary.append(", ");
        }
        summary.append(I18n.get("May"));
      }
      if (frequency.getJune()) {
        if (frequency.getJanuary()
            || frequency.getFebruary()
            || frequency.getMarch()
            || frequency.getApril()
            || frequency.getMay()) {
          summary.append(", ");
        }
        summary.append(I18n.get("June"));
      }
      if (frequency.getJuly()) {
        if (frequency.getJanuary()
            || frequency.getFebruary()
            || frequency.getMarch()
            || frequency.getApril()
            || frequency.getMay()
            || frequency.getJune()) {
          summary.append(", ");
        }
        summary.append(I18n.get("July"));
      }
      if (frequency.getAugust()) {
        if (frequency.getJanuary()
            || frequency.getFebruary()
            || frequency.getMarch()
            || frequency.getApril()
            || frequency.getMay()
            || frequency.getJune()
            || frequency.getJuly()) {
          summary.append(", ");
        }
        summary.append(I18n.get("August"));
      }
      if (frequency.getSeptember()) {
        if (frequency.getJanuary()
            || frequency.getFebruary()
            || frequency.getMarch()
            || frequency.getApril()
            || frequency.getMay()
            || frequency.getJune()
            || frequency.getJuly()
            || frequency.getAugust()) {
          summary.append(", ");
        }
        summary.append(I18n.get("September"));
      }
      if (frequency.getOctober()) {
        if (frequency.getJanuary()
            || frequency.getFebruary()
            || frequency.getMarch()
            || frequency.getApril()
            || frequency.getMay()
            || frequency.getJune()
            || frequency.getJuly()
            || frequency.getAugust()
            || frequency.getSeptember()) {
          summary.append(", ");
        }
        summary.append(I18n.get("October"));
      }
      if (frequency.getNovember()) {
        if (frequency.getJanuary()
            || frequency.getFebruary()
            || frequency.getMarch()
            || frequency.getApril()
            || frequency.getMay()
            || frequency.getJune()
            || frequency.getJuly()
            || frequency.getAugust()
            || frequency.getSeptember()
            || frequency.getOctober()) {
          summary.append(", ");
        }
        summary.append(I18n.get("November"));
      }
      if (frequency.getDecember()) {
        if (frequency.getJanuary()
            || frequency.getFebruary()
            || frequency.getMarch()
            || frequency.getApril()
            || frequency.getMay()
            || frequency.getJune()
            || frequency.getJuly()
            || frequency.getAugust()
            || frequency.getSeptember()
            || frequency.getOctober()
            || frequency.getNovember()) {
          summary.append(", ");
        }
        summary.append(I18n.get("December"));
      }

      int lastCommaIndex = summary.lastIndexOf(",");
      if (lastCommaIndex != -1) {
        summary.replace(lastCommaIndex, lastCommaIndex + 1, " " + I18n.get("and"));
      }
    }

    int everyNWeeksCommaIndex = summary.indexOf("~");
    if (everyNWeeksCommaIndex != -1) {
      summary.replace(everyNWeeksCommaIndex, everyNWeeksCommaIndex + 1, ",");
    }

    if (frequency.getEndDate() != null) {
      summary.append(", ");
      summary.append(I18n.get("until"));
      summary.append(" ");
      summary.append(frequency.getEndDate().toString());
    }

    return summary.toString();
  }

  @Override
  public List<LocalDate> getDates(Frequency frequency, LocalDate startDate, LocalDate endDate) {
    Set<LocalDate> dates = new HashSet<>();

    List<Integer> years = getYears(startDate, endDate);
    List<Integer> months = getMonths(frequency);
    List<Integer> days = getDays(frequency);

    if (frequency.getTypeSelect().equals(FrequencyRepository.TYPE_MONTH_DAYS)) {
      List<Integer> occurences = getOccurences(frequency);

      for (Integer year : years) {
        for (Integer month : months) {
          for (Integer day : days) {
            for (Integer occurence : occurences) {
              LocalDate date = getDay(day, occurence, year, month);

              if (DateTool.isBetween(startDate, endDate, date)) {
                dates.add(date);
              }
            }
          }
        }
      }
    } else {
      Integer leap = frequency.getEveryNWeeks();

      for (Integer year : years) {
        for (Integer day : days) {
          Calendar cal = Calendar.getInstance();
          cal.set(Calendar.YEAR, year);
          cal.set(Calendar.MONTH, Calendar.JANUARY);
          cal.set(Calendar.DAY_OF_WEEK, day);
          cal.set(Calendar.DAY_OF_WEEK_IN_MONTH, 1);
          do {
            if (months.contains(cal.get(Calendar.MONTH) + 1)) {
              LocalDate date =
                  LocalDate.of(year, cal.get(Calendar.MONTH) + 1, cal.get(Calendar.DATE));

              if (DateTool.isBetween(startDate, endDate, date)) {
                dates.add(date);
              }
            }
            cal.add(Calendar.DATE, leap * 7);
          } while (cal.get(Calendar.YEAR) == year);
        }
      }
    }

    ArrayList<LocalDate> sortedDates = new ArrayList<>(dates);
    sortedDates.sort(LocalDate::compareTo);
    return sortedDates;
  }

  /** Retrieves a LocalDate instance of given date in arguments. */
  public LocalDate getDay(int dayOfWeek, int dayOfWeekInMonth, int year, int month) {
    Calendar cal = Calendar.getInstance();
    cal.set(Calendar.DAY_OF_WEEK, dayOfWeek);
    cal.set(Calendar.DAY_OF_WEEK_IN_MONTH, dayOfWeekInMonth);
    cal.set(Calendar.YEAR, year);
    cal.set(
        Calendar.MONTH,
        month - 1); // here, months are counted from 0: so January is 0, February is 1, etc.
    return LocalDate.of(year, month, cal.get(Calendar.DATE));
  }

  /** Retrieves all years between {@code startDate} and {@code endDate}. */
  public List<Integer> getYears(LocalDate startDate, LocalDate endDate) {
    ArrayList<Integer> years = new ArrayList<>();

    int startYear = startDate.getYear();
    years.add(startYear);
    for (long i = 0; i < ChronoUnit.YEARS.between(startDate, endDate); i++) {
      years.add(startYear++);
    }

    return years;
  }

  @Override
  public List<Integer> getMonths(Frequency frequency) {
    List<Integer> months = new ArrayList<>();

    if (frequency.getJanuary()) {
      months.add(1);
    }
    if (frequency.getFebruary()) {
      months.add(2);
    }
    if (frequency.getMarch()) {
      months.add(3);
    }
    if (frequency.getApril()) {
      months.add(4);
    }
    if (frequency.getMay()) {
      months.add(5);
    }
    if (frequency.getJune()) {
      months.add(6);
    }
    if (frequency.getJuly()) {
      months.add(7);
    }
    if (frequency.getAugust()) {
      months.add(8);
    }
    if (frequency.getSeptember()) {
      months.add(9);
    }
    if (frequency.getOctober()) {
      months.add(10);
    }
    if (frequency.getNovember()) {
      months.add(11);
    }
    if (frequency.getDecember()) {
      months.add(12);
    }

    return months;
  }

  @Override
  public List<Integer> getDays(Frequency frequency) {
    List<Integer> days = new ArrayList<>();

    if (frequency.getSunday()) {
      days.add(1);
    }
    if (frequency.getMonday()) {
      days.add(2);
    }
    if (frequency.getTuesday()) {
      days.add(3);
    }
    if (frequency.getWednesday()) {
      days.add(4);
    }
    if (frequency.getThursday()) {
      days.add(5);
    }
    if (frequency.getFriday()) {
      days.add(6);
    }
    if (frequency.getSaturday()) {
      days.add(7);
    }

    return days;
  }

  @Override
  public List<Integer> getOccurences(Frequency frequency) {
    List<Integer> occurences = new ArrayList<>();

    if (frequency.getFirst()) {
      occurences.add(1);
    }
    if (frequency.getSecond()) {
      occurences.add(2);
    }
    if (frequency.getThird()) {
      occurences.add(3);
    }
    if (frequency.getFourth()) {
      occurences.add(4);
    }
    if (frequency.getLast()) {
      occurences.add(-1);
    }

    return occurences;
  }
}
