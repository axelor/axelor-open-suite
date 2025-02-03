package com.axelor.apps.production.service;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Product;
import com.axelor.apps.base.db.repo.ProductRepository;
import com.axelor.apps.production.db.BillOfMaterial;
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
        updateBomLineSolDetails(saleOrderLineDetails, bom);
      }
    }
    logger.debug("Updated saleOrderLine {} with bom {}", saleOrderLine, bom);
  }

  protected void updateBomLineSolDetails(
      SaleOrderLineDetails saleOrderLineDetails, BillOfMaterial bom) {
    if (!this.isSolDetailsSyncWithBomLine(saleOrderLineDetails)) {
      var bomLine = saleOrderLineDetails.getBillOfMaterialLine();
      // Updating the existing one
      if (bomLine != null) {
        updateBomLine(saleOrderLineDetails, bomLine, bom);
      }
      // Creating a new one
      else {
        createBomLine(saleOrderLineDetails, bom);
      }
    }
  }

  protected void updateBomLine(
      SaleOrderLineDetails saleOrderLineDetails, BillOfMaterialLine bomLine, BillOfMaterial bom) {
    logger.debug("Updating bomLine {} with sol details {}", bomLine, saleOrderLineDetails);
    bomLine.setQty(saleOrderLineDetails.getQty());
    bomLine.setProduct(saleOrderLineDetails.getProduct());
    bomLine.setUnit(saleOrderLineDetails.getUnit());
    bom.addBillOfMaterialLineListItem(bomLine);
  }

  protected void createBomLine(SaleOrderLineDetails saleOrderLineDetails, BillOfMaterial bom) {
    BillOfMaterialLine bomLine;
    logger.debug(
        "Creating bomLine from sol details {} and adding it to bom {}", saleOrderLineDetails, bom);
    bomLine = bomLineCreationService.createBomLineFromSolDetails(saleOrderLineDetails);
    logger.debug("Created bomLine {}", bomLine);
    bom.addBillOfMaterialLineListItem(bomLine);
    saleOrderLineDetails.setBillOfMaterialLine(bomLine);
    billOfMaterialLineRepository.save(bomLine);
  }

  @Override
  public boolean isSolDetailsUpdated(SaleOrderLine saleOrderLine) {

    if (saleOrderLine.getBillOfMaterial() == null) {
      return true;
    }

    var nbBomLinesAccountable =
        saleOrderLine.getBillOfMaterial().getBillOfMaterialLineList().stream()
            .map(BillOfMaterialLine::getProduct)
            .map(Product::getProductSubTypeSelect)
            .filter(type -> type.equals(ProductRepository.PRODUCT_SUB_TYPE_COMPONENT))
            .count();

    var nbSaleOrderLineDetails =
        Optional.ofNullable(saleOrderLine.getSaleOrderLineDetailsList()).orElse(List.of()).stream()
            .map(SaleOrderLineDetails::getTypeSelect)
            .filter(type -> type == SaleOrderLineDetailsRepository.TYPE_COMPONENT)
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

    return billOfMaterialLine != null
        && billOfMaterialLine.getQty().equals(saleOrderLineDetails.getQty())
        && billOfMaterialLine.getProduct().equals(saleOrderLineDetails.getProduct())
        && billOfMaterialLine.getUnit().equals(saleOrderLineDetails.getUnit());
  }
}
