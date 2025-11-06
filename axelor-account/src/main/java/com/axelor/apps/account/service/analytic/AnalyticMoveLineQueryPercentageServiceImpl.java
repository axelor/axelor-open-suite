/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2005-2025 Axelor (<http://axelor.com>).
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package com.axelor.apps.account.service.analytic;

import com.axelor.apps.account.db.AnalyticAxis;
import com.axelor.apps.account.db.AnalyticMoveLineQueryParameter;
import com.axelor.apps.account.exception.AccountExceptionMessage;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.common.ObjectUtils;
import com.axelor.i18n.I18n;
import com.google.inject.Inject;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class AnalyticMoveLineQueryPercentageServiceImpl
    implements AnalyticMoveLineQueryPercentageService {

  @Inject
  public AnalyticMoveLineQueryPercentageServiceImpl() {}

  @Override
  public BigDecimal getMissingPercentageOnAxis(
      AnalyticMoveLineQueryParameter parameter,
      List<AnalyticMoveLineQueryParameter> analyticMoveLineQueryParameterList) {
    if (ObjectUtils.isEmpty(analyticMoveLineQueryParameterList)
        || parameter == null
        || parameter.getAnalyticAxis() == null) {
      return new BigDecimal(100);
    }

    BigDecimal alreadyOnAxis =
        analyticMoveLineQueryParameterList.stream()
            .filter(it -> parameter.getAnalyticAxis().equals(it.getAnalyticAxis()))
            .map(AnalyticMoveLineQueryParameter::getPercentage)
            .reduce(BigDecimal::add)
            .orElse(BigDecimal.ZERO);

    return new BigDecimal(100).subtract(alreadyOnAxis).add(parameter.getPercentage());
  }

  @Override
  public void validateReverseParameterAxisPercentage(
      List<AnalyticMoveLineQueryParameter> analyticMoveLineQueryParameterList)
      throws AxelorException {
    if (ObjectUtils.isEmpty(analyticMoveLineQueryParameterList)) {
      return;
    }

    Map<AnalyticAxis, BigDecimal> percentageAxisMap =
        buildPercentageAxisMap(analyticMoveLineQueryParameterList);

    List<AnalyticAxis> errorAxis =
        percentageAxisMap.entrySet().stream()
            .filter(it -> it.getValue().compareTo(new BigDecimal(100)) != 0)
            .map(Map.Entry::getKey)
            .collect(Collectors.toList());

    if (ObjectUtils.isEmpty(errorAxis)) {
      return;
    }

    throw new AxelorException(
        TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
        String.format(
            I18n.get(AccountExceptionMessage.ANALYTIC_MOVE_LINE_QUERY_WRONG_SUM_FOR_AXIS),
            errorAxis.stream().map(AnalyticAxis::getName).collect(Collectors.joining(","))));
  }

  @Override
  public Map<AnalyticAxis, BigDecimal> buildPercentageAxisMap(
      List<AnalyticMoveLineQueryParameter> analyticMoveLineQueryParameterList) {
    Map<AnalyticAxis, BigDecimal> percentageAxisMap = new HashMap<>();
    if (ObjectUtils.isEmpty(analyticMoveLineQueryParameterList)) {
      return percentageAxisMap;
    }

    for (AnalyticMoveLineQueryParameter line : analyticMoveLineQueryParameterList) {
      AnalyticAxis analyticAxis = line.getAnalyticAxis();
      BigDecimal percentage = line.getPercentage();
      if (analyticAxis == null || percentage.signum() == 0) {
        continue;
      }

      if (percentageAxisMap.containsKey(analyticAxis)) {
        BigDecimal sum = percentageAxisMap.get(analyticAxis).add(percentage);
        percentageAxisMap.replace(analyticAxis, sum);
      } else {
        percentageAxisMap.put(analyticAxis, percentage);
      }
    }

    return percentageAxisMap;
  }
}
