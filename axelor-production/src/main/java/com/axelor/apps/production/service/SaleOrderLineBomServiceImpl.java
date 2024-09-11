package com.axelor.apps.production.service;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.repo.ProductRepository;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.apps.production.db.BillOfMaterial;
import com.axelor.apps.production.db.BillOfMaterialLine;
import com.axelor.apps.production.db.repo.BillOfMaterialLineRepository;
import com.axelor.apps.production.db.repo.BillOfMaterialRepository;
import com.axelor.apps.production.exceptions.ProductionExceptionMessage;
import com.axelor.apps.sale.db.SaleOrder;
import com.axelor.apps.sale.db.SaleOrderLine;
import com.axelor.apps.sale.service.app.AppSaleService;
import com.axelor.db.JPA;
import com.axelor.i18n.I18n;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class SaleOrderLineBomServiceImpl implements SaleOrderLineBomService {

  protected final SaleOrderLineBomLineMappingService saleOrderLineBomLineMappingService;
  protected final AppSaleService appSaleService;
  protected final BillOfMaterialRepository billOfMaterialRepository;
  protected final BillOfMaterialLineRepository billOfMaterialLineRepository;
  protected final BillOfMaterialLineService billOfMaterialLineService;

  @Inject
  public SaleOrderLineBomServiceImpl(
      SaleOrderLineBomLineMappingService saleOrderLineBomLineMappingService,
      AppSaleService appSaleService,
      BillOfMaterialRepository billOfMaterialRepository,
      BillOfMaterialLineRepository billOfMaterialLineRepository,
      BillOfMaterialLineService billOfMaterialLineService) {
    this.saleOrderLineBomLineMappingService = saleOrderLineBomLineMappingService;
    this.appSaleService = appSaleService;
    this.billOfMaterialRepository = billOfMaterialRepository;
    this.billOfMaterialLineRepository = billOfMaterialLineRepository;
    this.billOfMaterialLineService = billOfMaterialLineService;
  }

  @Override
  public List<SaleOrderLine> createSaleOrderLinesFromBom(
      BillOfMaterial billOfMaterial, SaleOrder saleOrder) throws AxelorException {
    Objects.requireNonNull(billOfMaterial);

    var saleOrderLinesList = new ArrayList<SaleOrderLine>();

    if (!appSaleService.getAppSale().getActivateMultiLevelSaleOrderLines()) {
      return saleOrderLinesList;
    }

    for (BillOfMaterialLine billOfMaterialLine : billOfMaterial.getBillOfMaterialLineList()) {
      var saleOrderLine =
          saleOrderLineBomLineMappingService.mapToSaleOrderLine(billOfMaterialLine, saleOrder);
      if (saleOrderLine != null) {
        saleOrderLinesList.add(saleOrderLine);
      }
    }

    return saleOrderLinesList;
  }

  @Override
  public BillOfMaterial customizeBomOf(SaleOrderLine saleOrderLine) throws AxelorException {
    return customizeBomOf(saleOrderLine, 0);
  }

  @Override
  @Transactional(rollbackOn = Exception.class)
  public void updateWithBillOfMaterial(SaleOrderLine saleOrderLine) throws AxelorException {
    // Easiest cases where a line has been added or modified.
    var bom = saleOrderLine.getBillOfMaterial();
    for (SaleOrderLine subSaleOrderLine : saleOrderLine.getSubSaleOrderLineList()) {
      if (!saleOrderLineBomLineMappingService.isSyncWithBomLine(subSaleOrderLine)) {
        var bomLine = subSaleOrderLine.getBillOfMaterialLine();
        // Updating the existing one
        if (bomLine != null) {
          bomLine.setQty(subSaleOrderLine.getQty());
          bomLine.setProduct(subSaleOrderLine.getProduct());
          bomLine.setUnit(subSaleOrderLine.getUnit());
          bomLine.setBillOfMaterial(subSaleOrderLine.getBillOfMaterial());
          bom.addBillOfMaterialLineListItem(bomLine);
        }
        // Creating a new one
        else {
          bomLine = createBomLineFrom(subSaleOrderLine);
          bom.addBillOfMaterialLineListItem(bomLine);
          saleOrderLine.setBillOfMaterialLine(bomLine);
        }
      }
    }

    var bomLines = saleOrderLine.getBillOfMaterial().getBillOfMaterialLineList().stream().filter(bomLine -> bomLine.getProduct().getProductSubTypeSelect().equals(ProductRepository.PRODUCT_SUB_TYPE_SEMI_FINISHED_PRODUCT)).collect(Collectors.toList());
    // Case where a line has been removed
    for (BillOfMaterialLine billOfMaterialLine : bomLines) {
      var isInSubList =
          saleOrderLine.getSubSaleOrderLineList().stream()
              .map(SaleOrderLine::getBillOfMaterialLine)
                  .filter(Objects::nonNull)
              .anyMatch(bomLine -> bomLine.equals(billOfMaterialLine));

      if (!isInSubList) {
        bom.removeBillOfMaterialLineListItem(billOfMaterialLine);
        billOfMaterialLine.setBillOfMaterialParent(null);

        billOfMaterialLineRepository.save(billOfMaterialLine);
      }
    }
    billOfMaterialRepository.save(bom);
  }

  protected BillOfMaterialLine createBomLineFrom(SaleOrderLine subSaleOrderLine) {
    return billOfMaterialLineService.createBillOfMaterialLine(
        subSaleOrderLine.getProduct(),
        subSaleOrderLine.getBillOfMaterial(),
        subSaleOrderLine.getQty(),
        subSaleOrderLine.getUnit(),
        0,
        subSaleOrderLine.getProduct().getStockManaged());
  }

  @Transactional(rollbackOn = Exception.class)
  protected BillOfMaterial customizeBomOf(SaleOrderLine saleOrderLine, int depth)
      throws AxelorException {
    if (saleOrderLine.getBillOfMaterial() == null) {
      return null;
    }
    if (depth > 1000) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_INCONSISTENCY,
          I18n.get(ProductionExceptionMessage.MAX_DEPTH_REACHED));
    }

    var billOfMaterial = saleOrderLine.getBillOfMaterial();
    var noOfPersonalizedBOM =
        billOfMaterialRepository
                .all()
                .filter(
                    "self.product = ?1 AND self.personalized = true", billOfMaterial.getProduct())
                .count()
            + 1;
    var personalizedBOM = JPA.copy(billOfMaterial, false);
    var name =
        personalizedBOM.getName()
            + " ("
            + I18n.get(ProductionExceptionMessage.BOM_1)
            + " "
            + noOfPersonalizedBOM
            + ")";
    personalizedBOM.setName(name);
    personalizedBOM.setPersonalized(true);
    saleOrderLine.setBillOfMaterial(personalizedBOM);

    for (SaleOrderLine subSaleOrderLine : saleOrderLine.getSubSaleOrderLineList()) {
      var bomLine = createBomLineFrom(subSaleOrderLine);
      // If it is not personalized, we will customize, else just use the personalized one.
      if (subSaleOrderLine.getIsToProduce() && !bomLine.getBillOfMaterial().getPersonalized()) {
        subSaleOrderLine.setBillOfMaterial(customizeBomOf(subSaleOrderLine, depth + 1));
      }
      // Relink billOfMaterialLine
      subSaleOrderLine.setBillOfMaterialLine(bomLine);
      personalizedBOM.addBillOfMaterialLineListItem(bomLine);
    }

    //Copy components lines
    billOfMaterial.getBillOfMaterialLineList().stream().filter(oldBomLine -> !oldBomLine.getProduct().getProductSubTypeSelect().equals(ProductRepository.PRODUCT_SUB_TYPE_SEMI_FINISHED_PRODUCT))
            .map(oldBomLine -> billOfMaterialLineRepository.copy(oldBomLine, false))
            .forEach(personalizedBOM::addBillOfMaterialLineListItem);

    return billOfMaterialRepository.save(personalizedBOM);
  }
}
