/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2022 Axelor (<http://axelor.com>).
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
package com.axelor.studio.web;

import com.axelor.inject.Beans;
import com.axelor.meta.db.MetaJsonModel;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.axelor.studio.db.MenuBuilder;
import com.axelor.studio.db.repo.MenuBuilderRepo;
import com.axelor.studio.db.repo.MenuBuilderRepository;
import com.axelor.studio.db.repo.MetaJsonModelRepo;
import com.google.inject.persist.Transactional;

public class MetaJsonModelController {

  @Transactional
  public void removeMenuBuilder(ActionRequest request, ActionResponse response) {

    MetaJsonModel metaJsonModel = request.getContext().asType(MetaJsonModel.class);
    if (metaJsonModel.getMenuBuilder() != null
        && metaJsonModel.getMenuBuilder().getId() != null
        && metaJsonModel.getMenuBuilder().getMetaMenu() != null) {
      MenuBuilder menuBuilder =
          Beans.get(MenuBuilderRepository.class).find(metaJsonModel.getMenuBuilder().getId());

      metaJsonModel = Beans.get(MetaJsonModelRepo.class).find(metaJsonModel.getId());
      metaJsonModel.setMenuBuilder(null);
      Beans.get(MetaJsonModelRepo.class).save(metaJsonModel);
      Beans.get(MenuBuilderRepo.class).remove(menuBuilder);
      response.setReload(true);
    }
  }
}
