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
package com.axelor.apps.supplychain.service;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.apps.stock.db.LogisticalForm;
import com.axelor.apps.stock.db.StockConfig;
import com.axelor.apps.stock.db.StockMove;
import com.axelor.apps.stock.db.repo.LogisticalFormRepository;
import com.axelor.apps.stock.service.StockMoveService;
import com.axelor.apps.stock.service.config.StockConfigService;
import com.axelor.apps.supplychain.db.Packaging;
import com.axelor.apps.supplychain.exception.SupplychainExceptionMessage;
import com.axelor.i18n.I18n;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.math.BigDecimal;
import java.util.List;
import org.apache.commons.collections.CollectionUtils;

public class LogisticalFormSupplychainServiceImpl implements LogisticalFormSupplychainService {

  protected StockConfigService stockConfigService;
  protected StockMoveService stockMoveService;
  protected StockMoveServiceSupplychain stockMoveServiceSupplychain;

  @Inject
  public LogisticalFormSupplychainServiceImpl(
      StockConfigService stockConfigService,
      StockMoveService stockMoveService,
      StockMoveServiceSupplychain stockMoveServiceSupplychain) {
    this.stockConfigService = stockConfigService;
    this.stockMoveService = stockMoveService;
    this.stockMoveServiceSupplychain = stockMoveServiceSupplychain;
  }

  @Override
  @Transactional(rollbackOn = {Exception.class})
  public void processCollected(LogisticalForm logisticalForm) throws AxelorException {
    if (logisticalForm.getStatusSelect() == null
        || logisticalForm.getStatusSelect() != LogisticalFormRepository.STATUS_CARRIER_VALIDATED) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_INCONSISTENCY,
          I18n.get(SupplychainExceptionMessage.LOGISTICAL_FORM_COLLECT_WRONG_STATUS));
    }

    List<StockMove> stockMoveList = logisticalForm.getStockMoveList();
    List<Packaging> packagingList = logisticalForm.getPackagingList();
    if (CollectionUtils.isEmpty(stockMoveList) || CollectionUtils.isEmpty(packagingList)) {
      return;
    }
    boolean hastQtyRemainingToPackage =
        stockMoveList.stream()
            .filter(stockMove -> CollectionUtils.isNotEmpty(stockMove.getStockMoveLineList()))
            .flatMap(stockMove -> stockMove.getStockMoveLineList().stream())
            .anyMatch(line -> line.getQtyRemainingToPackage().compareTo(BigDecimal.ZERO) > 0);

    if (hastQtyRemainingToPackage) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_INCONSISTENCY,
          I18n.get(SupplychainExceptionMessage.STOCK_MOVE_NOT_FULLY_PACKAGED));
    }
    stockMoveList.forEach(stockMoveServiceSupplychain::updateFullySpreadOverLogisticalFormsFlag);

    StockConfig stockConfig = stockConfigService.getStockConfig(logisticalForm.getCompany());
    if (stockConfig.getRealizeStockMovesUponParcelPalletCollection()) {
      for (StockMove stockMove : stockMoveList) {
        if (stockMove.getFullySpreadOverLogisticalFormsFlag()) {
          stockMoveService.realize(stockMove);
        }
      }
    }
    logisticalForm.setStatusSelect(LogisticalFormRepository.STATUS_COLLECTED);
  }
}
