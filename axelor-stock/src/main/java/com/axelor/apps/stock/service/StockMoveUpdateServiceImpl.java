/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2005-2024 Axelor (<http://axelor.com>).
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
import com.axelor.apps.base.db.Unit;
import com.axelor.apps.base.db.repo.ProductRepository;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.apps.base.service.UnitConversionService;
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.apps.stock.db.StockMove;
import com.axelor.apps.stock.db.StockMoveLine;
import com.axelor.apps.stock.db.repo.StockMoveRepository;
import com.axelor.inject.Beans;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.lang.invoke.MethodHandles;
import java.math.BigDecimal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class StockMoveUpdateServiceImpl implements StockMoveUpdateService {

  private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  protected StockMoveLineService stockMoveLineService;
  protected StockMoveService stockMoveService;
  protected AppBaseService appBaseService;
  protected StockMoveRepository stockMoveRepo;
  protected PartnerProductQualityRatingService partnerProductQualityRatingService;
  protected ProductRepository productRepository;

  @Inject
  public StockMoveUpdateServiceImpl(
      StockMoveLineService stockMoveLineService,
      StockMoveService stockMoveService,
      AppBaseService appBaseService,
      StockMoveRepository stockMoveRepository,
      PartnerProductQualityRatingService partnerProductQualityRatingService,
      ProductRepository productRepository) {
    this.stockMoveLineService = stockMoveLineService;
    this.stockMoveService = stockMoveService;
    this.appBaseService = appBaseService;
    this.stockMoveRepo = stockMoveRepository;
    this.partnerProductQualityRatingService = partnerProductQualityRatingService;
    this.productRepository = productRepository;
  }

  @Deprecated
  @Override
  public void updateStatus(StockMove stockMove, Integer targetStatus) throws AxelorException {
    if (targetStatus == StockMoveRepository.STATUS_PLANNED) {
      stockMoveService.plan(stockMove);
    } else if (targetStatus == StockMoveRepository.STATUS_REALIZED) {
      stockMoveService.realize(stockMove);
    } else if (targetStatus == StockMoveRepository.STATUS_CANCELED) {
      stockMoveService.cancel(stockMove);
    } else {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_INCONSISTENCY,
          "Workflow to update status to value %d is not supported for stock move.",
          targetStatus);
    }
  }

  /**
   * @deprecated To update unit or qty of an internal stock move with one product, mostly for mobile
   *     app (API AOS) *
   */
  @Deprecated
  @Override
  @Transactional(rollbackOn = {Exception.class})
  public void updateStockMoveMobility(StockMove stockMove, BigDecimal movedQty, Unit unit)
      throws AxelorException {
    StockMoveLine line = stockMove.getStockMoveLineList().get(0);
    if (unit != null) {
      BigDecimal convertQty =
          Beans.get(UnitConversionService.class)
              .convert(
                  line.getUnit(), unit, line.getQty(), line.getQty().scale(), line.getProduct());
      line.setUnit(unit);
      line.setQty(convertQty);
      line.setRealQty(convertQty);
    }
    if (movedQty != null) {
      // Only one product
      line.setQty(movedQty);
      line.setRealQty(movedQty);
    }
    stockMoveRepo.save(stockMove);
  }
}
