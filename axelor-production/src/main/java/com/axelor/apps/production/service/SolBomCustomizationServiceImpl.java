package com.axelor.apps.production.service;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.repo.ProductRepository;
import com.axelor.apps.production.db.BillOfMaterial;
import com.axelor.apps.production.db.SaleOrderLineDetails;
import com.axelor.apps.production.db.repo.BillOfMaterialRepository;
import com.axelor.apps.production.db.repo.SaleOrderLineDetailsRepository;
import com.axelor.apps.sale.db.SaleOrderLine;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.util.List;
import java.util.stream.Collectors;
import org.apache.commons.collections.CollectionUtils;

public class SolBomCustomizationServiceImpl implements SolBomCustomizationService {

  protected final BillOfMaterialService billOfMaterialService;
  protected final BillOfMaterialRepository billOfMaterialRepository;
  protected final BomLineCreationService bomLineCreationService;

  @Inject
  public SolBomCustomizationServiceImpl(
      BillOfMaterialService billOfMaterialService,
      BillOfMaterialRepository billOfMaterialRepository,
      BomLineCreationService bomLineCreationService) {
    this.billOfMaterialService = billOfMaterialService;
    this.billOfMaterialRepository = billOfMaterialRepository;
    this.bomLineCreationService = bomLineCreationService;
  }

  @Override
  @Transactional(rollbackOn = Exception.class)
  public BillOfMaterial customizeBomOf(SaleOrderLine saleOrderLine) throws AxelorException {
    return customizeBomOf(saleOrderLine, 0);
  }

  protected BillOfMaterial customizeBomOf(SaleOrderLine saleOrderLine, int depth)
      throws AxelorException {
    var billOfMaterial = saleOrderLine.getBillOfMaterial();
    if (billOfMaterial == null) {
      return null;
    }
    BillOfMaterial personalizedBOM =
        billOfMaterialService.getCustomizedBom(billOfMaterial, depth, false);

    saleOrderLine.setBillOfMaterial(personalizedBOM);

    if (saleOrderLine.getSubSaleOrderLineList() != null) {
      List<SaleOrderLine> subSaleOrderLineList =
          saleOrderLine.getSubSaleOrderLineList().stream()
              .filter(line -> line.getProduct().getProductSubTypeSelect() != null)
              .collect(Collectors.toList());
      for (SaleOrderLine subSaleOrderLine : subSaleOrderLineList) {
        if (subSaleOrderLine
            .getProduct()
            .getProductSubTypeSelect()
            .equals(ProductRepository.PRODUCT_SUB_TYPE_SEMI_FINISHED_PRODUCT)) {
          var bomLine = bomLineCreationService.createBomLineFromSol(subSaleOrderLine);
          // If it is not personalized, we will customize, else just use the personalized one.
          if (subSaleOrderLine.getIsToProduce() && !bomLine.getBillOfMaterial().getPersonalized()) {
            subSaleOrderLine.setBillOfMaterial(customizeBomOf(subSaleOrderLine, depth + 1));
          }
          // Relink billOfMaterialLine
          subSaleOrderLine.setBillOfMaterialLine(bomLine);
          personalizedBOM.addBillOfMaterialLineListItem(bomLine);
        }
      }
    }

    if (CollectionUtils.isNotEmpty(saleOrderLine.getSaleOrderLineDetailsList())) {
      for (SaleOrderLineDetails saleOrderLineDetails :
          saleOrderLine.getSaleOrderLineDetailsList()) {
        if (saleOrderLineDetails.getTypeSelect() == SaleOrderLineDetailsRepository.TYPE_COMPONENT) {
          var bomLine = bomLineCreationService.createBomLineFromSolDetails(saleOrderLineDetails);
          saleOrderLineDetails.setBillOfMaterialLine(bomLine);
          personalizedBOM.addBillOfMaterialLineListItem(bomLine);
        }
      }
    }

    return billOfMaterialRepository.save(personalizedBOM);
  }
}
