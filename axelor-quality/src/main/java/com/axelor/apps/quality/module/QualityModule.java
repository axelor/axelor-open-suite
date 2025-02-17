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
package com.axelor.apps.quality.module;

import com.axelor.app.AxelorModule;
import com.axelor.apps.quality.db.repo.ControlEntryManagementRepository;
import com.axelor.apps.quality.db.repo.ControlEntryPlanLineManagementRepository;
import com.axelor.apps.quality.db.repo.ControlEntryPlanLineRepository;
import com.axelor.apps.quality.db.repo.ControlEntryRepository;
import com.axelor.apps.quality.db.repo.ControlPlanFrequencyManagementRepository;
import com.axelor.apps.quality.db.repo.ControlPlanFrequencyRepository;
import com.axelor.apps.quality.db.repo.ControlPlanManagementRepository;
import com.axelor.apps.quality.db.repo.ControlPlanRepository;
import com.axelor.apps.quality.db.repo.QualityAlertManagementRepository;
import com.axelor.apps.quality.db.repo.QualityAlertRepository;
import com.axelor.apps.quality.db.repo.QualityControlManagementRepository;
import com.axelor.apps.quality.db.repo.QualityControlRepository;
import com.axelor.apps.quality.db.repo.QualityImprovementManagementRepository;
import com.axelor.apps.quality.db.repo.QualityImprovementRepository;
import com.axelor.apps.quality.service.ControlEntryPlanLineService;
import com.axelor.apps.quality.service.ControlEntryPlanLineServiceImpl;
import com.axelor.apps.quality.service.ControlEntryProgressValuesComputeService;
import com.axelor.apps.quality.service.ControlEntryProgressValuesComputeServiceImpl;
import com.axelor.apps.quality.service.ControlEntrySampleService;
import com.axelor.apps.quality.service.ControlEntrySampleServiceImpl;
import com.axelor.apps.quality.service.ControlEntrySampleUpdateService;
import com.axelor.apps.quality.service.ControlEntrySampleUpdateServiceImpl;
import com.axelor.apps.quality.service.ControlEntryService;
import com.axelor.apps.quality.service.ControlEntryServiceImpl;
import com.axelor.apps.quality.service.ControlPlanFrequencyComputeNameService;
import com.axelor.apps.quality.service.ControlPlanFrequencyComputeNameServiceImpl;
import com.axelor.apps.quality.service.ControlPlanFrequencyService;
import com.axelor.apps.quality.service.ControlPlanFrequencyServiceImpl;
import com.axelor.apps.quality.service.QIAnalysisService;
import com.axelor.apps.quality.service.QIAnalysisServiceImpl;
import com.axelor.apps.quality.service.QIIdentificationService;
import com.axelor.apps.quality.service.QIIdentificationServiceImpl;
import com.axelor.apps.quality.service.QIResolutionDecisionService;
import com.axelor.apps.quality.service.QIResolutionDecisionServiceImpl;
import com.axelor.apps.quality.service.QIResolutionService;
import com.axelor.apps.quality.service.QIResolutionServiceImpl;
import com.axelor.apps.quality.service.QualityControlService;
import com.axelor.apps.quality.service.QualityControlServiceImpl;
import com.axelor.apps.quality.service.QualityImprovementService;
import com.axelor.apps.quality.service.QualityImprovementServiceImpl;
import com.axelor.apps.quality.service.app.AppQualityService;
import com.axelor.apps.quality.service.app.AppQualityServiceImpl;
import com.axelor.apps.quality.service.app.QIActionDistributionService;
import com.axelor.apps.quality.service.app.QIActionDistributionServiceImpl;

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
    bind(QualityImprovementService.class).to(QualityImprovementServiceImpl.class);
    bind(ControlEntryPlanLineService.class).to(ControlEntryPlanLineServiceImpl.class);
    bind(ControlEntryService.class).to(ControlEntryServiceImpl.class);
    bind(ControlEntrySampleService.class).to(ControlEntrySampleServiceImpl.class);
    bind(QIActionDistributionService.class).to(QIActionDistributionServiceImpl.class);
    bind(QIResolutionService.class).to(QIResolutionServiceImpl.class);
    bind(ControlEntrySampleUpdateService.class).to(ControlEntrySampleUpdateServiceImpl.class);
    bind(ControlEntryProgressValuesComputeService.class)
        .to(ControlEntryProgressValuesComputeServiceImpl.class);
    bind(ControlEntryRepository.class).to(ControlEntryManagementRepository.class);
    bind(ControlPlanRepository.class).to(ControlPlanManagementRepository.class);
    bind(ControlEntryPlanLineRepository.class).to(ControlEntryPlanLineManagementRepository.class);
    bind(ControlPlanFrequencyRepository.class).to(ControlPlanFrequencyManagementRepository.class);
    bind(ControlPlanFrequencyService.class).to(ControlPlanFrequencyServiceImpl.class);
    bind(ControlPlanFrequencyComputeNameService.class)
        .to(ControlPlanFrequencyComputeNameServiceImpl.class);
  }
}
