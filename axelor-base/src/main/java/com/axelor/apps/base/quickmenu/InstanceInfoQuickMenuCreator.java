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
package com.axelor.apps.base.quickmenu;

import com.axelor.app.AppSettings;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.service.DateService;
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.apps.base.service.exception.TraceBackService;
import com.axelor.auth.AuthUtils;
import com.axelor.auth.db.Group;
import com.axelor.auth.db.User;
import com.axelor.i18n.I18n;
import com.axelor.ui.QuickMenu;
import com.axelor.ui.QuickMenuCreator;
import com.axelor.ui.QuickMenuItem;
import com.google.inject.Inject;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class InstanceInfoQuickMenuCreator implements QuickMenuCreator {

  protected DateService dateService;
  protected AppBaseService appBaseService;

  @Inject
  public InstanceInfoQuickMenuCreator(DateService dateService, AppBaseService appBaseService) {
    this.dateService = dateService;
    this.appBaseService = appBaseService;
  }

  @Override
  public QuickMenu create() {
    if (hasConfigEnabled()) {
      return null;
    }

    return new QuickMenu(I18n.get("Instance info"), 0, false, getItems());
  }

  protected boolean hasConfigEnabled() {
    boolean isTechnicalStaff =
        Optional.ofNullable(AuthUtils.getUser())
            .map(User::getGroup)
            .map(Group::getTechnicalStaff)
            .orElse(false);
    boolean isActivateInstanceInfoShortcut =
        appBaseService.getAppBase().getIsActivateInstanceInfoShortcut();
    return !isActivateInstanceInfoShortcut || !isTechnicalStaff;
  }

  protected List<QuickMenuItem> getItems() {
    List<QuickMenuItem> items = new ArrayList<>();
    QuickMenuItem todayDateMenuItem = getTodayDateMenuItem();
    if (todayDateMenuItem != null) {
      items.add(todayDateMenuItem);
    }
    items.add(getInstanceInfoMenuItem());

    return items;
  }

  protected QuickMenuItem getInstanceInfoMenuItem() {
    final boolean isDevMode = "dev".equals(AppSettings.get().get("application.mode", "prod"));
    return new QuickMenuItem(
        isDevMode ? I18n.get("Prod instance") : I18n.get("Test instance"), null);
  }

  protected QuickMenuItem getTodayDateMenuItem() {
    try {
      final ZonedDateTime dateT = appBaseService.getAppBase().getTodayDateT();
      if (dateT == null) {
        return null;
      }
      DateTimeFormatter dateTFormat = dateService.getDateTimeFormat();
      String dateStr = String.format("%s: %s", I18n.get("Today date"), dateT.format(dateTFormat));
      return new QuickMenuItem(dateStr, null);
    } catch (AxelorException e) {
      TraceBackService.trace(e);
      return null;
    }
  }
}
