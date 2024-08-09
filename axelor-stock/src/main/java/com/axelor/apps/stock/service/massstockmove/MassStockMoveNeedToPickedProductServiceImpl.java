package com.axelor.apps.stock.service.massstockmove;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Product;
import com.axelor.apps.base.service.ProductCompanyService;
import com.axelor.apps.stock.db.MassStockMove;
import com.axelor.apps.stock.db.MassStockMoveNeed;
import com.axelor.apps.stock.db.StockLocation;
import com.axelor.apps.stock.db.StockLocationLine;
import com.axelor.apps.stock.db.repo.PickedProductRepository;
import com.axelor.apps.stock.db.repo.StockLocationLineRepository;
import com.axelor.apps.stock.db.repo.StockLocationRepository;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;

public class MassStockMoveNeedToPickedProductServiceImpl
    implements MassStockMoveNeedToPickedProductService {

  protected final ProductCompanyService productCompanyService;
  protected final StockLocationLineRepository stockLocationLineRepository;
  protected final PickedProductService pickedProductService;
  protected final PickedProductRepository pickedProductRepository;

  @Inject
  public MassStockMoveNeedToPickedProductServiceImpl(
      ProductCompanyService productCompanyService,
      StockLocationLineRepository stockLocationLineRepository,
      PickedProductService pickedProductService,
      PickedProductRepository pickedProductRepository) {
    this.productCompanyService = productCompanyService;
    this.stockLocationLineRepository = stockLocationLineRepository;
    this.pickedProductService = pickedProductService;
    this.pickedProductRepository = pickedProductRepository;
  }

  @Transactional(rollbackOn = Exception.class)
  @Override
  public void generatePickedProductsFromMassStockMoveNeedList(
      MassStockMove massStockMove, List<MassStockMoveNeed> massStockMoveNeedList)
      throws AxelorException {

    for (MassStockMoveNeed massStockMoveNeed : massStockMoveNeedList) {
      generatePickedProductFromMassStockMoveNeed(massStockMoveNeed);
    }

    massStockMove.clearMassStockMoveNeedList();
  }

  @Override
  @Transactional(rollbackOn = Exception.class)
  public void generatePickedProductFromMassStockMoveNeed(MassStockMoveNeed massStockMoveNeed)
      throws AxelorException {
    Objects.requireNonNull(massStockMoveNeed);

    var product = massStockMoveNeed.getProductToMove();
    var massStockMove = massStockMoveNeed.getMassStockMove();
    var commonFromStockLocation = massStockMove.getCommonFromStockLocation();
    var trackingNumberConfiguration =
        productCompanyService.get(
            product, "trackingNumberConfiguration", massStockMove.getCompany());

    // No tracking number involved
    if (trackingNumberConfiguration == null) {
      generateWithNoTrackingNumber(
          massStockMoveNeed, commonFromStockLocation, product, massStockMove);
    }
    // Tracking number involved
    else {
      generateWithTrackingNumber(
          massStockMoveNeed, commonFromStockLocation, product, massStockMove);
    }
  }

  @Transactional(rollbackOn = Exception.class)
  protected void generateWithTrackingNumber(
      MassStockMoveNeed massStockMoveNeed,
      StockLocation commonFromStockLocation,
      Product product,
      MassStockMove massStockMove) {
    if (commonFromStockLocation != null) {
      // This case will only fetch from one stock location
      // It will always create ONE picked product with qty to move
      var stockLocationLine =
          stockLocationLineRepository
              .all()
              .filter(
                  "self.product = :product AND self.detailsStockLocation = :stockLocation AND self.currentQty > 0 AND self.stockMove.company = :company")
              .bind("product", product)
              .bind("stockLocation", commonFromStockLocation)
              .bind("company", massStockMove.getCompany())
              .fetchOne();

      pickedProductRepository.save(
          pickedProductService.createPickedProduct(
              massStockMove,
              stockLocationLine.getProduct(),
              stockLocationLine.getDetailsStockLocation(),
              massStockMoveNeed.getQtyToMove()));
    } else {
      // This case will fetch from all stock locations
      // It will create a picked product until qty requirement is met
      var stockLocationLineList =
          stockLocationLineRepository
              .all()
              .filter(
                  "self.product = :product AND self.detailsStockLocation.typeSelect != :virtualType AND self.detailsStockLocation != :cartStockLocation AND self.currentQty > 0 AND self.stockMove.company = :company")
              .bind("product", product)
              .bind("virtualType", StockLocationRepository.TYPE_VIRTUAL)
              .bind("cartStockLocation", massStockMove.getCartStockLocation())
              .bind("company", massStockMove.getCompany())
              .fetch();

      createPickedProductsFromDetailStockLocationLineList(
          stockLocationLineList, massStockMove, massStockMoveNeed.getQtyToMove());
    }
  }

  @Transactional(rollbackOn = Exception.class)
  protected void generateWithNoTrackingNumber(
      MassStockMoveNeed massStockMoveNeed,
      StockLocation commonFromStockLocation,
      Product product,
      MassStockMove massStockMove) {
    if (commonFromStockLocation != null) {
      // This case will only fetch from one stock location
      // It will always create ONE picked product with qty to move
      var stockLocationLine =
          stockLocationLineRepository
              .all()
              .filter(
                  "self.product = :product AND self.stockLocation = :stockLocation AND self.currentQty > 0 AND self.stockMove.company = :company")
              .bind("product", product)
              .bind("stockLocation", commonFromStockLocation)
              .bind("company", massStockMove.getCompany())
              .fetchOne();

      pickedProductRepository.save(
          pickedProductService.createPickedProduct(
              massStockMove,
              stockLocationLine.getProduct(),
              stockLocationLine.getStockLocation(),
              massStockMoveNeed.getQtyToMove()));
    } else {
      // This case will fetch from all stock locations
      // It will create a picked product until qty requirement is met
      var stockLocationLineList =
          stockLocationLineRepository
              .all()
              .filter(
                  "self.product = :product AND self.stockLocation.typeSelect != :virtualType AND self.stockLocation != :cartStockLocation AND self.currentQty > 0 AND self.stockMove.company = :company")
              .bind("product", product)
              .bind("virtualType", StockLocationRepository.TYPE_VIRTUAL)
              .bind("cartStockLocation", massStockMove.getCartStockLocation())
              .bind("company", massStockMove.getCompany())
              .fetch();

      createPickedProductsFromStockLocationLineList(
          stockLocationLineList, massStockMove, massStockMoveNeed.getQtyToMove());
    }
  }

  protected void createPickedProductsFromStockLocationLineList(
      List<StockLocationLine> sllList, MassStockMove massStockMove, BigDecimal qtyRequired) {

    var fetchQty = BigDecimal.ZERO;

    for (int i = 0; i < sllList.size() && fetchQty.compareTo(qtyRequired) < 0; i++) {
      var stockLocationLine = sllList.get(i);
      var maximumPickableQty =
          stockLocationLine.getCurrentQty().min(qtyRequired.subtract(fetchQty));

      var createdPickedProduct =
          pickedProductService.createPickedProduct(
              massStockMove,
              stockLocationLine.getProduct(),
              stockLocationLine.getStockLocation(),
              maximumPickableQty);

      fetchQty = fetchQty.add(maximumPickableQty);
      pickedProductRepository.save(createdPickedProduct);
    }
  }

  protected void createPickedProductsFromDetailStockLocationLineList(
      List<StockLocationLine> sllList, MassStockMove massStockMove, BigDecimal qtyRequired) {

    var fetchQty = BigDecimal.ZERO;

    for (int i = 0; i < sllList.size() && fetchQty.compareTo(qtyRequired) < 0; i++) {
      var stockLocationLine = sllList.get(i);
      var maximumPickableQty =
          stockLocationLine.getCurrentQty().min(qtyRequired.subtract(fetchQty));

      var createdPickedProduct =
          pickedProductService.createPickedProduct(
              massStockMove,
              stockLocationLine.getProduct(),
              stockLocationLine.getDetailsStockLocation(),
              maximumPickableQty);

      createdPickedProduct.setTrackingNumber(stockLocationLine.getTrackingNumber());

      fetchQty = fetchQty.add(maximumPickableQty);
      pickedProductRepository.save(createdPickedProduct);
    }
  }
}
