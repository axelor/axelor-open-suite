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
package com.axelor.apps.tool;

import com.axelor.common.ObjectUtils;
import com.axelor.db.JpaRepository;
import com.axelor.db.JpaSecurity;
import com.axelor.db.Model;
import com.axelor.db.ParallelTransactionExecutor;
import com.axelor.db.Query;
import com.axelor.db.mapper.Mapper;
import com.axelor.db.mapper.Property;
import com.axelor.db.tenants.TenantResolver;
import com.axelor.inject.Beans;
import com.axelor.rpc.filter.Filter;
import com.axelor.rpc.filter.JPQLFilter;
import com.google.common.collect.Lists;
import com.google.inject.persist.Transactional;
import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MassUpdateTool {

  private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  private MassUpdateTool() {}

  public static <T extends Model> Integer update(
      Class<T> beanClass, String fieldName, Object newValue, List<? extends Number> selectedIds) {

    final JpaRepository<T> beanRepo = JpaRepository.of(beanClass);
    final long startMS = System.currentTimeMillis();

    List<T> objectList = getQuery(fieldName, newValue, selectedIds, beanClass).fetch();

    if (objectList.isEmpty()) {
      return 0;
    }

    final ParallelTransactionExecutor executor = getExecutor();
    final Property property = Mapper.of(beanClass).getProperty(fieldName);

    List<List<T>> dataList = Lists.partition(objectList, 25);
    AtomicInteger counter = new AtomicInteger(0);

    dataList
        .parallelStream()
        .forEach(
            list -> executor.add(() -> updateStatus(list, newValue, property, counter, beanRepo)));
    executor.run();

    int recordsUpdated = counter.get();

    long endMS = System.currentTimeMillis();
    LOG.debug("{} records updated in {} miliseconds", recordsUpdated, endMS - startMS);

    return recordsUpdated;
  }

  private static <T extends Model> Query<T> getQuery(
      String fieldName,
      Object newValue,
      List<? extends Number> selectedIds,
      final Class<T> beanClass) {
    StringBuilder querySB = new StringBuilder();
    List<Object> params = new ArrayList<>();

    querySB.append(String.format("self.%s != ?", fieldName));
    params.add(newValue);

    if (ObjectUtils.notEmpty(selectedIds)) {
      querySB.append(
          String.format(
              " AND self.id in (%s)",
              selectedIds.parallelStream().map(Number::toString).collect(Collectors.joining(","))));
    }

    Filter domainFilter = JPQLFilter.forDomain(querySB.toString(), params);
    JpaSecurity security = Beans.get(JpaSecurity.class);
    Filter permFilter = security.getFilter(JpaSecurity.CAN_WRITE, beanClass);
    if (permFilter != null) {
      domainFilter = Filter.and(domainFilter, permFilter);
    }
    return domainFilter.build(beanClass);
  }

  @Transactional
  private static <T extends Model> void updateStatus(
      List<T> dataList,
      Object value,
      Property property,
      AtomicInteger counter,
      JpaRepository<T> beanRepo) {
    for (T object : dataList) {
      object = beanRepo.find(object.getId());
      property.set(object, value);
      beanRepo.save(object);
      counter.incrementAndGet();
    }
  }

  private static ParallelTransactionExecutor getExecutor() {
    final String tenantId = TenantResolver.currentTenantIdentifier();
    final String tenantHost = TenantResolver.currentTenantHost();
    return new ParallelTransactionExecutor(tenantId, tenantHost);
  }
}
