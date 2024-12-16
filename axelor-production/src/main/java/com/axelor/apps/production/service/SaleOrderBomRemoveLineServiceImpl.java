package com.axelor.apps.production.service;

import com.axelor.apps.production.db.BillOfMaterial;
import com.axelor.apps.production.db.BillOfMaterialLine;
import com.axelor.apps.production.db.repo.BillOfMaterialRepository;
import com.google.inject.Inject;
import java.lang.invoke.MethodHandles;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SaleOrderBomRemoveLineServiceImpl implements SaleOrderBomRemoveLineService {
  protected final BillOfMaterialRepository billOfMaterialRepository;
  private static final Logger logger =
      LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  @Inject
  public SaleOrderBomRemoveLineServiceImpl(BillOfMaterialRepository billOfMaterialRepository) {
    this.billOfMaterialRepository = billOfMaterialRepository;
  }

  @Override
  public void removeBomLines(
      List<BillOfMaterialLine> saleOrderLineBomLineList,
      BillOfMaterial bom,
      int productTypeSelect) {
    var bomLines =
        bom.getBillOfMaterialLineList().stream()
            .filter(
                bomLine -> bomLine.getProduct().getProductSubTypeSelect().equals(productTypeSelect))
            .collect(Collectors.toList());
    // Case where a line has been removed
    logger.debug("Removing bom lines");
    for (BillOfMaterialLine billOfMaterialLine : bomLines) {
      var isInSubList =
          saleOrderLineBomLineList.stream()
              .filter(Objects::nonNull)
              .anyMatch(bomLine -> bomLine.equals(billOfMaterialLine));

      if (!isInSubList) {
        logger.debug("BomLine does not exist, removing it");
        bom.removeBillOfMaterialLineListItem(billOfMaterialLine);
      }
    }
    billOfMaterialRepository.save(bom);
  }
}
