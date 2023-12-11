package com.axelor.apps.mobilesettings.rest.dto;

import com.axelor.apps.mobilesettings.db.MobileDashboard;
import com.axelor.utils.api.ResponseStructure;
import java.util.List;

public class MobileDashboardResponse extends ResponseStructure {

  protected String name;
  protected List<MobileDashboardLineResponse> dashboardLineList;

  public MobileDashboardResponse(
      MobileDashboard mobileDashboard,
      String name,
      List<MobileDashboardLineResponse> dashboardLineList) {
    super(mobileDashboard.getVersion());
    this.name = name;
    this.dashboardLineList = dashboardLineList;
  }

  public String getName() {
    return name;
  }

  public List<MobileDashboardLineResponse> getDashboardLineList() {
    return dashboardLineList;
  }
}
