/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2021 Axelor (<http://axelor.com>).
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
package com.axelor.apps.account.service;

import com.axelor.common.ObjectUtils;
import com.axelor.db.JpaRepository;
import com.axelor.db.Model;
import com.axelor.db.ParallelTransactionExecutor;
import com.axelor.db.Query;
import com.axelor.db.mapper.Mapper;
import com.axelor.db.tenants.TenantResolver;
import com.google.common.collect.Lists;
import com.google.inject.persist.Transactional;
import java.util.List;

public class MassUpdateServiceImpl implements MassUpdateService {

  @Override
  @Transactional(rollbackOn = Exception.class)
  public <T extends Model> Integer massUpdate(
      String metaModel, int statusSelect, List<Integer> selectedIds) throws Exception {
    final Class<T> modelClass = (Class<T>) Class.forName(metaModel);
    final Mapper mapper = Mapper.of(modelClass);
    final JpaRepository<T> beanRepo = JpaRepository.of(modelClass);
    Query<T> query = beanRepo.all();
    query.filter(
        "self.statusSelect != :statusSelect"
            + (ObjectUtils.notEmpty(selectedIds) ? " AND self.id in :ids" : ""));
    query.bind("statusSelect", statusSelect);
    if (ObjectUtils.notEmpty(selectedIds)) {
      query.bind("ids", selectedIds);
    }

    List<T> objectList = query.fetch();
    if (objectList.isEmpty()) {
      return 0;
    }

    final ParallelTransactionExecutor executor = getExecutor();
    List<List<T>> dataList = Lists.partition(objectList, 25);
    dataList
        .parallelStream()
        .forEach(list -> executor.add(() -> updateStatus(list, beanRepo, statusSelect, mapper)));
    executor.run();
    return objectList.size();
  }

  protected ParallelTransactionExecutor getExecutor() {
    final String tenantId = TenantResolver.currentTenantIdentifier();
    final String tenantHost = TenantResolver.currentTenantHost();
    return new ParallelTransactionExecutor(tenantId, tenantHost);
  }

  @Transactional
  protected <T extends Model> void updateStatus(
      List<T> dataList, JpaRepository<T> beanRepo, int statusSelect, Mapper mapper) {
    for (T object : dataList) {
      object = beanRepo.find(object.getId());
      mapper.set(object, "statusSelect", statusSelect);
      beanRepo.save(object);
    }
  }
}
