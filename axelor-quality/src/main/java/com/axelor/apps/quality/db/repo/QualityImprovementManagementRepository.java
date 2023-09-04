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
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.apps.base.service.exception.TraceBackService;
import com.axelor.apps.quality.db.QIIdentification;
import com.axelor.apps.quality.db.QIResolution;
import com.axelor.apps.quality.db.QualityImprovement;
import com.axelor.apps.quality.exception.QualityExceptionMessage;
import com.axelor.i18n.I18n;
import com.google.common.base.Strings;
import com.google.inject.Inject;
import javax.persistence.PersistenceException;

public class QualityImprovementManagementRepository extends QualityImprovementRepository {

  protected SequenceService sequenceService;
  protected AppBaseService appBaseService;

  @Inject
  public QualityImprovementManagementRepository(
      SequenceService sequenceService, AppBaseService appBaseService) {
    this.sequenceService = sequenceService;
    this.appBaseService = appBaseService;
  }

  @Override
  public QualityImprovement save(QualityImprovement qualityImprovement) {
    try {
      Company company = qualityImprovement.getCompany();
      if (Strings.isNullOrEmpty(qualityImprovement.getSequence())) {
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
      getOrCreateQIIdentification(qualityImprovement);
      getOrCreateQIResolution(qualityImprovement);

      return super.save(qualityImprovement);

    } catch (AxelorException e) {
      TraceBackService.traceExceptionFromSaveMethod(e);
      throw new PersistenceException(e.getMessage(), e);
    }
  }

  protected QIIdentification getOrCreateQIIdentification(QualityImprovement qualityImprovement) {
    QIIdentification qiIdentification = qualityImprovement.getQiIdentification();
    if (qiIdentification == null) {
      qiIdentification = new QIIdentification();
      qiIdentification.setQi(qualityImprovement);
    }
    return qiIdentification;
  }

  protected QIResolution getOrCreateQIResolution(QualityImprovement qualityImprovement) {
    QIResolution qiResolution = qualityImprovement.getQiResolution();
    if (qiResolution == null) {
      qiResolution = new QIResolution();
      qiResolution.setQi(qualityImprovement);
    }
    return qiResolution;
  }
}
