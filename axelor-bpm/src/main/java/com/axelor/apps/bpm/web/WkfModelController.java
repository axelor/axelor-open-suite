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
package com.axelor.apps.bpm.web;

import com.axelor.apps.base.db.Wizard;
import com.axelor.apps.bpm.db.WkfModel;
import com.axelor.apps.bpm.db.WkfProcess;
import com.axelor.apps.bpm.db.WkfProcessConfig;
import com.axelor.apps.bpm.db.repo.WkfModelRepository;
import com.axelor.apps.bpm.db.repo.WkfProcessConfigRepository;
import com.axelor.apps.bpm.service.WkfModelService;
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
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import javax.persistence.Table;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

public class WkfModelController {

  @Inject protected WkfModelRepository wkfModelRepository;

  @Inject private WkfModelService wkfModelService;

  private static final String CHART_MONTH_STATUS = "chart.wkf.model.status.per.month";

  private static final String CHART_DAY_STATUS = "chart.wkf.model.status.per.day";

  private static final String CHART_TIMESPENT_STATUS = "chart.wkf.model.time.spent.per.status";

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

    wkfModel = wkfModelRepository.find(wkfModel.getId());

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

    wkfModel = wkfModelRepository.find(wkfModel.getId());

    wkfModelService.start(wkfModel);

