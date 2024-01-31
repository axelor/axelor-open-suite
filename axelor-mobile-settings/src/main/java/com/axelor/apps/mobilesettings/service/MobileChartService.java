package com.axelor.apps.mobilesettings.service;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.mobilesettings.db.MobileChart;
import com.axelor.apps.mobilesettings.rest.dto.MobileChartValueResponse;
import java.util.List;

public interface MobileChartService {
  List<MobileChartValueResponse> getValueList(MobileChart mobileChart) throws AxelorException;

  String getQueryResponse(MobileChart mobileChart);
}
