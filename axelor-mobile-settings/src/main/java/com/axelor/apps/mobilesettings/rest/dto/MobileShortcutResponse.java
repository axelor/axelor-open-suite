package com.axelor.apps.mobilesettings.rest.dto;

import com.axelor.apps.mobilesettings.db.MobileShortcut;

public class MobileShortcutResponse {
  protected Long shortcutId;
  protected String iconName;
  protected String name;
  protected String mobileScreenName;

  public MobileShortcutResponse(MobileShortcut mobileShortcut) {
    shortcutId = mobileShortcut.getId();
    iconName = mobileShortcut.getIconName();
    name = mobileShortcut.getName();
    mobileScreenName = mobileShortcut.getMobileScreen().getTechnicalName();
  }

  public Long getShortcutId() {
    return shortcutId;
  }

  public String getIconName() {
    return iconName;
  }

  public String getName() {
    return name;
  }

  public String getMobileScreenName() {
    return mobileScreenName;
  }
}
