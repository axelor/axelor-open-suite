package com.axelor.apps.mobilesettings.service;

import com.axelor.apps.mobilesettings.db.MobileScreen;
import com.axelor.apps.mobilesettings.rest.dto.MobileScreenPostRequest;
import java.util.List;

public interface MobileScreenCreateService {
  List<MobileScreen> createMobileScreens(List<MobileScreenPostRequest> mobileScreenPostRequestList);

  MobileScreen createMobileScreen(String key, String title, boolean isUsableOnShortcut);
}
