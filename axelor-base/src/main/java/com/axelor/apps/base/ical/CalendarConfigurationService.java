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
package com.axelor.apps.base.ical;

import com.axelor.apps.base.db.CalendarConfiguration;
import com.axelor.apps.base.db.repo.CalendarConfigurationRepository;
import com.axelor.apps.tool.MetaActionTool;
import com.axelor.auth.db.Group;
import com.axelor.auth.db.Role;
import com.axelor.auth.db.User;
import com.axelor.auth.db.repo.GroupRepository;
import com.axelor.auth.db.repo.RoleRepository;
import com.axelor.auth.db.repo.UserRepository;
import com.axelor.exception.AxelorException;
import com.axelor.inject.Beans;
import com.axelor.meta.db.MetaAction;
import com.axelor.meta.db.MetaMenu;
import com.axelor.meta.db.repo.MetaActionRepository;
import com.axelor.meta.db.repo.MetaMenuRepository;
import com.axelor.meta.schema.actions.ActionView;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;

public class CalendarConfigurationService {

  private static final String NAME = "ical-calendar-";

  @Inject protected CalendarConfigurationRepository calendarConfigurationRepo;

  @Transactional(rollbackOn = {AxelorException.class, Exception.class})
  public void createEntryMenu(CalendarConfiguration calendarConfiguration) {

    String menuName =
        NAME + calendarConfiguration.getName().toLowerCase() + "-" + calendarConfiguration.getId();
    String subName = menuName.replaceAll("[-\\s]", ".");
    String title = calendarConfiguration.getName();
    User user = calendarConfiguration.getCalendarUser();
    Group group = calendarConfiguration.getCalendarGroup();

    MetaAction metaAction = this.createMetaAction("action." + subName, title);
    MetaMenu metaMenu =
        this.createMetaMenu(menuName, title, metaAction, calendarConfiguration.getParentMetaMenu());
    Beans.get(MetaMenuRepository.class).save(metaMenu);

    Role role = new Role();
    role.setName("role." + subName);
    role.addMenu(metaMenu);

    Beans.get(RoleRepository.class).save(role);

    user.addRole(role);
    Beans.get(UserRepository.class).save(user);

    if (group != null) {
      group.addRole(role);
      Beans.get(GroupRepository.class).save(group);
    }
    calendarConfiguration.setRole(role);
    calendarConfiguration.setMetaAction(metaAction);
    calendarConfigurationRepo.save(calendarConfiguration);
  }

  @Transactional(rollbackOn = {AxelorException.class, Exception.class})
  public void deleteEntryMenu(CalendarConfiguration calendarConfiguration) {

    MetaAction metaAction = calendarConfiguration.getMetaAction();
    Role role = calendarConfiguration.getRole();
    Group group = calendarConfiguration.getCalendarGroup();
    calendarConfiguration.setMetaAction(null);
    calendarConfiguration.setRole(null);
    calendarConfiguration.getCalendarUser().removeRole(role);

    if (group != null) {
      group.removeRole(role);
    }

    String menuName =
        NAME + calendarConfiguration.getName().toLowerCase() + "-" + calendarConfiguration.getId();

    MetaMenuRepository metaMenuRepository = Beans.get(MetaMenuRepository.class);
    MetaMenu metaMenu = metaMenuRepository.findByName(menuName);
    metaMenuRepository.remove(metaMenu);

    MetaActionRepository metaActionRepository = Beans.get(MetaActionRepository.class);
    metaActionRepository.remove(metaAction);

    RoleRepository roleRepository = Beans.get(RoleRepository.class);
    roleRepository.remove(role);
  }

  public MetaMenu createMetaMenu(
      String name, String title, MetaAction metaAction, MetaMenu parentMenu) {

    MetaMenu metaMenu = new MetaMenu();
    metaMenu.setName(name);
    metaMenu.setAction(metaAction);
    metaMenu.setModule("axelor-base");
    metaMenu.setTitle(title);
    metaMenu.setParent(parentMenu);

    return metaMenu;
  }

  public MetaAction createMetaAction(String name, String title) {

    String module = "axelor-base";
    String type = "action-view";
    String expr =
        String.format(
            "eval: __repo__(CalendarConfiguration).all().filter('self.metaAction.name = :name').bind('name', '%s').fetchOne().calendarSet.collect{ it.id }",
            name);

    ActionView actionView =
        ActionView.define(title)
            .name(name)
            .model("com.axelor.apps.base.db.ICalendarEvent")
            .add("calendar", "calendar-all")
            .add("grid", "calendar-event-grid")
            .add("form", "calandar-event-form")
            .domain("self.calendar.id in (:_calendarIdList)")
            .context("_calendarIdList", expr)
            .get();

    return MetaActionTool.actionToMetaAction(actionView, name, type, module);
  }
}
