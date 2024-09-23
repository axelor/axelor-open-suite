package com.axelor.apps.project.quickmenu;

import com.axelor.apps.project.db.Project;
import com.axelor.apps.project.service.app.AppProjectService;
import com.axelor.apps.project.web.UserController;
import com.axelor.auth.AuthUtils;
import com.axelor.auth.db.User;
import com.axelor.i18n.I18n;
import com.axelor.rpc.Context;
import com.axelor.studio.db.AppProject;
import com.axelor.ui.QuickMenu;
import com.axelor.ui.QuickMenuCreator;
import com.axelor.ui.QuickMenuItem;
import com.google.inject.Inject;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public class ActiveProjectQuickMenuCreator implements QuickMenuCreator {

  protected AppProjectService appProjectService;

  @Inject
  public ActiveProjectQuickMenuCreator(AppProjectService appProjectService) {
    this.appProjectService = appProjectService;
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
    AppProject appProject = appProjectService.getAppProject();
    return !appProject.getIsActivateProjectChangeShortcut();
  }

  protected List<QuickMenuItem> getItems() {
    User user = AuthUtils.getUser();
    Set<Project> projectSet =
        Optional.ofNullable(user).map(User::getProjectSet).orElse(new HashSet<>());
    List<QuickMenuItem> items = new ArrayList<>();

    if (projectSet.size() <= 1) {
      return items;
    }

    Project activeProject = user.getActiveProject();
    String action = UserController.class.getName() + ":" + "setActiveProject";

    for (Project project : projectSet) {
      QuickMenuItem item =
          new QuickMenuItem(
              project.getName(),
              action,
              new Context(project.getId(), Project.class),
              project.equals(activeProject));
      items.add(item);
    }
    return items;
  }
}
