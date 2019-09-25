/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2019 Axelor (<http://axelor.com>).
 *
 * This program is free software: you can redistribute it and/or  modify
 * it under the terms of the GNU Affero General Public License, version 3,
 * as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.axelor.apps.production.service;

import com.axelor.apps.ReportFactory;
import com.axelor.apps.base.db.Product;
import com.axelor.apps.base.db.repo.ProductRepository;
import com.axelor.apps.base.service.ProductService;
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.apps.production.db.BillOfMaterial;
import com.axelor.apps.production.db.BillOfMaterialLine;
import com.axelor.apps.production.db.TempBomTree;
import com.axelor.apps.production.db.repo.BillOfMaterialRepository;
import com.axelor.apps.production.db.repo.TempBomTreeRepository;
import com.axelor.apps.production.exceptions.IExceptionMessage;
import com.axelor.apps.production.report.IReport;
import com.axelor.apps.production.service.app.AppProductionService;
import com.axelor.apps.sale.db.SaleOrderLine;
import com.axelor.db.JPA;
import com.axelor.exception.AxelorException;
import com.axelor.exception.db.repo.TraceBackRepository;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.lang.invoke.MethodHandles;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BillOfMaterialServiceImpl implements BillOfMaterialService {

  private final Logger log = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  @Inject protected BillOfMaterialRepository billOfMaterialRepo;

  @Inject private TempBomTreeRepository tempBomTreeRepo;

  @Inject private ProductRepository productRepo;

  @Override
  public List<BillOfMaterial> getBillOfMaterialSet(Product product) {

    return billOfMaterialRepo.all().filter("self.product = ?1", product).fetch();
  }

  @Override
  @Transactional(rollbackOn = {Exception.class})
  public void updateProductCostPrice(BillOfMaterial billOfMaterial) throws AxelorException {

    Product product = billOfMaterial.getProduct();

    if (product.getCostTypeSelect() != ProductRepository.COST_TYPE_STANDARD) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
          I18n.get(IExceptionMessage.COST_TYPE_CANNOT_BE_CHANGED));
    }

    product.setCostPrice(
        billOfMaterial
            .getCostPrice()
            .divide(
                billOfMaterial.getQty(),
                Beans.get(AppBaseService.class).getNbDecimalDigitForUnitPrice(),
                BigDecimal.ROUND_HALF_UP));

    Beans.get(ProductService.class).updateSalePrice(product);

    billOfMaterialRepo.save(billOfMaterial);
  }

  @Override
  @Transactional(rollbackOn = {Exception.class})
  public BillOfMaterial customizeBillOfMaterial(SaleOrderLine saleOrderLine)
      throws AxelorException {

    BillOfMaterial billOfMaterial = saleOrderLine.getBillOfMaterial();
    return customizeBillOfMaterial(billOfMaterial);
  }

  @Override
  @Transactional(rollbackOn = {Exception.class})
  public BillOfMaterial customizeBillOfMaterial(BillOfMaterial billOfMaterial)
      throws AxelorException {
    return customizeBillOfMaterial(billOfMaterial, 0);
  }

  @Override
  @Transactional(rollbackOn = {Exception.class})
  public BillOfMaterial customizeBillOfMaterial(BillOfMaterial billOfMaterial, int depth)
      throws AxelorException {
    if (depth > 1000) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_INCONSISTENCY,
          I18n.get(IExceptionMessage.MAX_DEPTH_REACHED));
    }

    if (billOfMaterial != null) {
      BillOfMaterial personalizedBOM = JPA.copy(billOfMaterial, false);
      billOfMaterialRepo.save(personalizedBOM);
      personalizedBOM.setName(
          personalizedBOM.getName()
              + " ("
              + I18n.get(IExceptionMessage.BOM_1)
              + " "
              + personalizedBOM.getId()
              + ")");
      personalizedBOM.setPersonalized(true);

      ArrayList<BillOfMaterialLine> BOMLineList = new ArrayList<>();

      for (BillOfMaterialLine billOfMaterialLine : billOfMaterial.getBillOfMaterialLineList()) {
        BillOfMaterialLine bomLine = new BillOfMaterialLine();

        bomLine.setProduct(billOfMaterialLine.getProduct());
        bomLine.setPriority(billOfMaterialLine.getPriority());
        bomLine.setParent(personalizedBOM);
        bomLine.setQty(billOfMaterialLine.getQty());
        bomLine.setUnit(billOfMaterialLine.getUnit());

        if (bomLine.getBillOfMaterial() != null && bomLine.getBillOfMaterial().getIsSpecific()) {
          bomLine.setBillOfMaterial(
              customizeBillOfMaterial(billOfMaterialLine.getBillOfMaterial(), depth + 1));
        } else {
          bomLine.setBillOfMaterial(billOfMaterialLine.getBillOfMaterial());
        }
        BOMLineList.add(bomLine);
      }
      personalizedBOM.setBillOfMaterialLineList(BOMLineList);
      return personalizedBOM;
    }

    return null;
  }

  @Override
  @Transactional
  public BillOfMaterial generateNewVersion(BillOfMaterial billOfMaterial) {

    BillOfMaterial copy = billOfMaterialRepo.copy(billOfMaterial, true);

    copy.setOriginalBillOfMaterial(billOfMaterial);
    copy.clearCostSheetList();
    copy.setCostPrice(BigDecimal.ZERO);
    copy.setOriginalBillOfMaterial(billOfMaterial);
    copy.setVersionNumber(
        this.getLatestBillOfMaterialVersion(billOfMaterial, billOfMaterial.getVersionNumber(), true)
            + 1);

    return billOfMaterialRepo.save(copy);
  }

  public int getLatestBillOfMaterialVersion(
      BillOfMaterial billOfMaterial, int latestVersion, boolean deep) {

    List<BillOfMaterial> billOfMaterialSet;
    BillOfMaterial up = billOfMaterial;
    Long previousId = 0L;
    do {
      billOfMaterialSet =
          billOfMaterialRepo
              .all()
              .filter("self.originalBillOfMaterial = :origin AND self.id != :id")
              .bind("origin", up)
              .bind("id", previousId)
              .order("-versionNumber")
              .fetch();
      if (!billOfMaterialSet.isEmpty()) {
        latestVersion =
            (billOfMaterialSet.get(0).getVersionNumber() > latestVersion)
                ? billOfMaterialSet.get(0).getVersionNumber()
                : latestVersion;
        for (BillOfMaterial billOfMaterialIterator : billOfMaterialSet) {
          int search =
              this.getLatestBillOfMaterialVersion(billOfMaterialIterator, latestVersion, false);
          latestVersion = (search > latestVersion) ? search : latestVersion;
        }
      }
      previousId = up.getId();
      up = up.getOriginalBillOfMaterial();
    } while (up != null && deep);

    return latestVersion;
  }

  @Override
  public String getFileName(BillOfMaterial billOfMaterial) {

    return I18n.get("Bill of Materials")
        + "-"
        + billOfMaterial.getName()
        + ((billOfMaterial.getVersionNumber() > 1) ? "-V" + billOfMaterial.getVersionNumber() : "");
  }

  @Override
  public String getReportLink(
      BillOfMaterial billOfMaterial, String name, String language, String format)
      throws AxelorException {

    return ReportFactory.createReport(IReport.BILL_OF_MATERIAL, name + "-${date}")
        .addParam("Locale", language)
        .addParam("BillOfMaterialId", billOfMaterial.getId())
        .addFormat(format)
        .generate()
        .getFileLink();
  }

  @Override
  @Transactional
  public TempBomTree generateTree(BillOfMaterial billOfMaterial) throws AxelorException {

    TempBomTree bomTree;
    bomTree =
        tempBomTreeRepo
            .all()
            .filter("self.bomId = ?1 and self.parent = null", billOfMaterial.getId())
            .fetchOne();

    if (bomTree == null) {
      bomTree = new TempBomTree();
    }
    bomTree.setProdProcess(billOfMaterial.getProdProcess());
    bomTree.setProduct(billOfMaterial.getProduct());
    bomTree.setQty(billOfMaterial.getQty());
    bomTree.setUnit(billOfMaterial.getUnit());
    bomTree.setParent(null);
    bomTree.setBomId(billOfMaterial.getId());
    bomTree.setBomLineId(null);
    bomTree = tempBomTreeRepo.save(bomTree);

    List<Long> validBomLineIds = processChildBom(billOfMaterial, bomTree);

    validBomLineIds.add(0L);

    removeInvalidTree(validBomLineIds, bomTree);

    return bomTree;
  }

  @Transactional
  public TempBomTree getSubBomTree(BillOfMaterialLine bomLine, TempBomTree parent)
      throws AxelorException {

    TempBomTree bomTree;
    bomTree =
        tempBomTreeRepo
            .all()
            .filter("self.bomLineId = ?1 and self.parent = ?2", bomLine.getId(), parent)
            .fetchOne();

    if (bomTree == null) {
      bomTree = new TempBomTree();
    }

    BillOfMaterial bom = bomLine.getBillOfMaterial();
    bomTree.setBomLineId(bomLine.getId());
    bomTree.setProduct(bomLine.getProduct());
    bomTree.setQty(bomLine.getQty());
    bomTree.setUnit(bomLine.getUnit());
    bomTree.setPriority(bomLine.getPriority());
    if (bom != null) {
      bomTree.setBomId(bom.getId());
      bomTree.setProdProcess(bom.getProdProcess());
    } else {
      bomTree.setBomId(null);
    }
    bomTree.setParent(parent);

    bomTree = tempBomTreeRepo.save(bomTree);

    if (bom != null) {
      List<Long> validBomLineIds = processChildBom(bom, bomTree);

      validBomLineIds.add(0L);

      removeInvalidTree(validBomLineIds, bomTree);
    }

    return bomTree;
  }

  private List<Long> processChildBom(BillOfMaterial bom, TempBomTree bomTree)
      throws AxelorException {

    List<Long> validBomLineIds = new ArrayList<Long>();

    for (BillOfMaterialLine bomLine : bom.getBillOfMaterialLineList()) {
      getSubBomTree(bomLine, bomTree);
      validBomLineIds.add(bomLine.getId());
    }

    return validBomLineIds;
  }

  public void removeInvalidTree(List<Long> validBomLineIds, TempBomTree bomTree)
      throws AxelorException {

    List<TempBomTree> invalidBomTrees =
        tempBomTreeRepo
            .all()
            .filter("self.parent = ?1 and self.bomLineId not in (?2)", bomTree, validBomLineIds)
            .fetch();

    log.debug("Invalid bom trees: {}", invalidBomTrees);

    for (TempBomTree invalidBomTree : invalidBomTrees) {
      this.tempBomTreeRecursiveRemove(invalidBomTree, 0);
    }
  }

  /**
   * Removes the tempBomTree from the database along will all its subTrees recursively.
   *
   * @param tempBomTree
   */
  @Transactional
  protected void tempBomTreeRecursiveRemove(TempBomTree tempBomTree, int recursionLevel)
      throws AxelorException {
    if (recursionLevel >= 100) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_INCONSISTENCY,
          I18n.get(IExceptionMessage.TEMP_BOM_TREE_ABORT_RECURSION));
    }
    List<TempBomTree> childBomTrees =
        tempBomTreeRepo.all().filter("self.parent = ?1", tempBomTree).fetch();
    if (!childBomTrees.isEmpty()) {
      for (TempBomTree childBomTree : childBomTrees) {
        this.tempBomTreeRecursiveRemove(childBomTree, recursionLevel + 1);
      }
    }

    tempBomTreeRepo.remove(tempBomTree);
  }

  @Override
  @Transactional
  public void setBillOfMaterialAsDefault(BillOfMaterial billOfMaterial) {
    billOfMaterial.getProduct().setDefaultBillOfMaterial(billOfMaterial);
  }

  @Override
  public String computeName(BillOfMaterial bom) {
    Integer nbDecimalDigitForBomQty =
        Beans.get(AppProductionService.class).getAppProduction().getNbDecimalDigitForBomQty();
    return bom.getProduct().getName()
        + " - "
        + bom.getQty().setScale(nbDecimalDigitForBomQty, RoundingMode.HALF_EVEN)
        + " "
        + bom.getUnit().getName()
        + " - "
        + bom.getId();
  }

  @Override
  public List<BillOfMaterialLine> addRawMaterials(
      long billOfMaterialId, ArrayList<LinkedHashMap<String, Object>> rawMaterials) {
    if (rawMaterials != null && !rawMaterials.isEmpty()) {
      BillOfMaterial billOfMaterial = billOfMaterialRepo.find(billOfMaterialId);
      int priority = 0;
      List<BillOfMaterialLine> bomLineList = billOfMaterial.getBillOfMaterialLineList();
      if (bomLineList != null && !bomLineList.isEmpty()) {
        priority =
            Collections.max(
                bomLineList.stream().map(it -> it.getPriority()).collect(Collectors.toSet()));
      }

      for (LinkedHashMap<String, Object> rawMaterial : rawMaterials) {
        priority += 10;
        BillOfMaterialLine newComponent =
            createBomFromRawMaterial(
                Long.valueOf((int) rawMaterial.get("id")), billOfMaterial, priority);
        bomLineList.add(newComponent);
      }

      return bomLineList;
    }

    return null;
  }

  protected BillOfMaterialLine createBomFromRawMaterial(
      long productId, BillOfMaterial parent, int priority) {
    BillOfMaterialLine newBomLine = new BillOfMaterialLine();
    Product rawMaterial = productRepo.find(productId);
    newBomLine.setPriority(priority);
    newBomLine.setProduct(rawMaterial);
    newBomLine.setQty(BigDecimal.ONE);
    newBomLine.setUnit(rawMaterial.getUnit());
    newBomLine.setBillOfMaterial(null);
    newBomLine.setParent(parent);

    return newBomLine;
  }
}
