package com.axelor.apps.stock.service;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.repo.SequenceRepository;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.apps.base.service.administration.SequenceService;
import com.axelor.apps.base.service.exception.TraceBackService;
import com.axelor.apps.stock.db.MassStockMove;
import com.axelor.apps.stock.db.PickedProducts;
import com.axelor.apps.stock.db.StockLocationLine;
import com.axelor.apps.stock.db.StoredProducts;
import com.axelor.apps.stock.db.repo.MassStockMoveRepository;
import com.axelor.apps.stock.db.repo.PickedProductsRepository;
import com.axelor.apps.stock.db.repo.StockLocationLineRepository;
import com.axelor.apps.stock.exception.StockExceptionMessage;
import com.axelor.i18n.I18n;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.math.BigDecimal;
import java.util.List;

public class MassStockMoveServiceImpl implements MassStockMoveService {

  protected MassStockMoveRepository massStockMoveRepository;
  protected SequenceService sequenceService;
  protected PickedProductsRepository pickedProductRepo;
  protected StockLocationLineRepository stockLocationLineRepository;
  protected PickedProductsService pickedProductsService;
  protected StoredProductsService storedProductsService;

  @Inject
  public MassStockMoveServiceImpl(
      MassStockMoveRepository massStockMoveRepository,
      SequenceService sequenceService,
      PickedProductsRepository pickedProductRepo,
      StockLocationLineRepository stockLocationLineRepository,
      PickedProductsService pickedProductsService,
      StoredProductsService storedProductsService) {
    this.massStockMoveRepository = massStockMoveRepository;
    this.sequenceService = sequenceService;
    this.pickedProductRepo = pickedProductRepo;
    this.stockLocationLineRepository = stockLocationLineRepository;
    this.pickedProductsService = pickedProductsService;
    this.storedProductsService = storedProductsService;
  }

  @Override
  public void realizePicking(MassStockMove massStockMove) {
    for (PickedProducts pickedProducts : massStockMove.getPickedProductsList()) {
      try {
        pickedProductsService.createStockMoveAndStockMoveLine(massStockMove, pickedProducts);
      } catch (AxelorException e) {
        TraceBackService.trace(e);
      }
    }
  }

  @Override
  public void realizeStorage(MassStockMove massStockMove) {
    for (StoredProducts storedProducts : massStockMove.getStoredProductsList()) {
      try {
        storedProductsService.createStockMoveAndStockMoveLine(storedProducts);
      } catch (AxelorException e) {
        TraceBackService.trace(e);
      }
    }
  }

  @Override
  public void cancelPicking(MassStockMove massStockMove) {
    for (PickedProducts pickedProducts : massStockMove.getPickedProductsList()) {
      try {
        pickedProductsService.cancelStockMoveAndStockMoveLine(massStockMove, pickedProducts);
      } catch (AxelorException e) {
        TraceBackService.trace(e);
      }
    }
  }

  @Override
  public void cancelStorage(MassStockMove massStockMove) {
    for (StoredProducts storedProducts : massStockMove.getStoredProductsList()) {
      try {
        storedProductsService.cancelStockMoveAndStockMoveLine(storedProducts);
      } catch (AxelorException e) {
        TraceBackService.trace(e);
      }
    }
  }

  @Override
  @Transactional(rollbackOn = Exception.class)
  public void importProductFromStockLocation(MassStockMove massStockMove) throws AxelorException {
    List<StockLocationLine> stockLocationLineList =
        stockLocationLineRepository
            .all()
            .filter("self.stockLocation = ?1", massStockMove.getCommonFromStockLocation())
            .fetch();
    List<StockLocationLine> detailsStockLocationLineList =
        stockLocationLineRepository
            .all()
            .filter("self.detailsStockLocation =?1", massStockMove.getCommonFromStockLocation())
            .fetch();
    if (stockLocationLineList.isEmpty() && detailsStockLocationLineList.isEmpty()) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_NO_VALUE,
          I18n.get("No stock location lines have been found in the common from stock location."));
    }
    for (StockLocationLine line : stockLocationLineList) {
      if (line.getProduct().getTrackingNumberConfiguration() == null
          && line.getCurrentQty().compareTo(BigDecimal.ZERO) == 1) {
        PickedProducts pickedProduct =
            pickedProductsService.createPickedProduct(
                massStockMove,
                line.getProduct(),
                null,
                line.getStockLocation(),
                line.getCurrentQty(),
                BigDecimal.ZERO,
                null);
        massStockMove.addPickedProductsListItem(pickedProduct);
        pickedProductRepo.save(pickedProduct);
      }
    }
    for (StockLocationLine line : detailsStockLocationLineList) {
      if (line.getCurrentQty().compareTo(BigDecimal.ZERO) == 1
          && line.getProduct() != null
          && line.getProduct().getTrackingNumberConfiguration() != null) {
        PickedProducts pickedProduct =
            pickedProductsService.createPickedProduct(
                massStockMove,
                line.getProduct(),
                line.getTrackingNumber(),
                line.getDetailsStockLocation(),
                line.getCurrentQty(),
                BigDecimal.ZERO,
                null);
        massStockMove.addPickedProductsListItem(pickedProduct);
        pickedProductRepo.save(pickedProduct);
      }
    }

    massStockMoveRepository.save(massStockMove);
  }

  @Override
  @Transactional(rollbackOn = Exception.class)
  public void setStatusSelectToDraft(MassStockMove massStockMove) {
    massStockMove.setStatusSelect(MassStockMoveRepository.STATUS_DRAFT);
    massStockMoveRepository.save(massStockMove);
  }

  @Override
  @Transactional(rollbackOn = Exception.class)
  public String getAndSetSequence(Company company, MassStockMove massStockMoveToSet)
      throws AxelorException {
    String sequence = massStockMoveToSet.getSequence();

    String seq;
    if (sequence == null || sequence.isEmpty()) {
      seq =
          sequenceService.getSequenceNumber(
              SequenceRepository.MASS_STOCK_MOVE, company, MassStockMove.class, "sequence");
      if (seq == null) {
        throw new AxelorException(
            TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
            I18n.get(StockExceptionMessage.MASS_STOCK_MOVE_NO_SEQUENCE),
            company.getName());
      }
      massStockMoveToSet.setSequence(seq);
      massStockMoveRepository.save(massStockMoveToSet);
      return seq;
    }
    return sequence;
  }
}
