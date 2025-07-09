/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2005-2025 Axelor (<http://axelor.com>).
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
package com.axelor.apps.quality.service;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.quality.db.QIIdentification;
import com.axelor.apps.quality.db.QIResolution;
import com.axelor.apps.quality.db.QIResolutionDefault;
import com.axelor.apps.quality.db.QualityImprovement;
import com.axelor.apps.quality.db.repo.QualityImprovementRepository;
import com.axelor.auth.AuthUtils;
import com.axelor.meta.MetaFiles;
import com.axelor.meta.db.MetaFile;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.util.List;
import org.apache.commons.collections.CollectionUtils;

public class QualityImprovementCreateServiceImpl implements QualityImprovementCreateService {

  protected QualityImprovementRepository qualityImprovementRepository;
  protected QualityImprovementService qualityImprovementService;
  protected QualityImprovementCheckValuesService qualityImprovementCheckValuesService;
  protected MetaFiles metaFiles;

  @Inject
  public QualityImprovementCreateServiceImpl(
      QualityImprovementRepository qualityImprovementRepository,
      QualityImprovementService qualityImprovementService,
      QualityImprovementCheckValuesService qualityImprovementCheckValuesService,
      MetaFiles metaFiles) {
    this.qualityImprovementRepository = qualityImprovementRepository;
    this.qualityImprovementService = qualityImprovementService;
    this.qualityImprovementCheckValuesService = qualityImprovementCheckValuesService;
    this.metaFiles = metaFiles;
  }

  @Transactional(rollbackOn = Exception.class)
  @Override
  public QualityImprovement createQualityImprovement(
      QualityImprovement qualityImprovement,
      QIIdentification qiIdentification,
      QIResolution qiResolution)
      throws AxelorException {

    qualityImprovement.setCompany(AuthUtils.getUser().getActiveCompany());

    qiIdentification.setQi(qualityImprovement);
    qiResolution.setQi(qualityImprovement);
    qualityImprovement.setQiIdentification(qiIdentification);
    qualityImprovement.setQiResolution(qiResolution);

    qualityImprovement.setQiStatus(qualityImprovementService.getDefaultQIStatus());
    qualityImprovementCheckValuesService.checkQualityImprovementValues(qualityImprovement);

    qualityImprovement = qualityImprovementRepository.save(qualityImprovement);

    for (QIResolutionDefault qiResolutionDefault : qiResolution.getQiResolutionDefaultsList()) {
      List<MetaFile> fileList = qiResolutionDefault.getMetaFileList();
      if (CollectionUtils.isNotEmpty(fileList)) {
        fileList.forEach(file -> metaFiles.attach(file, file.getFileName(), qiResolutionDefault));
      }
    }
    return qualityImprovement;
  }
}
