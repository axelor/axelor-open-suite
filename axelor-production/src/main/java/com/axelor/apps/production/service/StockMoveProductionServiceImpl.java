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
package com.axelor.apps.production.service;

import com.axelor.apps.base.service.administration.SequenceService;
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.apps.stock.db.StockMove;
import com.axelor.apps.stock.db.repo.StockMoveLineRepository;
import com.axelor.apps.stock.db.repo.StockMoveRepository;
import com.axelor.apps.stock.service.PartnerProductQualityRatingService;
import com.axelor.apps.stock.service.StockMoveLineService;
import com.axelor.apps.supplychain.service.StockMoveServiceSupplychainImpl;
import com.axelor.exception.AxelorException;
import com.google.inject.Inject;

public class StockMoveProductionServiceImpl extends StockMoveServiceSupplychainImpl {

  @Inject
  public StockMoveProductionServiceImpl(
      StockMoveLineService stockMoveLineService,
      SequenceService sequenceService,
      StockMoveLineRepository stockMoveLineRepository,
      AppBaseService appBaseService,
      StockMoveRepository stockMoveRepository,
      PartnerProductQualityRatingService partnerProductQualityRatingService) {
    super(
        stockMoveLineService,
        sequenceService,
        stockMoveLineRepository,
        appBaseService,
        stockMoveRepository,
        partnerProductQualityRatingService);
  }

  @Override
  public void checkExpirationDates(StockMove stockMove) throws AxelorException {
    if (stockMove.getInManufOrder() != null) {
      stockMoveLineService.checkExpirationDates(stockMove);
    } else {
      super.checkExpirationDates(stockMove);
    }
  }
}
