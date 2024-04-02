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
