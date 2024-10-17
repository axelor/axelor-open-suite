package com.axelor.apps.mobilesettings.rest.dto;

public class MobileMenuResponse {

  protected String name;
  protected String technicalName;
  protected Integer menuOrder;

  public MobileMenuResponse(String name, String technicalName, Integer menuOrder) {
    this.name = name;
    this.technicalName = technicalName;
    this.menuOrder = menuOrder;
  }

  public String getName() {
    return name;
  }

  public String getTechnicalName() {
    return technicalName;
  }

  public Integer getMenuOrder() {
    return menuOrder;
  }
}
