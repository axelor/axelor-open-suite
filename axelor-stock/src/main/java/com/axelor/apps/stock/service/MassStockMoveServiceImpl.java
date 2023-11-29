package com.axelor.apps.stock.service;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.repo.SequenceRepository;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.apps.base.service.administration.SequenceService;
import com.axelor.apps.base.service.exception.TraceBackService;
import com.axelor.apps.stock.db.MassStockMove;
import com.axelor.apps.stock.db.PickedProduct;
import com.axelor.apps.stock.db.StockLocationLine;
import com.axelor.apps.stock.db.StoredProduct;
import com.axelor.apps.stock.db.repo.MassStockMoveRepository;
import com.axelor.apps.stock.db.repo.PickedProductRepository;
import com.axelor.apps.stock.db.repo.StockLocationLineRepository;
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

  @Inject
  public MassStockMoveServiceImpl(
      MassStockMoveRepository massStockMoveRepository,
      SequenceService sequenceService,
      PickedProductRepository pickedProductRepo,
      StoredProductRepository storedProductRepo,
      StockLocationLineRepository stockLocationLineRepository,
      PickedProductService pickedProductService,
      StoredProductService storedProductService) {
    this.massStockMoveRepository = massStockMoveRepository;
    this.sequenceService = sequenceService;
    this.pickedProductRepo = pickedProductRepo;
    this.storedProductRepo = storedProductRepo;
    this.stockLocationLineRepository = stockLocationLineRepository;
    this.pickedProductService = pickedProductService;
    this.storedProductService = storedProductService;
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
          && line.getCurrentQty().compareTo(BigDecimal.ZERO) == 1) {
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
          && line.getProduct().getTrackingNumberConfiguration() != null) {
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
}