    response.setReload(true);
  }

  public void terminate(ActionRequest request, ActionResponse response) {

    WkfModel wkfModel = request.getContext().asType(WkfModel.class);

    wkfModel = wkfModelRepository.find(wkfModel.getId());

    wkfModelService.terminate(wkfModel);

    response.setReload(true);
  }

  public void backToDraft(ActionRequest request, ActionResponse response) {

    WkfModel wkfModel = request.getContext().asType(WkfModel.class);

    wkfModel = wkfModelRepository.find(wkfModel.getId());

    wkfModelService.backToDraft(wkfModel);

    response.setReload(true);
  }

  public void createNewVersion(ActionRequest request, ActionResponse response) {

    WkfModel wkfModel = request.getContext().asType(WkfModel.class);

    wkfModel = wkfModelRepository.find(wkfModel.getId());

    wkfModel = wkfModelService.createNewVersion(wkfModel);

    response.setValue("newVersionId", wkfModel.getId());
  }

  public void showVersions(ActionRequest request, ActionResponse response) {

    WkfModel wkfModel = request.getContext().asType(WkfModel.class);

    List<Long> versionIds = new ArrayList<Long>();

    if (wkfModel.getId() != null) {
      wkfModel = wkfModelRepository.find(wkfModel.getId());
      versionIds = wkfModelService.findVersions(wkfModel);
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
        wkfModelService.importWkfModels(metaFile, isTranslate, sourceLanguage, targetLanguage);
    if (Strings.isNullOrEmpty(logText)) {
      response.setCanClose(true);
    } else {
      response.setValue("importLog", logText);
    }
  }

  public void importStandardBPM(ActionRequest request, ActionResponse response) {

    wkfModelService.importStandardBPM();

    response.setReload(true);
  }

  public void showDashboard(ActionRequest request, ActionResponse response) {

    try {
      WkfModel wkfModel = request.getContext().asType(WkfModel.class);
      wkfModel = wkfModelRepository.find(wkfModel.getId());
      if (CollectionUtils.isEmpty(wkfModel.getWkfProcessList())) {
        return;
      }

      if (wkfModel.getWkfProcessList().size() == 1) {
        response.setView(
            ActionView.define(I18n.get("Workflow dashboard"))
                .add("dashboard", "dasbhoard-wkf-model")
                .context("_wkfId", wkfModel.getId())
                .context("_process", wkfModel.getWkfProcessList().get(0).getName())
                .map());
      } else {
        response.setView(
            ActionView.define(I18n.get("Select process"))
                .model(Wizard.class.getName())
                .add("form", "wfk-model-select-process-wizard-form")
                .param("popup", "true")
                .param("popup-save", "false")
                .param("show-confirm", "false")
                .param("show-toolbar", "false")
                .context("_wkf", wkfModel)
                .map());
      }
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  public void setModelsDomain(ActionRequest request, ActionResponse response) {

    try {
      WkfModel wkfModel = null;
      List<Long> jsonModelIds = new ArrayList<>();
      List<Long> metaModelIds = new ArrayList<>();

      if (request.getContext().get("wkfId") != null) {
        wkfModel =
            wkfModelRepository.find(Long.parseLong(request.getContext().get("wkfId").toString()));
      }

      String process = (String) request.getContext().get("_process");

      if (wkfModel != null) {
        for (WkfProcess wkfProcess : wkfModel.getWkfProcessList()) {
          if (CollectionUtils.isEmpty(wkfProcess.getWkfProcessConfigList())
              || !wkfProcess.getName().equals(process)) {
            continue;
          }
          for (WkfProcessConfig processConfig : wkfProcess.getWkfProcessConfigList()) {
            if (processConfig.getMetaModel() != null) {
              metaModelIds.add(processConfig.getMetaModel().getId());
            }

            if (processConfig.getMetaJsonModel() != null) {
              jsonModelIds.add(processConfig.getMetaJsonModel().getId());
            }
          }
        }
      }

      response.setAttr(
          "metaJsonModel",
          "domain",
          !jsonModelIds.isEmpty()
              ? "self.id IN (" + StringUtils.join(jsonModelIds, ',') + ")"
              : "self.id IN (0)");

      response.setAttr(
          "metaModel",
          "domain",
          !metaModelIds.isEmpty()
              ? "self.id IN (" + StringUtils.join(metaModelIds, ',') + ")"
              : "self.id IN (0)");

    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  @SuppressWarnings("unchecked")
  public void showRecord(ActionRequest request, ActionResponse response) {

    try {
      Context context = request.getContext();
      String status = context.get("status").toString();
      String tableName = null;
      String jsonModel = null;

      ActionViewBuilder actionViewBuilder = null;
      if (context.get("metaModel") != null) {
        Long id =
            Long.parseLong(((Map<String, Object>) context.get("metaModel")).get("id").toString());
        MetaModel metaModel = Beans.get(MetaModelRepository.class).find(id);
        tableName = metaModel.getTableName();
        actionViewBuilder = createActionBuilder(status, metaModel);
      } else if (context.get("metaJsonModel") != null) {
        Long id =
            Long.parseLong(
                ((Map<String, Object>) context.get("metaJsonModel")).get("id").toString());
        MetaJsonModel metaJsonModel = Beans.get(MetaJsonModelRepository.class).find(id);
        jsonModel = metaJsonModel.getName();
        tableName = MetaJsonRecord.class.getAnnotation(Table.class).name();
        actionViewBuilder = createActionBuilder(status, metaJsonModel);
      }

      List<Long> idList = getRecordIds(context, tableName, jsonModel);

      response.setView(actionViewBuilder.context("ids", !idList.isEmpty() ? idList : 0).map());

      response.setCanClose(true);

    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  private ActionViewBuilder createActionBuilder(String status, MetaJsonModel metaJsonModel) {

    String title =
        Strings.isNullOrEmpty(status)
            ? metaJsonModel.getTitle()
            : metaJsonModel.getTitle() + "-" + status;

    ActionViewBuilder actionViewBuilder =
        ActionView.define(title)
            .model(MetaJsonRecord.class.getName())
            .add("grid", metaJsonModel.getGridView().getName())
            .add("form", metaJsonModel.getFormView().getName())
            .domain("self.jsonModel = :jsonModel AND self.id IN (:ids)")
            .context("jsonModel", metaJsonModel.getName());

    return actionViewBuilder;
  }

  private ActionViewBuilder createActionBuilder(String status, MetaModel metaModel) {

    String viewPrefix = Inflector.getInstance().dasherize(metaModel.getName());

    String title =
        Strings.isNullOrEmpty(status) ? metaModel.getName() : metaModel.getName() + "-" + status;

    ActionViewBuilder actionViewBuilder =
        ActionView.define(title)
            .model(metaModel.getFullName())
            .add("grid", viewPrefix + "-grid")
            .add("form", viewPrefix + "-form")
            .domain("self.id IN (:ids)");

    return actionViewBuilder;
  }

  private List<Long> getRecordIds(Context context, String tableName, String jsonModel) {

    String month = (String) context.get("month");
    String day = (String) context.get("day");
    LocalDate fromDate = LocalDate.parse(context.get("fromDate").toString());
    LocalDate toDate = LocalDate.parse(context.get("toDate").toString());
    String _chart = context.get("_chart").toString();
    String status = context.get("status").toString();

    List<Long> idList = new ArrayList<Long>();
    if (tableName != null) {
      switch (_chart) {
        case CHART_MONTH_STATUS:
          idList = wkfModelService.getStatusPerMonthRecord(tableName, status, month, jsonModel);
          break;
        case CHART_DAY_STATUS:
          idList = wkfModelService.getStatusPerDayRecord(tableName, status, day, jsonModel);
          break;
        case CHART_TIMESPENT_STATUS:
          idList =
              wkfModelService.getTimespentPerStatusRecord(
                  tableName, status, fromDate, toDate, jsonModel);
          break;
        default:
          break;
      }
    }

    return idList;
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

    ActionViewBuilder actionViewBuilder = null;
    if (isMetaModel) {
      MetaModel metaModel = Beans.get(MetaModelRepository.class).findByName(modelName);
      actionViewBuilder = this.createActionBuilder(status, metaModel);

    } else {
      MetaJsonModel metaJsonModel = Beans.get(MetaJsonModelRepository.class).findByName(modelName);
      actionViewBuilder = this.createActionBuilder(status, metaJsonModel);
    }

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
          ActionView.define(metaModel.getName())
              .model(metaModel.getFullName())
              .add("form", viewPrefix + "-form");

    } else {
      MetaJsonModel metaJsonModel = Beans.get(MetaJsonModelRepository.class).findByName(modelName);

      actionViewBuilder =
          ActionView.define(metaJsonModel.getTitle())
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
      wkfModel = wkfModelRepository.find(wkfModel.getId());
      User user = AuthUtils.getUser();
      boolean superUser = user.getCode().equals("admin");
      if (superUser) {
        return;
      }

      if (isAdmin(wkfModel, user)) {
        return;
      }

      response.setAttr("actionPanelBtn", "hidden", true);
      response.setAttr("adminPanel", "hidden", true);
      response.setAttr("managerPanel", "hidden", true);

      if (isManager(wkfModel, user)) {
        return;
      }

      response.setAttr("allProcessPanel", "hidden", true);

      if (isUser(wkfModel, user)) {
        return;
      }

      response.setAttr("userPanel", "hidden", true);
      response.setAttr("myProcessPanel", "hidden", true);

    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  public boolean isAdmin(WkfModel wkfModel, User user) {
    boolean isAdminUser = wkfModel.getAdminUserSet().contains(user);
    boolean isAdminRole =
        wkfModel.getAdminRoleSet().stream()
            .filter(
                role -> {
                  return user.getRoles().contains(role);
                })
            .findAny()
            .isPresent();

    if (isAdminUser || isAdminRole) {
      return true;
    }
    return false;
  }

  public boolean isManager(WkfModel wkfModel, User user) {
    boolean isManagerUser = wkfModel.getManagerUserSet().contains(user);
    boolean isManagerRole =
        wkfModel.getManagerRoleSet().stream()
            .filter(
                role -> {
                  return user.getRoles().contains(role);
                })
            .findAny()
            .isPresent();

    if (isManagerUser || isManagerRole) {
      return true;
    }
    return false;
  }

  public boolean isUser(WkfModel wkfModel, User user) {
    boolean isUser = wkfModel.getUserSet().contains(user);
    boolean isUserRole =
        wkfModel.getRoleSet().stream()
            .filter(
                role -> {
                  return user.getRoles().contains(role);
                })
            .findAny()
            .isPresent();

    if (isUser || isUserRole) {
      return true;
    }
    return false;
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
