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
package com.axelor.apps.supplychain.service.packaging;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Product;
import com.axelor.apps.base.db.repo.ProductRepository;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.apps.stock.db.LogisticalForm;
import com.axelor.apps.stock.db.StockMove;
import com.axelor.apps.stock.db.StockMoveLine;
import com.axelor.apps.supplychain.db.Packaging;
import com.axelor.apps.supplychain.db.PackagingLine;
import com.axelor.apps.supplychain.exception.SupplychainExceptionMessage;
import com.axelor.apps.supplychain.service.saleorder.packaging.SaleOrderPackagingDimensionService;
import com.axelor.db.JPA;
import com.axelor.i18n.I18n;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import org.apache.commons.collections.CollectionUtils;

public class PackagingStockMoveLineServiceImpl implements PackagingStockMoveLineService {

  protected SaleOrderPackagingDimensionService saleOrderPackagingDimensionService;

  @Inject
  public PackagingStockMoveLineServiceImpl(
      SaleOrderPackagingDimensionService saleOrderPackagingDimensionService) {
    this.saleOrderPackagingDimensionService = saleOrderPackagingDimensionService;
  }

  @Override
  public void updateQtyRemainingToPackage(LogisticalForm logisticalForm) throws AxelorException {
    List<Packaging> packagingList = logisticalForm.getPackagingList();
    if (CollectionUtils.isEmpty(packagingList)) {
      return;
    }
    List<PackagingLine> allPackagingLines = new ArrayList<>();
    for (Packaging packaging : packagingList) {
      getPackagingLineList(packaging, allPackagingLines);
    }
    updateQtyRemainingToPackage(allPackagingLines);
  }

  protected void getPackagingLineList(Packaging packaging, List<PackagingLine> packagingLineList) {
    if (packaging == null) {
      return;
    }
    if (CollectionUtils.isNotEmpty(packaging.getPackagingLineList())) {
      packagingLineList.addAll(packaging.getPackagingLineList());
    }
    if (CollectionUtils.isNotEmpty(packaging.getChildrenPackagingList())) {
      for (Packaging childPackaging : packaging.getChildrenPackagingList()) {
        getPackagingLineList(childPackaging, packagingLineList);
      }
    }
  }

  @Transactional(rollbackOn = Exception.class)
  protected void updateQtyRemainingToPackage(List<PackagingLine> packagingLineList)
      throws AxelorException {
    if (CollectionUtils.isEmpty(packagingLineList)) {
      return;
    }
    Map<StockMoveLine, BigDecimal> map =
        packagingLineList.stream()
            .filter(line -> line.getStockMoveLine() != null)
            .collect(
                Collectors.groupingBy(
                    PackagingLine::getStockMoveLine,
                    Collectors.mapping(
                        PackagingLine::getQty,
                        Collectors.reducing(BigDecimal.ZERO, BigDecimal::add))));

    for (Map.Entry<StockMoveLine, BigDecimal> entry : map.entrySet()) {
      StockMoveLine stockMoveLine = entry.getKey();
      BigDecimal qtyPacked = entry.getValue();

      BigDecimal qtyRemaining =
          stockMoveLine.getRealQty().subtract(qtyPacked).setScale(3, RoundingMode.HALF_UP);

      if (qtyRemaining.signum() < 0) {
        throw new AxelorException(
            TraceBackRepository.CATEGORY_INCONSISTENCY,
            I18n.get(SupplychainExceptionMessage.INVALID_PACKAGING_LINE_QTY));
      }
      stockMoveLine.setQtyRemainingToPackage(qtyRemaining);
    }
  }

