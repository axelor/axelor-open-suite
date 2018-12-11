/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2018 Axelor (<http://axelor.com>).
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
package com.axelor.studio.service.creator;

import com.axelor.auth.db.Group;
import com.axelor.meta.db.MetaMenu;
import com.axelor.meta.loader.XMLViews;
import com.axelor.meta.schema.ObjectViews;
import com.axelor.meta.schema.views.MenuItem;
import com.axelor.studio.db.MenuBuilder;
import com.axelor.studio.db.repo.MenuBuilderRepository;
import com.google.inject.Inject;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.JAXBException;

public class MenuCreatorService {

  @Inject private MenuBuilderRepository menuBuilderRepo;

  private List<MenuItem> menuItems = null;

  public void writeMenus(String module, File parentPath) throws JAXBException, IOException {

    menuItems = new ArrayList<MenuItem>();

    List<MenuBuilder> menuBuilders =
        menuBuilderRepo
            .all()
            .filter("self.metaModule.name = ?1 and self.parentMenu is null", module)
            .order("order")
            .fetch();

    menuBuilders.addAll(
        menuBuilderRepo
            .all()
            .filter("self.metaModule.name = ?1 and self.parentMenu is not null", module)
            .order("order")
            .fetch());

    for (MenuBuilder menuBuilder : menuBuilders) {
      MenuItem menuItem = new MenuItem();
      menuItem.setName(menuBuilder.getName());
      menuItem.setTitle(menuBuilder.getTitle());
      menuItem.setTop(menuBuilder.getTop());
      menuItem.setXmlId(getXmlId(menuBuilder));
      MetaMenu parentMenu = menuBuilder.getParentMenu();
      if (parentMenu != null) {
        menuItem.setParent(parentMenu.getName());
      } else {
        menuItem.setIcon(menuBuilder.getIcon());
        menuItem.setIconBackground(menuBuilder.getIconBackground());
      }

      menuItem.setOrder(menuBuilder.getOrder());
      if (menuBuilder.getActionBuilder() != null) {
        menuItem.setAction(menuBuilder.getActionBuilder().getName());
      } else if (menuBuilder.getActionBuilder() != null) {
        menuItem.setAction(menuBuilder.getActionBuilder().getName());
      }

      if (menuBuilder.getGroups() != null && !menuBuilder.getGroups().isEmpty()) {
        setGroup(menuBuilder, menuItem);
      }
      menuItems.add(menuItem);
    }

    File menuFile = new File(parentPath, "Menu.xml");
    if (!menuItems.isEmpty()) {
      writeMenuFile(menuFile);
    } else {
      menuFile.delete();
    }
  }

  private String getXmlId(MenuBuilder menuBuilder) {

    return menuBuilder.getMetaModule().getName() + "-" + menuBuilder.getName();
  }

  private void setGroup(MenuBuilder menuBuilder, MenuItem menuItem) {

    String groupNames = "";
    for (Group group : menuBuilder.getGroups()) {
      if (groupNames.isEmpty()) {
        groupNames = group.getCode();
      } else {
        groupNames += "," + group.getCode();
      }
    }

    menuItem.setGroups(groupNames);
  }

  private void writeMenuFile(File menuFile) throws JAXBException, IOException {

    FileWriter fileWriter = new FileWriter(menuFile);

    ObjectViews objectViews = new ObjectViews();

    objectViews.setMenus(menuItems);

    XMLViews.marshal(objectViews, fileWriter);

    fileWriter.close();
  }
}
