package com.axelor.apps.stock.service;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.repo.SequenceRepository;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.apps.base.service.administration.SequenceService;
import com.axelor.apps.base.service.exception.TraceBackService;
import com.axelor.apps.stock.db.MassStockMove;
import com.axelor.apps.stock.db.MassStockMoveNeed;
import com.axelor.apps.stock.db.PickedProduct;
import com.axelor.apps.stock.db.StockLocationLine;
import com.axelor.apps.stock.db.StockMoveLine;
import com.axelor.apps.stock.db.StoredProduct;
import com.axelor.apps.stock.db.repo.MassStockMoveNeedRepository;
import com.axelor.apps.stock.db.repo.MassStockMoveRepository;
import com.axelor.apps.stock.db.repo.PickedProductRepository;
import com.axelor.apps.stock.db.repo.StockLocationLineRepository;
import com.axelor.apps.stock.db.repo.StockMoveLineRepository;
import com.axelor.apps.stock.db.repo.StoredProductRepository;
import com.axelor.apps.stock.exception.StockExceptionMessage;
import com.axelor.i18n.I18n;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.math.BigDecimal;
import java.util.List;

public class MassStockMoveServiceImpl implements MassStockMoveService {

  protected MassStockMoveRepository massStockMoveRepository;
  protected SequenceService sequenceService;
  protected PickedProductRepository pickedProductRepo;
  protected StoredProductRepository storedProductRepo;
  protected StockLocationLineRepository stockLocationLineRepository;
  protected PickedProductService pickedProductService;
  protected StoredProductService storedProductService;
  protected StockMoveLineRepository stockMoveLineRepository;
  protected MassStockMoveNeedService massStockMoveNeedService;
  protected MassStockMoveNeedRepository massStockMoveNeedRepository;

  @Inject
  public MassStockMoveServiceImpl(
      MassStockMoveRepository massStockMoveRepository,
      SequenceService sequenceService,
      PickedProductRepository pickedProductRepo,
      StoredProductRepository storedProductRepo,
      StockLocationLineRepository stockLocationLineRepository,
      PickedProductService pickedProductService,
      StoredProductService storedProductService,
      StockMoveLineRepository stockMoveLineRepository,
      MassStockMoveNeedService massMoveNeedsService,
      MassStockMoveNeedRepository massStockMoveNeedRepository) {
    this.massStockMoveRepository = massStockMoveRepository;
    this.sequenceService = sequenceService;
    this.pickedProductRepo = pickedProductRepo;
    this.storedProductRepo = storedProductRepo;
    this.stockLocationLineRepository = stockLocationLineRepository;
    this.pickedProductService = pickedProductService;
    this.storedProductService = storedProductService;
    this.stockMoveLineRepository = stockMoveLineRepository;
    this.massStockMoveNeedService = massMoveNeedsService;
    this.massStockMoveNeedRepository = massStockMoveNeedRepository;
  }

  @Override
  public void realizePicking(MassStockMove massStockMove) {
    for (PickedProduct pickedProduct : massStockMove.getPickedProductList()) {
      try {
        if (BigDecimal.ZERO.compareTo(pickedProduct.getPickedQty()) < 0) {
          massStockMove = massStockMoveRepository.find(massStockMove.getId());
          pickedProduct = pickedProductRepo.find(pickedProduct.getId());
          pickedProductService.createStockMoveAndStockMoveLine(massStockMove, pickedProduct);
        }
      } catch (AxelorException e) {
        TraceBackService.trace(e);
      }
    }
  }

  @Override
  public void realizeStorage(MassStockMove massStockMove) {
    for (StoredProduct storedProduct : massStockMove.getStoredProductList()) {
      try {
        if (BigDecimal.ZERO.compareTo(storedProduct.getStoredQty()) < 0) {
          storedProduct = storedProductRepo.find(storedProduct.getId());
          storedProductService.createStockMoveAndStockMoveLine(storedProduct);
        }
      } catch (AxelorException e) {
        TraceBackService.trace(e);
      }
    }
  }

