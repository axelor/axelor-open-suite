/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2005-2024 Axelor (<http://axelor.com>).
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package com.axelor.apps.base.job;

import com.axelor.apps.base.service.administration.AbstractBatchService;
import com.axelor.db.JPA;
import com.axelor.db.Model;
import com.axelor.db.mapper.Mapper;
import com.axelor.inject.Beans;
import com.axelor.meta.db.MetaSchedule;
import com.axelor.meta.db.repo.MetaScheduleRepository;
import com.axelor.rpc.Context;
import com.axelor.script.GroovyScriptHelper;
import com.axelor.script.ScriptHelper;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.beanutils.PropertyUtils;
import org.quartz.JobDetail;
import org.quartz.JobExecutionContext;

public class BatchJob extends ThreadedJob {

  @Override
  public void executeInThread(JobExecutionContext context) {
    JobDetail jobDetail = context.getJobDetail();
    MetaSchedule metaSchedule =
        Beans.get(MetaScheduleRepository.class).findByName(jobDetail.getKey().getName());
    String batchServiceClassName = metaSchedule.getBatchServiceSelect();
    Class<? extends AbstractBatchService> batchServiceClass;

    try {
      batchServiceClass =
          Class.forName(batchServiceClassName).asSubclass(AbstractBatchService.class);
    } catch (ClassNotFoundException e) {
      throw new UncheckedJobExecutionException(e);
    }

    AbstractBatchService batchService = Beans.get(batchServiceClass);
    String batchCode = metaSchedule.getBatchCode();

    Model batchModel = batchService.findModelByCode(batchCode);

    if (batchModel == null) {
      String msg =
          String.format("Batch %s not found with service %s", batchCode, batchServiceClassName);
      throw new UncheckedJobExecutionException(msg);
    }

    // Apply job's parameters to the batch.
    Map<String, Object> originalProperties =
        applyBeanPropertiesWithScriptHelper(batchModel, jobDetail.getJobDataMap());

    try {
      batchService.run(batchModel);
    } catch (Exception e) {
      throw new UncheckedJobExecutionException(e);
    } finally {
      if (!JPA.em().contains(batchModel)) {
        batchModel = batchService.findModelByCode(batchCode);
      }

      // Restore original values on the batch.
      applyBeanProperties(batchModel, originalProperties);
    }
  }

  private Map<String, Object> applyBeanPropertiesWithScriptHelper(
      Object bean, Map<String, Object> properties) {
    Context scriptContext = new Context(Mapper.toMap(bean), bean.getClass());
    ScriptHelper scriptHelper = new GroovyScriptHelper(scriptContext);
    return applyBeanProperties(bean, properties, value -> scriptHelper.eval(value.toString()));
  }

  private Map<String, Object> applyBeanProperties(Object bean, Map<String, Object> properties) {
    return applyBeanProperties(bean, properties, value -> value);
  }

  private Map<String, Object> applyBeanProperties(
      Object bean, Map<String, Object> properties, Function<Object, Object> evalFunc) {
    Map<String, Object> originalProperties = new HashMap<>();

    JPA.runInTransaction(
        () -> {
          for (Map.Entry<String, Object> entry : properties.entrySet()) {
            String key = entry.getKey();
            if (PropertyUtils.isWriteable(bean, key)) {
              try {
                originalProperties.put(key, BeanUtils.getProperty(bean, key));
                Object value = evalFunc.apply(entry.getValue());
                BeanUtils.setProperty(bean, key, value);
              } catch (IllegalAccessException
                  | InvocationTargetException
                  | NoSuchMethodException e) {
                throw new UncheckedJobExecutionException(e);
              }
            }
          }
        });

    return originalProperties;
  }
}
