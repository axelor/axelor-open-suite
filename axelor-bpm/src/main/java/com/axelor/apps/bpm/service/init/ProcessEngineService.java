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
package com.axelor.apps.bpm.service.init;

import com.axelor.app.AppSettings;
import com.axelor.apps.baml.tools.BpmTools;
import com.axelor.apps.bpm.context.WkfCache;
import com.axelor.db.tenants.TenantConfig;
import com.axelor.db.tenants.TenantConfigProvider;
import com.axelor.inject.Beans;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.ProcessEngineConfiguration;
import org.camunda.bpm.engine.ProcessEngines;
import org.camunda.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.camunda.bpm.engine.repository.Deployment;
import org.camunda.bpm.engine.variable.Variables;

@Singleton
public class ProcessEngineService {

  private static final Map<String, ProcessEngine> engineMap =
      new ConcurrentHashMap<String, ProcessEngine>();

  @Inject
  public ProcessEngineService() {

    addEngine(BpmTools.getCurentTenant());

    WkfCache.initWkfModelCache();
    WkfCache.initWkfButttonCache();
  }

  public void addEngine(String tenantId) {

    TenantConfig tenantConfig = Beans.get(TenantConfigProvider.class).find(tenantId);

    if (tenantConfig == null) {
      return;
    }

    boolean multiTeant = AppSettings.get().getBoolean("application.multi_tenancy", false);

    ProcessEngineConfigurationImpl configImpl = Beans.get(WkfProcessEngineConfigurationImpl.class);

    ProcessEngine engine =
        configImpl
            .setJdbcDriver(tenantConfig.getJdbcDriver())
            .setJdbcUrl(tenantConfig.getJdbcUrl())
            .setJdbcUsername(tenantConfig.getJdbcUser())
            .setJdbcPassword(tenantConfig.getJdbcPassword())
            .setDatabaseSchemaUpdate(ProcessEngineConfiguration.DB_SCHEMA_UPDATE_TRUE)
            .setHistory(ProcessEngineConfiguration.HISTORY_AUDIT)
            .setJobExecutorActivate(!multiTeant)
            .setMetricsEnabled(false)
            .setDefaultSerializationFormat(Variables.SerializationDataFormats.JAVA.name())
            .buildProcessEngine();

    for (Deployment deployment : engine.getRepositoryService().createDeploymentQuery().list()) {
      engine
          .getManagementService()
          .registerProcessApplication(
              deployment.getId(), Beans.get(WkfProcessApplication.class).getReference());

      engine.getManagementService().registerDeploymentForJobExecutor(deployment.getId());
    }

    engineMap.put(tenantId, engine);
  }

  public ProcessEngine getEngine() {

    String tenantId = BpmTools.getCurentTenant();

    if (!engineMap.containsKey(tenantId)) {
      addEngine(tenantId);
    }

    return engineMap.get(tenantId);
  }

  public void removeEngine(String tenantId) {
    ProcessEngine engine = engineMap.get(tenantId);
    if (engine != null) {
      ProcessEngines.unregister(engine);
      engine.close();
    }
    engineMap.remove(tenantId);
    WkfCache.WKF_BUTTON_CACHE.remove(tenantId);
    WkfCache.WKF_MODEL_CACHE.remove(tenantId);
  }

  public String getWkfViewerUrl() {
    return "wkf-editor?%s&taskIds=%s&activityCount=%s";
  }
}
