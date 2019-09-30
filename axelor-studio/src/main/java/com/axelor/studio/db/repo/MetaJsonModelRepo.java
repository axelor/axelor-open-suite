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

import com.axelor.meta.db.MetaJsonModel;
import com.axelor.meta.db.repo.MetaJsonModelRepository;
import com.axelor.studio.db.AppBuilder;

public class MetaJsonModelRepo extends MetaJsonModelRepository {

  @Override
  public MetaJsonModel save(MetaJsonModel jsonModel) {
    jsonModel = super.save(jsonModel);

    if (jsonModel.getMenu() != null) {
      AppBuilder appBuilder = jsonModel.getAppBuilder();
      if (appBuilder != null) {
        jsonModel
            .getMenu()
            .setConditionToCheck("__config__.app.isApp('" + appBuilder.getCode() + "')");
      } else {
        jsonModel.getMenu().setConditionToCheck(null);
      }
    }
    return jsonModel;
  }
}
