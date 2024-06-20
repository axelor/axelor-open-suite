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

import com.axelor.utils.api.RequestPostStructure;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;

public class MobileMenuPostRequest extends RequestPostStructure {

  public static final String MOBILE_MENU_TYPE_MENU = "menu";
  public static final String MOBILE_MENU_TYPE_SEPARATOR = "separator";
  public static final String MOBILE_MENU_TYPE_SUBMENU = "submenu";

  private String menuKey;
  private String menuTitle;
  private Long menuOrder;
  private String menuParentApplication;
  private String parentMenuName;

  @NotNull
  @Pattern(
      regexp =
          MOBILE_MENU_TYPE_MENU + "|" + MOBILE_MENU_TYPE_SEPARATOR + "|" + MOBILE_MENU_TYPE_SUBMENU,
      flags = Pattern.Flag.CASE_INSENSITIVE)
  private String menuType;

  public String getMenuKey() {
    return menuKey;
  }

  public void setMenuKey(String menuKey) {
    this.menuKey = menuKey;
  }

  public String getMenuTitle() {
    return menuTitle;
  }

  public void setMenuTitle(String menuTitle) {
    this.menuTitle = menuTitle;
  }

  public Long getMenuOrder() {
    return menuOrder;
  }

  public void setMenuOrder(Long menuOrder) {
    this.menuOrder = menuOrder;
  }

  public String getMenuParentApplication() {
    return menuParentApplication;
  }

  public void setMenuParentApplication(String menuParentApplication) {
    this.menuParentApplication = menuParentApplication;
  }

  public String getParentMenuName() {
    return parentMenuName;
  }

  public void setParentMenuName(String parentMenuName) {
    this.parentMenuName = parentMenuName;
  }

  public String getMenuType() {
    return menuType;
  }

  public void setMenuType(String menuType) {
    this.menuType = menuType;
  }
}
