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
package com.axelor.apps.base.db.repo;

import com.axelor.apps.base.db.BaseBatch;
import com.axelor.apps.base.module.BaseModule;
import javax.annotation.Priority;
import javax.enterprise.inject.Alternative;

@Alternative
@Priority(BaseModule.PRIORITY)
public class BaseBatchBaseRepository extends BaseBatchRepository {

  @Override
  public BaseBatch copy(BaseBatch entity, boolean deep) {
    BaseBatch copy = super.copy(entity, deep);
    copy.setBatchList(null);
    return copy;
  }
}
