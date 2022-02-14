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
package com.axelor.apps.bpm.service;

import com.axelor.apps.bpm.db.WkfModel;
import com.axelor.apps.bpm.db.WkfProcess;
import com.axelor.apps.bpm.db.WkfProcessConfig;
import com.axelor.apps.bpm.db.WkfTaskConfig;
import com.axelor.apps.bpm.db.repo.WkfModelRepository;
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

public class BpmDashboardServiceImpl implements BpmDashboardService {

  public static int FETCH_LIMIT = 20;

  @Inject private WkfModelRepository wkfModelRepo;

  @Inject private WkfModelService wkfModelService;

  @Inject private WkfModelController wkfModelController;

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
        wkfModelService.computeTaskConfig(userTaskConfigs, modelName, isMetaModel, user, false);

    List<WkfTaskConfig> taskConfigs =
        wkfModelService.getTaskConfigs(process, modelName, isMetaModel, null, false);

    Object obj[] =
        wkfModelService.computeTaskConfig(taskConfigs, modelName, isMetaModel, null, false);

    return new HashMap<String, Object>() {
      {
        put("recordIdsUserPerModel", userObj[0]);
        put("statusUserList", userObj[1]);
        put("recordIdsPerModel", obj[0]);
        put("statusList", obj[1]);
      }
    };
  }
}
