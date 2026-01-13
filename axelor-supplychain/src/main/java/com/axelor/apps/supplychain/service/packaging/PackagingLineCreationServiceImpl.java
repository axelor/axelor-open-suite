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
package com.axelor.apps.supplychain.service.packaging;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.apps.stock.db.LogisticalForm;
import com.axelor.apps.stock.db.StockConfig;
import com.axelor.apps.stock.db.StockMoveLine;
import com.axelor.apps.stock.db.repo.StockMoveLineRepository;
import com.axelor.apps.stock.service.config.StockConfigService;
import com.axelor.apps.supplychain.db.Packaging;
import com.axelor.apps.supplychain.db.PackagingLine;
import com.axelor.apps.supplychain.db.repo.PackagingLineRepository;
import com.axelor.apps.supplychain.exception.SupplychainExceptionMessage;
import com.axelor.apps.supplychain.service.LogisticalFormComputeService;
import com.axelor.auth.AuthUtils;
import com.axelor.i18n.I18n;
import com.axelor.utils.helpers.StringHelper;
import com.google.inject.persist.Transactional;
import jakarta.inject.Inject;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import org.apache.commons.collections.CollectionUtils;

public class PackagingLineCreationServiceImpl implements PackagingLineCreationService {

  protected final PackagingLineRepository packagingLineRepository;
  protected final StockMoveLineRepository stockMoveLineRepository;
  protected final LogisticalFormComputeService logisticalFormComputeService;
  protected final StockConfigService stockConfigService;

  @Inject
  public PackagingLineCreationServiceImpl(
      PackagingLineRepository packagingLineRepository,
      StockMoveLineRepository stockMoveLineRepository,
      LogisticalFormComputeService logisticalFormComputeService,
      StockConfigService stockConfigService) {
    this.packagingLineRepository = packagingLineRepository;
    this.stockMoveLineRepository = stockMoveLineRepository;
    this.logisticalFormComputeService = logisticalFormComputeService;
    this.stockConfigService = stockConfigService;
  }

  @Override
  public void addPackagingLines(Packaging packaging, List<StockMoveLine> stockMoveLineList)
      throws AxelorException {
    if (packaging == null || CollectionUtils.isEmpty(stockMoveLineList)) {
      return;
    }
    for (StockMoveLine stockMoveLine : stockMoveLineList) {
      createPackagingLine(packaging, stockMoveLine, stockMoveLine.getQtyRemainingToPackage());
    }
  }

  @Override
  public String getStockMoveLineDomain(LogisticalForm logisticalForm) throws AxelorException {
    Company company =
        Optional.ofNullable(logisticalForm)
            .map(LogisticalForm::getCompany)
            .orElse(AuthUtils.getUser().getActiveCompany());
    StockConfig stockConfig = stockConfigService.getStockConfig(company);
    boolean allowInternalStockMove = stockConfig.getAllowInternalStockMoveOnLogisticalForm();
    String typeSelectFilter =
        allowInternalStockMove
            ? "(self.stockMove.typeSelect = 2 OR self.stockMove.typeSelect = 1)"
            : "self.stockMove.typeSelect = 2";
    if (logisticalForm == null) {
      return typeSelectFilter + " AND self.qtyRemainingToPackage > 0";
    }
    String stockMoveIds = StringHelper.getIdListString(logisticalForm.getStockMoveList());
    if (stockMoveIds.isEmpty()) {
      return "self.id = 0";
    }
    return String.format(
        typeSelectFilter + " AND self.qtyRemainingToPackage > 0 AND self.stockMove.id IN (%s)",
        stockMoveIds);
  }

  @Transactional(rollbackOn = Exception.class)
  @Override
  public PackagingLine createPackagingLine(
      Packaging packaging, StockMoveLine stockMoveLine, BigDecimal quantity)
      throws AxelorException {
    PackagingLine packagingLine = new PackagingLine();
    packagingLine.setPackaging(packaging);
    if (stockMoveLine != null) {
      checkStockMoveLine(stockMoveLine, packagingLine);
      packagingLine.setStockMoveLine(stockMoveLine);
    }

    setQty(stockMoveLine, quantity, packagingLine);
    return packagingLineRepository.save(packagingLine);
  }

  protected void checkStockMoveLine(StockMoveLine stockMoveLine, PackagingLine packagingLine)
      throws AxelorException {
    LogisticalForm logisticalForm = getParentLogisticalForm(packagingLine);
    if (!stockMoveLineRepository
        .all()
        .filter(getStockMoveLineDomain(logisticalForm))
        .fetch()
        .contains(stockMoveLine)) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_INCONSISTENCY,
          I18n.get(SupplychainExceptionMessage.PACKAGING_LINE_STOCK_MOVE_LINE_NOT_VALID));
    }
  }

  protected void setQty(
      StockMoveLine stockMoveLine, BigDecimal quantity, PackagingLine packagingLine) {
    if (quantity != null) {
      packagingLine.setQty(quantity);
    } else {
      if (stockMoveLine != null) {
        packagingLine.setQty(stockMoveLine.getQtyRemainingToPackage());
      } else {
        packagingLine.setQty(BigDecimal.ZERO);
      }
    }
  }

  @Transactional(rollbackOn = Exception.class)
  @Override
  public void updateQuantity(PackagingLine packagingLine, BigDecimal quantity)
      throws AxelorException {
    LogisticalForm logisticalForm = getParentLogisticalForm(packagingLine);
    packagingLine.setQty(quantity);
    packagingLineRepository.save(packagingLine);
    logisticalFormComputeService.computeLogisticalForm(logisticalForm);
  }

  @Transactional(rollbackOn = Exception.class)
  @Override
  public void deletePackagingLine(PackagingLine packagingLine) throws AxelorException {
    LogisticalForm logisticalForm = getParentLogisticalForm(packagingLine);
    Packaging packaging = packagingLine.getPackaging();
    packaging.removePackagingLineListItem(packagingLine);
    logisticalFormComputeService.computeLogisticalForm(logisticalForm);
  }

  @Override
  public LogisticalForm getParentLogisticalForm(PackagingLine packagingLine) {
    Packaging packaging = packagingLine.getPackaging();
    Packaging parentPackaging = getParentPackaging(packaging);
    return parentPackaging.getLogisticalForm();
  }

  protected Packaging getParentPackaging(Packaging packaging) {
    Packaging parentPackaging = packaging.getParentPackaging();
    if (parentPackaging != null) {
      return getParentPackaging(parentPackaging);
    }
    return packaging;
  }
}
