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
package com.axelor.apps.stock.service;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.PrintingTemplate;
import com.axelor.apps.base.service.exception.TraceBackService;
import com.axelor.apps.stock.db.StockLocation;
import com.axelor.apps.stock.db.repo.StockLocationRepository;
import com.axelor.apps.stock.exception.StockExceptionMessage;
import com.axelor.auth.AuthUtils;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.axelor.message.service.MailMessageService;
import com.google.inject.persist.Transactional;
import com.google.inject.servlet.RequestScoper;
import com.google.inject.servlet.ServletScopes;
import java.util.Collections;
import java.util.concurrent.Callable;

public class StockLocationPrintCallableService implements Callable<String> {

  private Integer printType;
  private PrintingTemplate stockLocationPrintTemplate;
  private String financialDataDateTimeString;
  private Boolean withoutDetailsByStockLocation;
  private Long[] stockLocationIds;

  public void setPrintType(Integer printType) {
    this.printType = printType;
  }

  public void setStockLocationPrintTemplate(PrintingTemplate stockLocationPrintTemplate) {
    this.stockLocationPrintTemplate = stockLocationPrintTemplate;
  }

  public void setFinancialDataDateTimeString(String financialDataDateTimeString) {
    this.financialDataDateTimeString = financialDataDateTimeString;
  }

  public void setWithoutDetailsByStockLocation(Boolean withoutDetailsByStockLocation) {
    this.withoutDetailsByStockLocation = withoutDetailsByStockLocation;
  }

  public void setStockLocationIds(Long[] stockLocationIds) {
    this.stockLocationIds = stockLocationIds;
  }

  @Override
  public String call() throws AxelorException {
    final RequestScoper scope = ServletScopes.scopeRequest(Collections.emptyMap());
    try (RequestScoper.CloseableScope ignored = scope.open()) {
      String fileLink =
          Beans.get(StockLocationPrintService.class)
              .print(
                  printType,
                  stockLocationPrintTemplate,
                  financialDataDateTimeString,
                  withoutDetailsByStockLocation,
                  stockLocationIds);
      sendReadyNotification();
      return fileLink;
    } catch (Exception e) {
      onRunnerException(e);
      throw e;
    }
  }

  protected void sendReadyNotification() {
    StockLocation stockLocation =
        Beans.get(StockLocationRepository.class).find(stockLocationIds[0]);
    Beans.get(MailMessageService.class)
        .sendNotification(
            AuthUtils.getUser(),
            I18n.get(StockExceptionMessage.STOCK_LOCATION_PRINT_READY_SUBJECT),
            String.format(
                I18n.get(StockExceptionMessage.STOCK_LOCATION_PRINT_READY_MESSAGE),
                stockLocation.getName()),
            stockLocation.getId(),
            StockLocation.class);
  }

  @Transactional
  protected void onRunnerException(Exception e) {
    TraceBackService.trace(e);
    StockLocation stockLocation =
        Beans.get(StockLocationRepository.class).find(stockLocationIds[0]);
    Beans.get(MailMessageService.class)
        .sendNotification(
            AuthUtils.getUser(),
            I18n.get(StockExceptionMessage.STOCK_LOCATION_PRINT_ERROR_SUBJECT),
            e.getMessage(),
            stockLocation.getId(),
            StockLocation.class);
  }
}
