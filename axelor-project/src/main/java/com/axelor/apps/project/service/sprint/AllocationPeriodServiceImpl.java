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
package com.axelor.apps.project.service.sprint;

import com.axelor.apps.base.db.Company;
import com.axelor.apps.project.db.AllocationPeriod;
import com.axelor.apps.project.db.repo.AllocationPeriodRepository;
import com.axelor.i18n.I18n;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.HashSet;
import java.util.Set;

public class AllocationPeriodServiceImpl implements AllocationPeriodService {

  protected static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("MMM d");

  protected AllocationPeriodRepository allocationPeriodRepo;

  @Inject
  public AllocationPeriodServiceImpl(AllocationPeriodRepository allocationPeriodRepo) {

    this.allocationPeriodRepo = allocationPeriodRepo;
  }

  @Override
  public Set<AllocationPeriod> generateAllocationPeriods(
      Company company, LocalDate fromDate, LocalDate toDate, int numberOfWeeksPerPeriod) {

    Set<AllocationPeriod> allocationPeriodSet = new HashSet<>();

    long totalPeriods = ChronoUnit.WEEKS.between(fromDate, toDate) / numberOfWeeksPerPeriod;
    long daysToAdd = 4 + (numberOfWeeksPerPeriod - 1) * 7;

    for (int i = 0; i <= totalPeriods; i++) {
      LocalDate periodStart = fromDate.plusWeeks(i * numberOfWeeksPerPeriod);
      LocalDate periodEnd = periodStart.plusDays(daysToAdd);

      if (periodEnd.isAfter(toDate)) {
        break;
      }

      AllocationPeriod allocationPeriod =
          allocationPeriodRepo.findByCompanyAndDates(company, periodStart, periodEnd);

      if (allocationPeriod == null) {
        allocationPeriod = createAllocationPeriod(company, periodStart, periodEnd);
      }

      allocationPeriodSet.add(allocationPeriod);
    }

    return allocationPeriodSet;
  }

  @Transactional(rollbackOn = Exception.class)
  protected AllocationPeriod createAllocationPeriod(
      Company company, LocalDate fromDate, LocalDate toDate) {

    AllocationPeriod allocationPeriod = new AllocationPeriod();
    allocationPeriod.setFromDate(fromDate);
    allocationPeriod.setToDate(toDate);
    allocationPeriod.setCompany(company);
    allocationPeriod.setName(
        I18n.get("Period")
            + " "
            + DATE_FORMATTER.format(fromDate)
            + " - "
            + DATE_FORMATTER.format(toDate));

    return allocationPeriodRepo.save(allocationPeriod);
  }
}
