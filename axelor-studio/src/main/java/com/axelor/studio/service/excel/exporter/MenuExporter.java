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
import com.axelor.db.mapper.Mapper;
import com.axelor.meta.db.MetaAction;
import com.axelor.meta.db.MetaJsonModel;
import com.axelor.meta.db.MetaMenu;
import com.axelor.meta.db.repo.MetaJsonModelRepository;
import com.axelor.meta.db.repo.MetaMenuRepository;
import com.axelor.meta.loader.XMLViews;
import com.axelor.meta.schema.ObjectViews;
import com.axelor.meta.schema.actions.ActionView;
import com.axelor.meta.schema.actions.ActionView.Context;
import com.axelor.meta.schema.actions.ActionView.View;
import com.axelor.studio.service.CommonService;
import com.axelor.studio.service.builder.ViewBuilderService;
import com.google.common.base.Strings;
import com.google.inject.Inject;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import javax.xml.bind.JAXBException;
import org.apache.commons.lang.StringUtils;

public class MenuExporter {

  @Inject private MetaMenuRepository metaMenuRepo;

  @Inject private ViewBuilderService viewBuilderService;

  @Inject private TranslationService translationService;

  @Inject private MetaJsonModelRepository metaJsonModelRepo;

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

    List<MetaMenu> customMenus =
        metaMenuRepo
            .all()
            .filter("self in (SELECT menu FROM MetaJsonModel WHERE isReal = true)")
            .order("order")
            .order("id")
            .fetch();

    addMenu(customMenus.iterator());
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

    Map<String, String> values = new HashMap<>();

    writer.write("Menu", null, CommonService.MENU_HEADERS);

    for (MetaMenu menu : menus) {
      values = new HashMap<>();
      values.put(CommonService.MENU_NAME, menu.getName().replaceAll("json-model", moduleName));
      values.put(
          CommonService.PARENT, menu.getParent() != null ? menu.getParent().getName() : null);
      values.put(CommonService.ORDER, menu.getOrder() != null ? menu.getOrder().toString() : null);
      values.put(CommonService.ICON, menu.getIcon());
      values.put(CommonService.BACKGROUND, menu.getIconBackground());
      values.put(CommonService.MENU_TITLE, menu.getTitle());
      values.put(
          CommonService.MENU_TITLE_FR, translationService.getTranslation(menu.getTitle(), "fr"));

      MetaAction action = menu.getAction();
      if (action != null && action.getType().equals("action-view")) {
        values = addAction(moduleName, values, action);
      }

      writer.write("Menu", null, values, CommonService.MENU_HEADERS);
    }
  }

  private Map<String, String> addAction(
      String moduleName, Map<String, String> values, MetaAction metaAction) {

    values.put(
        CommonService.ACTION,
        metaAction.getName().replace("all.json", moduleName.replace("-", ".")));

    try {
      ObjectViews objectViews = XMLViews.fromXML(metaAction.getXml());
      ActionView action = (ActionView) objectViews.getActions().get(0);
      String jsonModel = getJsonModel(action);
      MetaJsonModel metaJsonModel = metaJsonModelRepo.findByName(jsonModel);
      if (metaJsonModel != null) {
        if (!metaJsonModel.getIsReal()) {
          jsonModel = getModelName(action.getModel());
        }
      } else {
        jsonModel = getModelName(action.getModel());
      }
      values.put(CommonService.OBJECT, jsonModel);
      values.put(CommonService.FILTER, action.getDomain());
      List<View> views = action.getViews();
      if (views == null) {
        return values;
      }

      for (View view : views) {
        String type = view.getType();
        if (type == null) {
          continue;
        }
        if (values.get(CommonService.VIEWS) == null) {
          values.put(
              CommonService.VIEWS,
              viewBuilderService.getDefaultViewName(
                  view.getType(), values.get(CommonService.OBJECT)));
        } else {
          values.put(
              CommonService.VIEWS,
              values.get(CommonService.VIEWS)
                  + ","
                  + viewBuilderService.getDefaultViewName(
                      view.getType(), values.get(CommonService.OBJECT)));
        }
      }
    } catch (JAXBException e) {
      e.printStackTrace();
    }

    return values;
  }

  @SuppressWarnings("unchecked")
  private String getJsonModel(ActionView actionView) {
    String jsonModel = null;

    try {
      Mapper mapper = Mapper.of(ActionView.class);
      Field field = mapper.getBeanClass().getDeclaredField("contexts");
      field.setAccessible(true);
      List<ActionView.Context> contextList = (List<Context>) field.get(actionView);
      if (contextList != null) {
        jsonModel =
            contextList
                .stream()
                .filter(context -> context.getName().equals("jsonModel"))
                .findFirst()
                .get()
                .getExpression();
      }
    } catch (Exception e) {
      e.printStackTrace();
    }

    if (!Strings.isNullOrEmpty(jsonModel) && jsonModel.contains("'")) {
      return StringUtils.substringBetween(jsonModel, "'", "'");
    }

    return jsonModel;
  }

  private String getModelName(String name) {

    if (name == null) {
      return name;
    }

    String[] names = name.split("\\.");

    return names[names.length - 1];
  }
}
