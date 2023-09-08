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
package com.axelor.apps.quality.module;

import com.axelor.app.AxelorModule;
import com.axelor.apps.quality.db.repo.QualityAlertManagementRepository;
import com.axelor.apps.quality.db.repo.QualityAlertRepository;
import com.axelor.apps.quality.db.repo.QualityControlManagementRepository;
import com.axelor.apps.quality.db.repo.QualityControlRepository;
import com.axelor.apps.quality.db.repo.QualityImprovementManagementRepository;
import com.axelor.apps.quality.db.repo.QualityImprovementRepository;
import com.axelor.apps.quality.service.QIAnalysisService;
import com.axelor.apps.quality.service.QIAnalysisServiceImpl;
import com.axelor.apps.quality.service.QIIdentificationService;
import com.axelor.apps.quality.service.QIIdentificationServiceImpl;
import com.axelor.apps.quality.service.QIResolutionDecisionService;
import com.axelor.apps.quality.service.QIResolutionDecisionServiceImpl;
import com.axelor.apps.quality.service.QualityControlService;
import com.axelor.apps.quality.service.QualityControlServiceImpl;
import com.axelor.apps.quality.service.app.AppQualityService;
import com.axelor.apps.quality.service.app.AppQualityServiceImpl;

public class QualityModule extends AxelorModule {

  @Override
  protected void configure() {
    bind(QualityControlService.class).to(QualityControlServiceImpl.class);
    bind(QualityAlertRepository.class).to(QualityAlertManagementRepository.class);
    bind(QualityControlRepository.class).to(QualityControlManagementRepository.class);
    bind(AppQualityService.class).to(AppQualityServiceImpl.class);
    bind(QualityImprovementRepository.class).to(QualityImprovementManagementRepository.class);
    bind(QIIdentificationService.class).to(QIIdentificationServiceImpl.class);
    bind(QIResolutionDecisionService.class).to(QIResolutionDecisionServiceImpl.class);
    bind(QIAnalysisService.class).to(QIAnalysisServiceImpl.class);
  }
}
