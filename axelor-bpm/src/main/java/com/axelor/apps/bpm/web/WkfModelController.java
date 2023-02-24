/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2023 Axelor (<http://axelor.com>).
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
package com.axelor.apps.bpm.web;

import com.axelor.apps.bpm.db.WkfModel;
import com.axelor.apps.bpm.db.WkfProcessConfig;
import com.axelor.apps.bpm.db.repo.WkfModelRepository;
import com.axelor.apps.bpm.db.repo.WkfProcessConfigRepository;
import com.axelor.apps.bpm.service.WkfModelService;
import com.axelor.apps.bpm.service.dashboard.WkfDashboardCommonService;
import com.axelor.apps.bpm.service.deployment.BpmDeploymentService;
import com.axelor.apps.bpm.service.execution.WkfInstanceService;
import com.axelor.auth.AuthUtils;
import com.axelor.auth.db.User;
import com.axelor.common.Inflector;
import com.axelor.exception.AxelorException;
import com.axelor.exception.service.TraceBackService;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.axelor.meta.db.MetaFile;
import com.axelor.meta.db.MetaJsonModel;
import com.axelor.meta.db.MetaJsonRecord;
import com.axelor.meta.db.MetaModel;
import com.axelor.meta.db.repo.MetaFileRepository;
import com.axelor.meta.db.repo.MetaJsonModelRepository;
import com.axelor.meta.db.repo.MetaModelRepository;
import com.axelor.meta.schema.actions.ActionView;
import com.axelor.meta.schema.actions.ActionView.ActionViewBuilder;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.axelor.rpc.Context;
import com.google.common.base.Strings;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class WkfModelController {

  @Inject private WkfDashboardCommonService wkfDashboardCommonService;

  private static final String PROCESS_PER_STATUS = "processPerStatus";
  private static final String PROCESS_PER_USER = "processPerUser";

  @SuppressWarnings("unchecked")
  public void deploy(ActionRequest request, ActionResponse response) {

    Context context = request.getContext();

    WkfModel wkfModel = context.asType(WkfModel.class);

    Map<String, Map<String, String>> migrationMap =
        (Map<String, Map<String, String>>) context.get("wkfMigrationMap");

    Boolean isMigrateOld = (Boolean) context.get("isMigrateOld");

    if (isMigrateOld != null && !isMigrateOld) {
      migrationMap = null;
    }

    wkfModel = Beans.get(WkfModelRepository.class).find(wkfModel.getId());

    Beans.get(BpmDeploymentService.class).deploy(wkfModel, migrationMap);

    response.setReload(true);
  }

  public void terminateAll(ActionRequest request, ActionResponse response) {

    Beans.get(WkfInstanceService.class).terminateAll();
  }

  public void refreshRecord(ActionRequest request, ActionResponse response) {

    response.setReload(true);
  }

  public void start(ActionRequest request, ActionResponse response) {

    WkfModel wkfModel = request.getContext().asType(WkfModel.class);

    wkfModel = Beans.get(WkfModelRepository.class).find(wkfModel.getId());

    Beans.get(WkfModelService.class).start(wkfModel);

    response.setReload(true);
  }

  public void terminate(ActionRequest request, ActionResponse response) {

    WkfModel wkfModel = request.getContext().asType(WkfModel.class);

    wkfModel = Beans.get(WkfModelRepository.class).find(wkfModel.getId());

    Beans.get(WkfModelService.class).terminate(wkfModel);

    response.setReload(true);
  }

  public void backToDraft(ActionRequest request, ActionResponse response) {

    WkfModel wkfModel = request.getContext().asType(WkfModel.class);

    wkfModel = Beans.get(WkfModelRepository.class).find(wkfModel.getId());

    Beans.get(WkfModelService.class).backToDraft(wkfModel);

    response.setReload(true);
  }

  public void createNewVersion(ActionRequest request, ActionResponse response) {

    WkfModel wkfModel = request.getContext().asType(WkfModel.class);

    wkfModel = Beans.get(WkfModelRepository.class).find(wkfModel.getId());

    wkfModel = Beans.get(WkfModelService.class).createNewVersion(wkfModel);

    response.setValue("newVersionId", wkfModel.getId());
  }

  public void showVersions(ActionRequest request, ActionResponse response) {

    WkfModel wkfModel = request.getContext().asType(WkfModel.class);

    List<Long> versionIds = new ArrayList<Long>();

    if (wkfModel.getId() != null) {
      wkfModel = Beans.get(WkfModelRepository.class).find(wkfModel.getId());
      versionIds = Beans.get(WkfModelService.class).findVersions(wkfModel);
    }

    versionIds.add(0l);

    response.setView(
        ActionView.define(I18n.get("Previous Versions"))
            .model(WkfModel.class.getName())
            .add("grid", "wkf-model-grid")
            .add("form", "wkf-model-form")
            .domain("self.id in :versionIds")
            .context("versionIds", versionIds)
            .map());
  }

  public void getInstanceXml(ActionRequest request, ActionResponse response) {

    String instanceId = (String) request.getContext().get("instanceId");

    String xml = Beans.get(WkfInstanceService.class).getInstanceXml(instanceId);

    response.setValue("xml", xml);
  }

  @SuppressWarnings("unchecked")
  @Transactional
  public void importWkfModels(ActionRequest request, ActionResponse response)
      throws AxelorException {

    boolean isTranslate =
        request.getContext().get("isTranslate") == null
            ? false
            : (boolean) request.getContext().get("isTranslate");

    String sourceLanguage = (String) request.getContext().get("sourceLanguageSelect");
    String targetLanguage = (String) request.getContext().get("targetLanguageSelect");

    String metaFileId =
        ((Map<String, Object>) request.getContext().get("dataFile")).get("id").toString();

    MetaFile metaFile = Beans.get(MetaFileRepository.class).find(Long.parseLong(metaFileId));

    String logText =
        Beans.get(WkfModelService.class)
            .importWkfModels(metaFile, isTranslate, sourceLanguage, targetLanguage);
    if (Strings.isNullOrEmpty(logText)) {
      response.setCanClose(true);
    } else {
      response.setValue("importLog", logText);
    }
  }

  public void importStandardBPM(ActionRequest request, ActionResponse response) {

    Beans.get(WkfModelService.class).importStandardBPM();

    response.setReload(true);
  }

  public void restart(ActionRequest request, ActionResponse response) {

    Context context = request.getContext();

    String processInstanceId = (String) context.get("processInstanceId");
    String activityId = (String) context.get("activityId");

    if (processInstanceId != null && activityId != null) {
      Beans.get(WkfInstanceService.class).restart(processInstanceId, activityId);
    }

    response.setFlash(I18n.get("Instance Restarted"));
  }

  public void cancelNode(ActionRequest request, ActionResponse response) {

    Context context = request.getContext();

    String processInstanceId = (String) context.get("processInstanceId");
    String activityId = (String) context.get("activityId");

    if (processInstanceId != null && activityId != null) {
      Beans.get(WkfInstanceService.class).cancelNode(processInstanceId, activityId);
    }

    response.setFlash(I18n.get("Node cancelled"));
  }

  public void getProcessPerStatus(ActionRequest request, ActionResponse response) {
    try {
      List<Map<String, Object>> dataList = this.getDataList(request, PROCESS_PER_STATUS);
      response.setData(dataList);

    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  public void getProcessPerUser(ActionRequest request, ActionResponse response) {
    try {
      List<Map<String, Object>> dataList = this.getDataList(request, PROCESS_PER_USER);
      response.setData(dataList);

    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  private List<Map<String, Object>> getDataList(ActionRequest request, String type) {
    if (request.getData().get("id") == null) {
      return new ArrayList<>();
    }
    Long wkfModelId = Long.valueOf(request.getData().get("id").toString());
    WkfModel wkfModel = Beans.get(WkfModelRepository.class).find(wkfModelId);

    switch (type) {
      case PROCESS_PER_STATUS:
        return Beans.get(WkfModelService.class).getProcessPerStatus(wkfModel);

      case PROCESS_PER_USER:
        return Beans.get(WkfModelService.class).getProcessPerUser(wkfModel);

      default:
        return new ArrayList<>();
    }
  }

  @SuppressWarnings("unchecked")
  private void openRecordView(
      ActionRequest request,
      ActionResponse response,
      String statusKey,
      String modelKey,
      String recordkey) {

    LinkedHashMap<String, Object> _map =
        (LinkedHashMap<String, Object>) request.getData().get("context");

    String status = "";
    if (statusKey != null) {
      status = _map.get("title").toString();
    }
    String modelName = _map.get(modelKey).toString();
    boolean isMetaModel = (boolean) _map.get("isMetaModel");
    List<Long> recordIds = (List<Long>) _map.get(recordkey);

    ActionViewBuilder actionViewBuilder =
        Beans.get(WkfDashboardCommonService.class)
            .computeActionView(status, modelName, isMetaModel);

    response.setView(actionViewBuilder.context("ids", !recordIds.isEmpty() ? recordIds : 0).map());
  }

  public void getStatusPerView(ActionRequest request, ActionResponse response) {
    try {
      this.openRecordView(request, response, "title", "modelName", "statusRecordIds");

    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  public void getModelPerView(ActionRequest request, ActionResponse response) {
    try {
      this.openRecordView(request, response, null, "modelName", "recordIdsPerModel");

    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  @SuppressWarnings("unchecked")
  public void newRecord(ActionRequest request, ActionResponse response) {
    try {
      LinkedHashMap<String, Object> _map =
          (LinkedHashMap<String, Object>) request.getData().get("context");
      String modelName = _map.get("modelName").toString();
      boolean isMetaModel = (boolean) _map.get("isMetaModel");

      ActionViewBuilder actionViewBuilder = this.viewNewRecord(modelName, isMetaModel);
      response.setView(actionViewBuilder.map());

    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  @SuppressWarnings({"unchecked", "rawtypes"})
  public void newInstance(ActionRequest request, ActionResponse response) {
    try {
      LinkedHashMap<String, Object> _map =
          (LinkedHashMap<String, Object>) request.getData().get("context");

      WkfProcessConfig config =
          Beans.get(WkfProcessConfigRepository.class)
              .find(Long.valueOf(((Map) _map.get("processConfig")).get("id").toString()));

      boolean isMetaModel = config.getMetaModel() != null;
      String modelName =
          isMetaModel ? config.getMetaModel().getName() : config.getMetaJsonModel().getName();

      ActionViewBuilder actionViewBuilder = this.viewNewRecord(modelName, isMetaModel);
      response.setView(actionViewBuilder.map());

    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  private ActionViewBuilder viewNewRecord(String modelName, boolean isMetaModel) {
    ActionViewBuilder actionViewBuilder = null;
    if (isMetaModel) {
      MetaModel metaModel = Beans.get(MetaModelRepository.class).findByName(modelName);
      String viewPrefix = Inflector.getInstance().dasherize(metaModel.getName());

      actionViewBuilder =
          ActionView.define(I18n.get(metaModel.getName()))
              .model(metaModel.getFullName())
              .add("form", viewPrefix + "-form");

    } else {
      MetaJsonModel metaJsonModel = Beans.get(MetaJsonModelRepository.class).findByName(modelName);

      actionViewBuilder =
          ActionView.define(I18n.get(metaJsonModel.getTitle()))
              .model(MetaJsonRecord.class.getName())
              .add("form", metaJsonModel.getFormView().getName())
              .domain("self.jsonModel = :jsonModel")
              .context("jsonModel", modelName);
    }
    return actionViewBuilder;
  }

  public void changeAttrs(ActionRequest request, ActionResponse response) {
    try {
      WkfModel wkfModel = request.getContext().asType(WkfModel.class);
      wkfModel = Beans.get(WkfModelRepository.class).find(wkfModel.getId());
      User user = AuthUtils.getUser();
      boolean superUser = user.getCode().equals("admin");
      if (superUser) {
        return;
      }

      if (wkfDashboardCommonService.isAdmin(wkfModel, user)) {
        return;
      }

      response.setAttr("actionPanelBtn", "hidden", true);
      response.setAttr("adminPanel", "hidden", true);
      response.setAttr("managerPanel", "hidden", true);

      if (wkfDashboardCommonService.isManager(wkfModel, user)) {
        return;
      }

      response.setAttr("allProcessPanel", "hidden", true);

      if (wkfDashboardCommonService.isUser(wkfModel, user)) {
        return;
      }

      response.setAttr("userPanel", "hidden", true);
      response.setAttr("myProcessPanel", "hidden", true);

    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  public void openTaskToday(ActionRequest request, ActionResponse response) {
    try {
      this.openRecordView(request, response, null, "modelName", "taskTodayIds");

    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  public void openTaskNext(ActionRequest request, ActionResponse response) {
    try {
      this.openRecordView(request, response, null, "modelName", "taskNextIds");

    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  public void openLateTask(ActionRequest request, ActionResponse response) {
    try {
      this.openRecordView(request, response, null, "modelName", "lateTaskIds");

    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }
}
