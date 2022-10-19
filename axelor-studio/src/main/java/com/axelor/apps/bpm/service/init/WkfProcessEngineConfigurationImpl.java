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

import com.axelor.apps.bpm.context.WkfContextHelper;
import com.axelor.apps.bpm.script.AxelorScriptEngineFactory;
import com.axelor.apps.bpm.script.ContextFunctionMapper;
import com.axelor.db.JPA;
import com.axelor.inject.Beans;
import java.util.ArrayList;
import java.util.List;
import org.camunda.bpm.application.impl.event.ProcessApplicationEventListenerPlugin;
import org.camunda.bpm.engine.impl.cfg.StandaloneProcessEngineConfiguration;
import org.camunda.bpm.engine.impl.variable.serializer.JavaObjectSerializer;
import org.camunda.bpm.engine.impl.variable.serializer.TypedValueSerializer;
import org.camunda.bpm.engine.impl.variable.serializer.jpa.EntityManagerSession;
import org.camunda.bpm.engine.impl.variable.serializer.jpa.JPAVariableSerializer;
import org.camunda.bpm.engine.variable.type.ValueType;
import org.camunda.spin.plugin.impl.SpinProcessEnginePlugin;

public class WkfProcessEngineConfigurationImpl extends StandaloneProcessEngineConfiguration {

  @Override
  protected void invokePreInit() {
    processEnginePlugins.add(new SpinProcessEnginePlugin());
    processEnginePlugins.add(new ProcessApplicationEventListenerPlugin());
    super.invokePreInit();
  }

  @Override
  protected void initJpa() {
    sessionFactories.put(
        EntityManagerSession.class,
        new WkfEntityManagerSessionFactory(JPA.em().getEntityManagerFactory(), false, false));
    JPAVariableSerializer jpaType =
        (JPAVariableSerializer) variableSerializers.getSerializerByName(JPAVariableSerializer.NAME);
    // Add JPA-type
    if (jpaType == null) {
      // We try adding the variable right after byte serializer, if available
      int serializableIndex =
          variableSerializers.getSerializerIndexByName(ValueType.BYTES.getName());
      if (serializableIndex > -1) {
        variableSerializers.addSerializer(new JPAVariableSerializer(), serializableIndex);
      } else {
        variableSerializers.addSerializer(new JPAVariableSerializer());
      }
    }
    variableSerializers.addSerializer(new JavaObjectSerializer());
    @SuppressWarnings("rawtypes")
    List<TypedValueSerializer> customPreVariableTypes = new ArrayList<TypedValueSerializer>();
    customPreVariableTypes.add(new JPAVariableSerializer());
    customPreVariableTypes.add(new JavaObjectSerializer());
    setCustomPreVariableSerializers(customPreVariableTypes);
  }

  @Override
  protected void initScripting() {
    super.initScripting();
    AxelorScriptEngineFactory factory = new AxelorScriptEngineFactory();
    scriptingEngines.getScriptEngineManager().registerEngineName("axelor", factory);
    scriptingEngines.addScriptEngineFactory(factory);
  }

  @Override
  protected void initBeans() {
    super.initBeans();
    beans.put("$ctx", Beans.get(WkfContextHelper.class));
  }

  @Override
  protected void initExpressionManager() {
    super.initExpressionManager();
    expressionManager.addFunctionMapper(new ContextFunctionMapper());
  }
}
