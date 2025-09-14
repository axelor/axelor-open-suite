/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2005-2025 Axelor (<http://axelor.com>).
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
package com.axelor.apps.businessproject.service.batch;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Batch;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.apps.base.exceptions.BaseExceptionMessage;
import com.axelor.apps.base.service.administration.AbstractBatchService;
import com.axelor.apps.businessproject.db.BusinessProjectBatch;
import com.axelor.apps.businessproject.db.repo.BusinessProjectBatchRepository;
import com.axelor.db.EntityHelper;
import com.axelor.db.Model;
import com.axelor.db.mapper.Mapper;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.axelor.rpc.Context;
import com.axelor.rpc.JsonContext;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.apache.commons.collections.CollectionUtils;

public class BusinessProjectBatchService extends AbstractBatchService {

  @Override
  protected Class<? extends Model> getModelClass() {
    return BusinessProjectBatch.class;
  }

  @Override
  public Batch run(Model model) throws AxelorException {

    Batch batch;
    BusinessProjectBatch businessProjectBatch = (BusinessProjectBatch) model;

    switch (businessProjectBatch.getActionSelect()) {
      case BusinessProjectBatchRepository.ACTION_UPDATE_TASKS:
        batch = updateTask(businessProjectBatch);
        break;

      case BusinessProjectBatchRepository.ACTION_GENERATE_INVOICING_PROJECT:
        batch = generateInvoicingProject(businessProjectBatch);
        break;

      case BusinessProjectBatchRepository.ACTION_COMPUTE_PROJECT_TOTALS:
        batch = computeProjectTotals(businessProjectBatch);
        break;

      case BusinessProjectBatchRepository.ACTION_BACKUP_TO_PROJECT_HISTORY:
        batch = backuptoProjectHistory(businessProjectBatch);
        break;

      case BusinessProjectBatchRepository.ACTION_TRANSITION_BETWEEN_PAID_STATUS:
        batch = transitionBetweenPaidStatus(businessProjectBatch);
        break;

      default:
        throw new AxelorException(
            TraceBackRepository.CATEGORY_INCONSISTENCY,
            I18n.get(BaseExceptionMessage.BASE_BATCH_1),
            businessProjectBatch.getActionSelect(),
            businessProjectBatch.getCode());
    }
    return batch;
  }

  public Batch updateTask(BusinessProjectBatch businessProjectBatch) {
    return Beans.get(BatchUpdateTaskService.class).run(businessProjectBatch);
  }

  public Batch generateInvoicingProject(BusinessProjectBatch businessProjectBatch) {
    return Beans.get(BatchInvoicingProjectService.class).run(businessProjectBatch);
  }

  private Batch computeProjectTotals(BusinessProjectBatch businessProjectBatch) {
    return Beans.get(BatchComputeProjectTotalsService.class).run(businessProjectBatch);
  }

  protected Batch backuptoProjectHistory(BusinessProjectBatch businessProjectBatch) {
    return Beans.get(BatchBackupToProjectHistoryService.class).run(businessProjectBatch);
  }

  protected Batch transitionBetweenPaidStatus(BusinessProjectBatch businessProjectBatch) {
    return Beans.get(BatchTransitionBetweenPaidStatusService.class).run(businessProjectBatch);
  }

  @SuppressWarnings("unchecked")
  public static Map<String, Object> createJsonContext(Batch batch) throws ClassNotFoundException {

    Context context = new Context(batch.getClass());
    Class<? extends Model> klass =
        (Class<? extends Model>) Class.forName(batch.getClass().getName());

    JsonContext jsonContext =
        new JsonContext(context, Mapper.of(klass).getProperty("attrs"), batch.getAttrs());

    Map<String, Object> _map = new HashMap<String, Object>();
    _map.put("context", context);
    _map.put("jsonContext", jsonContext);
    return _map;
  }

  @SuppressWarnings("unchecked")
  public static void updateJsonObject(
      Batch batch, List<Object> recordList, String field, Map<String, Object> contextValues) {

    JsonContext jsonContext = (JsonContext) contextValues.get("jsonContext");
    List<Object> dataList = recordList;

    if (jsonContext.containsKey(field)) {
      dataList =
          ((List<Object>) jsonContext.get(field))
              .stream()
                  .map(
                      obj -> {
                        if (Mapper.toMap(EntityHelper.getEntity(obj)).get("id") != null) {
                          Map<String, Object> idMap = new HashMap<String, Object>();
                          idMap.put("id", Mapper.toMap(EntityHelper.getEntity(obj)).get("id"));
                          return idMap;
                        }
                        return obj;
                      })
                  .collect(Collectors.toList());

      dataList.addAll(recordList);
    }

    jsonContext.put(field, dataList);
    Context context = (Context) contextValues.get("context");
    batch.setAttrs(context.get("attrs").toString());
  }

  @SuppressWarnings("unchecked")
  public String getShowRecordIds(Batch batch, String field) throws ClassNotFoundException {

    Context context = new Context(batch.getClass());
    Class<? extends Model> klass =
        (Class<? extends Model>) Class.forName(batch.getClass().getName());

    JsonContext jsonContext =
        new JsonContext(context, Mapper.of(klass).getProperty("attrs"), batch.getAttrs());

    List<Map<String, Object>> recordList = (List<Map<String, Object>>) jsonContext.get(field);

      return !CollectionUtils.isEmpty(recordList)
          ? recordList.stream()
              .map(_map -> _map.get("id").toString())
              .collect(Collectors.joining(","))
          : null;
  }
}
