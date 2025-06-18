package com.axelor.apps.account.service.analytic;

import com.axelor.apps.account.db.AnalyticAxis;
import com.axelor.apps.account.db.AnalyticMoveLineQueryParameter;
import com.axelor.apps.base.AxelorException;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public interface AnalyticMoveLineQueryPercentageService {
  BigDecimal getMissingPercentageOnAxis(
      AnalyticMoveLineQueryParameter parameter,
      List<AnalyticMoveLineQueryParameter> analyticMoveLineQueryParameterList);

  void validateReverseParameterAxisPercentage(
      List<AnalyticMoveLineQueryParameter> analyticMoveLineQueryParameterList)
      throws AxelorException;

  Map<AnalyticAxis, BigDecimal> buildPercentageAxisMap(
      List<AnalyticMoveLineQueryParameter> analyticMoveLineQueryParameterList);
}
