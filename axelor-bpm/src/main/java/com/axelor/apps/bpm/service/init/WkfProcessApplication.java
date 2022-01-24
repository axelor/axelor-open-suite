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

import com.axelor.apps.baml.tools.BpmTools;
import com.axelor.apps.bpm.listener.WkfExecutionListener;
import com.axelor.inject.Beans;
import org.camunda.bpm.application.ProcessApplication;
import org.camunda.bpm.application.impl.EmbeddedProcessApplication;
import org.camunda.bpm.engine.delegate.ExecutionListener;

@ProcessApplication
public class WkfProcessApplication extends EmbeddedProcessApplication {

  public ExecutionListener getExecutionListener() {

    return Beans.get(WkfExecutionListener.class);
  }

  @Override
  public String getName() {
    return BpmTools.getCurentTenant();
  }
}
