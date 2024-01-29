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
package com.axelor.apps.hr.db.repo;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.repo.SequenceRepository;
import com.axelor.apps.base.service.administration.SequenceService;
import com.axelor.apps.base.service.exception.TraceBackService;
import com.axelor.apps.hr.db.EmploymentContract;
import com.google.inject.Inject;

public class EmploymentContractHRRepository extends EmploymentContractRepository {

  @Inject protected SequenceService sequenceService;

  @Override
  public EmploymentContract save(EmploymentContract employmentContract) {
    if (employmentContract.getRef() == null) {
      String seq = null;
      try {
        seq =
            sequenceService.getSequenceNumber(
                SequenceRepository.EMPLOYMENT_CONTRACT,
                employmentContract.getPayCompany(),
                EmploymentContract.class,
                "ref");
      } catch (AxelorException e) {
        TraceBackService.traceExceptionFromSaveMethod(e);
      }
      employmentContract.setRef(seq);
    }

    return super.save(employmentContract);
  }
}
