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
package com.axelor.apps.mobilesettings.rest.dto;

import com.axelor.utils.api.RequestPostStructure;

public class MobileScreenPostRequest extends RequestPostStructure {
  private String screenKey;
  private String screenTitle;
  private boolean isUsableOnShortcut;

  public String getScreenKey() {
    return screenKey;
  }

  public void setScreenKey(String screenKey) {
    this.screenKey = screenKey;
  }

  public String getScreenTitle() {
    return screenTitle;
  }

  public void setScreenTitle(String screenTitle) {
    this.screenTitle = screenTitle;
  }

  public boolean getIsUsableOnShortcut() {
    return isUsableOnShortcut;
  }

  public void setIsUsableOnShortcut(boolean usableOnShortcut) {
    isUsableOnShortcut = usableOnShortcut;
  }
}