  @Override
  public String validateAndUpdateStockMoveList(
      LogisticalForm savedLogisticalForm, LogisticalForm logisticalForm) throws AxelorException {
    if (savedLogisticalForm == null) {
      List<StockMove> stockMoveList = logisticalForm.getStockMoveList();
      if (CollectionUtils.isNotEmpty(stockMoveList)) {
        for (StockMove stockMove : stockMoveList) {
          attachExistingPackagingsToLogisticalForm(logisticalForm, stockMove);
        }
      }
      return "";
    }

    List<StockMove> oldStockMoveList =
        Optional.ofNullable(savedLogisticalForm.getStockMoveList()).orElse(Collections.emptyList());
    List<StockMove> newStockMoveList =
        Optional.ofNullable(logisticalForm.getStockMoveList()).orElse(Collections.emptyList());

    Optional<StockMove> stockMoveToRemove =
        oldStockMoveList.stream()
            .filter(
                stockMove ->
                    newStockMoveList.stream()
                        .noneMatch(newStockMove -> newStockMove.getId().equals(stockMove.getId())))
            .findFirst();
    if (stockMoveToRemove.isPresent()) {
      String error =
          detachPackagingsFromLogisticalForm(savedLogisticalForm, stockMoveToRemove.get());
      logisticalForm.removeStockMoveListItem(stockMoveToRemove.get());
      return error;
    }

    Optional<StockMove> stockMoveToAdd =
        newStockMoveList.stream()
            .filter(
                stockMove ->
                    oldStockMoveList.stream()
                        .noneMatch(oldStockMove -> oldStockMove.getId().equals(stockMove.getId())))
            .findFirst();
    if (stockMoveToAdd.isPresent()) {
      attachExistingPackagingsToLogisticalForm(savedLogisticalForm, stockMoveToAdd.get());
      savedLogisticalForm.addStockMoveListItem(stockMoveToAdd.get());
    }
    return "";
  }

  protected void attachExistingPackagingsToLogisticalForm(
      LogisticalForm logisticalForm, StockMove stockMove) {
    List<Packaging> packagings =
        JPA.all(Packaging.class)
            .filter(
                ":stockMove MEMBER OF self.stockMoveSet AND self.logisticalForm IS NULL AND self.parentPackaging IS NULL")
            .bind("stockMove", stockMove)
            .fetch();
    if (CollectionUtils.isEmpty(packagings)) {
      return;
    }
    for (Packaging packaging : packagings) {
      logisticalForm.addPackagingListItem(packaging);
    }
  }

  protected String detachPackagingsFromLogisticalForm(
      LogisticalForm logisticalForm, StockMove stockMove) throws AxelorException {
    List<Packaging> packagingList = logisticalForm.getPackagingList();
    if (CollectionUtils.isEmpty(packagingList)) {
      return "";
    }
    for (Packaging packaging : packagingList) {
      String error = checkPackagingForRemoval(packaging, stockMove);
      if (!error.isEmpty()) {
        return error;
      }
    }
    packagingList.removeAll(
        packagingList.stream()
            .filter(p -> p.getStockMoveSet().contains(stockMove))
            .collect(Collectors.toList()));
    return "";
  }

  protected String checkPackagingForRemoval(Packaging packaging, StockMove stockMove) {
    if (packaging == null) {
      return "";
    }
    if (packaging.getStockMoveSet().contains(stockMove)) {
      boolean hasAnotherStockMove =
          packaging.getPackagingLineList().stream()
              .anyMatch(
                  line ->
                      line.getStockMoveLine() != null
                          && line.getStockMoveLine().getStockMove() != null
                          && !line.getStockMoveLine().getStockMove().equals(stockMove));

      if (hasAnotherStockMove) {
        return String.format(
            I18n.get(SupplychainExceptionMessage.STOCK_MOVE_REMOVAL_NOT_ALLOWED),
            packaging.getPackagingNumber());
      }
    }
    if (CollectionUtils.isNotEmpty(packaging.getChildrenPackagingList())) {
      for (Packaging childPackaging : packaging.getChildrenPackagingList()) {
        String error = checkPackagingForRemoval(childPackaging, stockMove);
        if (!error.isEmpty()) {
          return error;
        }
      }
    }
    return "";
  }

