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
package com.axelor.apps.supplychain.service.declarationofexchanges;

import com.axelor.apps.base.db.EconomicArea;
import com.axelor.apps.base.db.repo.ProductRepository;
import com.axelor.apps.stock.exception.IExceptionMessage;
import com.axelor.apps.stock.service.app.AppStockService;
import com.axelor.apps.stock.service.config.StockConfigService;
import com.axelor.apps.supplychain.db.DeclarationOfExchanges;
import com.axelor.auth.AuthUtils;
import com.axelor.exception.AxelorException;
import com.axelor.exception.db.repo.TraceBackRepository;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.google.common.collect.ImmutableMap;
import com.google.inject.Inject;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.Path;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;
import org.apache.commons.lang3.tuple.Pair;

public class DeclarationOfExchangesServiceImpl implements DeclarationOfExchangesService {

  protected Map<String, Map<String, Class<? extends DeclarationOfExchangesExporter>>>
      exportServiceClassMap;

  protected StockConfigService stockConfigService;

  @Inject
  public DeclarationOfExchangesServiceImpl(StockConfigService stockConfigService) {
    this.stockConfigService = stockConfigService;
  }

  @Override
  public Pair<Path, String> export(DeclarationOfExchanges declarationOfExchanges)
      throws AxelorException {
    if (declarationOfExchanges.getCountry().getEconomicArea() == null) {
      throw new AxelorException(
          declarationOfExchanges.getCountry(),
          TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
          I18n.get(IExceptionMessage.DECLARATION_OF_EXCHANGES_ECONOMIC_AREA_MISSING),
          declarationOfExchanges.getCountry().getName());
    }

    Map<String, Class<? extends DeclarationOfExchangesExporter>> map = null;
    EconomicArea economicArea = Beans.get(AppStockService.class).getAppStock().getEconomicArea();
    if (economicArea == null) {
      throw new AxelorException(
          declarationOfExchanges,
          TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
          I18n.get(IExceptionMessage.DECLARATION_OF_EXCHANGES_ECONOMIC_AREA_MISSING_IN_APP_STOCK));
    }
    exportServiceClassMap = getExportServiceClassMap(economicArea);
    map =
        exportServiceClassMap.get(declarationOfExchanges.getCountry().getEconomicArea().getCode());
    if (map == null) {
      throw new AxelorException(
          declarationOfExchanges,
          TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
          I18n.get(IExceptionMessage.DECLARATION_OF_EXCHANGES_ECONOMIC_AREA_UNSUPPORTED),
          declarationOfExchanges.getCountry().getEconomicArea().getName());
    }

    Class<? extends DeclarationOfExchangesExporter> exportServiceClass =
        map.get(declarationOfExchanges.getProductTypeSelect());
    String languageCode = AuthUtils.getUser().getLanguage();
    ResourceBundle bundle = I18n.getBundle(new Locale(languageCode));

    if (exportServiceClass == null) {
      throw new UnsupportedOperationException(
          String.format(
              I18n.get("Unsupported product type: %s"),
              declarationOfExchanges.getProductTypeSelect()));
    }

    DeclarationOfExchangesExporter exporter;

    try {
      exporter =
          exportServiceClass
              .getConstructor(DeclarationOfExchanges.class, ResourceBundle.class)
              .newInstance(declarationOfExchanges, bundle);
    } catch (InstantiationException
        | IllegalAccessException
        | IllegalArgumentException
        | InvocationTargetException
        | NoSuchMethodException
        | SecurityException e) {
      throw new AxelorException(
          e,
          declarationOfExchanges,
          TraceBackRepository.CATEGORY_INCONSISTENCY,
          e.getLocalizedMessage());
    }

    return exporter.export();
  }

  @Override
  public Map<String, Map<String, Class<? extends DeclarationOfExchangesExporter>>>
      getExportServiceClassMap(EconomicArea economicArea) {
    return ImmutableMap.of(
        economicArea.getCode(),
        ImmutableMap.of(
            ProductRepository.PRODUCT_TYPE_STORABLE,
            DeclarationOfExchangesExporterGoods.class,
            ProductRepository.PRODUCT_TYPE_SERVICE,
            DeclarationOfExchangesExporterServices.class));
  }
}
