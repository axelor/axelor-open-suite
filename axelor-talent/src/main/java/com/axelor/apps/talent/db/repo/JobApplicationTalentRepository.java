/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2005-2023 Axelor (<http://axelor.com>).
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
package com.axelor.apps.talent.db.repo;

import com.axelor.apps.talent.db.JobApplication;
import com.axelor.apps.talent.service.JobApplicationToolService;
import com.google.inject.Inject;

public class JobApplicationTalentRepository extends JobApplicationRepository {

  protected JobApplicationToolService jobApplicationToolService;

  @Inject
  public JobApplicationTalentRepository(JobApplicationToolService jobApplicationToolService) {
    this.jobApplicationToolService = jobApplicationToolService;
  }

  @Override
  public JobApplication save(JobApplication entity) {

    entity.setFullName(jobApplicationToolService.computeFullName(entity));

    return super.save(entity);
  }
}