  @Override
  public void updateStockMovePackagingInfo(LogisticalForm logisticalForm) throws AxelorException {
    List<Packaging> packagingList = logisticalForm.getPackagingList();
    if (CollectionUtils.isEmpty(packagingList)) {
      return;
    }
    Set<Packaging> allPackagings = new HashSet<>();
    for (Packaging packaging : packagingList) {
      getAllPackagingList(packaging, allPackagings);
    }

    Set<PackagingLine> allPackagingLines = new HashSet<>();
    for (Packaging packaging : allPackagings) {
      getAllPackagingLineList(packaging, allPackagingLines);
    }
    Set<StockMove> allStockMoves = new HashSet<>();
    for (Packaging packaging : allPackagings) {
      if (CollectionUtils.isNotEmpty(packaging.getStockMoveSet())) {
        allStockMoves.addAll(packaging.getStockMoveSet());
      }
    }
    for (StockMove stockMove : allStockMoves) {
      updateStockMovePackagingInfo(stockMove, allPackagings, allPackagingLines);
    }
  }

  protected void getAllPackagingList(Packaging packaging, Set<Packaging> packagingSet) {
    if (packaging == null || packagingSet.contains(packaging)) {
      return;
    }
    packagingSet.add(packaging);
    List<Packaging> childrenPackagingList = packaging.getChildrenPackagingList();
    if (CollectionUtils.isNotEmpty(childrenPackagingList)) {
      for (Packaging childrenPackaging : childrenPackagingList) {
        getAllPackagingList(childrenPackaging, packagingSet);
      }
    }
  }

  protected void getAllPackagingLineList(Packaging packaging, Set<PackagingLine> packagingLineSet) {
    if (packaging == null) {
      return;
    }
    if (CollectionUtils.isNotEmpty(packaging.getPackagingLineList())) {
      packagingLineSet.addAll(packaging.getPackagingLineList());
    }
    List<Packaging> childrenPackagingList = packaging.getChildrenPackagingList();
    if (CollectionUtils.isNotEmpty(childrenPackagingList)) {
      for (Packaging childrenPackaging : childrenPackagingList) {
        getAllPackagingLineList(childrenPackaging, packagingLineSet);
      }
    }
  }

  protected void updateStockMovePackagingInfo(
      StockMove stockMove, Set<Packaging> packagingSet, Set<PackagingLine> packagingLineSet)
      throws AxelorException {
    int numOfPackages = 0;
    int numOfPalettes = 0;
    BigDecimal grossMass = BigDecimal.ZERO;

    for (Packaging packaging : packagingSet) {
      if (CollectionUtils.isNotEmpty(packaging.getStockMoveSet())
          && packaging.getStockMoveSet().contains(stockMove)) {

        int packagingLevelSelect = packaging.getPackagingLevelSelect();
        if (packagingLevelSelect == ProductRepository.PACKAGING_LEVEL_BOX) {
          numOfPackages++;
        } else if (packagingLevelSelect == ProductRepository.PACKAGING_LEVEL_PALLET) {
          numOfPalettes++;
        }
        grossMass = grossMass.add(packaging.getGrossMass());
      }
    }
    for (PackagingLine packagingLine : packagingLineSet) {
      StockMoveLine stockMoveLine = packagingLine.getStockMoveLine();
      if (stockMoveLine != null && stockMove.equals(stockMoveLine.getStockMove())) {

        Product product = stockMoveLine.getProduct();
        if (product != null) {
          BigDecimal productMass =
              saleOrderPackagingDimensionService
                  .getConvertedWeight(product.getGrossMass(), product)
                  .multiply(packagingLine.getQty())
                  .setScale(3, RoundingMode.HALF_UP);
          grossMass = grossMass.add(productMass);
        }
      }
    }
    stockMove.setNumOfPackages(numOfPackages);
    stockMove.setNumOfPalettes(numOfPalettes);
    stockMove.setGrossMass(grossMass);
  }
}
