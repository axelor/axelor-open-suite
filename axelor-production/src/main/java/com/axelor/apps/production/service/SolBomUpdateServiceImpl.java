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
package com.axelor.apps.production.service;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Product;
import com.axelor.apps.base.db.repo.ProductRepository;
import com.axelor.apps.production.db.BillOfMaterial;
import com.axelor.apps.production.db.BillOfMaterialLine;
import com.axelor.apps.production.db.repo.BillOfMaterialLineRepository;
import com.axelor.apps.production.db.repo.BillOfMaterialRepository;
import com.axelor.apps.sale.db.SaleOrderLine;
import com.axelor.apps.sale.db.repo.SaleOrderLineRepository;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.lang.invoke.MethodHandles;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SolBomUpdateServiceImpl implements SolBomUpdateService {

  protected final SaleOrderLineBomLineMappingService saleOrderLineBomLineMappingService;
  protected final BomLineCreationService bomLineCreationService;
  protected final BillOfMaterialRepository billOfMaterialRepository;
  protected final BillOfMaterialLineRepository billOfMaterialLineRepository;
  private static final Logger logger =
      LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  @Inject
  public SolBomUpdateServiceImpl(
      SaleOrderLineBomLineMappingService saleOrderLineBomLineMappingService,
      BomLineCreationService bomLineCreationService,
      BillOfMaterialRepository billOfMaterialRepository,
      BillOfMaterialLineRepository billOfMaterialLineRepository) {
    this.saleOrderLineBomLineMappingService = saleOrderLineBomLineMappingService;
    this.bomLineCreationService = bomLineCreationService;
    this.billOfMaterialRepository = billOfMaterialRepository;
    this.billOfMaterialLineRepository = billOfMaterialLineRepository;
  }

  @Override
  @Transactional(rollbackOn = Exception.class)
  public void updateSolWithBillOfMaterial(SaleOrderLine saleOrderLine) throws AxelorException {
    // Easiest cases where a line has been added or modified.
    logger.debug("Updating {}", saleOrderLine);
    var bom = saleOrderLine.getBillOfMaterial();
    if (saleOrderLine.getSubSaleOrderLineList() != null) {
      List<SaleOrderLine> subSaleOrderLineList =
          saleOrderLine.getSubSaleOrderLineList().stream()
              .filter(line -> line.getProduct().getProductSubTypeSelect() != null)
              .collect(Collectors.toList());
      for (SaleOrderLine subSaleOrderLine : subSaleOrderLineList) {
        updateBomLineSol(subSaleOrderLine, bom);
      }
    }
    logger.debug("Updated saleOrderLine {} with bom {}", saleOrderLine, bom);
  }

  protected void updateBomLineSol(SaleOrderLine subSaleOrderLine, BillOfMaterial bom) {
    if (subSaleOrderLine
            .getProduct()
            .getProductSubTypeSelect()
            .equals(ProductRepository.PRODUCT_SUB_TYPE_SEMI_FINISHED_PRODUCT)
        && !saleOrderLineBomLineMappingService.isSyncWithBomLine(subSaleOrderLine)) {
      var bomLine = subSaleOrderLine.getBillOfMaterialLine();
      // Updating the existing one
      if (bomLine != null) {
        updateBomLine(subSaleOrderLine, bomLine, bom);
      }
      // Creating a new one
      else {
        createBomLine(subSaleOrderLine, bom);
      }
    }
  }

  protected void updateBomLine(
      SaleOrderLine subSaleOrderLine, BillOfMaterialLine bomLine, BillOfMaterial bom) {
    logger.debug("Updating bomLine {} with subSaleOrderLine {}", bomLine, subSaleOrderLine);
    bomLine.setQty(subSaleOrderLine.getQty());
    bomLine.setProduct(subSaleOrderLine.getProduct());
    bomLine.setUnit(subSaleOrderLine.getUnit());
    bomLine.setPriority(subSaleOrderLine.getSequence() * 10);

    int saleSupplySelect = subSaleOrderLine.getSaleSupplySelect();

    if (saleSupplySelect != SaleOrderLineRepository.SALE_SUPPLY_PRODUCE) {
      bomLine.setBillOfMaterial(null);
    } else {
      bomLine.setBillOfMaterial(subSaleOrderLine.getBillOfMaterial());
    }
    bom.addBillOfMaterialLineListItem(bomLine);
  }

  protected void createBomLine(SaleOrderLine subSaleOrderLine, BillOfMaterial bom) {
    BillOfMaterialLine bomLine;
    logger.debug(
        "Creating bomLine from subSaleOrderLine {} and adding it to bom {}", subSaleOrderLine, bom);
    bomLine = bomLineCreationService.createBomLineFromSol(subSaleOrderLine);
    logger.debug("Created bomLine {}", bomLine);
    bom.addBillOfMaterialLineListItem(bomLine);
    subSaleOrderLine.setBillOfMaterialLine(bomLine);
    billOfMaterialLineRepository.save(bomLine);
  }

  @Override
  public boolean isUpdated(SaleOrderLine saleOrderLine) {

    if (saleOrderLine.getBillOfMaterial() == null) {
      return true;
    }

    // Checking if subBomLines are all in bom.bomLineList
    // If it's not the case then there were changes
    var containsAllSubBomLines =
        Optional.ofNullable(saleOrderLine.getSubSaleOrderLineList()).orElse(List.of()).stream()
            .filter(
                line ->
                    line.getSaleSupplySelect() == SaleOrderLineRepository.SALE_SUPPLY_PRODUCE
                        || line.getSaleSupplySelect()
                            == SaleOrderLineRepository.SALE_SUPPLY_FROM_STOCK_AND_PRODUCE)
            .map(SaleOrderLine::getBillOfMaterialLine)
            .allMatch(
                subBomLine ->
                    saleOrderLine
                        .getBillOfMaterial()
                        .getBillOfMaterialLineList()
                        .contains(subBomLine));

    var nbBomLinesAccountable =
        saleOrderLine.getBillOfMaterial().getBillOfMaterialLineList().stream()
            .map(BillOfMaterialLine::getProduct)
            .map(Product::getProductSubTypeSelect)
            .filter(type -> type.equals(ProductRepository.PRODUCT_SUB_TYPE_SEMI_FINISHED_PRODUCT))
            .count();

    var subSaleOrderLineListSize =
        Optional.ofNullable(saleOrderLine.getSubSaleOrderLineList()).orElse(List.of()).stream()
            .map(SaleOrderLine::getProduct)
            .map(Product::getProductSubTypeSelect)
            .filter(type -> type.equals(ProductRepository.PRODUCT_SUB_TYPE_SEMI_FINISHED_PRODUCT))
            .count();

    var bomLineWithBom =
        saleOrderLine.getBillOfMaterial().getBillOfMaterialLineList().stream()
            .filter(line -> line.getBillOfMaterial() != null)
            .count();
    var subLineWithProduceSaleSupply =
        Optional.ofNullable(saleOrderLine.getSubSaleOrderLineList()).orElse(List.of()).stream()
            .filter(
                line ->
                    line.getSaleSupplySelect() == SaleOrderLineRepository.SALE_SUPPLY_PRODUCE
                        || line.getSaleSupplySelect()
                            == SaleOrderLineRepository.SALE_SUPPLY_FROM_STOCK_AND_PRODUCE)
            .count();

    return nbBomLinesAccountable == subSaleOrderLineListSize
        && Optional.ofNullable(saleOrderLine.getSubSaleOrderLineList()).orElse(List.of()).stream()
            .filter(
                subSaleOrderLine ->
                    subSaleOrderLine != null
                        && subSaleOrderLine
                            .getProduct()
                            .getProductSubTypeSelect()
                            .equals(ProductRepository.PRODUCT_SUB_TYPE_SEMI_FINISHED_PRODUCT))
            .allMatch(saleOrderLineBomLineMappingService::isSyncWithBomLine)
        && bomLineWithBom == subLineWithProduceSaleSupply
        && containsAllSubBomLines;
  }
}
