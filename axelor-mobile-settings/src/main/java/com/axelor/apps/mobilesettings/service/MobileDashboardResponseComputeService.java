package com.axelor.apps.mobilesettings.service;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.mobilesettings.db.MobileDashboard;
import com.axelor.apps.mobilesettings.rest.dto.MobileDashboardResponse;
import java.util.Optional;

public interface MobileDashboardResponseComputeService {
  Optional<MobileDashboardResponse> computeMobileDashboardResponse(MobileDashboard mobileDashboard)
      throws AxelorException;
}
