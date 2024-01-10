package com.axelor.apps.stock.service;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Product;
import com.axelor.apps.base.db.Unit;
import com.axelor.apps.stock.db.MassStockMove;
import com.axelor.apps.stock.db.PickedProduct;
import com.axelor.apps.stock.db.StockLocation;
import com.axelor.apps.stock.db.StockMove;
import com.axelor.apps.stock.db.StockMoveLine;
import com.axelor.apps.stock.db.StoredProduct;
import com.axelor.apps.stock.db.TrackingNumber;
import com.axelor.apps.stock.db.repo.MassStockMoveRepository;
import com.axelor.apps.stock.db.repo.StockMoveLineRepository;
import com.axelor.apps.stock.db.repo.StockMoveRepository;
import com.axelor.apps.stock.db.repo.StoredProductRepository;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.math.BigDecimal;
import java.time.LocalDate;

public class StoredProductServiceImpl implements StoredProductService {

  protected StoredProductRepository storedProductRepository;
  protected MassStockMoveRepository massStockMoveRepository;
  protected StockMoveRepository stockMoveRepository;
  protected StockMoveLineRepository stockMoveLineRepository;
  protected StockMoveService stockMoveService;
  protected StockMoveLineService stockMoveLineService;

  @Inject
  public StoredProductServiceImpl(
      StoredProductRepository storedProductRepository,
      MassStockMoveRepository massStockMoveRepository,
      StockMoveRepository stockMoveRepository,
      StockMoveLineRepository stockMoveLineRepository,
      StockMoveService stockMoveService,
      StockMoveLineService stockMoveLineService) {
    this.storedProductRepository = storedProductRepository;
    this.massStockMoveRepository = massStockMoveRepository;
    this.stockMoveRepository = stockMoveRepository;
    this.stockMoveLineRepository = stockMoveLineRepository;
    this.stockMoveService = stockMoveService;
    this.stockMoveLineService = stockMoveLineService;
  }

  @Override
  public StoredProduct createStoredProduct(
      Product product,
      TrackingNumber trackingNumber,
      BigDecimal currentQty,
      StockLocation toStockLocation,
      BigDecimal storedQty,
      StockMoveLine stockMoveLine,
      MassStockMove massStockMove,
      PickedProduct pickedProduct,
      Unit unit) {
    StoredProduct storedProduct = new StoredProduct();
    storedProduct.setStoredProduct(product);
    storedProduct.setTrackingNumber(trackingNumber);
    storedProduct.setCurrentQty(currentQty);
    storedProduct.setUnit(product.getUnit());
    storedProduct.setToStockLocation(toStockLocation);
    storedProduct.setStoredQty(storedQty);
    storedProduct.setStockMoveLine(stockMoveLine);
    storedProduct.setMassStockMove(massStockMove);
    storedProduct.setUnit(unit);
    return storedProduct;
  }

  @Transactional(rollbackOn = {Exception.class})
  protected void createStockMoveForStoredProduct(StoredProduct storedProduct)
      throws AxelorException {
    StockLocation toStockLocation = storedProduct.getToStockLocation();
    StockLocation fromStockLocation = storedProduct.getMassStockMove().getCartStockLocation();
    BigDecimal qty = storedProduct.getStoredQty();
    StockMove stockMove =
        stockMoveService.createStockMove(
            storedProduct.getMassStockMove().getCartStockLocation().getAddress(),
            storedProduct.getToStockLocation().getAddress(),
            storedProduct.getMassStockMove().getCompany(),
            fromStockLocation,
            toStockLocation,
            LocalDate.now(),
            LocalDate.now(),
            null,
            StockMoveRepository.TYPE_INTERNAL);
    StockMoveLine stockMoveLine =
        stockMoveLineService.createStockMoveLine(
            stockMove,
            storedProduct.getStoredProduct(),
            storedProduct.getTrackingNumber(),
            qty,
            qty,
            storedProduct.getUnit(),
            null,
            fromStockLocation,
            toStockLocation);
    stockMove.setMassStockMove(storedProduct.getMassStockMove());
    stockMove.setOrigin(storedProduct.getMassStockMove().getSequence());
    stockMoveService.plan(stockMove);
    stockMoveService.realize(stockMove);
    storedProduct.setStockMoveLine(stockMoveLine);
    storedProductRepository.save(storedProduct);
  }
}
