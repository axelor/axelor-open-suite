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
package com.axelor.apps.intervention.module;

import com.axelor.app.AxelorModule;
import com.axelor.apps.contract.service.ContractServiceImpl;
import com.axelor.apps.intervention.db.repo.CustomerRequestRepository;
import com.axelor.apps.intervention.db.repo.EquipmentModelRepository;
import com.axelor.apps.intervention.db.repo.InterventionQuestionRepository;
import com.axelor.apps.intervention.db.repo.InterventionRepository;
import com.axelor.apps.intervention.events.InterventionObserver;
import com.axelor.apps.intervention.repo.CustomerRequestManagementRepository;
import com.axelor.apps.intervention.repo.EquipmentModelManagementRepository;
import com.axelor.apps.intervention.repo.InterventionManagementRepository;
import com.axelor.apps.intervention.repo.InterventionQuestionManagementRepository;
import com.axelor.apps.intervention.service.AppInterventionService;
import com.axelor.apps.intervention.service.AppInterventionServiceImpl;
import com.axelor.apps.intervention.service.ContractInterventionServiceImpl;
import com.axelor.apps.intervention.service.CustomerRequestService;
import com.axelor.apps.intervention.service.CustomerRequestServiceImpl;
import com.axelor.apps.intervention.service.EquipmentLineService;
import com.axelor.apps.intervention.service.EquipmentLineServiceImpl;
import com.axelor.apps.intervention.service.EquipmentModelService;
import com.axelor.apps.intervention.service.EquipmentModelServiceImpl;
import com.axelor.apps.intervention.service.EquipmentRestService;
import com.axelor.apps.intervention.service.EquipmentRestServiceImpl;
import com.axelor.apps.intervention.service.EquipmentService;
import com.axelor.apps.intervention.service.EquipmentServiceImpl;
import com.axelor.apps.intervention.service.InterventionPartnerService;
import com.axelor.apps.intervention.service.InterventionPartnerServiceImpl;
import com.axelor.apps.intervention.service.InterventionQuestionService;
import com.axelor.apps.intervention.service.InterventionQuestionServiceImpl;
import com.axelor.apps.intervention.service.InterventionRangeService;
import com.axelor.apps.intervention.service.InterventionRangeServiceImpl;
import com.axelor.apps.intervention.service.InterventionRestService;
import com.axelor.apps.intervention.service.InterventionRestServiceImpl;
import com.axelor.apps.intervention.service.InterventionService;
import com.axelor.apps.intervention.service.InterventionServiceImpl;
import com.axelor.apps.intervention.service.InterventionSurveyGenerator;
import com.axelor.apps.intervention.service.InterventionSurveyGeneratorImpl;
import com.axelor.apps.intervention.service.ParkModelService;
import com.axelor.apps.intervention.service.ParkModelServiceImpl;
import com.axelor.apps.intervention.service.RangeQuestionService;
import com.axelor.apps.intervention.service.RangeQuestionServiceImpl;
import com.axelor.apps.intervention.service.planning.PlanningDateTimeProcessor;
import com.axelor.apps.intervention.service.planning.PlanningDateTimeProcessorImpl;
import com.axelor.apps.intervention.service.planning.PlanningDateTimeService;
import com.axelor.apps.intervention.service.planning.PlanningDateTimeServiceImpl;

public class InterventionModule extends AxelorModule {

  @Override
  protected void configure() {
    bind(EquipmentModelRepository.class).to(EquipmentModelManagementRepository.class);
    bind(CustomerRequestRepository.class).to(CustomerRequestManagementRepository.class);
    bind(InterventionRepository.class).to(InterventionManagementRepository.class);
    bind(InterventionQuestionRepository.class).to(InterventionQuestionManagementRepository.class);

    bind(AppInterventionService.class).to(AppInterventionServiceImpl.class);
    bind(ContractServiceImpl.class).to(ContractInterventionServiceImpl.class);
    bind(CustomerRequestService.class).to(CustomerRequestServiceImpl.class);
    bind(EquipmentLineService.class).to(EquipmentLineServiceImpl.class);
    bind(EquipmentModelService.class).to(EquipmentModelServiceImpl.class);
    bind(EquipmentService.class).to(EquipmentServiceImpl.class);
    bind(InterventionPartnerService.class).to(InterventionPartnerServiceImpl.class);
    bind(InterventionQuestionService.class).to(InterventionQuestionServiceImpl.class);
    bind(InterventionRangeService.class).to(InterventionRangeServiceImpl.class);
    bind(InterventionService.class).to(InterventionServiceImpl.class);
    bind(InterventionSurveyGenerator.class).to(InterventionSurveyGeneratorImpl.class);
    bind(ParkModelService.class).to(ParkModelServiceImpl.class);
    bind(RangeQuestionService.class).to(RangeQuestionServiceImpl.class);

    bind(PlanningDateTimeService.class).to(PlanningDateTimeServiceImpl.class);
    bind(PlanningDateTimeProcessor.class).to(PlanningDateTimeProcessorImpl.class);

    bind(InterventionObserver.class);
    bind(InterventionRestService.class).to(InterventionRestServiceImpl.class);
    bind(EquipmentRestService.class).to(EquipmentRestServiceImpl.class);
  }
}
