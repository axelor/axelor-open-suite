/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2005-2024 Axelor (<http://axelor.com>).
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
package com.axelor.apps.mobilesettings.rest.dto;

import com.axelor.apps.mobilesettings.db.MobileShortcut;

public class MobileShortcutResponse {
  protected Long shortcutId;
  protected String iconName;
  protected String name;
  protected String mobileScreenName;

  public MobileShortcutResponse(MobileShortcut mobileShortcut) {
    shortcutId = mobileShortcut.getId();
    iconName = mobileShortcut.getIconName();
    name = mobileShortcut.getName();
    mobileScreenName = mobileShortcut.getMobileScreen().getTechnicalName();
  }

  public Long getShortcutId() {
    return shortcutId;
  }

  public String getIconName() {
    return iconName;
  }

  public String getName() {
    return name;
  }

  public String getMobileScreenName() {
    return mobileScreenName;
  }
}
