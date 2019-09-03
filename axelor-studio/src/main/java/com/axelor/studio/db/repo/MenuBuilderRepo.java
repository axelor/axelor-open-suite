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

import com.axelor.meta.MetaStore;
import com.axelor.studio.db.ActionBuilder;
import com.axelor.studio.db.MenuBuilder;
import com.axelor.studio.service.StudioMetaService;
import com.axelor.studio.service.builder.MenuBuilderService;
import com.google.inject.Inject;

public class MenuBuilderRepo extends MenuBuilderRepository {

  @Inject private MenuBuilderService menuBuilderService;

  @Inject private ActionBuilderRepo actionBuilderRepo;

  @Inject private StudioMetaService metaService;

  @Override
  public MenuBuilder save(MenuBuilder menuBuilder) {

    if (menuBuilder.getName() == null) {
      menuBuilder.setName("studio-menu-" + menuBuilder.getId());
    }

    if (menuBuilder.getActionBuilder() != null) {
      menuBuilder.getActionBuilder().setMenuAction(true);
    }

    menuBuilder = super.save(menuBuilder);

    menuBuilderService.build(menuBuilder);

    return menuBuilder;
  }

  @Override
  public MenuBuilder copy(MenuBuilder menuBuilder, boolean deep) {

    ActionBuilder actionBuilder = menuBuilder.getActionBuilder();
    menuBuilder.setActionBuilder(null);

    menuBuilder = super.copy(menuBuilder, deep);

    if (actionBuilder != null) {
      menuBuilder.setActionBuilder(actionBuilderRepo.copy(actionBuilder, deep));
    }

    return menuBuilder;
  }

  @Override
  public void remove(MenuBuilder menuBuilder) {

    metaService.removeMetaMenu(menuBuilder.getName());

    ActionBuilder actionBuilder = menuBuilder.getActionBuilder();

    menuBuilder.setActionBuilder(null);
    if (actionBuilder != null) {
      try {
        actionBuilderRepo.remove(actionBuilder);
      } catch (Exception e) {
      }
    }

    MetaStore.clear();

    super.remove(menuBuilder);
  }
}
