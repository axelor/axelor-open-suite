package com.axelor.apps.mobilesettings.service;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.apps.mobilesettings.db.MobileChart;
import com.axelor.apps.mobilesettings.rest.dto.MobileChartValueResponse;
import com.axelor.db.JPA;
import com.axelor.i18n.I18n;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.persistence.Query;
import org.apache.commons.collections.CollectionUtils;

public class MobileChartServiceImpl implements MobileChartService {

  @Override
  public List<MobileChartValueResponse> getValueList(MobileChart mobileChart)
      throws AxelorException {

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
  public String getQueryResponse(MobileChart mobileChart) {
    try {
      return formatList(runQuery(mobileChart));
    } catch (Exception e) {
      return e.getMessage();
    }
  }

  protected List<Object> runQuery(MobileChart mobileChart) throws AxelorException {
    if (mobileChart == null) {
      return Collections.emptyList();
    }

    List<Object> result;

    try {
      Query query = JPA.em().createQuery(mobileChart.getQuery());
      result = query.getResultList();
    } catch (Exception e) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
          I18n.get("There is an error with the query : \n") + e.getMessage());
    }
    return result;
  }

  protected String formatList(List<Object> objectList) {
    StringBuilder result = new StringBuilder("[");
    int count = 0;
    for (Object listObject : objectList) {
      result.append("[");
      result.append(((Object[]) listObject)[0]);
      result.append(",");
      result.append(((Object[]) listObject)[1]);
      result.append("]");
      count++;
      if (count != objectList.size()) {
        result.append(",");
      }
    }
    result.append("]");
    return result.toString();
  }
}
