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
package com.axelor.apps.supplychain.db.repo;

import com.axelor.apps.account.db.AnalyticMoveLine;
import com.axelor.apps.account.db.repo.AnalyticMoveLineMngtRepository;

public class AnalyticMoveLineSupplychainRepository extends AnalyticMoveLineMngtRepository {
  @Override
  public AnalyticMoveLine copy(AnalyticMoveLine entity, boolean deep) {
    AnalyticMoveLine copy = super.copy(entity, deep);
    copy.setPurchaseOrderLine(null);
    copy.setSaleOrderLine(null);
    return copy;
  }
}
