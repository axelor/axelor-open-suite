package com.axelor.apps.mobilesettings.rest.dto;

import com.axelor.apps.mobilesettings.db.MobileDashboard;
import com.axelor.auth.db.Role;
import com.axelor.utils.api.ResponseStructure;
import java.util.List;
import java.util.Set;

public class MobileDashboardResponse extends ResponseStructure {

  protected String name;
  protected String appName;
  protected Boolean isCustom;
  protected String menuTitle;
  protected String iconName;
  protected String parentModuleName;
  protected Integer menuOrder;
  protected Set<Role> authorizedRoleSet;
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
    this.authorizedRoleSet = mobileDashboard.getAuthorizedRoleSet();
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

  public Set<Role> getAuthorizedRoleSet() {
    return authorizedRoleSet;
  }

  public List<MobileDashboardLineResponse> getDashboardLineList() {
    return dashboardLineList;
  }
}
