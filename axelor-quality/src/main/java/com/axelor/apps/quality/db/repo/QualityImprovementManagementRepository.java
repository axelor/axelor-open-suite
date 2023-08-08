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
package com.axelor.apps.quality.db.repo;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.repo.SequenceRepository;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.apps.base.service.administration.SequenceService;
import com.axelor.apps.base.service.exception.TraceBackService;
import com.axelor.apps.quality.db.QualityImprovement;
import com.axelor.apps.quality.exception.QualityExceptionMessage;
import com.axelor.i18n.I18n;
import com.google.common.base.Strings;
import com.google.inject.Inject;
import javax.persistence.PersistenceException;

public class QualityImprovementManagementRepository extends QualityImprovementRepository {

  protected SequenceService sequenceService;

  @Inject
  public QualityImprovementManagementRepository(SequenceService sequenceService) {
    this.sequenceService = sequenceService;
  }

  @Override
  public QualityImprovement save(QualityImprovement qualityImprovement) {
    try {
      if (Strings.isNullOrEmpty(qualityImprovement.getSequence())) {
        Company company = qualityImprovement.getCompany();
        String sequence =
            sequenceService.getSequenceNumber(
                SequenceRepository.QUALITY_IMPROVEMENT,
                company,
                QualityImprovement.class,
                "sequence");

        if (sequence == null) {
          throw new AxelorException(
              company,
              TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
              I18n.get(QualityExceptionMessage.QUALITY_IMPROVEMENT_SEQUENCE_ERROR),
              company.getName());
        } else {
          qualityImprovement.setSequence(sequence);
        }
      }
      return super.save(qualityImprovement);

    } catch (AxelorException e) {
      TraceBackService.traceExceptionFromSaveMethod(e);
      throw new PersistenceException(e.getMessage(), e);
    }
  }
}
