package com.axelor.apps.mobilesettings.web;

import com.axelor.apps.mobilesettings.db.MobileConfig;
import com.axelor.apps.mobilesettings.db.MobileMenu;
import com.axelor.apps.mobilesettings.db.repo.MobileMenuRepository;
import com.axelor.inject.Beans;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import java.util.List;

public class MobileConfigController {

  public void selectAllMenus(ActionRequest request, ActionResponse response) {
    MobileConfig mobileConfig = request.getContext().asType(MobileConfig.class);
    List<MobileMenu> menus =
        Beans.get(MobileMenuRepository.class)
            .findByParentApplication(mobileConfig.getSequence())
            .fetch();
    response.setValue("menus", menus);
  }
}
