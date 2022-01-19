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
package com.axelor.apps.bpm.service.execution;

import com.axelor.apps.bpm.db.WkfInstance;
import com.axelor.apps.bpm.db.WkfProcess;
import com.axelor.apps.bpm.db.WkfTaskConfig;
import com.axelor.db.Model;
import com.axelor.exception.AxelorException;
import com.axelor.meta.CallMethod;
import com.google.inject.persist.Transactional;
import java.util.List;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.delegate.DelegateExecution;

public interface WkfInstanceService {

  @Transactional
  public String evalInstance(Model model, String signal)
      throws ClassNotFoundException, AxelorException;

  @Transactional
  public WkfInstance createWkfInstance(String processInstanceId, WkfProcess wkfProcess);

  public boolean isActiveProcessInstance(String processInstanceId, RuntimeService runTimeService);

  public void deleteProcessInstance(String processInstanceId);

  @CallMethod
  public boolean isActiveTask(String processInstanceId, String taskId);

  @CallMethod
  public List<String> findProcessInstanceByNode(
      String nodeKey, String processId, String type, boolean permanent);

  public void onNodeActivation(WkfTaskConfig wkfTaskConfig, DelegateExecution execution);

  public void terminateAll();

  public String getInstanceXml(String instanceId);

  public boolean isActivatedTask(String processInstanceId, String taskId);

  public void restart(String processInstanceId, String activityId);

  public List<String> getNodes(String processInstanceId);

  public void cancelNode(String processInstanceId, String activityId);
}
