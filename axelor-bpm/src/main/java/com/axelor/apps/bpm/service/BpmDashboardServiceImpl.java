/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2021 Axelor (<http://axelor.com>).
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
package com.axelor.apps.bpm.service;

import com.axelor.apps.bpm.db.WkfInstance;
import com.axelor.apps.bpm.db.WkfModel;
import com.axelor.apps.bpm.db.WkfProcess;
import com.axelor.apps.bpm.db.WkfProcessConfig;
import com.axelor.apps.bpm.db.WkfTaskConfig;
import com.axelor.apps.bpm.db.repo.WkfInstanceRepository;
import com.axelor.apps.bpm.db.repo.WkfModelRepository;
import com.axelor.apps.bpm.db.repo.WkfTaskConfigRepository;
import com.axelor.apps.bpm.service.execution.WkfInstanceService;
import com.axelor.apps.bpm.service.init.ProcessEngineService;
import com.axelor.apps.bpm.web.WkfModelController;
import com.axelor.auth.AuthUtils;
import com.axelor.auth.db.User;
import com.google.inject.Inject;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.camunda.bpm.engine.RuntimeService;

public class BpmDashboardServiceImpl implements BpmDashboardService {

  public static final int FETCH_LIMIT = 20;
  public static final String ASSIGNED_ME = "assignedToMe";
  public static final String ASSIGNED_OTHER = "assignedToOther";
  public static final String ACTIVE_STATUS = "activeStatus";
  public static final String ACTIVE_INST = "Active";
  public static final String INACTIVE_INST = "Inactive";

  @Inject private WkfModelRepository wkfModelRepo;

  @Inject private WkfModelService wkfModelService;

  @Inject private WkfModelController wkfModelController;

  @Inject private WkfInstanceRepository wkfInstanceRepo;

  @Inject private ProcessEngineService engineService;

  @Inject private WkfInstanceService wkfInstanceService;

  @Inject private WkfTaskConfigRepository wkfTaskConfigRepo;

  private List<WkfModel> getWkfModelsByUser(User user) {
    List<WkfModel> wkfModelList = wkfModelRepo.all().order("code").fetch();
    List<WkfModel> filterWkfModels = new ArrayList<>();

    for (WkfModel wkfModel : wkfModelList) {
      List<WkfProcess> processes = wkfModelService.getProcesses(wkfModel);
      if (CollectionUtils.isEmpty(processes)) {
        continue;
      }

      boolean isSuperAdmin = user.getCode().equals("admin");
      boolean isAdmin = wkfModelController.isAdmin(wkfModel, user);
      boolean isManager = wkfModelController.isManager(wkfModel, user);
      boolean isUser = wkfModelController.isUser(wkfModel, user);

      if (!isSuperAdmin && !isAdmin && !isManager && !isUser) {
        continue;
      }

      filterWkfModels.add(wkfModel);
    }
    return filterWkfModels;
  }

  @SuppressWarnings({"unchecked", "serial"})
  @Override
  public Map<String, Object> getData(int offset) {
    User user = AuthUtils.getUser();
    Map<String, Object> dataMap = new HashMap<>();
    List<Map<String, Object>> modelList = new ArrayList<>();

    List<WkfModel> wkfModelList = this.getWkfModelsByUser(user);
    long totalRecord = wkfModelList.size();

    List<WkfModel> showWkfModels =
        wkfModelList.stream().skip(offset).limit(FETCH_LIMIT).collect(Collectors.toList());

    for (WkfModel wkfModel : showWkfModels) {

      boolean isSuperAdmin = user.getCode().equals("admin");
      boolean isAdmin = wkfModelController.isAdmin(wkfModel, user);
      boolean isManager = wkfModelController.isManager(wkfModel, user);
      boolean isUser = wkfModelController.isUser(wkfModel, user);

      List<WkfProcess> processes = wkfModelService.getProcesses(wkfModel);
      List<Map<String, Object>> processList = new ArrayList<>();

      for (WkfProcess process : processes) {
        List<Map<String, Object>> configList = new ArrayList<>();

        List<WkfProcessConfig> processConfigs = process.getWkfProcessConfigList();
        wkfModelService.sortProcessConfig(processConfigs);

        List<String> _modelList = new ArrayList<>();
        for (WkfProcessConfig processConfig : processConfigs) {

          boolean isMetaModel = processConfig.getMetaModel() != null;
          String modelName =
              isMetaModel
                  ? processConfig.getMetaModel().getName()
                  : processConfig.getMetaJsonModel().getName();

          if (_modelList.contains(modelName)) {
            continue;
          }
          _modelList.add(modelName);

          Map<String, Object> _map = this.computeConfigs(process, modelName, isMetaModel, user);
          List<Long> recordIdsUserPerModel = (List<Long>) _map.get("recordIdsUserPerModel");
          List<Map<String, Object>> statusUserList =
              (List<Map<String, Object>>) _map.get("statusUserList");

          List<Long> recordIdsPerModel = (List<Long>) _map.get("recordIdsPerModel");
          List<Map<String, Object>> statusList = (List<Map<String, Object>>) _map.get("statusList");

          List<Long> recordIdsModel = new ArrayList<>();

          if (isSuperAdmin || isAdmin || isManager) {
            recordIdsModel.addAll(recordIdsPerModel);
            recordIdsModel.addAll(recordIdsUserPerModel);
          } else if (isUser) {
            recordIdsModel.addAll(recordIdsUserPerModel);
          }

          configList.add(
              new HashMap<String, Object>() {
                {
                  put("type", "model");
                  put(
                      "title",
                      !StringUtils.isBlank(processConfig.getTitle())
                          ? processConfig.getTitle()
                          : modelName);
                  put("modelName", modelName);
                  put("modelRecordCount", recordIdsModel.size());
                  put("isMetaModel", isMetaModel);
                  put("recordIdsPerModel", recordIdsModel);
                  put("userStatuses", statusUserList);
                  put("statuses", statusList);
                }
              });
        }
        processList.add(
            new HashMap<String, Object>() {
              {
                put(
                    "title",
                    !StringUtils.isBlank(process.getDescription())
                        ? process.getDescription()
                        : process.getName());
                put("itemList", configList);
              }
            });
      }

      modelList.add(
          new HashMap<String, Object>() {
            {
              put("title", wkfModel.getName());
              put("processList", processList);
              put("isSuperAdmin", isSuperAdmin);
              put("isAdmin", isAdmin);
              put("isManager", isManager);
              put("isUser", isUser);
            }
          });
    }

    dataMap.put("$modelList", modelList);
    dataMap.put("$offset", offset);
    dataMap.put("$limit", FETCH_LIMIT);
    dataMap.put("$totalRecord", totalRecord);
    return dataMap;
  }

  @SuppressWarnings({"serial"})
  private Map<String, Object> computeConfigs(
      WkfProcess process, String modelName, boolean isMetaModel, User user) {

    List<WkfTaskConfig> userTaskConfigs =
        wkfModelService.getTaskConfigs(process, modelName, isMetaModel, user, true);

    Object userObj[] =
        wkfModelService.computeTaskConfig(
            userTaskConfigs, modelName, isMetaModel, user, false, ASSIGNED_ME);

    List<WkfTaskConfig> taskConfigs =
        wkfModelService.getTaskConfigs(process, modelName, isMetaModel, user, false);

    Object obj[] =
        wkfModelService.computeTaskConfig(
            taskConfigs, modelName, isMetaModel, user, false, ASSIGNED_OTHER);

    return new HashMap<String, Object>() {
      {
        put("recordIdsUserPerModel", userObj[0]);
        put("statusUserList", userObj[1]);
        put("recordIdsPerModel", obj[0]);
        put("statusList", obj[1]);
      }
    };
  }

  @Override
  public List<Map<String, Object>> getChartData(WkfModel wkfModel, String type) {
    List<Map<String, Object>> dataMapList = new ArrayList<>();
    User user = AuthUtils.getUser();

    List<WkfProcess> processes = wkfModelService.getProcesses(wkfModel);

    for (WkfProcess process : processes) {
      List<WkfProcessConfig> processConfigs = process.getWkfProcessConfigList();
      wkfModelService.sortProcessConfig(processConfigs);

      List<String> _modelList = new ArrayList<>();
      for (WkfProcessConfig processConfig : processConfigs) {

        boolean isMetaModel = processConfig.getMetaModel() != null;
        String modelName =
            isMetaModel
                ? processConfig.getMetaModel().getName()
                : processConfig.getMetaJsonModel().getName();

        if (_modelList.contains(modelName)) {
          continue;
        }
        _modelList.add(modelName);

        Map<String, Object> _map =
            wkfModelService.computeStatus(isMetaModel, modelName, process, null, null);

        switch (type) {
          case ASSIGNED_ME:
            this.getAssignedToMeTask(process, modelName, isMetaModel, dataMapList, user);
            break;

          case ASSIGNED_OTHER:
            this.getAssignedToOtherTask(process, modelName, isMetaModel, dataMapList, user);
            break;

          case WkfModelServiceImpl.LATE_TASK:
            this.getLateTask(_map, process, dataMapList);
            break;

          case ACTIVE_STATUS:
            this.getActiveStatus(modelName, _map, processConfig, dataMapList);
            break;
        }
      }
    }
    return dataMapList;
  }

  @SuppressWarnings("unchecked")
  private void getAssignedToMeTask(
      WkfProcess process,
      String modelName,
      boolean isMetaModel,
      List<Map<String, Object>> dataMapList,
      User user) {

    Map<String, Object> _map = this.computeConfigs(process, modelName, isMetaModel, user);

    List<Map<String, Object>> statusList = (List<Map<String, Object>>) _map.get("statusUserList");

    for (Map<String, Object> statusMap : statusList) {
      Map<String, Object> dataMap = new HashMap<>();
      String processName =
          (!StringUtils.isBlank(process.getDescription())
              ? process.getDescription()
              : process.getName());
      dataMap.put("status", statusMap.get("title").toString() + " (" + processName + ")");
      dataMap.put("total", statusMap.get("statusCount"));
      dataMapList.add(dataMap);
    }
  }

  @SuppressWarnings("unchecked")
  private void getAssignedToOtherTask(
      WkfProcess process,
      String modelName,
      boolean isMetaModel,
      List<Map<String, Object>> dataMapList,
      User user) {

    Map<String, Object> _map = this.computeConfigs(process, modelName, isMetaModel, user);

    List<Map<String, Object>> statusList = (List<Map<String, Object>>) _map.get("statusList");

    for (Map<String, Object> statusMap : statusList) {
      Map<String, Object> dataMap = new HashMap<>();
      String processName =
          (!StringUtils.isBlank(process.getDescription())
              ? process.getDescription()
              : process.getName());
      dataMap.put("status", statusMap.get("title").toString() + " (" + processName + ")");
      dataMap.put("total", statusMap.get("statusCount"));
      dataMapList.add(dataMap);
    }
  }

  @SuppressWarnings("unchecked")
  private void getLateTask(
      Map<String, Object> _map, WkfProcess process, List<Map<String, Object>> dataMapList) {

    Map<String, Object> taskMap = (Map<String, Object>) _map.get("tasks");

    List<Map<String, Object>> lateTaskMapList =
        (List<Map<String, Object>>) taskMap.get("lateTaskMapList");

    for (Map<String, Object> lateTaskMap : lateTaskMapList) {
      Map<String, Object> dataMap = new HashMap<>();
      String processName =
          (!StringUtils.isBlank(process.getDescription())
              ? process.getDescription()
              : process.getName());
      dataMap.put("status", lateTaskMap.get("status").toString() + " (" + processName + ")");
      int lateTaskCnt = (int) lateTaskMap.get("lateTaskIds");
      dataMap.put("total", lateTaskCnt);
      dataMap.put("model", lateTaskMap.get("model").toString());
      dataMapList.add(dataMap);
    }
  }

  @SuppressWarnings("unchecked")
  private void getActiveStatus(
      String modelName,
      Map<String, Object> _map,
      WkfProcessConfig processConfig,
      List<Map<String, Object>> dataMapList) {

    WkfProcess process = processConfig.getWkfProcess();
    List<Map<String, Object>> statusList = (List<Map<String, Object>>) _map.get("statuses");

    for (Map<String, Object> statusMap : statusList) {
      Map<String, Object> dataMap = new HashMap<>();
      String processName =
          (!StringUtils.isBlank(process.getDescription())
              ? process.getDescription()
              : process.getName());
      dataMap.put("status", statusMap.get("title").toString() + " (" + processName + ")");
      dataMap.put("total", statusMap.get("statusCount"));
      dataMap.put("model", modelName);
      dataMapList.add(dataMap);
    }
  }

  @SuppressWarnings("serial")
  @Override
  public List<Map<String, Object>> getInstances() {
    RuntimeService runtimeService = engineService.getEngine().getRuntimeService();

    List<Map<String, Object>> dataMapList = new ArrayList<>();

    List<WkfInstance> wkfInstances = wkfInstanceRepo.all().fetch();

    int activeCnt = 0;
    int inactiveCnt = 0;
    for (WkfInstance wkfInstance : wkfInstances) {
      boolean isActiveInstance =
          wkfInstanceService.isActiveProcessInstance(wkfInstance.getInstanceId(), runtimeService);
      if (isActiveInstance) {
        activeCnt++;
      } else {
        inactiveCnt++;
      }
    }
    final int actCnt = activeCnt;
    final int inactCnt = inactiveCnt;

    dataMapList.add(
        new HashMap<String, Object>() {
          {
            put("status", ACTIVE_INST);
            put("total", actCnt);
          }
        });
    dataMapList.add(
        new HashMap<String, Object>() {
          {
            put("status", INACTIVE_INST);
            put("total", inactCnt);
          }
        });

    return dataMapList;
  }

  @Override
  public Map<String, Object> getStatusRecords(WkfModel wkfModel, String status, String type) {

    Map<String, Object> dataMap = new HashMap<>();
    List<WkfProcess> processList = wkfModelService.getProcesses(wkfModel);

    for (WkfProcess process : processList) {
      List<WkfProcessConfig> processConfigs = process.getWkfProcessConfigList();
      wkfModelService.sortProcessConfig(processConfigs);

      List<String> _modelList = new ArrayList<>();
      for (WkfProcessConfig processConfig : processConfigs) {

        boolean isMetaModel = processConfig.getMetaModel() != null;
        String modelName =
            isMetaModel
                ? processConfig.getMetaModel().getName()
                : processConfig.getMetaJsonModel().getName();

        if (_modelList.contains(modelName)) {
          continue;
        }
        _modelList.add(modelName);

        List<Long> recordIds =
            this.computeConfig(status, modelName, isMetaModel, process, wkfModel, type);

        if (!CollectionUtils.isEmpty(recordIds)) {
          dataMap.put("modelName", modelName);
          dataMap.put("isMetaModel", isMetaModel);
          dataMap.put("recordIds", recordIds);
          return dataMap;
        }
      }
    }
    return new HashMap<>();
  }

  @SuppressWarnings("unchecked")
  private List<Long> computeConfig(
      String status,
      String modelName,
      boolean isMetaModel,
      WkfProcess process,
      WkfModel wkfModel,
      String type) {

    String filter = "self.description = ?1 AND self.processId = ?2 AND self.wkfModel = ?3";

    WkfTaskConfig config =
        wkfTaskConfigRepo.all().filter(filter, status, process.getProcessId(), wkfModel).fetchOne();
    if (config == null) {
      return new ArrayList<>();
    }

    List<String> processInstanceIds =
        wkfInstanceService.findProcessInstanceByNode(
            config.getName(), config.getProcessId(), config.getType(), false);

    if (type.equals(BpmDashboardServiceImpl.ASSIGNED_ME)
        || type.equals(BpmDashboardServiceImpl.ASSIGNED_OTHER)) {
      return wkfModelService.getStatusRecordIds(
          config, processInstanceIds, modelName, isMetaModel, AuthUtils.getUser(), type);

    } else if (type.equals(BpmDashboardServiceImpl.ACTIVE_STATUS)) {
      return wkfModelService.getStatusRecordIds(
          config, processInstanceIds, modelName, isMetaModel, null, type);

    } else if (type.equals(WkfModelServiceImpl.LATE_TASK)) {
      Map<String, Object> taskMap = new HashMap<>();
      wkfModelService.getTasks(
          config, processInstanceIds, modelName, isMetaModel, null, taskMap, null, type);

      List<Long> lateTaskIds = (List<Long>) taskMap.get("lateTaskIds");
      return lateTaskIds;
    }
    return new ArrayList<>();
  }

  @Override
  public List<Long> getInstanceRecords(String status) {
    RuntimeService runtimeService = engineService.getEngine().getRuntimeService();

    List<WkfInstance> wkfInstances = wkfInstanceRepo.all().fetch();

    List<Long> activeRecordIds = new ArrayList<>();
    List<Long> inactiveRecordIds = new ArrayList<>();

    for (WkfInstance wkfInstance : wkfInstances) {
      boolean isActiveInstance =
          wkfInstanceService.isActiveProcessInstance(wkfInstance.getInstanceId(), runtimeService);
      if (isActiveInstance) {
        activeRecordIds.add(wkfInstance.getId());
      } else {
        inactiveRecordIds.add(wkfInstance.getId());
      }
    }

    if (status.equals(ACTIVE_INST)) {
      return activeRecordIds;
    } else if (status.equals(INACTIVE_INST)) {
      return inactiveRecordIds;
    }
    return new ArrayList<>();
  }
}
