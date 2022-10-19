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
package com.axelor.apps.bpm.listener;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.camunda.bpm.engine.impl.bpmn.parser.BpmnParse;
import org.camunda.bpm.engine.impl.cfg.StandaloneProcessEngineConfiguration;
import org.camunda.bpm.engine.impl.el.ExpressionManager;
import org.camunda.bpm.engine.impl.javax.el.ELContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SendTaskExecution implements JavaDelegate {

  protected static final Logger log = LoggerFactory.getLogger(SendTaskExecution.class);

  @Override
  public void execute(DelegateExecution execution) throws Exception {

    String message = null;

    message =
        execution
            .getBpmnModelElementInstance()
            .getAttributeValueNs(
                BpmnParse.CAMUNDA_BPMN_EXTENSIONS_NS.getNamespaceUri(), "messageName");

    log.debug("Message to send: {}", message);
    if (message != null) {
      StandaloneProcessEngineConfiguration configuration =
          (StandaloneProcessEngineConfiguration)
              execution.getProcessEngine().getProcessEngineConfiguration();
      ExpressionManager manager = configuration.getExpressionManager();
      ELContext elContext = manager.getElContext(execution);
      String msg = (String) manager.createValueExpression(message).getValue(elContext);
      log.debug("Message after eval expression: {}", msg);
      execution
          .getProcessEngineServices()
          .getRuntimeService()
          .createMessageCorrelation(msg)
          .correlateAll();
    }
  }
}
