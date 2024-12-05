package com.axelor.apps.production.service;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Product;
import com.axelor.apps.base.db.repo.ProductRepository;
import com.axelor.apps.production.db.BillOfMaterial;
import com.axelor.apps.production.db.BillOfMaterialLine;
import com.axelor.apps.production.db.repo.BillOfMaterialLineRepository;
import com.axelor.apps.production.db.repo.BillOfMaterialRepository;
import com.axelor.apps.sale.db.SaleOrderLine;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.lang.invoke.MethodHandles;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SolBomUpdateServiceImpl implements SolBomUpdateService {

  protected final SaleOrderLineBomLineMappingService saleOrderLineBomLineMappingService;
  protected final BomLineCreationService bomLineCreationService;
  protected final BillOfMaterialRepository billOfMaterialRepository;
  protected final BillOfMaterialLineRepository billOfMaterialLineRepository;
  protected final SaleOrderBomRemoveLineService saleOrderBomRemoveLineService;
  private static final Logger logger =
      LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  @Inject
  public SolBomUpdateServiceImpl(
      SaleOrderLineBomLineMappingService saleOrderLineBomLineMappingService,
      BomLineCreationService bomLineCreationService,
      BillOfMaterialRepository billOfMaterialRepository,
      BillOfMaterialLineRepository billOfMaterialLineRepository,
      SaleOrderBomRemoveLineService saleOrderBomRemoveLineService) {
    this.saleOrderLineBomLineMappingService = saleOrderLineBomLineMappingService;
    this.bomLineCreationService = bomLineCreationService;
    this.billOfMaterialRepository = billOfMaterialRepository;
    this.billOfMaterialLineRepository = billOfMaterialLineRepository;
    this.saleOrderBomRemoveLineService = saleOrderBomRemoveLineService;
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

    List<BillOfMaterialLine> saleOrderLineBomLineList =
        saleOrderLine.getSubSaleOrderLineList().stream()
            .map(SaleOrderLine::getBillOfMaterialLine)
            .filter(Objects::nonNull)
            .collect(Collectors.toList());
    saleOrderBomRemoveLineService.removeBomLines(
        saleOrderLineBomLineList, bom, ProductRepository.PRODUCT_SUB_TYPE_SEMI_FINISHED_PRODUCT);
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
    bomLine.setBillOfMaterial(subSaleOrderLine.getBillOfMaterial());
    bomLine.setPriority(subSaleOrderLine.getSequence() * 10);
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

    return nbBomLinesAccountable == subSaleOrderLineListSize
        && Optional.ofNullable(saleOrderLine.getSubSaleOrderLineList()).orElse(List.of()).stream()
            .filter(
                subSaleOrderLine ->
                    subSaleOrderLine
                        .getProduct()
                        .getProductSubTypeSelect()
                        .equals(ProductRepository.PRODUCT_SUB_TYPE_SEMI_FINISHED_PRODUCT))
            .allMatch(saleOrderLineBomLineMappingService::isSyncWithBomLine);
  }
}
