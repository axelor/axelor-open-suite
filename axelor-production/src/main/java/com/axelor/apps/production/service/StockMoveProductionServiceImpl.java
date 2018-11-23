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
