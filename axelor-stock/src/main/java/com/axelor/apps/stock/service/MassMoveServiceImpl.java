package com.axelor.apps.stock.service;

import java.math.BigDecimal;
import java.util.List;

import javax.swing.JOptionPane;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.repo.SequenceRepository;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.apps.base.service.administration.SequenceService;
import com.axelor.apps.stock.db.MassMove;
import com.axelor.apps.stock.db.PickedProducts;
import com.axelor.apps.stock.db.StockLocation;
import com.axelor.apps.stock.db.StockLocationLine;
import com.axelor.apps.stock.db.repo.MassMoveRepository;
import com.axelor.apps.stock.db.repo.PickedProductsRepository;
import com.axelor.apps.stock.db.repo.StockLocationLineRepository;
import com.axelor.apps.stock.exception.StockExceptionMessage;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;

public class MassMoveServiceImpl implements MassMoveService {

  protected MassMoveRepository massMoveRepository;

  @Inject
  public MassMoveServiceImpl(MassMoveRepository massMoveRepository) {
    this.massMoveRepository = massMoveRepository;
  }

  @Inject protected SequenceService sequenceService;

  @Override
  @Transactional(rollbackOn = Exception.class)
  public void importProductFromStockLocation(MassMove massMove) throws AxelorException {

    try {

      List<StockLocationLine> stockLocationLineList =
          Beans.get(StockLocationLineRepository.class)
              .all()
              .filter("self.stockLocation = ?1", massMove.getCommonFromStockLocation())
              .fetch();
      List<StockLocationLine> detailsStockLocationLineList =
          Beans.get(StockLocationLineRepository.class)
              .all()
              .filter("self.detailsStockLocation =?1", massMove.getCommonFromStockLocation())
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
          pickedProduct.setMassMove(massMove);
          massMove.getPickedProductsList().add(pickedProduct);
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
          pickedProduct.setMassMove(massMove);

          massMove.getPickedProductsList().add(pickedProduct);
          Beans.get(PickedProductsRepository.class).save(pickedProduct);
        }
      }

      Beans.get(MassMoveRepository.class).save(massMove);
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  @Override
  @Transactional(rollbackOn = Exception.class)
  public void setStatusSelectToDraft(MassMove massMove) {
    massMove.setStatusSelect(1);
    Beans.get(MassMoveRepository.class).save(massMove);
  }

  @Override
  @Transactional(rollbackOn = Exception.class)
  public MassMove createMassMoveMobility(
      Integer statusSelect,
      Company company,
      StockLocation cartStockLocation,
      StockLocation commonFromStockLocation,
      StockLocation CommonToStockLocation)
      throws AxelorException {

    MassMove massMove = new MassMove();
    massMove.setSequence(getAndSetSequence(company, massMove));
    massMove.setStatusSelect(statusSelect);
    massMove.setCartStockLocation(cartStockLocation);
    massMove.setCommonFromStockLocation(commonFromStockLocation);
    massMove.setCommonToStockLocation(CommonToStockLocation);
    massMove.setCompany(company);
    massMoveRepository.save(massMove);
    return massMove;
  }

  @Override
  @Transactional(rollbackOn = Exception.class)
  public void updateMassMoveMobility(
      Long id,
      Integer statusSelect,
      Company company,
      StockLocation cartStockLocation,
      StockLocation commonFromStockLocation,
      StockLocation CommonToStockLocation) {

    MassMove massMove = massMoveRepository.find(id);
    massMove.setStatusSelect(statusSelect);
    massMove.setCartStockLocation(cartStockLocation);
    massMove.setCommonFromStockLocation(commonFromStockLocation);
    massMove.setCommonToStockLocation(CommonToStockLocation);
    massMove.setCompany(company);
    massMoveRepository.save(massMove);
  }

  @Override
  @Transactional(rollbackOn = Exception.class)
  public String getAndSetSequence(Company company, MassMove massMoveToSet) throws AxelorException {
    String sequence = massMoveToSet.getSequence();

    String seq;
    if (sequence == null || sequence.isEmpty()) {
      seq =
          sequenceService.getSequenceNumber(
              SequenceRepository.MASS_MOVE, company, MassMove.class, "sequence");
      if (seq == null) {
        throw new AxelorException(
            TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
            I18n.get(StockExceptionMessage.MASS_MOVE_NO_SEQUENCE),
            company.getName());
      }
      massMoveToSet.setSequence(seq);
      massMoveRepository.save(massMoveToSet);
      return seq;
    }
    return sequence;
  }
}
