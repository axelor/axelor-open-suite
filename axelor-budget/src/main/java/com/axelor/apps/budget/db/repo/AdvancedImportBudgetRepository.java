/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2005-2023 Axelor (<http://axelor.com>).
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
package com.axelor.apps.budget.db.repo;

import com.axelor.apps.base.db.AdvancedImport;
import com.axelor.apps.base.db.repo.AdvancedImportBaseRepository;

public class AdvancedImportBudgetRepository extends AdvancedImportBaseRepository {

  /** For not allowing user to delete these two records. */
  public static final String IMPORT_ID_1 = "101"; // Budget Template Import Id

  public static final String IMPORT_ID_2 = "102"; // Budget Instance Import Id

  @Override
  public void remove(AdvancedImport entity) {
    if (entity.getImportId() != null
        && (entity.getImportId().equals(IMPORT_ID_1) || entity.getImportId().equals(IMPORT_ID_2))) {
      return;
    }
    super.remove(entity);
  }
}
