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
package com.axelor.apps.bpm.service.dashboard;

import com.axelor.apps.bpm.db.WkfModel;
import com.axelor.apps.bpm.db.WkfProcess;
import com.axelor.apps.bpm.db.WkfProcessConfig;
import com.axelor.apps.bpm.db.WkfTaskConfig;
import com.axelor.apps.bpm.db.repo.WkfTaskConfigRepository;
import com.axelor.apps.bpm.service.execution.WkfInstanceService;
import com.axelor.db.JPA;
import com.axelor.db.Model;
import com.axelor.meta.db.MetaJsonRecord;
import com.google.common.base.Strings;
import com.google.inject.Inject;
import java.math.BigInteger;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.persistence.Query;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

public class BpmManagerDashboardTaskServiceImpl implements BpmManagerDashboardTaskService {

  @Inject private WkfInstanceService wkfInstanceService;

  @Inject private WkfTaskConfigRepository wkfTaskConfigRepo;

  @Inject private WkfDashboardCommonService wkfDashboardCommonService;

  @SuppressWarnings({"unchecked"})
  @Override
  public void getTaskByProcess(
      Map<String, Object> _map,
      WkfProcess process,
      String taskByProcessType,
      List<Map<String, Object>> dataMapList) {

    Map<String, Object> taskMap = (Map<String, Object>) _map.get("tasks");

    List<Long> lateTaskIds = (List<Long>) taskMap.get("lateTaskIds");
    List<Long> taskTodayIds = (List<Long>) taskMap.get("taskTodayIds");
    List<Long> taskNextIds = (List<Long>) taskMap.get("taskNextIds");

    int nonTaskCnt = taskTodayIds.size() + taskNextIds.size();

    final String processName =
        (!StringUtils.isBlank(process.getDescription())
            ? process.getDescription()
            : process.getName());

    Map<String, Object> dataMap = new HashMap<>();
    dataMap.put("model", taskMap.get("modelName").toString());
    dataMap.put(
        "total",
        taskByProcessType.equals(WkfDashboardCommonService.LATE_TASK)
            ? lateTaskIds.size()
            : taskByProcessType.equals(WkfDashboardCommonService.NON_LATE_TASK) ? nonTaskCnt : 0);
    dataMap.put("process", processName);
    dataMapList.add(dataMap);
  }

  @Override
  public Map<String, Object> getTaskByProcessRecords(
      WkfModel wkfModel, String processName, String model, String typeSelect) {

    Map<String, Object> dataMap = new HashMap<>();
    List<WkfProcess> processList = wkfDashboardCommonService.findProcesses(wkfModel, processName);

    if (CollectionUtils.isEmpty(processList)) {
      return dataMap;
    }
    WkfProcess process = processList.get(0);

    WkfProcessConfig processConfig =
        process.getWkfProcessConfigList().stream()
            .filter(
                config ->
                    (config.getMetaModel() != null && config.getMetaModel().getName().equals(model))
                        || (config.getMetaJsonModel() != null
                            && config.getMetaJsonModel().getName().equals(model)))
            .findFirst()
            .get();

    boolean isMetaModel = processConfig.getMetaModel() != null;
    String modelName =
        isMetaModel
            ? processConfig.getMetaModel().getName()
            : processConfig.getMetaJsonModel().getName();

    List<Long> recordIds =
        this.computeTaskByProcessConfig(modelName, isMetaModel, process, wkfModel, typeSelect);

    if (!CollectionUtils.isEmpty(recordIds)) {
      dataMap.put("modelName", modelName);
      dataMap.put("isMetaModel", isMetaModel);
      dataMap.put("recordIds", recordIds);
      return dataMap;
    }
    return dataMap;
  }

