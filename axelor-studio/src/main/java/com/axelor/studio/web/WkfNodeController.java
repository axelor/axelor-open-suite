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
package com.axelor.studio.web;

import com.axelor.inject.Beans;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.axelor.studio.db.MenuBuilder;
import com.axelor.studio.db.WkfNode;
import com.axelor.studio.db.repo.MenuBuilderRepo;
import com.axelor.studio.db.repo.MenuBuilderRepository;
import com.axelor.studio.db.repo.WkfNodeRepository;
import com.google.inject.persist.Transactional;

public class WkfNodeController {

  @Transactional
  public void removeMenuBuilder(ActionRequest request, ActionResponse response) {

    WkfNode wkfNode = request.getContext().asType(WkfNode.class);
    if (wkfNode.getMenuBuilder() != null
        && wkfNode.getMenuBuilder().getId() != null
        && wkfNode.getMenuBuilder().getMetaMenu() != null) {
      MenuBuilder menuBuilder =
          Beans.get(MenuBuilderRepository.class).find(wkfNode.getMenuBuilder().getId());

      wkfNode = Beans.get(WkfNodeRepository.class).find(wkfNode.getId());
      wkfNode.setMenuBuilder(null);
      Beans.get(WkfNodeRepository.class).save(wkfNode);
      Beans.get(MenuBuilderRepo.class).remove(menuBuilder);
      response.setReload(true);
    }
  }
}