  @Override
  public int cancelPicking(MassStockMove massStockMove) {
    int errors = 0;
    for (PickedProduct pickedProduct : massStockMove.getPickedProductList()) {
      try {
        if (pickedProduct.getStockMoveLine() != null) {
          massStockMove = massStockMoveRepository.find(massStockMove.getId());
          pickedProduct = pickedProductRepo.find(pickedProduct.getId());
          pickedProductService.cancelStockMoveAndStockMoveLine(massStockMove, pickedProduct);
        }
      } catch (AxelorException e) {
        TraceBackService.trace(e);
        errors++;
      }
    }
    return errors;
  }

  @Override
  public void cancelStorage(MassStockMove massStockMove) {
    for (StoredProduct storedProduct : massStockMove.getStoredProductList()) {
      try {
        if (storedProduct.getStockMoveLine() != null) {
          storedProductService.cancelStockMoveAndStockMoveLine(storedProduct);
        }
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
          I18n.get(StockExceptionMessage.MASS_STOCK_MOVE_NO_LOCATION_LINE));
    }
    for (StockLocationLine line : stockLocationLineList) {
      if (line.getProduct().getTrackingNumberConfiguration() == null
          && line.getCurrentQty().compareTo(BigDecimal.ZERO) == 1
          && !isProductInList(line, massStockMove)) {
        PickedProduct pickedProduct =
            pickedProductService.createPickedProduct(
                massStockMove,
                line.getProduct(),
                null,
                line.getStockLocation(),
                line.getCurrentQty(),
                BigDecimal.ZERO,
                null);
        massStockMove.addPickedProductListItem(pickedProduct);
        pickedProductRepo.save(pickedProduct);
      }
    }
    for (StockLocationLine line : detailsStockLocationLineList) {
      if (line.getCurrentQty().compareTo(BigDecimal.ZERO) == 1
          && line.getProduct() != null
          && line.getProduct().getTrackingNumberConfiguration() != null
          && !isProductInList(line, massStockMove)) {
        PickedProduct pickedProduct =
            pickedProductService.createPickedProduct(
                massStockMove,
                line.getProduct(),
                line.getTrackingNumber(),
                line.getDetailsStockLocation(),
                line.getCurrentQty(),
                BigDecimal.ZERO,
                null);
        massStockMove.addPickedProductListItem(pickedProduct);
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

  private boolean isProductInList(StockLocationLine line, MassStockMove massStockMove) {
    if (line.getTrackingNumber() == null) {
      return massStockMove.getPickedProductList().stream()
          .anyMatch(
              pickedProduct ->
                  line.getProduct().getId().equals(pickedProduct.getPickedProduct().getId()));
    }
    return massStockMove.getPickedProductList().stream()
        .anyMatch(
            pickedProduct ->
                line.getProduct().getId().equals(pickedProduct.getPickedProduct().getId())
                    && line.getTrackingNumber().equals(pickedProduct.getTrackingNumber()));
  }

  @Override
  @Transactional(rollbackOn = Exception.class)
  public void useStockMoveLinesIdsToCreateMassStockMoveNeeds(
      MassStockMove massStockMove, List<Long> stockMoveLinesToAdd) {
    for (Long stockMoveLineId : stockMoveLinesToAdd) {
      StockMoveLine stockMoveLine = stockMoveLineRepository.find(stockMoveLineId);
      MassStockMoveNeed massStockMoveNeed =
          massStockMoveNeedService.createMassStockMoveNeed(
              massStockMove, stockMoveLine.getProduct(), stockMoveLine.getRealQty());
      massStockMoveNeedRepository.save(massStockMoveNeed);
      massStockMove.addProductToMoveListItem(massStockMoveNeed);
    }
    massStockMoveRepository.save(massStockMove);
  }

  @Override
  @Transactional(rollbackOn = Exception.class)
  public void clearProductToMoveList(MassStockMove massStockMove) {
    massStockMove.getProductToMoveList().clear();
    massStockMoveRepository.save(massStockMove);
  }
}
