package com.axelor.apps.mobilesettings.service;

import com.axelor.apps.mobilesettings.db.MobileDashboard;
import com.axelor.apps.mobilesettings.rest.dto.MobileDashboardResponse;

public interface MobileDashboardResponseComputeService {
  MobileDashboardResponse computeMobileDashboardResponse(MobileDashboard mobileDashboard);
}
