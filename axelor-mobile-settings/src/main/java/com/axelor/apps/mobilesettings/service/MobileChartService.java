package com.axelor.apps.mobilesettings.service;

import com.axelor.apps.mobilesettings.db.MobileChart;
import com.axelor.apps.mobilesettings.rest.dto.MobileChartValueResponse;
import java.util.List;
import net.minidev.json.JSONArray;

public interface MobileChartService {
  List<MobileChartValueResponse> getValueList(MobileChart mobileChart);

  JSONArray getJsonResponse(MobileChart mobileChart);
}
