/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2005-2024 Axelor (<http://axelor.com>).
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package com.axelor.apps.mobilesettings.service;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.apps.mobilesettings.db.MobileMenu;
import com.axelor.apps.mobilesettings.db.repo.MobileMenuRepository;
import com.axelor.apps.mobilesettings.exception.MobileSettingsExceptionMessage;
import com.axelor.apps.mobilesettings.rest.dto.MobileMenuPostRequest;
import com.axelor.common.StringUtils;
import com.axelor.i18n.I18n;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.apache.commons.collections.CollectionUtils;

public class MobileMenuCreateServiceImpl implements MobileMenuCreateService {

  protected MobileMenuRepository mobileMenuRepository;

  @Inject
  public MobileMenuCreateServiceImpl(MobileMenuRepository mobileMenuRepository) {
    this.mobileMenuRepository = mobileMenuRepository;
  }

  @Transactional(rollbackOn = {Exception.class})
  @Override
  public List<MobileMenu> createMobileMenus(List<MobileMenuPostRequest> mobileMenuPostRequestList)
      throws AxelorException {
    if (CollectionUtils.isEmpty(mobileMenuPostRequestList)) {
      return Collections.emptyList();
    }
    List<MobileMenu> mobileMenuList = new ArrayList<>();
    for (MobileMenuPostRequest mobileMenuPostRequest : mobileMenuPostRequestList) {
      checkMenuType(mobileMenuPostRequest);
      createOrUpdateMenu(mobileMenuPostRequest, mobileMenuList);
    }
    return mobileMenuList;
  }

  @Transactional(rollbackOn = {Exception.class})
  protected void createOrUpdateMenu(
      MobileMenuPostRequest mobileMenuPostRequest, List<MobileMenu> mobileMenuList)
      throws AxelorException {
    String menuKey = mobileMenuPostRequest.getMenuKey();
    String menuTitle = mobileMenuPostRequest.getMenuTitle();
    Long menuOrder = mobileMenuPostRequest.getMenuOrder();
    String menuParent = mobileMenuPostRequest.getMenuParentApplication();
    String menuType = mobileMenuPostRequest.getMenuType();
    String parentMenuName = mobileMenuPostRequest.getParentMenuName();
    MobileMenu mobileMenu = mobileMenuRepository.findByTechnicalName(menuKey);

    if (mobileMenu == null) {
      mobileMenu =
          createMobileMenu(
              menuKey, menuTitle, menuOrder.intValue(), menuParent, menuType, parentMenuName);
      mobileMenuList.add(mobileMenu);
    } else {
      updateMobileMenu(menuTitle, mobileMenu, menuOrder, menuParent, menuType, parentMenuName);
    }
    checkSubMenu(mobileMenu);
  }

  @Transactional
  protected void updateMobileMenu(
      String menuTitle,
      MobileMenu mobileMenu,
      Long menuOrder,
      String menuParent,
      String menuType,
      String parentMenuName) {
    if (StringUtils.notEmpty(menuTitle)) {
      mobileMenu.setName(menuTitle);
    }
    if (menuOrder != null) {
      mobileMenu.setMenuOrder(menuOrder.intValue());
    }
    if (StringUtils.notEmpty(menuParent)) {
      mobileMenu.setParentApplication(menuParent);
    }
    if (StringUtils.notEmpty(menuType)) {
      mobileMenu.setMenuType(menuType);
    }
    if (StringUtils.notEmpty(parentMenuName)) {
      mobileMenu.setParentMenuName(parentMenuName);
    }
  }

  @Transactional
  @Override
  public MobileMenu createMobileMenu(
      String key,
      String title,
      int order,
      String parentApplication,
      String menuType,
      String parentMenuName) {
    MobileMenu mobileMenu = new MobileMenu();
    mobileMenu.setTechnicalName(key);
    mobileMenu.setName(title);
    mobileMenu.setMenuOrder(order);
    mobileMenu.setParentApplication(parentApplication);
    mobileMenu.setMenuType(menuType);
    mobileMenu.setParentMenuName(parentMenuName);
    return mobileMenuRepository.save(mobileMenu);
  }

  protected void checkMenuType(MobileMenuPostRequest mobileMenuPostRequest) throws AxelorException {
    List<String> authorizedMenuType = new ArrayList<>();
    authorizedMenuType.add(MobileMenuRepository.MOBILE_MENU_TYPE_MENU);
    authorizedMenuType.add(MobileMenuRepository.MOBILE_MENU_TYPE_SEPARATOR);
    authorizedMenuType.add(MobileMenuRepository.MOBILE_MENU_TYPE_SUBMENU);
    if (!authorizedMenuType.contains(mobileMenuPostRequest.getMenuType())) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
          I18n.get(MobileSettingsExceptionMessage.MOBILE_MENU_WRONG_MENU_TYPE));
    }
  }

  protected void checkSubMenu(MobileMenu mobileMenu) throws AxelorException {
    if (MobileMenuRepository.MOBILE_MENU_TYPE_SUBMENU.equals(mobileMenu.getMenuType())
        && StringUtils.isEmpty(mobileMenu.getParentMenuName())) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_MISSING_FIELD,
          I18n.get(MobileSettingsExceptionMessage.MOBILE_MENU_SUBMENU_NO_PARENT));
    }
  }
}
