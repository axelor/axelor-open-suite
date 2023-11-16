package com.axelor.apps.mobilesettings.service;

import com.axelor.apps.mobilesettings.db.MobileChart;
import com.axelor.apps.mobilesettings.rest.dto.MobileChartValueResponse;
import com.axelor.db.JPA;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.persistence.Query;
import net.minidev.json.JSONArray;
import net.minidev.json.JSONObject;
import org.apache.commons.collections.CollectionUtils;

public class MobileChartServiceImpl implements MobileChartService {

  @Override
  public List<MobileChartValueResponse> getValueList(MobileChart mobileChart) {

    List<Object> resultList = runQuery(mobileChart);
    if (CollectionUtils.isEmpty(resultList)) {
      return Collections.emptyList();
    }

    List<MobileChartValueResponse> mobileChartValueResponseList = new ArrayList<>();
    for (Object objectValues : resultList) {
      mobileChartValueResponseList.add(
          new MobileChartValueResponse(
              ((Object[]) objectValues)[0].toString(), (Long) ((Object[]) objectValues)[1]));
    }
    return mobileChartValueResponseList;
  }

  @Override
  public JSONArray getJsonResponse(MobileChart mobileChart) {
    List<Object> resultList = runQuery(mobileChart);
    if (CollectionUtils.isEmpty(resultList)) {
      return null;
    }

    JSONArray jsonArrayResponse = new JSONArray();
    for (Object objectList : resultList) {
      JSONObject jsonObject = new JSONObject();
      jsonObject.put("label", ((Object[]) objectList)[0]);
      jsonObject.put("value", ((Object[]) objectList)[1]);
      jsonArrayResponse.add(jsonObject);
    }
    return jsonArrayResponse;
  }

  protected List<Object> runQuery(MobileChart mobileChart) {
    if (mobileChart == null) {
      return Collections.emptyList();
    }

    Query query = JPA.em().createQuery(mobileChart.getQuery());
    return query.getResultList();
  }
}
