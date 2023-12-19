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
