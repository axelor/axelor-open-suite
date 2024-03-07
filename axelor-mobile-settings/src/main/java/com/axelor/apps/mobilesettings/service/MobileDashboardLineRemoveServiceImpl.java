package com.axelor.apps.mobilesettings.service;

import com.axelor.apps.mobilesettings.db.MobileDashboard;
import com.axelor.apps.mobilesettings.db.MobileDashboardLine;
import com.axelor.apps.mobilesettings.db.repo.MobileDashboardLineRepository;
import com.google.inject.Inject;
import java.util.List;
import org.apache.commons.collections.CollectionUtils;

public class MobileDashboardLineRemoveServiceImpl implements MobileDashboardLineRemoveService {

  protected MobileDashboardLineRepository mobileDashboardLineRepository;

  @Inject
  public MobileDashboardLineRemoveServiceImpl(
      MobileDashboardLineRepository mobileDashboardLineRepository) {
    this.mobileDashboardLineRepository = mobileDashboardLineRepository;
  }

  @Override
  public void deletesLines(MobileDashboard mobileDashboard) {
    List<MobileDashboardLine> mobileDashboardLineList =
        mobileDashboard.getMobileDashboardLineList();
    if (CollectionUtils.isEmpty(mobileDashboardLineList)) {
      return;
    }
    mobileDashboard
        .getMobileDashboardLineList()
        .forEach(line -> mobileDashboardLineRepository.remove(line));
  }
}