  @SuppressWarnings("unchecked")
  private List<Long> computeTaskByProcessConfig(
      String modelName,
      boolean isMetaModel,
      WkfProcess process,
      WkfModel wkfModel,
      String typeSelect) {

    List<WkfTaskConfig> taskConfigs =
        wkfTaskConfigRepo
            .all()
            .filter(
                "(self.modelName = ?1 OR self.jsonModelName = ?1) "
                    + "AND self.processId = ?2 AND self.wkfModel = ?3",
                modelName,
                process.getProcessId(),
                wkfModel)
            .fetch();

    List<Long> recordIds = new ArrayList<>();

    taskConfigs.forEach(
        config -> {
          List<String> processInstanceIds =
              wkfInstanceService.findProcessInstanceByNode(
                  config.getName(), config.getProcessId(), config.getType(), false);

          Map<String, Object> taskMap = new HashMap<>();
          wkfDashboardCommonService.getTasks(
              config, processInstanceIds, modelName, isMetaModel, null, taskMap, null, null);

          if (typeSelect.equals(WkfDashboardCommonService.NON_LATE_TASK)) {
            recordIds.addAll(((List<Long>) taskMap.get("taskTodayIds")));
            recordIds.addAll(((List<Long>) taskMap.get("taskNextIds")));

          } else if (typeSelect.equals(WkfDashboardCommonService.LATE_TASK)) {
            recordIds.addAll((List<Long>) taskMap.get("lateTaskIds"));
          }
        });
    return recordIds;
  }

  @Override
  public List<Map<String, Object>> getTaskCompletionByDays(LocalDate fromDate, LocalDate toDate) {
    List<Map<String, Object>> dataMapList = new ArrayList<>();

    List<WkfTaskConfig> taskConfigs =
        wkfTaskConfigRepo.all().filter("self.type = 'userTask'").fetch();

    for (LocalDate date = fromDate; date.compareTo(toDate) <= 0; date = date.plusDays(1)) {
      int lateTaskCnt = 0;
      int validatedTaskCnt = 0;
      Map<String, Object> lateTaskMap = new HashMap<>();
      Map<String, Object> validatedTaskMap = new HashMap<>();

      for (WkfTaskConfig config : taskConfigs) {
        List<String> validatedTaskInstanceIds =
            wkfInstanceService.findProcessInstanceByNode(
                config.getName(), config.getProcessId(), config.getType(), true);

        List<String> lateTaskInstanceIds =
            wkfInstanceService.findProcessInstanceByNode(
                config.getName(), config.getProcessId(), config.getType(), false);

        boolean isMetaModel = StringUtils.isNotEmpty(config.getModelName());
        String modelName = isMetaModel ? config.getModelName() : config.getJsonModelName();

        String qry =
            "SELECT COUNT(task.id_) AS total FROM act_hi_taskinst task "
                + "WHERE task.proc_def_id_ = :processInstanceId "
                + "AND task.name_ = :status "
                + "AND task.proc_inst_id_ IN (:instanceId) "
                + "AND DATE(task.end_time_) = :toDate";

        Query query = JPA.em().createNativeQuery(qry);
        query.setParameter(
            "processInstanceId",
            !Strings.isNullOrEmpty(config.getProcessId()) ? config.getProcessId() : "");
        query.setParameter("status", config.getDescription());
        query.setParameter("toDate", date);
        query.setParameter("instanceId", validatedTaskInstanceIds);
        validatedTaskCnt += ((BigInteger) query.getSingleResult()).intValue();

        if (!isMetaModel) {
          List<MetaJsonRecord> lateTaskRecords =
              wkfDashboardCommonService.getMetaJsonRecords(
                  config,
                  lateTaskInstanceIds,
                  modelName,
                  null,
                  WkfDashboardCommonService.LATE_TASK,
                  null,
                  date);

          lateTaskCnt += lateTaskRecords.size();

        } else {
          List<Model> lateTaskRecords =
              wkfDashboardCommonService.getMetaModelRecords(
                  config,
                  lateTaskInstanceIds,
                  modelName,
                  null,
                  WkfDashboardCommonService.LATE_TASK,
                  null,
                  date);

          lateTaskCnt += lateTaskRecords.size();
        }
      }

      lateTaskMap.put("status", WkfDashboardCommonService.NUM_LATE_TASK);
      lateTaskMap.put("total", lateTaskCnt);
      lateTaskMap.put("dates", date.toString());

      validatedTaskMap.put("status", WkfDashboardCommonService.NUM_VALIDATE_TASK);
      validatedTaskMap.put("total", validatedTaskCnt);
      validatedTaskMap.put("dates", date.toString());

      dataMapList.add(lateTaskMap);
      dataMapList.add(validatedTaskMap);
    }

    return dataMapList;
  }
}
