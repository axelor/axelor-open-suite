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

import com.axelor.apps.production.db.ManufOrder;
import com.axelor.apps.production.db.OperationOrder;
import com.axelor.apps.production.translation.ITranslation;
import com.axelor.common.StringUtils;
import com.axelor.i18n.I18n;
import com.axelor.utils.helpers.StringHtmlListBuilder;
import jakarta.inject.Inject;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.collections.CollectionUtils;

public class ManufOrderMessageServiceImpl implements ManufOrderMessageService {
  protected final OperationOrderHazardPhraseService operationOrderHazardPhraseService;

  @Inject
  public ManufOrderMessageServiceImpl(
      OperationOrderHazardPhraseService operationOrderHazardPhraseService) {
    this.operationOrderHazardPhraseService = operationOrderHazardPhraseService;
  }

  @Override
  public String getManufOrderStartMessage(ManufOrder manufOrder) {
    String commentMessage = getSaleOrderCommentMessage(manufOrder);
    String hazardPhraseMessage = getManufOrderHazardPhrases(manufOrder);
    if (commentMessage.isEmpty() && hazardPhraseMessage.isEmpty()) {
      return "";
    }
    if (commentMessage.isEmpty()) {
      return hazardPhraseMessage;
    }
    if (hazardPhraseMessage.isEmpty()) {
      return commentMessage;
    }
    return commentMessage + hazardPhraseMessage;
  }

  @Override
  public String getManufOrderHazardPhrases(ManufOrder manufOrder) {
    List<OperationOrder> operationOrderList = manufOrder.getOperationOrderList();
    if (CollectionUtils.isEmpty(operationOrderList)) {
      return "";
    }
    List<String> alertList = getAlertList(operationOrderList);
    if (CollectionUtils.isEmpty(alertList)) {
      return "";
    }
    return StringHtmlListBuilder.formatMessage(I18n.get(ITranslation.WARNING), alertList);
  }

  protected String getSaleOrderCommentMessage(ManufOrder manufOrder) {
    List<String> comments = new ArrayList<>();
    if (StringUtils.notBlank(manufOrder.getMoCommentFromSaleOrder())) {
      comments.add(manufOrder.getMoCommentFromSaleOrder());
    }
    if (StringUtils.notBlank(manufOrder.getMoCommentFromSaleOrderLine())) {
      comments.add(manufOrder.getMoCommentFromSaleOrderLine());
    }
    if (comments.isEmpty()) {
      return "";
    }
    return StringHtmlListBuilder.formatMessage(I18n.get(ITranslation.PRODUCTION_COMMENT), comments);
  }

  protected List<String> getAlertList(List<OperationOrder> operationOrderList) {
    List<String> alertList = new ArrayList<>();
    for (OperationOrder operationOrder : operationOrderList) {
      if (operationOrder.getProdProcessLine() == null
          || CollectionUtils.isEmpty(operationOrder.getProdProcessLine().getHazardPhraseSet())) {
        continue;
      }
      alertList.add(
          StringHtmlListBuilder.formatMessage(
              operationOrder.getOperationName(),
              operationOrderHazardPhraseService.getAlertList(operationOrder)));
    }
    return alertList;
  }
}
