/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2023 Axelor (<http://axelor.com>).
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
import com.axelor.auth.db.Group;
import com.axelor.auth.db.Role;
import com.axelor.auth.db.User;
import com.axelor.common.Inflector;
import com.axelor.db.mapper.Mapper;
import com.axelor.db.mapper.Property;
import com.axelor.meta.CallMethod;
import com.axelor.meta.db.MetaAction;
import com.axelor.meta.db.MetaJsonRecord;
import com.axelor.meta.db.MetaMenu;
import com.axelor.meta.db.MetaModel;
import com.axelor.meta.db.repo.MetaActionRepository;
import com.axelor.meta.db.repo.MetaMenuRepository;
import com.axelor.meta.db.repo.MetaModelRepository;
import com.axelor.team.db.Team;
import com.axelor.team.db.repo.TeamRepository;
import com.google.common.base.Strings;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.apache.commons.collections.CollectionUtils;

public class WkfMenuService {

  public static final String MENU_PREFIX = "wkf-node-menu-";
  public static final String USER_MENU_PREFIX = "wkf-node-user-menu-";

  @Inject protected MetaMenuRepository metaMenuRepository;

  @Inject protected MetaActionRepository metaActionRepository;

  @Inject protected MetaModelRepository metaModelRepository;

  @Inject protected TeamRepository teamRepo;

  protected Inflector inflector = Inflector.getInstance();

  @Transactional
  public void createOrUpdateMenu(WkfTaskConfig wkfTaskConfig) {

    String name = MENU_PREFIX + wkfTaskConfig.getId();
    MetaMenu metaMenu = findOrCreateMenu(name);
    metaMenu.setTitle(wkfTaskConfig.getMenuName());
    MetaAction action = createOrUpdateAction(metaMenu, wkfTaskConfig, false);
    if (action == null) {
      if (metaMenu.getId() != null) {
        metaMenuRepository.remove(metaMenu);
      }
      return;
    }
    metaMenu.setAction(action);
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

  protected MetaMenu findOrCreateMenu(String name) {

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

    if (userMenu && query == null) {
      if (metaAction.getId() != null) {
        metaActionRepository.remove(metaAction);
      }
      return null;
    }

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
            + (userMenu
                ? "<context name=\"currentUserId\" expr=\"eval:__user__.id\" />\n<context name=\"teamIds\" expr=\"call:com.axelor.apps.bpm.service.deployment.WkfMenuService:getTeamIds(__user__)\" />\n"
                : "")
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

  protected String createQuery(WkfTaskConfig wkfTaskConfig, boolean userMenu, boolean isJson) {

    Property property = null;
    String query = "self.processInstanceId in (:processInstanceIds)";
    if (isJson) {
      query += " AND self.jsonModel = :jsonModel";
    }

    if (userMenu) {
      String path = wkfTaskConfig.getUserPath();
      String param = ":currentUserId";
      if (Strings.isNullOrEmpty(path)) {
        path = wkfTaskConfig.getTeamPath();
        if (Strings.isNullOrEmpty(path)) {
          return null;
        }
        param = ":teamIds";
      }

      if (!isJson) {
        String model = getModelName(wkfTaskConfig);
        try {
          property = Mapper.of(Class.forName(model)).getProperty(path.split("\\.")[0]);
        } catch (ClassNotFoundException e) {
          e.printStackTrace();
        }
      }

      if (property == null) {
        path = "attrs." + path;
      }

      query += " AND self." + path + ".id in (" + param + ")";
    }

    return query;
  }

  protected String getViewPrefix(WkfTaskConfig wkfTaskConfig, boolean isJson) {

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

  @CallMethod
  public List<Long> getTeamIds(User user) {
    List<Long> teamIds = new ArrayList<>();

    Set<Role> userRoles = user.getRoles();
    Group userGroup = user.getGroup();

    List<Team> teams =
        teamRepo
            .all()
            .filter(
                "?1 MEMBER OF self.members OR self IN "
                    + "(SELECT team FROM Team team INNER JOIN team.roles role "
                    + "WHERE role IN (?2) OR role IN (?3))",
                user,
                userGroup != null
                    ? CollectionUtils.isNotEmpty(userGroup.getRoles()) ? userGroup.getRoles() : null
                    : null,
                CollectionUtils.isNotEmpty(userRoles) ? userRoles : null)
            .fetch();

    if (CollectionUtils.isEmpty(teams)) {
      return teamIds;
    }

    teamIds = teams.stream().map(team -> team.getId()).collect(Collectors.toList());
    return teamIds;
  }
}
