package com.axelor.apps.production.service;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.repo.ProductRepository;
import com.axelor.apps.production.db.BillOfMaterialLine;
import com.axelor.apps.production.db.SaleOrderLineDetails;
import com.axelor.apps.production.db.repo.BillOfMaterialLineRepository;
import com.axelor.apps.production.db.repo.BillOfMaterialRepository;
import com.axelor.apps.production.db.repo.SaleOrderLineDetailsRepository;
import com.axelor.apps.sale.db.SaleOrderLine;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.lang.invoke.MethodHandles;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SolDetailsBomUpdateServiceImpl implements SolDetailsBomUpdateService {

  protected final BillOfMaterialRepository billOfMaterialRepository;
  protected final BillOfMaterialLineRepository billOfMaterialLineRepository;
  protected final BomLineCreationService bomLineCreationService;
  private static final Logger logger =
      LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  @Inject
  public SolDetailsBomUpdateServiceImpl(
      BillOfMaterialRepository billOfMaterialRepository,
      BillOfMaterialLineRepository billOfMaterialLineRepository,
      BomLineCreationService bomLineCreationService) {
    this.billOfMaterialRepository = billOfMaterialRepository;
    this.billOfMaterialLineRepository = billOfMaterialLineRepository;
    this.bomLineCreationService = bomLineCreationService;
  }

  @Override
  @Transactional(rollbackOn = Exception.class)
  public void updateSolDetailslWithBillOfMaterial(SaleOrderLine saleOrderLine)
      throws AxelorException {
    // Easiest cases where a line has been added or modified.
    logger.debug("Updating {}", saleOrderLine);

    var bom = saleOrderLine.getBillOfMaterial();
    List<SaleOrderLineDetails> saleOrderLineDetailsList =
        saleOrderLine.getSaleOrderLineDetailsList().stream()
            .filter(line -> line.getTypeSelect() == SaleOrderLineDetailsRepository.TYPE_COMPONENT)
            .collect(Collectors.toList());
    if (CollectionUtils.isNotEmpty(saleOrderLineDetailsList)) {
      for (SaleOrderLineDetails saleOrderLineDetails : saleOrderLineDetailsList) {
        if (!this.isSolDetailsSyncWithBomLine(saleOrderLineDetails)) {
          var bomLine = saleOrderLineDetails.getBillOfMaterialLine();
          // Updating the existing one
          if (bomLine != null) {
            logger.debug("Updating bomLine {} with sol details {}", bomLine, saleOrderLineDetails);
            bomLine.setQty(saleOrderLineDetails.getQty());
            bomLine.setProduct(saleOrderLineDetails.getProduct());
            bomLine.setUnit(saleOrderLineDetails.getUnit());
            bom.addBillOfMaterialLineListItem(bomLine);
          }
          // Creating a new one
          else {
            logger.debug(
                "Creating bomLine from sol details {} and adding it to bom {}",
                saleOrderLineDetails,
                bom);
            bomLine = bomLineCreationService.createBomLineFromSolDetails(saleOrderLineDetails);
            logger.debug("Created bomLine {}", bomLine);
            bom.addBillOfMaterialLineListItem(bomLine);
            saleOrderLineDetails.setBillOfMaterialLine(bomLine);
            billOfMaterialLineRepository.save(bomLine);
          }
        }
      }
    }

    var bomLines =
        bom.getBillOfMaterialLineList().stream()
            .filter(
                bomLine ->
                    bomLine
                        .getProduct()
                        .getProductSubTypeSelect()
                        .equals(ProductRepository.PRODUCT_SUB_TYPE_COMPONENT))
            .collect(Collectors.toList());
    // Case where a line has been removed
    logger.debug("Removing bom lines");
    for (BillOfMaterialLine billOfMaterialLine : bomLines) {
      var isInSubList =
          saleOrderLine.getSaleOrderLineDetailsList().stream()
              .map(SaleOrderLineDetails::getBillOfMaterialLine)
              .filter(Objects::nonNull)
              .anyMatch(bomLine -> bomLine.equals(billOfMaterialLine));

      logger.debug(
          "Checking existence of billOfMaterialLine {} in {}",
          billOfMaterialLine,
          saleOrderLine.getSaleOrderLineDetailsList());
      if (!isInSubList) {
        logger.debug("BomLine does not exist, removing it");
        bom.removeBillOfMaterialLineListItem(billOfMaterialLine);
      }
    }
    billOfMaterialRepository.save(bom);
    logger.debug("Updated saleOrderLine {} with bom {}", saleOrderLine, bom);
  }

  @Override
  public boolean isSolDetailsUpdated(SaleOrderLine saleOrderLine) {

    if (saleOrderLine.getBillOfMaterial() == null) {
      return true;
    }

    var nbBomLinesAccountable =
        saleOrderLine.getBillOfMaterial().getBillOfMaterialLineList().stream()
            .map(BillOfMaterialLine::getProduct)
            .filter(
                product ->
                    product
                        .getProductSubTypeSelect()
                        .equals(ProductRepository.PRODUCT_SUB_TYPE_COMPONENT))
            .count();

    var nbSaleOrderLineDetails =
        Optional.ofNullable(saleOrderLine.getSaleOrderLineDetailsList()).orElse(List.of()).stream()
            .filter(line -> line.getTypeSelect() == SaleOrderLineDetailsRepository.TYPE_COMPONENT)
            .count();
    return nbBomLinesAccountable == nbSaleOrderLineDetails
        && Optional.ofNullable(saleOrderLine.getSaleOrderLineDetailsList())
            .orElse(List.of())
            .stream()
            .filter(line -> line.getTypeSelect() == SaleOrderLineDetailsRepository.TYPE_COMPONENT)
            .allMatch(this::isSolDetailsSyncWithBomLine);
  }

  protected boolean isSolDetailsSyncWithBomLine(SaleOrderLineDetails saleOrderLineDetails) {
    Objects.requireNonNull(saleOrderLineDetails);
    BillOfMaterialLine billOfMaterialLine = saleOrderLineDetails.getBillOfMaterialLine();

    return billOfMaterialLine.getQty().equals(saleOrderLineDetails.getQty())
        && billOfMaterialLine.getProduct().equals(saleOrderLineDetails.getProduct())
        && billOfMaterialLine.getUnit().equals(saleOrderLineDetails.getUnit());
  }
}
