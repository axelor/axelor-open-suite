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
package com.axelor.apps.bpm.service.dashboard;

import com.axelor.apps.bpm.db.WkfModel;
import com.axelor.apps.bpm.db.WkfProcess;
import com.axelor.apps.bpm.db.WkfProcessConfig;
import com.axelor.auth.AuthUtils;
import com.axelor.auth.db.User;
import com.google.inject.Inject;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.apache.commons.lang3.StringUtils;

public class BpmManagerDashboardServiceImpl implements BpmManagerDashboardService {

  public static final int FETCH_LIMIT = 20;

  @Inject private WkfDashboardCommonService wkfDashboardCommonService;

  @Inject private BpmManagerDashboardUserService bpmMgrDashboardUserService;

  @Inject private BpmManagerDashboardTaskService bpmMgrDashboardTaskService;

  @SuppressWarnings({"unchecked", "serial"})
  @Override
  public Map<String, Object> showProcess(int offset) {
    User user = AuthUtils.getUser();
    Map<String, Object> dataMap = new HashMap<>();
    List<Map<String, Object>> modelList = new ArrayList<>();

    List<WkfModel> wkfModelList = bpmMgrDashboardUserService.getWkfModelsByUser(user);
    long totalRecord = wkfModelList.size();

    List<WkfModel> showWkfModels =
        wkfModelList.stream().skip(offset).limit(FETCH_LIMIT).collect(Collectors.toList());

    for (WkfModel wkfModel : showWkfModels) {

      boolean isSuperAdmin = user.getCode().equals("admin");
      boolean isAdmin = wkfDashboardCommonService.isAdmin(wkfModel, user);
      boolean isManager = wkfDashboardCommonService.isManager(wkfModel, user);
      boolean isUser = wkfDashboardCommonService.isUser(wkfModel, user);

      List<WkfProcess> processes = wkfDashboardCommonService.findProcesses(wkfModel, null);
      List<Map<String, Object>> processList = new ArrayList<>();

      for (WkfProcess process : processes) {
        List<Map<String, Object>> configList = new ArrayList<>();

        List<WkfProcessConfig> processConfigs = process.getWkfProcessConfigList();
        wkfDashboardCommonService.sortProcessConfig(processConfigs);

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
              this.computeAssignedTaskConfigs(process, modelName, isMetaModel, user);
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
              put("versionTag", wkfModel.getVersionTag());
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
  private Map<String, Object> computeAssignedTaskConfigs(
      WkfProcess process, String modelName, boolean isMetaModel, User user) {

    Object obj[] =
        bpmMgrDashboardUserService.computeAssignedTaskConfig(
            process, modelName, isMetaModel, user, true, WkfDashboardCommonService.ASSIGNED_ME);

    Object userObj[] =
        bpmMgrDashboardUserService.computeAssignedTaskConfig(
            process, modelName, isMetaModel, user, false, WkfDashboardCommonService.ASSIGNED_OTHER);

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
  public List<Map<String, Object>> getChartData(
      WkfModel wkfModel, String type, String taskByProcessType) {

    List<Map<String, Object>> dataMapList = new ArrayList<>();
    User user = AuthUtils.getUser();

    List<WkfProcess> processes = wkfDashboardCommonService.findProcesses(wkfModel, null);

    for (WkfProcess process : processes) {
      List<WkfProcessConfig> processConfigs = process.getWkfProcessConfigList();
      wkfDashboardCommonService.sortProcessConfig(processConfigs);

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
            wkfDashboardCommonService.computeStatus(isMetaModel, modelName, process, null, null);

        switch (type) {
          case WkfDashboardCommonService.ASSIGNED_ME:
            bpmMgrDashboardUserService.getAssignedToMeTask(
                process, modelName, isMetaModel, dataMapList, user);
            break;

          case WkfDashboardCommonService.ASSIGNED_OTHER:
            bpmMgrDashboardUserService.getAssignedToOtherTask(
                process, modelName, isMetaModel, dataMapList, user);
            break;

          case WkfDashboardCommonService.TASK_BY_PROCESS:
            bpmMgrDashboardTaskService.getTaskByProcess(
                _map, process, taskByProcessType, dataMapList);
            break;
        }
      }
    }
    return dataMapList;
  }
}
