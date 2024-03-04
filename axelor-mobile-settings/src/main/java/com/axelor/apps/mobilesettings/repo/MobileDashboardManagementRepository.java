package com.axelor.apps.mobilesettings.repo;

import com.axelor.apps.mobilesettings.db.MobileDashboard;
import com.axelor.apps.mobilesettings.db.repo.MobileDashboardRepository;
import com.axelor.apps.mobilesettings.service.MobileDashboardLineRemoveService;
import com.axelor.inject.Beans;

public class MobileDashboardManagementRepository extends MobileDashboardRepository {

  @Override
  public void remove(MobileDashboard entity) {
    Beans.get(MobileDashboardLineRemoveService.class).deletesLines(entity);
    super.remove(entity);
  }
}
