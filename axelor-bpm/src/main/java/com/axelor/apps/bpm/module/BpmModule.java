/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2022 Axelor (<http://axelor.com>).
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
package com.axelor.apps.bpm.module;

import com.axelor.app.AxelorModule;
import com.axelor.apps.baml.service.BamlService;
import com.axelor.apps.baml.service.BamlServiceImpl;
import com.axelor.apps.bpm.db.repo.BpmWkfInstanceRepository;
import com.axelor.apps.bpm.db.repo.BpmWkfModelRepository;
import com.axelor.apps.bpm.db.repo.WkfInstanceRepository;
import com.axelor.apps.bpm.db.repo.WkfModelRepository;
import com.axelor.apps.bpm.listener.WkfRequestListener;
import com.axelor.apps.bpm.mapper.BpmMapperScriptGeneratorServiceImpl;
import com.axelor.apps.bpm.service.AppLoaderExportBpmServiceImpl;
import com.axelor.apps.bpm.service.AppLoaderImportBpmServiceImpl;
import com.axelor.apps.bpm.service.BpmDashboardService;
import com.axelor.apps.bpm.service.BpmDashboardServiceImpl;
import com.axelor.apps.bpm.service.WkfCommonService;
import com.axelor.apps.bpm.service.WkfCommonServiceImpl;
import com.axelor.apps.bpm.service.WkfDisplayService;
import com.axelor.apps.bpm.service.WkfDisplayServiceImpl;
import com.axelor.apps.bpm.service.WkfModelService;
import com.axelor.apps.bpm.service.WkfModelServiceImpl;
import com.axelor.apps.bpm.service.deployment.BpmDeploymentService;
import com.axelor.apps.bpm.service.deployment.BpmDeploymentServiceImpl;
import com.axelor.apps.bpm.service.deployment.MetaAttrsService;
import com.axelor.apps.bpm.service.deployment.MetaAttrsServiceImpl;
import com.axelor.apps.bpm.service.execution.WkfEmailService;
import com.axelor.apps.bpm.service.execution.WkfEmailServiceImpl;
import com.axelor.apps.bpm.service.execution.WkfInstanceService;
import com.axelor.apps.bpm.service.execution.WkfInstanceServiceImpl;
import com.axelor.apps.bpm.service.execution.WkfTaskService;
import com.axelor.apps.bpm.service.execution.WkfTaskServiceImpl;
import com.axelor.apps.bpm.service.execution.WkfUserActionService;
import com.axelor.apps.bpm.service.execution.WkfUserActionServiceImpl;
import com.axelor.apps.bpm.service.init.WkfProcessApplication;
import com.axelor.apps.dmn.service.DmnDeploymentService;
import com.axelor.apps.dmn.service.DmnDeploymentServiceImpl;
import com.axelor.apps.dmn.service.DmnExportService;
import com.axelor.apps.dmn.service.DmnExportServiceImpl;
import com.axelor.apps.dmn.service.DmnImportService;
import com.axelor.apps.dmn.service.DmnImportServiceImpl;
import com.axelor.apps.dmn.service.DmnService;
import com.axelor.apps.dmn.service.DmnServiceImpl;
import com.axelor.studio.service.loader.AppLoaderExportServiceImpl;
import com.axelor.studio.service.loader.AppLoaderImportServiceImpl;
import com.axelor.studio.service.mapper.MapperScriptGeneratorServiceImpl;

public class BpmModule extends AxelorModule {

  @Override
  protected void configure() {

    bind(WkfRequestListener.class);
    bind(WkfProcessApplication.class);
    bind(WkfInstanceRepository.class).to(BpmWkfInstanceRepository.class);
    bind(WkfModelRepository.class).to(BpmWkfModelRepository.class);
    bind(WkfCommonService.class).to(WkfCommonServiceImpl.class);
    bind(WkfDisplayService.class).to(WkfDisplayServiceImpl.class);
    bind(WkfModelService.class).to(WkfModelServiceImpl.class);
    bind(BpmDeploymentService.class).to(BpmDeploymentServiceImpl.class);
    bind(MetaAttrsService.class).to(MetaAttrsServiceImpl.class);
    bind(WkfEmailService.class).to(WkfEmailServiceImpl.class);
    bind(WkfInstanceService.class).to(WkfInstanceServiceImpl.class);
    bind(WkfTaskService.class).to(WkfTaskServiceImpl.class);
    bind(WkfUserActionService.class).to(WkfUserActionServiceImpl.class);
    bind(DmnDeploymentService.class).to(DmnDeploymentServiceImpl.class);
    bind(DmnService.class).to(DmnServiceImpl.class);
    bind(BamlService.class).to(BamlServiceImpl.class);
    bind(DmnExportService.class).to(DmnExportServiceImpl.class);
    bind(DmnImportService.class).to(DmnImportServiceImpl.class);
    bind(MapperScriptGeneratorServiceImpl.class).to(BpmMapperScriptGeneratorServiceImpl.class);
    bind(AppLoaderExportServiceImpl.class).to(AppLoaderExportBpmServiceImpl.class);
    bind(BpmDashboardService.class).to(BpmDashboardServiceImpl.class);
    bind(AppLoaderImportServiceImpl.class).to(AppLoaderImportBpmServiceImpl.class);
  }
}
