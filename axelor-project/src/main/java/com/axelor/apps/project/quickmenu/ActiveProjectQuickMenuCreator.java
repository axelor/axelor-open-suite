/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2005-2025 Axelor (<http://axelor.com>).
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package com.axelor.apps.project.quickmenu;

import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.apps.project.db.Project;
import com.axelor.apps.project.web.UserController;
import com.axelor.auth.AuthUtils;
import com.axelor.auth.db.User;
import com.axelor.common.ObjectUtils;
import com.axelor.common.StringUtils;
import com.axelor.i18n.I18n;
import com.axelor.rpc.Context;
import com.axelor.studio.db.AppBase;
import com.axelor.studio.db.repo.AppBaseRepository;
import com.axelor.ui.QuickMenu;
import com.axelor.ui.QuickMenuCreator;
import com.axelor.ui.QuickMenuItem;
import com.google.inject.Inject;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

public class ActiveProjectQuickMenuCreator implements QuickMenuCreator {

  protected AppBaseService appBaseService;

  @Inject
  public ActiveProjectQuickMenuCreator(AppBaseService appBaseService) {
    this.appBaseService = appBaseService;
  }

  @Override
  public QuickMenu create() {

    if (hasConfigEnabled()) {
      return null;
    }

    return new QuickMenu(
        Optional.ofNullable(AuthUtils.getUser())
            .map(User::getActiveProject)
            .map(Project::getName)
            .orElse(I18n.get("Active project")),
        5,
        true,
        getItems());
  }

  protected boolean hasConfigEnabled() {
    AppBase appBase = appBaseService.getAppBase();
    return StringUtils.isBlank(appBase.getShortcutMultiSelect())
        || !appBase.getShortcutMultiSelect().contains(AppBaseRepository.SHORTCUT_ACTIVE_PROJECT);
  }

  protected List<QuickMenuItem> getItems() {
    User user = AuthUtils.getUser();
    Set<Project> projectSet =
        Optional.ofNullable(user).map(User::getProjectSet).orElse(new HashSet<>());
    List<QuickMenuItem> items = new ArrayList<>();

    if (ObjectUtils.isEmpty(projectSet)) {
      return items;
    }

    Project activeProject = user.getActiveProject();
    String action = UserController.class.getName() + ":" + "setActiveProject";

    for (Project project : projectSet) {
      if (!Boolean.TRUE.equals(project.getArchived())
          && (project.getCompany() == null
              || user.getActiveCompany() == null
              || (Objects.equals(project.getCompany(), user.getActiveCompany())))) {
        QuickMenuItem item =
            new QuickMenuItem(
                project.getName(),
                action,
                new Context(project.getId(), Project.class),
                project.equals(activeProject));
        items.add(item);
      }
    }
    return items;
  }
}
