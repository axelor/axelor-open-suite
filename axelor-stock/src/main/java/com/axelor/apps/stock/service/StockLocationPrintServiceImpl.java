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
package com.axelor.apps.stock.service;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.PrintingTemplate;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.apps.base.exceptions.BaseExceptionMessage;
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.apps.base.service.printing.template.PrintingTemplatePrintService;
import com.axelor.apps.base.service.printing.template.model.PrintingGenFactoryContext;
import com.axelor.apps.stock.db.StockLocation;
import com.axelor.apps.stock.db.repo.StockLocationRepository;
import com.axelor.apps.stock.exception.StockExceptionMessage;
import com.axelor.apps.stock.service.config.StockConfigService;
import com.axelor.common.ObjectUtils;
import com.axelor.i18n.I18n;
import com.google.inject.Inject;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

public class StockLocationPrintServiceImpl implements StockLocationPrintService {

  protected StockConfigService stockConfigService;
  protected PrintingTemplatePrintService printingTemplatePrintService;
  protected StockLocationRepository stockLocationRepository;
  protected StockLocationService stockLocationService;
  protected AppBaseService appBaseService;

  @Inject
  public StockLocationPrintServiceImpl(
      StockConfigService stockConfigService,
      PrintingTemplatePrintService printingTemplatePrintService,
      StockLocationRepository stockLocationRepository,
      StockLocationService stockLocationService,
      AppBaseService appBaseService) {
    this.stockConfigService = stockConfigService;
    this.printingTemplatePrintService = printingTemplatePrintService;
    this.stockLocationRepository = stockLocationRepository;
    this.stockLocationService = stockLocationService;
    this.appBaseService = appBaseService;
  }

  @Override
  public String print(
      Integer printType,
      PrintingTemplate stockLocationPrintTemplate,
      String financialDataDateTimeString,
      Boolean withoutDetailsByStockLocation,
      Long... stockLocationIds)
      throws AxelorException {

    Long firstStockLocationId = stockLocationIds[0];
    StockLocation stockLocation = stockLocationRepository.find(firstStockLocationId);
    Company company = stockLocation.getCompany();

    LocalDateTime financialDataDateTime =
        StockLocationRepository.PRINT_TYPE_LOCATION_FINANCIAL_DATA == printType
                && financialDataDateTimeString != null
            ? LocalDateTime.ofInstant(
                Instant.parse(financialDataDateTimeString), ZoneId.systemDefault())
            : appBaseService.getTodayDateTime(company).toLocalDateTime();

    return print(
        printType,
        stockLocationPrintTemplate,
        financialDataDateTime,
        withoutDetailsByStockLocation,
        stockLocationIds);
  }

  @Override
  public String print(
      Integer printType,
      PrintingTemplate stockLocationPrintTemplate,
      LocalDateTime financialDataDateTime,
      Boolean withoutDetailsByStockLocation,
      Long... stockLocationIds)
      throws AxelorException {

    if (stockLocationIds == null || stockLocationIds.length == 0) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_INCONSISTENCY, I18n.get(StockExceptionMessage.LOCATION_2));
    }

    Long firstStockLocationId = stockLocationIds[0];
    StockLocation stockLocation = stockLocationRepository.find(firstStockLocationId);

    String stockLocationIdsString;
    if (withoutDetailsByStockLocation) {
      stockLocationIdsString = Long.toString(firstStockLocationId);
    } else {
      Set<Long> stockLocationAndSubStockLocationsIds = new LinkedHashSet<>();
      stockLocationAndSubStockLocationsIds.addAll(
          stockLocationService.getContentStockLocationIds(stockLocation));
      for (int i = 1; i < stockLocationIds.length; i++) {
        Long stockLocationId = stockLocationIds[i];
        if (stockLocationAndSubStockLocationsIds.contains(stockLocationId)) {
          continue;
        }
        stockLocationAndSubStockLocationsIds.addAll(
            stockLocationService.getContentStockLocationIds(
                stockLocationRepository.find(stockLocationIds[0])));
      }

      stockLocationIdsString =
          stockLocationAndSubStockLocationsIds.stream()
              .map(id -> Long.toString(id))
              .reduce("", (id1, id2) -> id1 + "," + id2)
              .substring(1);
    }

    if (ObjectUtils.isEmpty(stockLocationPrintTemplate)) {
      stockLocationPrintTemplate =
          stockConfigService
              .getStockConfig(stockLocation.getCompany())
              .getStockLocationPrintTemplate();
    }
    if (ObjectUtils.isEmpty(stockLocationPrintTemplate)) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
          I18n.get(BaseExceptionMessage.TEMPLATE_CONFIG_NOT_FOUND));
    }

    PrintingGenFactoryContext factoryContext = new PrintingGenFactoryContext(stockLocation);
    factoryContext.setContext(
        Map.of(
            "StockLocationId",
            stockLocationIdsString,
            "PrintType",
            printType,
            "FinancialDataDateTime",
            financialDataDateTime,
            "WithoutDetailsByStockLocation",
            withoutDetailsByStockLocation));

    return printingTemplatePrintService.getPrintLink(stockLocationPrintTemplate, factoryContext);
  }

  @Override
  public String getOutputFileName(Long[] stockLocationIds) {
    Long firstStockLocationId = stockLocationIds[0];
    StockLocation stockLocation = stockLocationRepository.find(firstStockLocationId);
    String title = I18n.get("Stock location");
    if (stockLocation.getName() != null) {
      title =
          stockLocationIds.length == 1
              ? I18n.get("Stock location") + " " + stockLocation.getName()
              : I18n.get("Stock location(s)");
    }
    return title;
  }
}
