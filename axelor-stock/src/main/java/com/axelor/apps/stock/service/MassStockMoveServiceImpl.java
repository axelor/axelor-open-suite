package com.axelor.apps.stock.service;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.repo.SequenceRepository;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.apps.base.service.administration.SequenceService;
import com.axelor.apps.stock.db.MassStockMove;
import com.axelor.apps.stock.db.PickedProducts;
import com.axelor.apps.stock.db.StockLocationLine;
import com.axelor.apps.stock.db.repo.MassStockMoveRepository;
import com.axelor.apps.stock.db.repo.PickedProductsRepository;
import com.axelor.apps.stock.db.repo.StockLocationLineRepository;
import com.axelor.apps.stock.exception.StockExceptionMessage;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.math.BigDecimal;
import java.util.List;
import javax.swing.JOptionPane;

public class MassStockMoveServiceImpl implements MassStockMoveService {

  protected MassStockMoveRepository massStockMoveRepository;
  protected SequenceService sequenceService;
  protected PickedProductsRepository pickedProductRepo;

  @Inject
  public MassStockMoveServiceImpl(
      MassStockMoveRepository massStockMoveRepository,
      SequenceService sequenceService,
      PickedProductsRepository pickedProductRepo) {
    this.massStockMoveRepository = massStockMoveRepository;
    this.sequenceService = sequenceService;
    this.pickedProductRepo = pickedProductRepo;
  }

  @Override
  @Transactional(rollbackOn = Exception.class)
  public void importProductFromStockLocation(MassStockMove massStockMove) throws AxelorException {

    try {

      List<StockLocationLine> stockLocationLineList =
          Beans.get(StockLocationLineRepository.class)
              .all()
              .filter("self.stockLocation = ?1", massStockMove.getCommonFromStockLocation())
              .fetch();
      List<StockLocationLine> detailsStockLocationLineList =
          Beans.get(StockLocationLineRepository.class)
              .all()
              .filter("self.detailsStockLocation =?1", massStockMove.getCommonFromStockLocation())
              .fetch();
      if (stockLocationLineList.isEmpty() && detailsStockLocationLineList.isEmpty()) {
        JOptionPane.showMessageDialog(
            null,
            "No stock location lines have been found in the common from stock location.",
            "Warning",
            JOptionPane.WARNING_MESSAGE);
        return;
      }
      for (StockLocationLine line : stockLocationLineList) {
        if (line.getProduct().getTrackingNumberConfiguration() == null
            && line.getCurrentQty().compareTo(BigDecimal.ZERO) == 1) {
          PickedProducts pickedProduct = new PickedProducts();
          pickedProduct.setPickedProduct(line.getProduct());
          pickedProduct.setTrackingNumber(null);
          pickedProduct.setFromStockLocation(line.getStockLocation());
          pickedProduct.setCurrentQty(line.getCurrentQty());
          pickedProduct.setUnit(line.getProduct().getUnit());
          pickedProduct.setMassStockMove(massStockMove);
          massStockMove.getPickedProductsList().add(pickedProduct);
          Beans.get(PickedProductsRepository.class).save(pickedProduct);
        }
      }
      for (StockLocationLine line : detailsStockLocationLineList) {
        if (line.getCurrentQty().compareTo(BigDecimal.ZERO) == 1
            && line.getProduct() != null
            && line.getProduct().getTrackingNumberConfiguration() != null) {
          PickedProducts pickedProduct = new PickedProducts();
          pickedProduct.setPickedProduct(line.getProduct());
          pickedProduct.setTrackingNumber(line.getTrackingNumber());
          pickedProduct.setFromStockLocation(line.getDetailsStockLocation());
          pickedProduct.setCurrentQty(line.getCurrentQty());
          pickedProduct.setUnit(line.getProduct().getUnit());
          pickedProduct.setMassStockMove(massStockMove);

          massStockMove.getPickedProductsList().add(pickedProduct);
          pickedProductRepo.save(pickedProduct);
        }
      }

      massStockMoveRepository.save(massStockMove);
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  @Override
  @Transactional(rollbackOn = Exception.class)
  public void setStatusSelectToDraft(MassStockMove massStockMove) {
    massStockMove.setStatusSelect(1);
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
