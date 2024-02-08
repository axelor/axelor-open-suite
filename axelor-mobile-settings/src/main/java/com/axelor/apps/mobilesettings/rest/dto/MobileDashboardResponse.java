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

import com.axelor.apps.mobilesettings.db.MobileDashboard;
import com.axelor.utils.api.ResponseStructure;
import java.util.List;

public class MobileDashboardResponse extends ResponseStructure {

  protected String name;
  protected String appName;
  protected Boolean isCustom;
  protected String menuTitle;
  protected String iconName;
  protected Integer menuOrder;
  protected List<MobileDashboardLineResponse> dashboardLineList;

  public MobileDashboardResponse(
      MobileDashboard mobileDashboard, List<MobileDashboardLineResponse> dashboardLineList) {
    super(mobileDashboard.getVersion());
    this.name = mobileDashboard.getName();
    this.appName = mobileDashboard.getAppName();
    this.isCustom = mobileDashboard.getIsCustom();
    this.menuTitle = mobileDashboard.getMenuTitle();
    this.iconName = mobileDashboard.getIconName();
    this.menuOrder = mobileDashboard.getMenuOrder();
    this.dashboardLineList = dashboardLineList;
  }

  public String getName() {
    return name;
  }

  public String getAppName() {
    return appName;
  }

  public Boolean getIsCustom() {
    return isCustom;
  }

  public String getMenuTitle() {
    return menuTitle;
  }

  public String getIconName() {
    return iconName;
  }

  public Integer getMenuOrder() {
    return menuOrder;
  }

  public List<MobileDashboardLineResponse> getDashboardLineList() {
    return dashboardLineList;
  }
}
