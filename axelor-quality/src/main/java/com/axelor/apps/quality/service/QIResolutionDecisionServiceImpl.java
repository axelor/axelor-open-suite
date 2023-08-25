/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2005-2023 Axelor (<http://axelor.com>).
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
package com.axelor.apps.quality.service;

import com.axelor.apps.quality.db.QIResolution;
import com.axelor.apps.quality.db.QIResolutionDecision;
import com.axelor.apps.quality.db.QIResolutionDefault;
import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;
import org.apache.commons.collections.CollectionUtils;

public class QIResolutionDecisionServiceImpl implements QIResolutionDecisionService {

  @Override
  public boolean checkQuantity(
      QIResolutionDecision qiResolutionDecision, QIResolution qiResolution) {
    QIResolutionDefault qiResolutionDefault = qiResolutionDecision.getQiResolutionDefault();

    if (qiResolutionDefault == null
        || qiResolution == null
        || CollectionUtils.isEmpty(qiResolution.getQiResolutionDecisionsList())) {
      return true;
    }
    List<QIResolutionDecision> qiResolutionDecisionList =
        qiResolution.getQiResolutionDecisionsList().stream()
            .filter(it -> qiResolutionDefault.equals(it.getQiResolutionDefault()))
            .collect(Collectors.toList());
    if (CollectionUtils.isEmpty(qiResolutionDecisionList)) {
      return true;
    }
    BigDecimal sumOfQuantites =
        qiResolutionDecisionList.stream()
            .map(QIResolutionDecision::getQuantity)
            .reduce(BigDecimal.ZERO, BigDecimal::add);

    return sumOfQuantites.compareTo(qiResolutionDefault.getQuantity()) <= 0;
  }
}
