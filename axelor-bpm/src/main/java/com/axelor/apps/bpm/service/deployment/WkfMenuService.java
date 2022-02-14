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
package com.axelor.apps.bpm.service.deployment;

import com.axelor.apps.bpm.db.WkfTaskConfig;
import com.axelor.apps.bpm.service.execution.WkfInstanceService;
import com.axelor.common.Inflector;
import com.axelor.db.mapper.Mapper;
import com.axelor.db.mapper.Property;
import com.axelor.meta.db.MetaAction;
import com.axelor.meta.db.MetaJsonRecord;
import com.axelor.meta.db.MetaMenu;
import com.axelor.meta.db.MetaModel;
import com.axelor.meta.db.repo.MetaActionRepository;
import com.axelor.meta.db.repo.MetaMenuRepository;
import com.axelor.meta.db.repo.MetaModelRepository;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.util.HashMap;
import java.util.Map;

public class WkfMenuService {

  public static final String MENU_PREFIX = "wkf-node-menu-";
  public static final String USER_MENU_PREFIX = "wkf-node-user-menu-";

  @Inject protected MetaMenuRepository metaMenuRepository;

  @Inject protected MetaActionRepository metaActionRepository;

  @Inject protected MetaModelRepository metaModelRepository;

  protected Inflector inflector = Inflector.getInstance();

  @Transactional
  public void createOrUpdateMenu(WkfTaskConfig wkfTaskConfig) {

    String name = MENU_PREFIX + wkfTaskConfig.getId();
    MetaMenu metaMenu = findOrCreateMenu(name);
    metaMenu.setTitle(wkfTaskConfig.getMenuName());
    metaMenu.setAction(createOrUpdateAction(metaMenu, wkfTaskConfig, false));
    metaMenu.setParent(null);
    metaMenu.setTagCount(wkfTaskConfig.getDisplayTagCount());
    if (wkfTaskConfig.getParentMenuName() != null) {
      metaMenu.setParent(metaMenuRepository.findByName(wkfTaskConfig.getParentMenuName()));
    }
    if (wkfTaskConfig.getPositionMenuName() != null) {
      MetaMenu positionMenu = metaMenuRepository.findByName(wkfTaskConfig.getPositionMenuName());
      if (positionMenu != null) {
        if (wkfTaskConfig.getMenuPosition() != null
            && wkfTaskConfig.getMenuPosition().equals("before")) {
          metaMenu.setOrder(positionMenu.getOrder() - 1);
        } else {
          metaMenu.setOrder(positionMenu.getOrder() + 1);
        }
      }
    }
    metaMenuRepository.save(metaMenu);
  }

  @Transactional
  public void createOrUpdateUserMenu(WkfTaskConfig wkfTaskConfig) {

    String name = USER_MENU_PREFIX + wkfTaskConfig.getId();
    MetaMenu metaMenu = findOrCreateMenu(name);
    metaMenu.setTitle(wkfTaskConfig.getUserMenuName());
    metaMenu.setAction(createOrUpdateAction(metaMenu, wkfTaskConfig, true));
    metaMenu.setParent(null);
    if (wkfTaskConfig.getUserParentMenuName() != null) {
      metaMenu.setParent(metaMenuRepository.findByName(wkfTaskConfig.getUserParentMenuName()));
    }
    metaMenu.setTagCount(wkfTaskConfig.getUserDisplayTagCount());
    if (wkfTaskConfig.getUserPositionMenuName() != null) {
      MetaMenu positionMenu =
          metaMenuRepository.findByName(wkfTaskConfig.getUserPositionMenuName());
      if (positionMenu != null) {
        if (wkfTaskConfig.getUserMenuPosition().equals("before")) {
          metaMenu.setOrder(positionMenu.getOrder() - 1);
        } else {
          metaMenu.setOrder(positionMenu.getOrder() + 1);
        }
      }
    }
    metaMenuRepository.save(metaMenu);
  }

  private MetaMenu findOrCreateMenu(String name) {

    MetaMenu menu = metaMenuRepository.findByName(name);
    if (menu == null) {
      menu = new MetaMenu(name);
    }
    return menu;
  }

