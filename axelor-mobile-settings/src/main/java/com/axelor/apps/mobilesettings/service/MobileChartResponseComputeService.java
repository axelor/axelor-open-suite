package com.axelor.apps.mobilesettings.service;

import com.axelor.apps.mobilesettings.db.MobileChart;
import com.axelor.apps.mobilesettings.rest.dto.MobileChartNoVersionResponse;
import com.axelor.apps.mobilesettings.rest.dto.MobileChartResponse;

public interface MobileChartResponseComputeService {
  MobileChartResponse computeMobileChartResponse(MobileChart mobileChart);

  MobileChartNoVersionResponse computeMobileChartNoVersionResponse(MobileChart mobileChart);
}
