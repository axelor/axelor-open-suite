/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2005-2025 Axelor (<http://axelor.com>).
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

import com.axelor.apps.mobilesettings.db.MobileScreen;
import com.axelor.apps.mobilesettings.db.repo.MobileScreenRepository;
import com.axelor.apps.mobilesettings.rest.dto.MobileScreenPostRequest;
import com.axelor.common.StringUtils;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.apache.commons.collections.CollectionUtils;

public class MobileScreenCreateServiceImpl implements MobileScreenCreateService {

  protected MobileScreenRepository mobileScreenRepository;

  @Inject
  public MobileScreenCreateServiceImpl(MobileScreenRepository mobileScreenRepository) {
    this.mobileScreenRepository = mobileScreenRepository;
  }

  @Override
  public List<MobileScreen> createMobileScreens(
      List<MobileScreenPostRequest> mobileScreenPostRequestList) {
    if (CollectionUtils.isEmpty(mobileScreenPostRequestList)) {
      return Collections.emptyList();
    }
    List<MobileScreen> mobileScreenList = new ArrayList<>();
    for (MobileScreenPostRequest mobileScreenPostRequest : mobileScreenPostRequestList) {
      createOrUpdateScreen(mobileScreenPostRequest, mobileScreenList);
    }
    return mobileScreenList;
  }

  protected void createOrUpdateScreen(
      MobileScreenPostRequest mobileScreenPostRequest, List<MobileScreen> mobileScreenList) {
    String screenKey = mobileScreenPostRequest.getScreenKey();
    String screenTitle = mobileScreenPostRequest.getScreenTitle();
    boolean isUsableOnShortcut = mobileScreenPostRequest.getIsUsableOnShortcut();
    MobileScreen mobileScreen = mobileScreenRepository.findByTechnicalName(screenKey);
    if (mobileScreen == null) {
      mobileScreenList.add(createMobileScreen(screenKey, screenTitle, isUsableOnShortcut));
    } else {
      updateMobileScreen(screenTitle, mobileScreen, isUsableOnShortcut);
    }
  }

  @Transactional
  protected void updateMobileScreen(
      String screenTitle, MobileScreen mobileScreen, boolean isUsableOnShortcut) {
    if (StringUtils.notEmpty(screenTitle)) {
      mobileScreen.setName(screenTitle);
    }
    mobileScreen.setIsUsableOnShortcut(isUsableOnShortcut);
  }

  @Transactional
  @Override
  public MobileScreen createMobileScreen(String key, String title, boolean isUsableOnShortcut) {
    MobileScreen mobileScreen = new MobileScreen();
    mobileScreen.setTechnicalName(key);
    mobileScreen.setName(title);
    mobileScreen.setIsUsableOnShortcut(isUsableOnShortcut);
    return mobileScreenRepository.save(mobileScreen);
  }
}
