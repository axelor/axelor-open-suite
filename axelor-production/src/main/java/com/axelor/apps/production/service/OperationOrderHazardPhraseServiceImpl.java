/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2005-2026 Axelor (<http://axelor.com>).
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
package com.axelor.apps.production.service;

import com.axelor.apps.base.db.HazardPhrase;
import com.axelor.apps.production.db.OperationOrder;
import com.axelor.apps.production.translation.ITranslation;
import com.axelor.common.StringUtils;
import com.axelor.i18n.I18n;
import com.axelor.utils.helpers.StringHtmlListBuilder;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import org.apache.commons.collections.CollectionUtils;

public class OperationOrderHazardPhraseServiceImpl implements OperationOrderHazardPhraseService {

  @Override
  public String getOperationOrderHazardPhrases(OperationOrder operationOrder) {
    List<String> alertList = getAlertList(operationOrder);
    if (CollectionUtils.isEmpty(alertList)) {
      return "";
    }
    return StringHtmlListBuilder.formatMessage(I18n.get(ITranslation.WARNING), alertList);
  }

  @Override
  public List<String> getAlertList(OperationOrder operationOrder) {
    List<String> alertList = new ArrayList<>();
    if (operationOrder.getProdProcessLine() == null) {
      return alertList;
    }
    Set<HazardPhrase> hazardPhraseSet = operationOrder.getProdProcessLine().getHazardPhraseSet();
    if (CollectionUtils.isEmpty(hazardPhraseSet)) {
      return alertList;
    }
    for (HazardPhrase hazardPhrase : hazardPhraseSet) {
      String alert = hazardPhrase.getPhrase();
      if (StringUtils.notBlank(hazardPhrase.getClpDesignation())) {
        alert = alert + " : " + hazardPhrase.getClpDesignation();
      }
      alertList.add(alert);
    }
    return alertList;
  }
}
