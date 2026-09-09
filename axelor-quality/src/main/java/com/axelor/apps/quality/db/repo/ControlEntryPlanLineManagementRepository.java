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
package com.axelor.apps.quality.db.repo;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.quality.db.ControlEntryPlanLine;
import com.axelor.apps.quality.service.ControlEntrySampleUpdateService;
import com.axelor.apps.quality.service.ControlTypeFieldValueService;
import com.axelor.inject.Beans;
import jakarta.inject.Inject;
import jakarta.persistence.PersistenceException;

public class ControlEntryPlanLineManagementRepository extends ControlEntryPlanLineRepository {

  protected ControlTypeFieldValueService controlTypeFieldValueService;

  @Inject
  public ControlEntryPlanLineManagementRepository(
      ControlTypeFieldValueService controlTypeFieldValueService) {
    this.controlTypeFieldValueService = controlTypeFieldValueService;
  }

  /** A line saved on its own keeps the result of its sample consistent. */
  @Override
  public ControlEntryPlanLine save(ControlEntryPlanLine entity) {
    ControlEntryPlanLine saved = super.save(entity);
    if (saved.getControlEntrySample() != null) {
      try {
        Beans.get(ControlEntrySampleUpdateService.class)
            .updateResult(saved.getControlEntrySample());
      } catch (AxelorException e) {
        throw new PersistenceException(e);
      }
    }
    return saved;
  }

  /**
   * The measured values are never carried over. The reference values are duplicated on a deep copy
   * only, so that duplicating a control plan keeps its configuration without making the creation of
   * the control entry samples, which copies each line shallowly, build values it would throw away.
   */
  @Override
  public ControlEntryPlanLine copy(ControlEntryPlanLine entity, boolean deep) {
    ControlEntryPlanLine copy = super.copy(entity, deep);

    copy.clearEntryValueList();
    copy.clearPlanValueList();

    if (deep && entity.getPlanValueList() != null) {
      entity.getPlanValueList().stream()
          .map(controlTypeFieldValueService::copyValue)
          .forEach(copy::addPlanValueListItem);
    }

    return copy;
  }
}
