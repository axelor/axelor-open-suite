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

  public boolean isUsableOnShortcut() {
    return isUsableOnShortcut;
  }

  public void setUsableOnShortcut(boolean usableOnShortcut) {
    isUsableOnShortcut = usableOnShortcut;
  }
}
