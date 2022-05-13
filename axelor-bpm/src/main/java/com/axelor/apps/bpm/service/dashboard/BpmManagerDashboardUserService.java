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
import com.axelor.auth.db.User;
import java.util.List;
import java.util.Map;

public interface BpmManagerDashboardUserService {

  public List<WkfModel> getWkfModelsByUser(User user);

  public void getAssignedToMeTask(
      WkfProcess process,
      String modelName,
      boolean isMetaModel,
      List<Map<String, Object>> dataMapList,
      User user);

  public void getAssignedToOtherTask(
      WkfProcess process,
      String modelName,
      boolean isMetaModel,
      List<Map<String, Object>> dataMapList,
      User user);

  public Object[] computeAssignedTaskConfig(
      WkfProcess process,
      String modelName,
      boolean isMetaModel,
      User user,
      boolean withTask,
      String assignedType);

  public Map<String, Object> getStatusRecords(WkfModel wkfModel, String status, String type);

  public List<Map<String, Object>> getAvgTimePerUserData(WkfModel wkfModel, String unitType);

  public List<Map<String, Object>> getTaskToDoPerUser(WkfModel wkfModel);

  public List<Map<String, Object>> getTaskDoneTodayPerUser(WkfModel wkfModel);
}
