package com.axelor.csv.script;

import com.axelor.apps.base.AxelorException;
import com.axelor.meta.db.MetaModule;
import com.axelor.meta.db.repo.MetaModuleRepository;
import com.axelor.studio.db.AppMobileSettings;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.util.Map;

public class ImportAppMobileSetting {

  @Inject protected MetaModuleRepository metaModuleRepository;

  @Transactional(rollbackOn = {Exception.class})
  public Object getMinimalVersion(Object bean, Map<String, Object> values) throws AxelorException {
    assert bean instanceof AppMobileSettings;
    AppMobileSettings appMobileSettings = (AppMobileSettings) bean;

    String mobileSettingModule = "axelor-mobile-settings";
    int beginIndex = 0;
    int endIndex = 4;
    String minimalVersionEnding = "0";

    MetaModule metaModule =
        metaModuleRepository.all().filter("self.name=?", mobileSettingModule).fetchOne();
    String moduleVersion =
        metaModule.getModuleVersion().substring(beginIndex, endIndex) + minimalVersionEnding;
    appMobileSettings.setMinimalRequiredMobileAppVersion(moduleVersion);
    return appMobileSettings;
  }
}
