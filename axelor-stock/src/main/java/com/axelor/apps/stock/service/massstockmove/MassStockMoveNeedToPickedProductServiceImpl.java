/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2005-2025 Axelor (<http://axelor.com>).
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package com.axelor.apps.stock.service.massstockmove;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Product;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.apps.base.service.ProductCompanyService;
import com.axelor.apps.stock.db.MassStockMove;
import com.axelor.apps.stock.db.MassStockMoveNeed;
import com.axelor.apps.stock.db.PickedProduct;
import com.axelor.apps.stock.db.StockLocation;
import com.axelor.apps.stock.db.StockLocationLine;
import com.axelor.apps.stock.db.repo.PickedProductRepository;
import com.axelor.apps.stock.db.repo.StockLocationLineRepository;
import com.axelor.apps.stock.db.repo.StockLocationRepository;
import com.axelor.apps.stock.exception.StockExceptionMessage;
import com.axelor.i18n.I18n;
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
    var commonToStockLocation = massStockMove.getCommonToStockLocation();
    var trackingNumberConfiguration =
        productCompanyService.get(
            product, "trackingNumberConfiguration", massStockMove.getCompany());

    // No tracking number involved
    if (trackingNumberConfiguration == null) {
      generateWithNoTrackingNumber(
          massStockMoveNeed,
          commonFromStockLocation,
          commonToStockLocation,
          product,
          massStockMove);
    }
    // Tracking number involved
    else {
      generateWithTrackingNumber(
          massStockMoveNeed,
          commonFromStockLocation,
          commonToStockLocation,
          product,
          massStockMove);
    }
  }

  @Transactional(rollbackOn = Exception.class)
  protected void generateWithTrackingNumber(
      MassStockMoveNeed massStockMoveNeed,
      StockLocation commonFromStockLocation,
      StockLocation commonToStockLocation,
      Product product,
      MassStockMove massStockMove)
      throws AxelorException {

    List<StockLocationLine> stockLocationLineList = null;
    if (commonFromStockLocation != null) {
      // This case will only fetch from one stock location
      // It will always create ONE picked product with qty to move

      stockLocationLineList =
          stockLocationLineRepository
              .all()
              .filter(
                  "self.product = :product AND self.detailsStockLocation = :stockLocation AND self.currentQty > 0 AND self.detailsStockLocation.company = :company")
              .bind("product", product)
              .bind("stockLocation", commonFromStockLocation)
              .bind("company", massStockMove.getCompany())
              .fetch();

      if (stockLocationLineList == null || stockLocationLineList.isEmpty()) {
        // Case where we will retry to  fetch with a different domain because toStockLocation is
        // filled, so the massStockMoveNeed might have been generated from different stock location
        if (commonToStockLocation != null) {
          stockLocationLineList =
              getStockLocationLinesFromAllStockLocations(
                  commonToStockLocation, product, massStockMove);
        } else {
          throw new AxelorException(
              TraceBackRepository.CATEGORY_INCONSISTENCY,
              I18n.get(
                  StockExceptionMessage.STOCK_MOVE_MASS_COULD_NOT_CREATE_PICKED_PRODUCT_FROM_NEED),
              product.getFullName(),
              massStockMove.getCompany().getName());
        }
      }

    } else {
      // This case will fetch from all stock locations
      // It will create a picked product until qty requirement is met
      stockLocationLineList =
          getStockLocationLinesFromAllStockLocations(commonToStockLocation, product, massStockMove);
    }

    if (stockLocationLineList == null || stockLocationLineList.isEmpty()) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_INCONSISTENCY,
          I18n.get(StockExceptionMessage.STOCK_MOVE_MASS_COULD_NOT_CREATE_PICKED_PRODUCT_FROM_NEED),
          product.getFullName(),
          massStockMove.getCompany().getName());
    }
    createPickedProductsFromDetailStockLocationLineList(
        stockLocationLineList, massStockMove, massStockMoveNeed.getQtyToMove());
  }

  protected List<StockLocationLine> getStockLocationLinesFromAllStockLocations(
      StockLocation commonToStockLocation, Product product, MassStockMove massStockMove) {
    List<StockLocationLine> stockLocationLineList;
    String stockLocationLineDomain =
        "self.product = :product AND self.detailsStockLocation.typeSelect != :virtualType AND self.detailsStockLocation != :cartStockLocation AND self.currentQty > 0 AND self.detailsStockLocation.company = :company";

    stockLocationLineList =
        stockLocationLineRepository
            .all()
            .filter(stockLocationLineDomain)
            .bind("product", product)
            .bind("virtualType", StockLocationRepository.TYPE_VIRTUAL)
            .bind("cartStockLocation", massStockMove.getCartStockLocation())
            .bind("company", massStockMove.getCompany())
            .fetch();
    return stockLocationLineList;
  }

  @Transactional(rollbackOn = Exception.class)
  protected void generateWithNoTrackingNumber(
      MassStockMoveNeed massStockMoveNeed,
      StockLocation commonFromStockLocation,
      StockLocation commonToStockLocation,
      Product product,
      MassStockMove massStockMove)
      throws AxelorException {
    if (commonFromStockLocation != null) {
      // This case will only fetch from one stock location
      // It will always create ONE picked product with qty to move

      var stockLocationLine =
          stockLocationLineRepository
              .all()
              .filter(
                  "self.product = :product AND self.stockLocation = :stockLocation AND self.currentQty > 0 AND self.stockLocation.company = :company")
              .bind("product", product)
              .bind("stockLocation", commonFromStockLocation)
              .bind("company", massStockMove.getCompany())
              .fetchOne();
      if (stockLocationLine != null) {
        pickedProductRepository.save(
            pickedProductService.createPickedProduct(
                massStockMove,
                stockLocationLine.getProduct(),
                stockLocationLine.getStockLocation(),
                massStockMoveNeed.getQtyToMove(),
                null));
      } else {
        // Case where we will retry to create pickedProduct because toStockLocation is filled, so
        // the massStockMoveNeed might have been generated from different stock location
        if (commonToStockLocation != null) {
          createPickedProductFromAllStockLocations(
              massStockMoveNeed, commonToStockLocation, product, massStockMove);
        } else {
          throw new AxelorException(
              TraceBackRepository.CATEGORY_INCONSISTENCY,
              I18n.get(
                  StockExceptionMessage.STOCK_MOVE_MASS_COULD_NOT_CREATE_PICKED_PRODUCT_FROM_NEED),
              product.getFullName(),
              massStockMove.getCompany().getName());
        }
      }
    } else {
      // This case will fetch from all stock locations
      // It will create a picked product until qty requirement is met
      createPickedProductFromAllStockLocations(
          massStockMoveNeed, commonToStockLocation, product, massStockMove);
    }
  }

  protected void createPickedProductFromAllStockLocations(
      MassStockMoveNeed massStockMoveNeed,
      StockLocation commonToStockLocation,
      Product product,
      MassStockMove massStockMove)
      throws AxelorException {
    String stockLocationLineDomain =
        "self.product = :product AND self.stockLocation.typeSelect != :virtualType AND self.stockLocation != :cartStockLocation AND self.currentQty > 0 AND self.stockLocation.company = :company";
    var stockLocationLineList =
        stockLocationLineRepository
            .all()
            .filter(stockLocationLineDomain)
            .bind("product", product)
            .bind("virtualType", StockLocationRepository.TYPE_VIRTUAL)
            .bind("cartStockLocation", massStockMove.getCartStockLocation())
            .bind("company", massStockMove.getCompany())
            .fetch();

    if (stockLocationLineList == null || stockLocationLineList.isEmpty()) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_INCONSISTENCY,
          I18n.get(StockExceptionMessage.STOCK_MOVE_MASS_COULD_NOT_CREATE_PICKED_PRODUCT_FROM_NEED),
          product.getFullName(),
          massStockMove.getCompany().getName());
    }

    createPickedProductsFromStockLocationLineList(
        stockLocationLineList, massStockMove, massStockMoveNeed.getQtyToMove());
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
              maximumPickableQty,
              null);

      fetchQty = fetchQty.add(maximumPickableQty);
      pickedProductRepository.save(createdPickedProduct);
    }
  }

  protected void createPickedProductsFromDetailStockLocationLineList(
      List<StockLocationLine> sllList, MassStockMove massStockMove, BigDecimal qtyRequired) {

    var fetchQty = BigDecimal.ZERO;

    if (sllList != null) {
      for (int i = 0; i < sllList.size() && fetchQty.compareTo(qtyRequired) < 0; i++) {

        var stockLocationLine = sllList.get(i);

        // checking if tracking number for this product already used
        if (massStockMove.getPickedProductList().stream()
            .anyMatch(
                pickedProduct ->
                    isAlreadyStockLocationAlreadyUsed(pickedProduct, stockLocationLine))) {
          continue;
        }

        var maximumPickableQty =
            stockLocationLine.getCurrentQty().min(qtyRequired.subtract(fetchQty));

        var createdPickedProduct =
            pickedProductService.createPickedProduct(
                massStockMove,
                stockLocationLine.getProduct(),
                stockLocationLine.getDetailsStockLocation(),
                maximumPickableQty,
                null);

        createdPickedProduct.setTrackingNumber(stockLocationLine.getTrackingNumber());

        fetchQty = fetchQty.add(maximumPickableQty);
        pickedProductRepository.save(createdPickedProduct);
      }
    }
  }

  protected boolean isAlreadyStockLocationAlreadyUsed(
      PickedProduct pickedProduct, StockLocationLine stockLocationLine) {
    return pickedProduct.getPickedProduct().equals(stockLocationLine.getProduct())
        && pickedProduct.getFromStockLocation().equals(stockLocationLine.getDetailsStockLocation())
        && pickedProduct.getTrackingNumber().equals(stockLocationLine.getTrackingNumber());
  }
}
