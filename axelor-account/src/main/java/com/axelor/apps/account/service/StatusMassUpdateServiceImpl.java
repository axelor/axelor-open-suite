/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2005-2026 Axelor (<http://axelor.com>).
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
package com.axelor.apps.account.service;

import com.axelor.db.JPA;
import com.axelor.db.JpaRepository;
import com.axelor.db.JpaSecurity;
import com.axelor.db.Model;
import com.axelor.db.Query;
import com.axelor.db.mapper.Mapper;
import com.axelor.db.mapper.Property;
import com.axelor.rpc.filter.Filter;
import com.axelor.rpc.filter.JPQLFilter;
import com.google.common.collect.Lists;
import jakarta.inject.Inject;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import org.apache.commons.collections.CollectionUtils;

public class StatusMassUpdateServiceImpl implements StatusMassUpdateService {

  protected static final int FETCH_LIMIT = 25;

  protected final JpaSecurity jpaSecurity;

  @Inject
  public StatusMassUpdateServiceImpl(JpaSecurity jpaSecurity) {
    this.jpaSecurity = jpaSecurity;
  }

  @Override
  public <T extends Model> int update(
      Class<T> modelClass, String fieldName, Object value, List<? extends Number> selectedIds) {
    List<Long> idsToUpdate =
        getQuery(modelClass, fieldName, value, selectedIds).select("id").fetch(0, 0).stream()
            .map(record -> (Long) record.get("id"))
            .collect(Collectors.toList());
    if (idsToUpdate.isEmpty()) {
      return 0;
    }

    JpaRepository<T> repository = JpaRepository.of(modelClass);
    Property property = Mapper.of(modelClass).getProperty(fieldName);
    for (List<Long> batchIds : Lists.partition(idsToUpdate, FETCH_LIMIT)) {
      JPA.runInTransaction(
          () -> {
            for (Long id : batchIds) {
              T record = repository.find(id);
              property.set(record, value);
              repository.save(record);
            }
          });
      JPA.clear();
    }
    return idsToUpdate.size();
  }

  protected <T extends Model> Query<T> getQuery(
      Class<T> modelClass, String fieldName, Object value, List<? extends Number> selectedIds) {
    StringBuilder domain = new StringBuilder(String.format("self.%s != ?", fieldName));
    List<Object> params = new ArrayList<>();
    params.add(value);
    if (CollectionUtils.isNotEmpty(selectedIds)) {
      domain.append(" AND self.id IN (?)");
      params.add(selectedIds.stream().map(Number::longValue).collect(Collectors.toList()));
    }

    Filter filter = JPQLFilter.forDomain(domain.toString(), params.toArray());
    Filter securityFilter = jpaSecurity.getFilter(JpaSecurity.CAN_WRITE, modelClass);
    if (securityFilter != null) {
      filter = Filter.and(filter, securityFilter);
    }
    return filter.build(modelClass);
  }
}
