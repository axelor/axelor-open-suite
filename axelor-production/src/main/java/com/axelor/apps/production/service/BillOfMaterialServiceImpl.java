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
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.Product;
import com.axelor.apps.base.db.repo.ProductRepository;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.apps.base.service.ProductCompanyService;
import com.axelor.apps.base.service.ProductService;
import com.axelor.apps.production.db.BillOfMaterial;
import com.axelor.apps.production.db.BillOfMaterialLine;
import com.axelor.apps.production.db.TempBomTree;
import com.axelor.apps.production.db.repo.BillOfMaterialRepository;
import com.axelor.apps.production.db.repo.TempBomTreeRepository;
import com.axelor.apps.production.exceptions.ProductionExceptionMessage;
import com.axelor.apps.production.service.costsheet.CostSheetService;
import com.axelor.apps.sale.db.SaleOrderLine;
import com.axelor.auth.AuthUtils;
import com.axelor.auth.db.User;
import com.axelor.db.JPA;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.lang.invoke.MethodHandles;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import javax.persistence.Query;
import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BillOfMaterialServiceImpl implements BillOfMaterialService {

  private final Logger log = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  protected BillOfMaterialRepository billOfMaterialRepo;

  protected TempBomTreeRepository tempBomTreeRepo;

  protected ProductRepository productRepo;

  protected ProductCompanyService productCompanyService;

  protected BillOfMaterialLineService billOfMaterialLineService;

  protected BillOfMaterialService billOfMaterialService;

  protected CostSheetService costSheetService;

  @Inject
  public BillOfMaterialServiceImpl(
      BillOfMaterialRepository billOfMaterialRepo,
      TempBomTreeRepository tempBomTreeRepo,
      ProductRepository productRepo,
      ProductCompanyService productCompanyService,
      BillOfMaterialLineService billOfMaterialLineService,
      BillOfMaterialService billOfMaterialService,
      CostSheetService costSheetService) {
    this.billOfMaterialRepo = billOfMaterialRepo;
    this.tempBomTreeRepo = tempBomTreeRepo;
    this.productRepo = productRepo;
    this.productCompanyService = productCompanyService;
    this.billOfMaterialLineService = billOfMaterialLineService;
    this.billOfMaterialService = billOfMaterialService;
    this.costSheetService = costSheetService;
  }

  private List<Long> processedBom;
  private List<Long> processedBomLine;

  @Override
  @Transactional(rollbackOn = {Exception.class})
  public void updateProductCostPrice(BillOfMaterial billOfMaterial) throws AxelorException {

    Product product = billOfMaterial.getProduct();

    if ((Integer) productCompanyService.get(product, "costTypeSelect", billOfMaterial.getCompany())
        != ProductRepository.COST_TYPE_STANDARD) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
          I18n.get(ProductionExceptionMessage.COST_TYPE_CANNOT_BE_CHANGED));
    }

    productCompanyService.set(
        product, "costPrice", billOfMaterial.getCostPrice(), billOfMaterial.getCompany());

    if ((Boolean)
        productCompanyService.get(product, "autoUpdateSalePrice", billOfMaterial.getCompany())) {
      Beans.get(ProductService.class).updateSalePrice(product, billOfMaterial.getCompany());
    }

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
    BillOfMaterial personalizedBOM = getCustomizedBom(billOfMaterial, depth, true);
    if (personalizedBOM == null) return null;
    List<BillOfMaterialLine> billOfMaterialLineList = billOfMaterial.getBillOfMaterialLineList();

    for (BillOfMaterialLine billOfMaterialLine : billOfMaterialLineList) {
      if (billOfMaterialLine.getBillOfMaterial() != null) {
        billOfMaterialLine.setBillOfMaterial(
            customizeBillOfMaterial(billOfMaterialLine.getBillOfMaterial(), depth + 1));
      }
    }

    return billOfMaterialRepo.save(personalizedBOM);
  }

  @Override
  public BillOfMaterial getCustomizedBom(BillOfMaterial billOfMaterial, int depth, boolean deepCopy)
      throws AxelorException {
    if (billOfMaterial == null) {
      return null;
    }
    if (depth > 1000) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_INCONSISTENCY,
          I18n.get(ProductionExceptionMessage.MAX_DEPTH_REACHED));
    }

    long noOfPersonalizedBOM =
        billOfMaterialRepo
                .all()
                .filter(
                    "self.product = ?1 AND self.personalized = true", billOfMaterial.getProduct())
                .count()
            + 1;
    BillOfMaterial personalizedBOM = JPA.copy(billOfMaterial, deepCopy);
    String name =
        personalizedBOM.getName()
            + " ("
            + I18n.get(ProductionExceptionMessage.BOM_1)
            + " "
            + noOfPersonalizedBOM
            + ")";
    personalizedBOM.setName(name);
    personalizedBOM.setPersonalized(true);
    return personalizedBOM;
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
  @Transactional
  public TempBomTree generateTree(BillOfMaterial billOfMaterial, boolean useProductDefaultBom)
      throws AxelorException {

    processedBom = new ArrayList<>();
    processedBomLine = new ArrayList<>();

    return getBomTree(billOfMaterial, null, null, null, useProductDefaultBom);
  }

  @Transactional
  public TempBomTree getBomTree(
      BillOfMaterial bom,
      BillOfMaterialLine bomLine,
      BillOfMaterial parentBom,
      TempBomTree parent,
      boolean useProductDefaultBom)
      throws AxelorException {

    TempBomTree bomTree;
    if (parentBom == null) {
      bomTree =
          tempBomTreeRepo.all().filter("self.bom = ?1 and self.parentBom = null", bom).fetchOne();
    } else {
      bomTree =
          tempBomTreeRepo
              .all()
              .filter("self.billOfMaterialLine = ?1 and self.parentBom = ?2", bomLine, parentBom)
              .fetchOne();
    }

    if (bomTree == null) {
      bomTree = new TempBomTree();
    }

    bomTree.setBillOfMaterialLine(bomLine);
    if (bom != null) {
      bomTree.setProdProcess(bom.getProdProcess());
      bomTree.setProduct(bom.getProduct());
      bomTree.setQty(bom.getQty());
      bomTree.setUnit(bom.getUnit());
    } else if (bomLine != null) {
      bomTree.setProduct(bomLine.getProduct());
      bomTree.setQty(bomLine.getQty());
      bomTree.setUnit(bomLine.getUnit());
    }
    bomTree.setParentBom(parentBom);
    bomTree.setParent(parent);
    bomTree.setBom(bom);
    bomTree = tempBomTreeRepo.save(bomTree);

    if (bom != null) {
      processedBom.add(bom.getId());
    }

    if (bomLine != null) {
      processedBomLine.add(bomLine.getId());
    }

    // It is a node with not children if bom is null
    List<Long> validTreeIds = new ArrayList<>();
    if (bom != null) {
      validTreeIds.addAll(processChildBom(bom, bomTree, useProductDefaultBom));
    }
    validTreeIds.add(0L);
    removeInvalidTree(validTreeIds, bom);
    return bomTree;
  }

  protected List<Long> processChildBom(
      BillOfMaterial bom, TempBomTree bomTree, boolean useProductDefaultBom)
      throws AxelorException {

    List<Long> validTreeIds = new ArrayList<Long>();

    for (BillOfMaterialLine bomLineChild : bom.getBillOfMaterialLineList()) {

      BillOfMaterial bomChild = bomLineChild.getBillOfMaterial();

      if (useProductDefaultBom
          && ((bomChild != null && CollectionUtils.isEmpty(bomChild.getBillOfMaterialLineList()))
              || bomChild == null)
          && bomLineChild.getProduct() != null) {
        bomChild = billOfMaterialService.getBOM(bomLineChild.getProduct(), bom.getCompany());
      }

      if (bomLineChild != null && !processedBomLine.contains(bomLineChild.getId())) {
        TempBomTree childTree =
            getBomTree(bomChild, bomLineChild, bom, bomTree, useProductDefaultBom);
        validTreeIds.add(childTree.getId());
      }
    }

    return validTreeIds;
  }

  @Transactional
  public void removeInvalidTree(List<Long> validTreeIds, BillOfMaterial bom) {

    List<TempBomTree> invalidBomTrees =
        tempBomTreeRepo
            .all()
            .filter("self.id not in (?1) and self.parentBom = ?2", validTreeIds, bom)
            .fetch();

    log.debug("Invalid bom trees: {}", invalidBomTrees);

    if (!invalidBomTrees.isEmpty()) {
      List<TempBomTree> childBomTrees =
          tempBomTreeRepo.all().filter("self.parent in (?1)", invalidBomTrees).fetch();

      for (TempBomTree childBomTree : childBomTrees) {
        childBomTree.setParent(null);
        tempBomTreeRepo.save(childBomTree);
      }
    }

    for (TempBomTree invalidBomTree : invalidBomTrees) {
      tempBomTreeRepo.remove(invalidBomTree);
    }
  }

  @Override
  @Transactional(rollbackOn = {Exception.class})
  public void setBillOfMaterialAsDefault(BillOfMaterial billOfMaterial) throws AxelorException {
    Company company = billOfMaterial.getCompany();
    Product product = billOfMaterial.getProduct();
    if (company != null) {
      productCompanyService.set(product, "defaultBillOfMaterial", billOfMaterial, company);
    }
    product.setDefaultBillOfMaterial(billOfMaterial);
  }

  @Override
  @Transactional(rollbackOn = {Exception.class})
  public void addRawMaterials(
      long billOfMaterialId, ArrayList<LinkedHashMap<String, Object>> rawMaterials)
      throws AxelorException {
    BillOfMaterial billOfMaterial = billOfMaterialRepo.find(billOfMaterialId);
    if (rawMaterials != null && !rawMaterials.isEmpty()) {
      int priority = 0;
      if (!CollectionUtils.isEmpty(billOfMaterial.getBillOfMaterialLineList())) {
        priority =
            Collections.max(
                billOfMaterial.getBillOfMaterialLineList().stream()
                    .map(it -> it.getPriority())
                    .collect(Collectors.toSet()));
      }

      for (LinkedHashMap<String, Object> rawMaterial : rawMaterials) {
        priority += 10;
        BillOfMaterialLine newComponent =
            billOfMaterialLineService.createFromRawMaterial(
                Long.valueOf((int) rawMaterial.get("id")), priority, billOfMaterial);
        billOfMaterial.addBillOfMaterialLineListItem(newComponent);
      }
    } else {
      return;
    }
  }

  @Override
  public BillOfMaterial getDefaultBOM(Product originalProduct, Company company)
      throws AxelorException {

    if (company == null) {
      company = Optional.ofNullable(AuthUtils.getUser()).map(User::getActiveCompany).orElse(null);
    }

    BillOfMaterial billOfMaterial = null;
    if (originalProduct != null) {
      billOfMaterial =
          (BillOfMaterial)
              productCompanyService.get(originalProduct, "defaultBillOfMaterial", company);

      if (billOfMaterial == null) {
        billOfMaterial = originalProduct.getDefaultBillOfMaterial();
      }
    }

    return billOfMaterial;
  }

  @Override
  public List<BillOfMaterial> getAlternativesBOM(Product originalProduct, Company company)
      throws AxelorException {

    BillOfMaterial defaultBOM = this.getDefaultBOM(originalProduct, company);
    return billOfMaterialRepo
        .all()
        .filter(
            "self.product = ?1 AND self.company = ?2 AND self.id != ?3 AND self.statusSelect = ?4",
            originalProduct,
            company,
            defaultBOM != null ? defaultBOM.getId() : 0,
            BillOfMaterialRepository.STATUS_APPLICABLE)
        .fetch();
  }

  protected BillOfMaterial getAnyBOM(Product originalProduct, Company company) {
    return billOfMaterialRepo
        .all()
        .filter(
            "self.product = ?1 AND self.company = ?2 AND self.statusSelect = ?3 AND (self.archived is null or self.archived is false)",
            originalProduct,
            company,
            BillOfMaterialRepository.STATUS_APPLICABLE)
        .order("id")
        .fetchOne();
  }

  @Override
  public BillOfMaterial getBOM(Product originalProduct, Company company) throws AxelorException {

    if (company == null) {
      company = Optional.ofNullable(AuthUtils.getUser()).map(User::getActiveCompany).orElse(null);
    }

    BillOfMaterial billOfMaterial = null;
    if (originalProduct != null) {

      // First we try to search for company specific default BOM in the the original product.
      Object obj =
          productCompanyService.getWithNoDefault(originalProduct, "defaultBillOfMaterial", company);

      if (obj != null) {
        billOfMaterial = (BillOfMaterial) obj;
      }

      BillOfMaterial defaultBillOfMaterial = originalProduct.getDefaultBillOfMaterial();
      // If we can't find any, check for the default BOM for the product if it has the same company
      // as the cost calculation
      if (billOfMaterial == null
          && defaultBillOfMaterial != null
          && defaultBillOfMaterial.getCompany() != null
          && defaultBillOfMaterial.getCompany().equals(company)) {
        billOfMaterial = defaultBillOfMaterial;
      }

      // If we can't find any
      if (billOfMaterial == null) {
        // Get any BOM with original product and company.
        billOfMaterial = getAnyBOM(originalProduct, company);
      }
    }

    return billOfMaterial;
  }

  @Override
  public List<Long> getBillOfMaterialProductsId(Set<Company> companySet) throws AxelorException {

    if (companySet == null || companySet.isEmpty()) {
      return Collections.emptyList();
    }
    String stringQuery =
        "SELECT DISTINCT self.product.id from BillOfMaterial as self WHERE self.company.id in (?1) AND self.product IS NOT NULL";
    Query query = JPA.em().createQuery(stringQuery, Long.class);

    query.setParameter(1, companySet.stream().map(Company::getId).collect(Collectors.toList()));
    List<Long> productIds = (List<Long>) query.getResultList();

    return productIds;
  }

  @Override
  @Transactional(rollbackOn = Exception.class)
  public BillOfMaterial setDraftStatus(BillOfMaterial billOfMaterial) throws AxelorException {
    if (billOfMaterial.getStatusSelect() == null) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_INCONSISTENCY,
          I18n.get(ProductionExceptionMessage.BILL_OF_MATERIAL_NULL_STATUS));
    } else if (billOfMaterial.getStatusSelect() != null
        && billOfMaterial.getStatusSelect() == BillOfMaterialRepository.STATUS_DRAFT) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_INCONSISTENCY,
          I18n.get(ProductionExceptionMessage.BILL_OF_MATERIAL_ALREADY_DRAFT_STATUS));
    }
    billOfMaterial.setStatusSelect(BillOfMaterialRepository.STATUS_DRAFT);
    return billOfMaterialRepo.save(billOfMaterial);
  }

  @Override
  @Transactional(rollbackOn = Exception.class)
  public BillOfMaterial setValidateStatus(BillOfMaterial billOfMaterial) throws AxelorException {
    if (billOfMaterial.getStatusSelect() == null
        || billOfMaterial.getStatusSelect() != BillOfMaterialRepository.STATUS_DRAFT) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_INCONSISTENCY,
          I18n.get(ProductionExceptionMessage.BILL_OF_MATERIAL_VALIDATED_WRONG_STATUS));
    }
    billOfMaterial.setStatusSelect(BillOfMaterialRepository.STATUS_VALIDATED);
    return billOfMaterialRepo.save(billOfMaterial);
  }

  @Override
  @Transactional(rollbackOn = Exception.class)
  public BillOfMaterial setApplicableStatus(BillOfMaterial billOfMaterial) throws AxelorException {
    if (billOfMaterial.getStatusSelect() == null
        || billOfMaterial.getStatusSelect() != BillOfMaterialRepository.STATUS_VALIDATED) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_INCONSISTENCY,
          I18n.get(ProductionExceptionMessage.BILL_OF_MATERIAL_APPLICABLE_WRONG_STATUS));
    }
    billOfMaterial.setStatusSelect(BillOfMaterialRepository.STATUS_APPLICABLE);
    return billOfMaterialRepo.save(billOfMaterial);
  }

  @Override
  @Transactional(rollbackOn = Exception.class)
  public BillOfMaterial setObsoleteStatus(BillOfMaterial billOfMaterial) throws AxelorException {
    if (billOfMaterial.getStatusSelect() == null
        || billOfMaterial.getStatusSelect() != BillOfMaterialRepository.STATUS_APPLICABLE) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_INCONSISTENCY,
          I18n.get(ProductionExceptionMessage.BILL_OF_MATERIAL_OBSOLETE_WRONG_STATUS));
    }
    billOfMaterial.setStatusSelect(BillOfMaterialRepository.STATUS_OBSOLETE);
    return billOfMaterialRepo.save(billOfMaterial);
  }

  @Override
  public int getPriority(BillOfMaterial billOfMaterial) {

    if (!CollectionUtils.isEmpty(billOfMaterial.getBillOfMaterialLineList())) {
      return billOfMaterial.getBillOfMaterialLineList().stream()
          .map(boml -> boml.getPriority())
          .min(Integer::compareTo)
          .orElse(0);
    }

    return 0;
  }

  @Override
  public List<BillOfMaterial> getSubBillOfMaterial(BillOfMaterial billOfMaterial) {

    if (billOfMaterial.getBillOfMaterialLineList() != null) {
      return billOfMaterial.getBillOfMaterialLineList().stream()
          .filter(boml -> boml.getBillOfMaterial() != null)
          .map(BillOfMaterialLine::getBillOfMaterial)
          .collect(Collectors.toList());
    }

    return new ArrayList<>();
  }

  @Override
  public Map<BillOfMaterial, BigDecimal> getSubBillOfMaterialMapWithLineQty(
      BillOfMaterial billOfMaterial) {

    if (billOfMaterial.getBillOfMaterialLineList() != null) {
      return billOfMaterial.getBillOfMaterialLineList().stream()
          .filter(boml -> boml.getBillOfMaterial() != null)
          .collect(
              Collectors.toMap(BillOfMaterialLine::getBillOfMaterial, BillOfMaterialLine::getQty));
    }

    return new HashMap<>();
  }
}
