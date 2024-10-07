package com.axelor.apps.stock.service.massstockmove;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.Product;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.apps.base.service.ProductCompanyService;
import com.axelor.apps.stock.db.MassStockMove;
import com.axelor.apps.stock.db.PickedProduct;
import com.axelor.apps.stock.db.StockLocation;
import com.axelor.apps.stock.db.StockLocationLine;
import com.axelor.apps.stock.db.StoredProduct;
import com.axelor.apps.stock.db.TrackingNumber;
import com.axelor.apps.stock.db.repo.PickedProductRepository;
import com.axelor.apps.stock.db.repo.StockLocationLineRepository;
import com.axelor.apps.stock.exception.StockExceptionMessage;
import com.axelor.i18n.I18n;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class PickedProductServiceImpl implements PickedProductService {

  protected final StockLocationLineRepository stockLocationLineRepository;
  protected final ProductCompanyService productCompanyService;
  protected final PickedProductRepository pickedProductRepository;

  @Inject
  public PickedProductServiceImpl(
      StockLocationLineRepository stockLocationLineRepository,
      ProductCompanyService productCompanyService,
      PickedProductRepository pickedProductRepository) {
    this.stockLocationLineRepository = stockLocationLineRepository;
    this.productCompanyService = productCompanyService;
    this.pickedProductRepository = pickedProductRepository;
  }

  @Override
  public StoredProduct createFromPickedProduct(PickedProduct pickedProduct) throws AxelorException {

    var storedProduct = new StoredProduct();

    storedProduct.setMassStockMove(pickedProduct.getMassStockMove());
    storedProduct.setStoredProduct(pickedProduct.getPickedProduct());
    storedProduct.setStoredQty(BigDecimal.ZERO);
    storedProduct.setTrackingNumber(pickedProduct.getTrackingNumber());
    storedProduct.setUnit(pickedProduct.getUnit());
    pickedProduct.addStoredProductListItem(storedProduct);
    storedProduct.setToStockLocation(storedProduct.getMassStockMove().getCommonToStockLocation());

    return storedProduct;
  }

  @Override
  public PickedProduct createPickedProduct(
      MassStockMove massStockMove,
      Product product,
      StockLocation stockLocation,
      BigDecimal qty,
      TrackingNumber trackingNumber) {

    var pickedProduct = new PickedProduct();
    massStockMove.addPickedProductListItem(pickedProduct);
    pickedProduct.setPickedProduct(product);
    pickedProduct.setFromStockLocation(stockLocation);
    pickedProduct.setPickedQty(qty);
    pickedProduct.setUnit(product.getUnit());
    pickedProduct.setTrackingNumber(trackingNumber);

    return pickedProduct;
  }

  @Override
  @Transactional(rollbackOn = {Exception.class})
  public List<PickedProduct> generatePickedProductsFromStockLocation(
      MassStockMove massStockMove, StockLocation stockLocation) throws AxelorException {

    Objects.requireNonNull(massStockMove);
    if (stockLocation == null) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_INCONSISTENCY,
          I18n.get(StockExceptionMessage.STOCK_MOVE_MASS_NO_FROM_STOCK_LOCATION_SELECTED));
    }
    var streamStockLocationLines =
        stockLocationLineRepository
            .all()
            .filter("self.stockLocation = :stockLocation")
            .bind("stockLocation", stockLocation)
            .fetchStream();
    var streamDetailsStockLocationLines =
        stockLocationLineRepository
            .all()
            .filter("self.detailsStockLocation = :stockLocation")
            .bind("stockLocation", stockLocation)
            .fetchStream();

    var stockLocationLines =
        Stream.concat(streamStockLocationLines, streamDetailsStockLocationLines)
            .collect(Collectors.toList());

    for (StockLocationLine stockLocationLine : stockLocationLines) {

      // Case where tracking number is required for product, but no tracking number on the stock
      // location line
      // Or no quantity at all
      // Or already added
      if ((productCompanyService.get(
                      stockLocationLine.getProduct(),
                      "trackingNumberConfiguration",
                      stockLocation.getCompany())
                  != null
              && stockLocationLine.getTrackingNumber() == null)
          || stockLocationLine.getCurrentQty().compareTo(BigDecimal.ZERO) <= 0
          || isAlreadyAdded(
              stockLocationLine,
              massStockMove.getPickedProductList(),
              stockLocation.getCompany())) {
        continue;
      }

      var pickedProduct =
          createPickedProduct(
              massStockMove,
              stockLocationLine.getProduct(),
              stockLocation,
              stockLocationLine.getCurrentQty(),
              stockLocationLine.getTrackingNumber());

      pickedProductRepository.save(pickedProduct);
    }

    return massStockMove.getPickedProductList();
  }

  protected boolean isAlreadyAdded(
      StockLocationLine stockLocationLine, List<PickedProduct> pickedProductList, Company company)
      throws AxelorException {

    var product = stockLocationLine.getProduct();
    var trackingNumberConfiguration =
        productCompanyService.get(
            stockLocationLine.getProduct(), "trackingNumberConfiguration", company);
    if (trackingNumberConfiguration != null) {
      return pickedProductList.stream()
          .anyMatch(
              pickedProduct ->
                  pickedProduct.getPickedProduct().equals(product)
                      && pickedProduct
                          .getTrackingNumber()
                          .equals(stockLocationLine.getTrackingNumber())
                      && pickedProduct
                          .getFromStockLocation()
                          .equals(
                              Optional.ofNullable(stockLocationLine.getStockLocation())
                                  .orElse(stockLocationLine.getDetailsStockLocation())));
    } else {
      return pickedProductList.stream()
          .anyMatch(
              pickedProduct ->
                  pickedProduct.getPickedProduct().equals(product)
                      && pickedProduct
                          .getFromStockLocation()
                          .equals(
                              Optional.ofNullable(stockLocationLine.getStockLocation())
                                  .orElse(stockLocationLine.getDetailsStockLocation())));
    }
  }
}
