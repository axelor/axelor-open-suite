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
package com.axelor.apps.talent.db.repo;

import com.axelor.apps.base.db.repo.SequenceRepository;
import com.axelor.apps.base.service.administration.SequenceService;
import com.axelor.apps.talent.db.JobPosition;
import com.google.inject.Inject;

public class JobPositionTalentRepository extends JobPositionRepository {

  @Inject private SequenceService sequenceService;

  @Override
  public JobPosition save(JobPosition jobPosition) {

    if (jobPosition.getStatusSelect() > 0 && jobPosition.getJobReference() == null) {
      jobPosition.setJobReference(
          sequenceService.getSequenceNumber(SequenceRepository.JOB_POSITION));
    }

    return super.save(jobPosition);
  }
}
