package com.axelor.apps.mobilesettings.rest.dto;

import com.axelor.utils.api.ResponseStructure;

public class MobileSettingsResponse extends ResponseStructure {
  protected final Boolean isStockAppEnabled;
  protected final Boolean isProductionAppEnabled;
  protected final Boolean isCrmAppEnabled;
  protected final Boolean isHelpdeskAppEnabled;
  protected final Boolean isHRAppEnabled;
  protected final Boolean isLoginUserQrcodeEnabled;
  protected final Boolean isTrackerMessageEnabled;
  protected final MobileStockSettingsResponse stockSettings;
  protected final MobileHRSettingsResponse hrSettings;

  public MobileSettingsResponse(
      Integer version,
      Boolean isStockAppEnabled,
      Boolean isProductionAppEnabled,
      Boolean isCrmAppEnabled,
      Boolean isHelpdeskAppEnabled,
      Boolean isHRAppEnabled,
      Boolean isLoginUserQrcodeEnabled,
      Boolean isTrackerMessageEnabled,
      MobileStockSettingsResponse stockSettings,
      MobileHRSettingsResponse hrSettings) {
    super(version);
    this.isStockAppEnabled = isStockAppEnabled;
    this.isProductionAppEnabled = isProductionAppEnabled;
    this.isCrmAppEnabled = isCrmAppEnabled;
    this.isHelpdeskAppEnabled = isHelpdeskAppEnabled;
    this.isHRAppEnabled = isHRAppEnabled;
    this.isLoginUserQrcodeEnabled = isLoginUserQrcodeEnabled;
    this.isTrackerMessageEnabled = isTrackerMessageEnabled;
    this.stockSettings = stockSettings;
    this.hrSettings = hrSettings;
  }

  public Boolean getStockAppEnabled() {
    return isStockAppEnabled;
  }

  public Boolean getProductionAppEnabled() {
    return isProductionAppEnabled;
  }

  public Boolean getCrmAppEnabled() {
    return isCrmAppEnabled;
  }

  public Boolean getHelpdeskAppEnabled() {
    return isHelpdeskAppEnabled;
  }

  public Boolean getHRAppEnabled() {
    return isHRAppEnabled;
  }

  public Boolean getLoginUserQrcodeEnabled() {
    return isLoginUserQrcodeEnabled;
  }

  public Boolean getTrackerMessageEnabled() {
    return isTrackerMessageEnabled;
  }

  public MobileStockSettingsResponse getStockSettings() {
    return stockSettings;
  }

  public MobileHRSettingsResponse getHrSettings() {
    return hrSettings;
  }
}
