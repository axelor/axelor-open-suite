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
package com.axelor.studio.service.excel.exporter;

import com.axelor.apps.tool.service.TranslationService;
import com.axelor.meta.db.MetaAction;
import com.axelor.meta.db.MetaMenu;
import com.axelor.meta.db.repo.MetaMenuRepository;
import com.axelor.meta.loader.XMLViews;
import com.axelor.meta.schema.ObjectViews;
import com.axelor.meta.schema.actions.ActionView;
import com.axelor.meta.schema.actions.ActionView.View;
import com.google.inject.Inject;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import javax.xml.bind.JAXBException;

public class MenuExporter {

  public static final String[] MENU_HEADERS =
      new String[] {
        "Notes",
        "Object",
        "Views",
        "Name",
        "Title",
        "Title FR",
        "Parent",
        "Order",
        "Icon",
        "Background",
        "Filters",
        "Action"
      };

  public static final int OBJECT = 1;
  public static final int VIEWS = 2;
  public static final int NAME = 3;
  public static final int TITLE = 4;
  public static final int TITLE_FR = 5;
  public static final int PARENT = 6;
  public static final int ORDER = 7;
  public static final int ICON = 8;
  public static final int BACKGROUND = 9;
  public static final int FILTER = 10;
  public static final int ACTION = 11;

  @Inject private MetaMenuRepository metaMenuRepo;

  @Inject private TranslationService translationService;

  List<MetaMenu> menus = null;

  public void export(String moduleName, DataWriter writer) {
    menus = new ArrayList<>();
    getMenus();
    writeMenus(moduleName, writer);
  }

  public void getMenus() {
    if (!menus.isEmpty()) {
      return;
    }

    menus = new ArrayList<MetaMenu>();

    List<MetaMenu> parentMenus =
        metaMenuRepo
            .all()
            .filter(
                "self.left = true "
                    + "AND self.module is null "
                    + "AND self.action is null "
                    + "AND self.parent is null")
            .order("order")
            .order("id")
            .fetch();

    addMenu(parentMenus.iterator());
  }

  private void addMenu(Iterator<MetaMenu> menuIter) {

    if (!menuIter.hasNext()) return;

    MetaMenu menu = menuIter.next();
    menus.add(menu);

    List<MetaMenu> subMenus =
        metaMenuRepo.all().filter("self.parent = ?1", menu).order("order").order("id").fetch();

    addMenu(subMenus.iterator());

    addMenu(menuIter);
  }

  private void writeMenus(String moduleName, DataWriter writer) {

    String[] values = new String[MENU_HEADERS.length];
    values = MENU_HEADERS;

    writer.write("Menu", null, values);

    for (MetaMenu menu : menus) {
      values = new String[MENU_HEADERS.length];

      values[NAME] = menu.getName().replaceAll("json-model", moduleName);
      values[PARENT] = menu.getParent() != null ? menu.getParent().getName() : null;
      values[ORDER] = menu.getOrder() != null ? menu.getOrder().toString() : null;
      values[ICON] = menu.getIcon();
      values[BACKGROUND] = menu.getIconBackground();
      values[TITLE] = menu.getTitle();
      values[TITLE_FR] = translationService.getTranslation(menu.getTitle(), "fr");

      MetaAction action = menu.getAction();
      if (action != null && action.getType().equals("action-view")) {
        values = addAction(moduleName, values, action);
      }

      writer.write("Menu", null, values);
    }
  }

  private String[] addAction(String moduleName, String[] values, MetaAction metaAction) {

    values[ACTION] = metaAction.getName().replace("all.json", moduleName.replace("-", "."));
    values[OBJECT] = getModelName(metaAction.getModel());

    try {
      ObjectViews objectViews = XMLViews.fromXML(metaAction.getXml());
      ActionView action = (ActionView) objectViews.getActions().get(0);
      values[FILTER] = action.getDomain();
      List<View> views = action.getViews();
      if (views == null) {
        return values;
      }

      for (View view : views) {
        String name = view.getName();
        if (name == null) {
          continue;
        }
        if (values[VIEWS] == null) {
          values[VIEWS] = view.getName();
        } else {
          values[VIEWS] += "," + view.getName();
        }
      }
    } catch (JAXBException e) {
      e.printStackTrace();
    }

    return values;
  }

  private String getModelName(String name) {

    if (name == null) {
      return name;
    }

    String[] names = name.split("\\.");

    return names[names.length - 1];
  }
}
