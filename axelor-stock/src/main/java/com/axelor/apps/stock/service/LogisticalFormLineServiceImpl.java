/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2019 Axelor (<http://axelor.com>).
 *
 * This program is free software: you can redistribute it and/or  modify
 * it under the terms of the GNU Affero General Public License, version 3,
 * as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.axelor.apps.stock.service;

import com.axelor.apps.base.db.Partner;
import com.axelor.apps.stock.db.LogisticalForm;
import com.axelor.apps.stock.db.LogisticalFormLine;
import com.axelor.apps.stock.db.StockMoveLine;
import com.axelor.apps.stock.db.repo.StockMoveRepository;
import com.axelor.apps.stock.exception.IExceptionMessage;
import com.axelor.apps.stock.exception.LogisticalFormError;
import com.axelor.apps.tool.StringTool;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.axelor.script.ScriptHelper;
import com.google.common.base.Strings;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class LogisticalFormLineServiceImpl implements LogisticalFormLineService {

  private static final Pattern DIMENSIONS_PATTERN =
      Pattern.compile(
          "\\s*\\d+(\\.\\d*)?\\s*[x\\*]\\s*\\d+(\\.\\d*)?\\s*[x\\*]\\s*\\d+(\\.\\d*)?\\s*");

  @Override
  public BigDecimal getUnspreadQty(LogisticalFormLine logisticalFormLine) {
    StockMoveLine stockMoveLine = logisticalFormLine.getStockMoveLine();
    return Beans.get(StockMoveLineService.class)
        .computeSpreadableQtyOverLogisticalFormLines(
            stockMoveLine, logisticalFormLine.getLogisticalForm());
  }

  @Override
  public String getStockMoveLineDomain(LogisticalFormLine logisticalFormLine) {
    long partnerId = 0;
    List<String> domainList = new ArrayList<>();
    LogisticalForm logisticalForm = logisticalFormLine.getLogisticalForm();

    if (logisticalForm != null) {
      Partner deliverToCustomerPartner = logisticalForm.getDeliverToCustomerPartner();

      if (deliverToCustomerPartner != null) {
        partnerId = deliverToCustomerPartner.getId();
      }
    }

    domainList.add(String.format("self.stockMove.partner.id = %d", partnerId));
    domainList.add(
        String.format("self.stockMove.typeSelect = %d", StockMoveRepository.TYPE_OUTGOING));
    domainList.add(
        String.format(
            "self.stockMove.statusSelect in (%d, %d)",
            StockMoveRepository.STATUS_PLANNED, StockMoveRepository.STATUS_REALIZED));
    domainList.add("self.realQty > 0");
    domainList.add("COALESCE(self.stockMove.fullySpreadOverLogisticalFormsFlag, FALSE) = FALSE");

    List<StockMoveLine> fullySpreadStockMoveLineList =
        Beans.get(LogisticalFormService.class).getFullySpreadStockMoveLineList(logisticalForm);

    if (!fullySpreadStockMoveLineList.isEmpty()) {
      String idListString = StringTool.getIdListString(fullySpreadStockMoveLineList);
      domainList.add(String.format("self.id NOT IN (%s)", idListString));
    }

    return domainList
        .stream()
        .map(domain -> String.format("(%s)", domain))
        .collect(Collectors.joining(" AND "));
  }

  @Override
  public void validateDimensions(LogisticalFormLine logisticalFormLine) throws LogisticalFormError {
    String dimensions = logisticalFormLine.getDimensions();
    if (!Strings.isNullOrEmpty(dimensions) && !DIMENSIONS_PATTERN.matcher(dimensions).matches()) {
      throw new LogisticalFormError(
          logisticalFormLine,
          I18n.get(IExceptionMessage.LOGISTICAL_FORM_LINE_INVALID_DIMENSIONS),
          logisticalFormLine.getSequence() + 1);
    }
  }

  @Override
  public BigDecimal evalVolume(LogisticalFormLine logisticalFormLine, ScriptHelper scriptHelper)
      throws LogisticalFormError {
    validateDimensions(logisticalFormLine);
    String script = logisticalFormLine.getDimensions();

    if (Strings.isNullOrEmpty(script)) {
      return BigDecimal.ZERO;
    }

    return (BigDecimal)
        scriptHelper.eval(String.format("new BigDecimal(%s)", script.replaceAll("x", "*")));
  }

  @Override
  public void initParcelPallet(LogisticalFormLine logisticalFormLine) {
    LogisticalFormService logisticalFormService = Beans.get(LogisticalFormService.class);
    logisticalFormLine.setParcelPalletNumber(
        logisticalFormService.getNextParcelPalletNumber(
            logisticalFormLine.getLogisticalForm(), logisticalFormLine.getTypeSelect()));
    logisticalFormLine.setSequence(
        logisticalFormService.getNextLineSequence(logisticalFormLine.getLogisticalForm()));
  }
}
