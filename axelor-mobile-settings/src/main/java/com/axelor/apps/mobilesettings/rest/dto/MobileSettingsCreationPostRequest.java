package com.axelor.apps.mobilesettings.rest.dto;

import com.axelor.utils.api.RequestPostStructure;
import java.util.List;

public class MobileSettingsCreationPostRequest extends RequestPostStructure {

  private List<MobileMenuPostRequest> mobileMenuList;
  private List<MobileScreenPostRequest> mobileScreenList;

  public List<MobileMenuPostRequest> getMobileMenuList() {
    return mobileMenuList;
  }

  public void setMobileMenuList(List<MobileMenuPostRequest> mobileMenuPostRequestList) {
    this.mobileMenuList = mobileMenuPostRequestList;
  }

  public List<MobileScreenPostRequest> getMobileScreenList() {
    return mobileScreenList;
  }

  public void setMobileScreenList(List<MobileScreenPostRequest> mobileScreenPostRequestList) {
    this.mobileScreenList = mobileScreenPostRequestList;
  }
}
