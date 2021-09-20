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
package com.axelor.apps.account.db.repo;

import com.axelor.apps.account.db.AnalyticMoveLine;
import com.axelor.apps.account.module.AccountModule;
import javax.annotation.Priority;
import javax.enterprise.inject.Alternative;

@Alternative
@Priority(AccountModule.PRIORITY)
public class AnalyticMoveLineMngtRepository extends AnalyticMoveLineRepository {
  @Override
  public AnalyticMoveLine copy(AnalyticMoveLine entity, boolean deep) {
    AnalyticMoveLine copy = super.copy(entity, deep);
    copy.setMoveLine(null);
    copy.setInvoiceLine(null);
    return copy;
  }
}
