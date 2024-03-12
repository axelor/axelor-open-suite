package com.axelor.apps.mobilesettings.service;

import com.axelor.apps.mobilesettings.db.MobileMenu;
import com.axelor.apps.mobilesettings.db.repo.MobileMenuRepository;
import com.axelor.apps.mobilesettings.rest.dto.MobileMenuPostRequest;
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

  @Override
  public List<MobileMenu> createMobileMenus(List<MobileMenuPostRequest> mobileMenuPostRequestList) {
    if (CollectionUtils.isEmpty(mobileMenuPostRequestList)) {
      return Collections.emptyList();
    }
    List<MobileMenu> mobileMenuList = new ArrayList<>();
    for (MobileMenuPostRequest mobileMenuPostRequest : mobileMenuPostRequestList) {
      mobileMenuList.add(
          createMobileMenu(
              mobileMenuPostRequest.getMenuKey(),
              mobileMenuPostRequest.getMenuTitle(),
              mobileMenuPostRequest.getMenuOrder().intValue(),
              mobileMenuPostRequest.getMenuParentApplication()));
    }
    return mobileMenuList;
  }

  @Transactional
  @Override
  public MobileMenu createMobileMenu(
      String key, String title, int order, String parentApplication) {
    MobileMenu mobileMenu = new MobileMenu();
    mobileMenu.setTechnicalName(key);
    mobileMenu.setName(title);
    mobileMenu.setMenuOrder(order);
    mobileMenu.setParentApplication(parentApplication);
    return mobileMenuRepository.save(mobileMenu);
  }
}
