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

import com.axelor.meta.MetaStore;
import com.axelor.studio.db.ActionBuilder;
import com.axelor.studio.service.StudioMetaService;
import com.axelor.studio.service.builder.ActionBuilderService;
import com.google.inject.Inject;

public class ActionBuilderRepo extends ActionBuilderRepository {

  @Inject private StudioMetaService metaService;

  @Inject private ActionBuilderService builderService;

  @Override
  public ActionBuilder save(ActionBuilder builder) {

    builder = super.save(builder);

    builderService.build(builder);

    return builder;
  }

  @Override
  public void remove(ActionBuilder actionBuilder) {

    metaService.removeMetaActions(actionBuilder.getName());

    MetaStore.clear();

    super.remove(actionBuilder);
  }
}
