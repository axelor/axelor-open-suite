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
import com.axelor.meta.db.MetaJsonField;
import com.axelor.meta.db.MetaModel;
import com.axelor.meta.db.repo.MetaJsonFieldRepository;
import com.axelor.meta.db.repo.MetaModelRepository;
import com.axelor.studio.db.AppBuilder;
import com.axelor.studio.service.JsonFieldService;
import com.axelor.studio.service.StudioMetaService;
import com.google.inject.Inject;

public class MetaJsonFieldRepo extends MetaJsonFieldRepository {

  @Inject MetaModelRepository metaModelRepo;

  @Override
  public MetaJsonField save(MetaJsonField metaJsonField) {

    AppBuilder appBuilder = metaJsonField.getAppBuilder();
    if (appBuilder != null) {
      metaJsonField.setIncludeIf("__config__.app.isApp('" + appBuilder.getCode() + "')");
    }

    Beans.get(JsonFieldService.class).updateSelection(metaJsonField);

    return super.save(metaJsonField);
  }

  @Override
  public void remove(MetaJsonField metaJsonField) {

    if (metaJsonField.getJsonModel() == null) {

      MetaModel metaModel =
          metaModelRepo.all().filter("self.fullName = ?1", metaJsonField.getModel()).fetchOne();

      Beans.get(StudioMetaService.class)
          .trackingFields(metaModel, metaJsonField.getName(), "Field removed");
    }

    Beans.get(JsonFieldService.class).removeSelection(metaJsonField);

    super.remove(metaJsonField);
  }
}
