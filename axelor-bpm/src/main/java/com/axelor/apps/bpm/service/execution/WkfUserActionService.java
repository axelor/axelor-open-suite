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

import com.axelor.apps.bpm.db.WkfTaskConfig;
import com.axelor.apps.tool.context.FullContext;
import com.axelor.auth.db.User;
import com.google.inject.persist.Transactional;
import org.camunda.bpm.engine.delegate.DelegateExecution;

public interface WkfUserActionService {

  @Transactional
  public void createUserAction(WkfTaskConfig wkfTaskConfig, DelegateExecution execution);

  public String processTitle(String title, FullContext wkfContext);

  public FullContext getModelCtx(WkfTaskConfig wkfTaskConfig, DelegateExecution execution)
      throws ClassNotFoundException;

  public User getUser(String userPath, FullContext wkfContext);
}
