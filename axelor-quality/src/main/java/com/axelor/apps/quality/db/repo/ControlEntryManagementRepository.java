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
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.apps.quality.db.ControlEntry;
import com.axelor.apps.quality.db.ControlEntrySample;
import com.axelor.apps.quality.service.ControlEntrySampleUpdateService;
import com.axelor.inject.Beans;
import jakarta.inject.Inject;
import jakarta.persistence.PersistenceException;

public class ControlEntryManagementRepository extends ControlEntryRepository {

  protected AppBaseService appBaseService;

  @Inject
  public ControlEntryManagementRepository(AppBaseService appBaseService) {
    this.appBaseService = appBaseService;
  }

  /** The result of a sample is derived from the results of its lines, whatever the save path. */
  @Override
  public ControlEntry save(ControlEntry entity) {
    if (entity.getControlEntrySamplesList() != null) {
      ControlEntrySampleUpdateService updateService =
          Beans.get(ControlEntrySampleUpdateService.class);
      try {
        for (ControlEntrySample sample : entity.getControlEntrySamplesList()) {
          updateService.updateResult(sample);
        }
      } catch (AxelorException e) {
        throw new PersistenceException(e);
      }
    }
    return super.save(entity);
  }

  @Override
  public ControlEntry copy(ControlEntry entity, boolean deep) {
    ControlEntry copy = super.copy(entity, deep);

    copy.setStatusSelect(DRAFT_STATUS);
    copy.setEntryDateTime(appBaseService.getTodayDateTime().toLocalDateTime());
    copy.clearControlEntrySamplesList();

    return copy;
  }
}
