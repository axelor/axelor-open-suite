package com.axelor.apps.base.service;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.MapGroup;
import java.util.List;
import java.util.Map;
import wslite.json.JSONException;

public interface MapGroupService {

  List<Map<String, Object>> computeData(MapGroup mapGroup) throws AxelorException, JSONException;
}
