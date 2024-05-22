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
package com.axelor.apps.quality.db.repo;

import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.apps.quality.db.ControlEntry;
import com.axelor.inject.Beans;

public class ControlEntryManagementRepository extends ControlEntryRepository {

  @Override
  public ControlEntry copy(ControlEntry entity, boolean deep) {
    ControlEntry copy = super.copy(entity, deep);

    copy.setStatusSelect(DRAFT_STATUS);
    copy.setEntryDateTime(Beans.get(AppBaseService.class).getTodayDateTime().toLocalDateTime());
    copy.clearControlEntrySamplesList();

    return copy;
  }
}
