package com.axelor.apps.mobilesettings.rest.dto;

import java.util.List;

public class MobileConfigResponse {

  protected String sequence;
  protected Boolean isAppEnabled;
  protected List<String> restrictedMenuList;

  public MobileConfigResponse(
      String sequence, Boolean isAppEnabled, List<String> restrictedMenuList) {
    this.sequence = sequence;
    this.isAppEnabled = isAppEnabled;
    this.restrictedMenuList = restrictedMenuList;
  }

  public String getSequence() {
    return sequence;
  }

  public Boolean getIsAppEnabled() {
    return isAppEnabled;
  }

  public List<String> getRestrictedMenuList() {
    return restrictedMenuList;
  }
}
