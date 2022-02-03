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

import com.axelor.common.ObjectUtils;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.axelor.meta.db.MetaAction;
import com.axelor.meta.db.MetaMenu;
import com.axelor.meta.db.repo.MetaMenuRepository;
import com.axelor.meta.loader.XMLViews;
import com.axelor.meta.schema.ObjectViews;
import com.axelor.meta.schema.actions.ActionView;
import com.axelor.meta.schema.actions.ActionView.ActionViewBuilder;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.axelor.rpc.Context;
import com.axelor.studio.db.ActionBuilder;
import com.axelor.studio.db.MenuBuilder;
import com.axelor.studio.service.builder.MenuBuilderService;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class MenuBuilderController {

  @SuppressWarnings("unchecked")
  public void fetchMenu(ActionRequest request, ActionResponse response) {

    Context context = request.getContext();

    Map<String, Object> values = null;
    Map<String, Object> existingMenu = (Map<String, Object>) context.get("existingMenu");
    if (existingMenu == null) {
      values = getEmptyMenu();
    } else {
      Long menuId = Long.parseLong(existingMenu.get("id").toString());
      values = getMenu(Beans.get(MetaMenuRepository.class).find(menuId));
    }

    response.setValues(values);
  }

  private Map<String, Object> getMenu(MetaMenu menu) {

    Map<String, Object> values = new HashMap<>();
    values.put("name", menu.getName());
    values.put("title", menu.getTitle());
    values.put("icon", menu.getIcon());
    values.put("iconBackground", menu.getIconBackground());
    values.put("order", menu.getOrder());
    values.put("conditionToCheck", menu.getConditionToCheck());
    values.put("parentMenu", menu.getParent());
    values.put("tag", menu.getTag());
    values.put("tagGet", menu.getTagGet());
    values.put("tagCount", menu.getTagCount());
    values.put("tagStyle", menu.getTagStyle());
    values.put("groups", menu.getGroups());
    values.put("top", menu.getTop());
    values.put("roles", menu.getRoles());
    values.put("conditionTocheck", menu.getConditionToCheck());
    values.put("link", menu.getLink());
    values.put("left", menu.getLeft());
    values.put("mobile", menu.getMobile());
    values.put("hidden", menu.getHidden());

    if (menu.getAction() != null && menu.getAction().getType().contentEquals("action-view")) {
      Optional<ActionBuilder> actionBuilderOpt =
          Beans.get(MenuBuilderService.class).createActionBuilder(menu.getAction());
      actionBuilderOpt.ifPresent(
          actionBuilder -> {
            values.put("actionBuilder", actionBuilder);
            values.put("showAction", true);
          });
    }

    return values;
  }

  private Map<String, Object> getEmptyMenu() {

    Map<String, Object> values = new HashMap<>();
    values.put("name", null);
    values.put("title", null);
    values.put("icon", null);
    values.put("iconBackground", null);
    values.put("order", null);
    values.put("conditionToCheck", null);
    values.put("parentMenu", null);
    values.put("tag", null);
    values.put("tagGet", null);
    values.put("tagCount", null);
    values.put("tagStyle", null);
    values.put("groups", null);
    values.put("top", null);
    values.put("roles", null);
    values.put("conditionTocheck", null);
    values.put("link", null);
    values.put("left", null);
    values.put("mobile", null);
    values.put("hidden", null);
    values.put("actionBuilder", null);
    values.put("showAction", false);

    return values;
  }

  public void showMenuBuilderRecords(ActionRequest request, ActionResponse response) {

    MenuBuilder menuBuilder = request.getContext().asType(MenuBuilder.class);
    if (menuBuilder.getMetaMenu() == null || menuBuilder.getMetaMenu().getAction() == null) {
      return;
    }

    try {
      MetaAction metaAction = menuBuilder.getMetaMenu().getAction();
      ObjectViews objectViews = XMLViews.fromXML(metaAction.getXml());
      ActionView actionView = (ActionView) objectViews.getActions().get(0);

      ActionViewBuilder actionViewBuilder = ActionView.define(I18n.get(actionView.getTitle()));
      actionViewBuilder.model(actionView.getModel());
      actionViewBuilder.icon(actionView.getIcon());
      actionViewBuilder.domain(actionView.getDomain());
      actionViewBuilder.context("jsonModel", menuBuilder.getActionBuilder().getModel());
      actionView.getViews().forEach(view -> actionViewBuilder.add(view.getType(), view.getName()));
      if (ObjectUtils.notEmpty(actionView.getParams())) {
        actionView
            .getParams()
            .forEach(param -> actionViewBuilder.param(param.getName(), param.getValue()));
      }

      response.setView(actionViewBuilder.map());
    } catch (Exception e) {
    }
  }
}
