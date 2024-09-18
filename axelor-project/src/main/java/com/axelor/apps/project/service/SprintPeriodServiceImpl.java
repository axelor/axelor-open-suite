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
package com.axelor.apps.project.service;

import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.repo.CompanyRepository;
import com.axelor.apps.base.service.weeklyplanning.WeeklyPlanningService;
import com.axelor.apps.project.db.SprintPeriod;
import com.axelor.apps.project.db.repo.SprintPeriodRepository;
import com.axelor.i18n.I18n;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class SprintPeriodServiceImpl implements SprintPeriodService {

  public CompanyRepository companyRepo;
  public SprintPeriodRepository sprintPeriodRepo;
  public WeeklyPlanningService weeklyPlanningService;

  @Inject
  public SprintPeriodServiceImpl(
      CompanyRepository companyRepo,
      SprintPeriodRepository sprintPeriodRepo,
      WeeklyPlanningService weeklyPlanningService) {

    this.companyRepo = companyRepo;
    this.sprintPeriodRepo = sprintPeriodRepo;
    this.weeklyPlanningService = weeklyPlanningService;
  }

  @Override
  @Transactional
  public void sprintPeriodGenerate(
      Long companyId,
      LocalDate fromDate,
      LocalDate toDate,
      int nbOfDaysPerSprint,
      boolean considerWeekend) {

    Company company = companyRepo.find(companyId);

    LocalDate currentDate = fromDate;
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM d");

    while (!currentDate.isAfter(toDate)) {
      int daysCount = 0;
      LocalDate endDate = currentDate;

      while (daysCount < nbOfDaysPerSprint && !endDate.isAfter(toDate)) {

        if (!considerWeekend || !weeklyPlanningService.checkDateIsWeekend(company, endDate)) {
          daysCount++;
        }

        if (daysCount < nbOfDaysPerSprint) {
          endDate = endDate.plusDays(1);
        }
      }

      if (endDate.isAfter(toDate)) {
        endDate = toDate;
      }

      SprintPeriod period = new SprintPeriod();
      period.setName(
          I18n.get("Period")
              + " "
              + formatter.format(currentDate)
              + " to "
              + formatter.format(endDate));
      period.setCompany(company);
      period.setFromDate(currentDate);
      period.setToDate(endDate);

      sprintPeriodRepo.save(period);

      currentDate = endDate.plusDays(1);
    }
  }
}
