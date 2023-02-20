package com.axelor.apps.mobilesettings.service;

import com.axelor.apps.mobilesettings.db.MobileConfig;
import com.axelor.apps.mobilesettings.db.repo.MobileConfigRepository;
import com.axelor.studio.db.AppMobileSettings;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;

public class AppMobileSettingsServiceImpl implements AppMobileSettingsService {

  private final MobileConfigRepository mobileConfigRepository;

  @Inject
  public AppMobileSettingsServiceImpl(MobileConfigRepository mobileConfigRepository) {
    this.mobileConfigRepository = mobileConfigRepository;
  }

  @Transactional(rollbackOn = {Exception.class})
  public void updateMobileConfig(Boolean isAppActivated, String sequence) {
    MobileConfig mobileConfig =
        mobileConfigRepository.all().filter("self.sequence = ?", sequence).fetchOne();
    if (!isAppActivated.equals(mobileConfig.getIsAppEnable())) {
      mobileConfig.setIsAppEnable(isAppActivated);
      mobileConfigRepository.save(mobileConfig);
    }
  }

  @Override
  @Transactional(rollbackOn = {Exception.class})
  public void updateAllMobileConfig(AppMobileSettings appMobileSettings) {
    updateMobileConfig(
        appMobileSettings.getIsStockAppEnable(), MobileConfigRepository.APP_SEQUENCE_STOCK);
    updateMobileConfig(
        appMobileSettings.getIsProductionAppEnable(),
        MobileConfigRepository.APP_SEQUENCE_MANUFACTURING);
  }
}
