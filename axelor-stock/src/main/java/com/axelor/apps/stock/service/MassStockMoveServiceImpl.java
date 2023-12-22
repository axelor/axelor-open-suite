package com.axelor.apps.stock.service;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.repo.SequenceRepository;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.apps.base.service.administration.SequenceService;
import com.axelor.apps.stock.db.MassStockMove;
import com.axelor.apps.stock.db.repo.MassStockMoveRepository;
import com.axelor.apps.stock.exception.StockExceptionMessage;
import com.axelor.i18n.I18n;
import com.google.inject.Inject;

public class MassStockMoveServiceImpl implements MassStockMoveService {

  protected MassStockMoveRepository massStockMoveRepository;

  @Inject
  public MassStockMoveServiceImpl(MassStockMoveRepository massStockMoveRepository) {
    this.massStockMoveRepository = massStockMoveRepository;
  }

  @Inject protected SequenceService sequenceService;

  @Override
  public String getSequence(MassStockMove massStockMove) throws AxelorException {
    Company company = massStockMove.getCompany();
    if (company == null) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
          I18n.get(StockExceptionMessage.MASS_STOCK_MOVE_NO_COMPANY));
    }
    String sequence =
        sequenceService.getSequenceNumber(
            SequenceRepository.MASS_STOCK_MOVE, company, MassStockMove.class, "sequence");
    if (sequence == null || sequence.isBlank()) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
          I18n.get(StockExceptionMessage.MASS_STOCK_MOVE_NO_SEQUENCE),
          company.getName());
    }
    return sequence;
  }
}
