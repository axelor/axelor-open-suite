/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2019 Axelor (<http://axelor.com>).
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

import com.axelor.meta.db.MetaJsonField;
import com.axelor.meta.db.repo.MetaJsonFieldRepository;
import com.axelor.studio.db.AppBuilder;

public class MetaJsonFieldRepo extends MetaJsonFieldRepository {

  @Override
  public MetaJsonField save(MetaJsonField metajsonField) {

    AppBuilder appBuilder = metajsonField.getAppBuilder();
    if (appBuilder != null) {
      metajsonField.setIncludeIf("__config__.app.isApp('" + appBuilder.getCode() + "')");
    }

    return super.save(metajsonField);
  }
}
