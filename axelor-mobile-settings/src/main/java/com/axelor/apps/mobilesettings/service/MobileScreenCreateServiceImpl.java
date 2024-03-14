package com.axelor.apps.mobilesettings.service;

import com.axelor.apps.mobilesettings.db.MobileScreen;
import com.axelor.apps.mobilesettings.db.repo.MobileScreenRepository;
import com.axelor.apps.mobilesettings.rest.dto.MobileScreenPostRequest;
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
      mobileScreenList.add(
          createMobileScreen(
              mobileScreenPostRequest.getScreenKey(),
              mobileScreenPostRequest.getScreenTitle(),
              mobileScreenPostRequest.getIsUsableOnShortcut()));
    }
    return mobileScreenList;
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
