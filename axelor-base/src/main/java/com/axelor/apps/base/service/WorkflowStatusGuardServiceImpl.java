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
package com.axelor.apps.base.service;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.apps.base.exceptions.BaseExceptionMessage;
import com.axelor.db.EntityHelper;
import com.axelor.db.JpaRepository;
import com.axelor.db.Model;
import com.axelor.db.mapper.Mapper;
import com.axelor.i18n.I18n;
import java.util.Map;
import java.util.Objects;

public class WorkflowStatusGuardServiceImpl implements WorkflowStatusGuardService {

  protected static final String ID = "id";
  protected static final String VERSION = "version";
  protected static final String STATUS_SELECT = "statusSelect";

  @Override
  public void checkStatusUnchanged(
      JpaRepository<? extends Model> repository, Map<String, Object> values)
      throws AxelorException {
    if (repository == null || values == null || !values.containsKey(STATUS_SELECT)) {
      return;
    }
    Integer requestedStatus = toInteger(values.get(STATUS_SELECT));
    Long id = findId(values);
    if (id == null) {
      return;
    }

    // Same rules as JPA.edit: an existing record sent without its version is not edited, and a
    // stale version is rejected by the optimistic lock.
    Object requestedVersion = values.get(VERSION);
    if (requestedVersion == null) {
      return;
    }
    Model stored = repository.find(id);
    if (stored == null || !Objects.equals(requestedVersion, stored.getVersion())) {
      return;
    }
    Object storedStatus = Mapper.of(EntityHelper.getEntityClass(stored)).get(stored, STATUS_SELECT);
    if (Objects.equals(storedStatus, requestedStatus)) {
      return;
    }
    throw new AxelorException(
        stored,
        TraceBackRepository.CATEGORY_INCONSISTENCY,
        I18n.get(BaseExceptionMessage.WORKFLOW_STATUS_CHANGE_NOT_ALLOWED));
  }

  /** Same parsing as JPA.edit: a missing, null or non-positive id is a new record. */
  protected Long findId(Map<String, Object> values) {
    Object id = values.get(ID);
    if (id == null) {
      return null;
    }
    long value = Long.parseLong(id.toString());
    return value > 0 ? value : null;
  }

  protected Integer toInteger(Object value) {
    if (value == null) {
      return null;
    }
    return value instanceof Number
        ? ((Number) value).intValue()
        : Integer.valueOf(value.toString());
  }
}
