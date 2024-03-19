package com.axelor.apps.mobilesettings.service;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.mobilesettings.db.MobileMenu;
import com.axelor.apps.mobilesettings.rest.dto.MobileMenuPostRequest;
import java.util.List;

public interface MobileMenuCreateService {

  List<MobileMenu> createMobileMenus(List<MobileMenuPostRequest> mobileMenuPostRequestList)
      throws AxelorException;

  MobileMenu createMobileMenu(
      String key,
      String title,
      int order,
      String parentApplication,
      String menuType,
      String parentMenuName);
}