  @Transactional
  public MetaAction createOrUpdateAction(
      MetaMenu metaMenu, WkfTaskConfig wkfTaskConfig, boolean userMenu) {

    if (wkfTaskConfig.getModelName() == null && wkfTaskConfig.getJsonModelName() == null) {
      return null;
    }

    String name = metaMenu.getName().replace("-", ".");
    MetaAction metaAction = metaActionRepository.findByName(name);

    if (metaAction == null) {
      metaAction = new MetaAction(name);
    }
    metaAction.setType("action-view");
    String model = getModelName(wkfTaskConfig);
    metaAction.setModel(model);
    boolean isJson = model.equals(MetaJsonRecord.class.getName());
    String query = createQuery(wkfTaskConfig, userMenu, isJson);
    Map<String, String> viewMap = getViewNames(wkfTaskConfig, userMenu, isJson);
    boolean permanent =
        userMenu ? wkfTaskConfig.getUserPermanentMenu() : wkfTaskConfig.getPermanentMenu();

    String xml =
        "<action-view name=\""
            + name
            + "\" model=\""
            + model
            + "\" title=\""
            + metaMenu.getTitle()
            + "\">\n"
            + "\t<view type=\"grid\" name=\""
            + viewMap.get("grid")
            + "\" />\n"
            + "\t<view type=\"form\" name=\""
            + viewMap.get("form")
            + "\" />\n"
            + "\t<domain>"
            + query
            + "</domain>\n"
            + "\t<context name=\"processInstanceIds\" expr=\"call:"
            + WkfInstanceService.class.getName()
            + ":findProcessInstanceByNode('"
            + wkfTaskConfig.getName()
            + "','"
            + wkfTaskConfig.getProcessId()
            + "','"
            + wkfTaskConfig.getType()
            + "',"
            + permanent
            + ")\" />\n"
            + (userMenu ? "<context name=\"currentUserId\" expr=\"eval:__user__.id\" />\n" : "")
            + (isJson
                ? "<context name=\"jsonModel\" expr=\""
                    + wkfTaskConfig.getJsonModelName()
                    + "\" />\n"
                : "")
            + "</action-view>";

    metaAction.setXml(xml);

    return metaActionRepository.save(metaAction);
  }

  private Map<String, String> getViewNames(
      WkfTaskConfig wkfTaskConfig, boolean userMenu, boolean isJson) {
    String viewPrefix = getViewPrefix(wkfTaskConfig, isJson);

    String form = viewPrefix + "-form";
    String grid = viewPrefix + "-grid";

    if (userMenu) {
      if (wkfTaskConfig.getUserFormView() != null) {
        form = wkfTaskConfig.getUserFormView();
      }
      if (wkfTaskConfig.getUserGridView() != null) {
        grid = wkfTaskConfig.getUserGridView();
      }

    } else {
      if (wkfTaskConfig.getFormView() != null) {
        form = wkfTaskConfig.getFormView();
      }
      if (wkfTaskConfig.getGridView() != null) {
        grid = wkfTaskConfig.getGridView();
      }
    }

    Map<String, String> viewMap = new HashMap<String, String>();
    viewMap.put("form", form);
    viewMap.put("grid", grid);

    return viewMap;
  }

  private String createQuery(WkfTaskConfig wkfTaskConfig, boolean userMenu, boolean isJson) {

    String query = "self.processInstanceId in (:processInstanceIds)";
    if (isJson) {
      query += " AND self.jsonModel = :jsonModel";
      if (userMenu) {
        query += " AND self.attrs." + wkfTaskConfig.getUserPath() + ".id = :currentUserId";
      }
    } else if (userMenu) {
      String path = wkfTaskConfig.getUserPath();
      String model = getModelName(wkfTaskConfig);
      try {
        Property property = Mapper.of(Class.forName(model)).getProperty(path.split("\\.")[0]);
        if (property == null) {
          path = "attrs." + path;
        }
      } catch (ClassNotFoundException e) {
        e.printStackTrace();
      }
      query += " AND self." + path + ".id = :currentUserId";
    }

    return query;
  }

  private String getViewPrefix(WkfTaskConfig wkfTaskConfig, boolean isJson) {

    if (isJson) {
      return "custom-model-" + wkfTaskConfig.getJsonModelName();
    }

    return inflector.dasherize(wkfTaskConfig.getModelName());
  }

  public String getModelName(WkfTaskConfig wkfTaskConfig) {

    if (wkfTaskConfig.getModelName() != null) {
      MetaModel metaModel = metaModelRepository.findByName(wkfTaskConfig.getModelName());
      if (metaModel != null) {
        return metaModel.getFullName();
      }
    } else if (wkfTaskConfig.getJsonModelName() != null) {
      return MetaJsonRecord.class.getName();
    }

    return null;
  }

  @Transactional
  public void removeMenu(WkfTaskConfig wkfTaskConfig) {

    String name = MENU_PREFIX + wkfTaskConfig.getId();

    MetaMenu metaMenu = metaMenuRepository.findByName(name);
    if (metaMenu != null) {
      metaMenuRepository.remove(metaMenu);
    }

    removeAction(name);
  }

  @Transactional
  public void removeUserMenu(WkfTaskConfig wkfTaskConfig) {

    String name = USER_MENU_PREFIX + wkfTaskConfig.getId();

    MetaMenu metaMenu = metaMenuRepository.findByName(name);

    if (metaMenu != null) {
      metaMenuRepository.remove(metaMenu);
    }

    removeAction(name);
  }

  @Transactional
  public void removeAction(String menuName) {

    MetaAction metaAction = metaActionRepository.findByName(menuName.replace("-", "."));

    if (metaAction != null) {
      metaActionRepository.remove(metaAction);
    }
  }
}
