package com.axelor.apps.mobilesettings.rest.dto;

import com.axelor.utils.api.RequestPostStructure;

public class MobileMenuPostRequest extends RequestPostStructure {
  private String menuKey;
  private String menuTitle;
  private Long menuOrder;
  private String menuParentApplication;

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
}
