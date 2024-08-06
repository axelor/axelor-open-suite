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
package com.axelor.apps.intervention.service.planning;

import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.WeeklyPlanning;
import com.google.inject.Inject;
import java.time.LocalDateTime;

public class PlanningDateTimeServiceImpl implements PlanningDateTimeService {
  protected final PlanningDateTimeProcessor planningDateTimeProcessor;

  @Inject
  public PlanningDateTimeServiceImpl(PlanningDateTimeProcessor planningDateTimeProcessor) {
    this.planningDateTimeProcessor = planningDateTimeProcessor;
  }

  @Override
  public LocalDateTime add(
      Company company, WeeklyPlanning planning, LocalDateTime dateTime, Long seconds) {
    if (planning == null || dateTime == null || seconds == null) {
      return null;
    }
    return planningDateTimeProcessor
        .with(planning, company)
        .from(dateTime)
        .processing(Operation.ADD, seconds)
        .compute();
  }

  @Override
  public LocalDateTime sub(
      Company company, WeeklyPlanning planning, LocalDateTime dateTime, Long seconds) {
    if (planning == null || dateTime == null || seconds == null) {
      return null;
    }
    return planningDateTimeProcessor
        .with(planning, company)
        .from(dateTime)
        .processing(Operation.SUB, seconds)
        .compute();
  }

  @Override
  public Long diff(
      Company company, WeeklyPlanning planning, LocalDateTime dateTime, LocalDateTime target) {
    return planningDateTimeProcessor.with(planning, company).from(dateTime).to(target).diff();
  }
}
