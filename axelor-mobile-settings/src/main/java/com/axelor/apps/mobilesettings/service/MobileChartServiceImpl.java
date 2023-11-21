package com.axelor.apps.mobilesettings.service;

import com.axelor.apps.mobilesettings.db.MobileChart;
import com.axelor.apps.mobilesettings.rest.dto.MobileChartValueResponse;
import com.axelor.db.JPA;
import java.math.BigDecimal;
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

      String label = getLabel(((Object[]) objectValues)[0]);
      double value = getDoubleValue(((Object[]) objectValues)[1]);
      mobileChartValueResponseList.add(new MobileChartValueResponse(label, value));
    }
    return mobileChartValueResponseList;
  }

  protected String getLabel(Object label) {
    if (label == null) {
      return "null";
    }

    return label.toString();
  }

  protected double getDoubleValue(Object value) {
    if (value == null) {
      return 0;
    }

    if (value instanceof Integer) {
      return Double.valueOf((Integer) value);
    }

    if (value instanceof BigDecimal) {
      return ((BigDecimal) value).doubleValue();
    }

    if (value instanceof Long) {
      return ((Long) value).doubleValue();
    }

    return (double) value;
  }

  @Override
  public JSONArray getJsonResponse(MobileChart mobileChart) {
    List<Object> resultList = runQuery(mobileChart);
    if (CollectionUtils.isEmpty(resultList)) {
      return getEmptyJsonArray();
    }

    return getJsonArray(resultList);
  }

  protected JSONArray getJsonArray(List<Object> resultList) {
    JSONArray jsonArrayResponse = new JSONArray();
    for (Object objectList : resultList) {
      JSONObject jsonObject = new JSONObject();
      jsonObject.put("label", ((Object[]) objectList)[0]);
      jsonObject.put("value", ((Object[]) objectList)[1]);
      jsonArrayResponse.add(jsonObject);
    }
    return jsonArrayResponse;
  }

  protected JSONArray getEmptyJsonArray() {
    JSONArray jsonArrayResponse = new JSONArray();
    JSONObject jsonObject = new JSONObject();
    jsonObject.put("label", "Value not found");
    jsonArrayResponse.add(jsonObject);
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
