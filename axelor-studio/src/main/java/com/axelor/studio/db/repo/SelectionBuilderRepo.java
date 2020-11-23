/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2020 Axelor (<http://axelor.com>).
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
package com.axelor.studio.db.repo;

import com.axelor.inject.Beans;
import com.axelor.studio.db.SelectionBuilder;
import com.axelor.studio.service.builder.SelectionBuilderService;

public class SelectionBuilderRepo extends SelectionBuilderRepository {

  @Override
  public SelectionBuilder save(SelectionBuilder selectionBuilder) {

    Beans.get(SelectionBuilderService.class).build(selectionBuilder);

    return super.save(selectionBuilder);
  }

  @Override
  public void remove(SelectionBuilder selectionBuilder) {

    Beans.get(SelectionBuilderService.class)
        .removeSelection(null, SelectionBuilderService.SELECTION_PREFIX + selectionBuilder.getId());

    super.remove(selectionBuilder);
  }
}
