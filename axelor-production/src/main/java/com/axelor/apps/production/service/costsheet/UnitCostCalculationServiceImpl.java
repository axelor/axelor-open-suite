/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2005-2024 Axelor (<http://axelor.com>).
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
package com.axelor.apps.production.service.costsheet;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.Product;
import com.axelor.apps.base.db.ProductCategory;
import com.axelor.apps.base.db.ProductFamily;
import com.axelor.apps.base.db.repo.ProductRepository;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.apps.base.service.ProductCompanyService;
import com.axelor.apps.base.service.ProductService;
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.apps.base.service.exception.TraceBackService;
import com.axelor.apps.production.db.BillOfMaterial;
import com.axelor.apps.production.db.CostSheet;
import com.axelor.apps.production.db.UnitCostCalcLine;
import com.axelor.apps.production.db.UnitCostCalculation;
import com.axelor.apps.production.db.repo.BillOfMaterialRepository;
import com.axelor.apps.production.db.repo.UnitCostCalcLineRepository;
import com.axelor.apps.production.db.repo.UnitCostCalculationRepository;
import com.axelor.apps.production.exceptions.ProductionExceptionMessage;
import com.axelor.apps.production.service.BillOfMaterialService;
import com.axelor.apps.production.service.app.AppProductionService;
import com.axelor.auth.AuthUtils;
import com.axelor.auth.db.User;
import com.axelor.common.ObjectUtils;
import com.axelor.data.csv.CSVImporter;
import com.axelor.db.JPA;
import com.axelor.dms.db.DMSFile;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.axelor.meta.MetaFiles;
import com.axelor.meta.db.MetaField;
import com.axelor.meta.db.MetaFile;
import com.axelor.utils.StringTool;
import com.axelor.utils.file.CsvTool;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.common.io.Files;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.invoke.MethodHandles;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import javax.validation.ValidationException;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UnitCostCalculationServiceImpl implements UnitCostCalculationService {

  private final Logger log = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  protected ProductRepository productRepository;
  protected UnitCostCalculationRepository unitCostCalculationRepository;
  protected UnitCostCalcLineService unitCostCalcLineService;
  protected CostSheetService costSheetService;
  protected UnitCostCalcLineRepository unitCostCalcLineRepository;
  protected AppProductionService appProductionService;
  protected ProductService productService;
  protected ProductCompanyService productCompanyService;
  protected AppBaseService appBaseService;
  protected BillOfMaterialService billOfMaterialService;

  protected Map<Long, Integer> productMap;

  @Inject
  public UnitCostCalculationServiceImpl(
      ProductRepository productRepository,
      UnitCostCalculationRepository unitCostCalculationRepository,
      UnitCostCalcLineService unitCostCalcLineService,
      CostSheetService costSheetService,
      UnitCostCalcLineRepository unitCostCalcLineRepository,
      AppProductionService appProductionService,
      ProductService productService,
      ProductCompanyService productCompanyService,
      AppBaseService appBaseService,
      BillOfMaterialService billOfMaterialService) {
    this.productRepository = productRepository;
    this.unitCostCalculationRepository = unitCostCalculationRepository;
    this.unitCostCalcLineService = unitCostCalcLineService;
    this.costSheetService = costSheetService;
    this.unitCostCalcLineRepository = unitCostCalcLineRepository;
    this.appProductionService = appProductionService;
    this.productService = productService;
    this.productCompanyService = productCompanyService;
    this.appBaseService = appBaseService;
    this.billOfMaterialService = billOfMaterialService;
  }

  @Override
  public MetaFile exportUnitCostCalc(UnitCostCalculation unitCostCalculation, String fileName)
      throws IOException {
    List<String[]> list = new ArrayList<>();
    List<UnitCostCalcLine> unitCostCalcLineList = unitCostCalculation.getUnitCostCalcLineList();

    Collections.sort(
        unitCostCalcLineList,
        new Comparator<UnitCostCalcLine>() {
          @Override
          public int compare(
              UnitCostCalcLine unitCostCalcLine1, UnitCostCalcLine unitCostCalcLine2) {
            return unitCostCalcLine1
                .getProduct()
                .getCode()
                .compareTo(unitCostCalcLine2.getProduct().getCode());
          }
        });

    for (UnitCostCalcLine unitCostCalcLine : unitCostCalcLineList) {
      String[] item = new String[4];
      item[0] =
          unitCostCalcLine.getProduct() == null ? "" : unitCostCalcLine.getProduct().getCode();
      item[1] =
          unitCostCalcLine.getProduct() == null ? "" : unitCostCalcLine.getProduct().getName();
      item[2] = unitCostCalcLine.getComputedCost().toString();
      item[3] = unitCostCalcLine.getCostToApply().toString();

      list.add(item);
    }

    File file = MetaFiles.createTempFile(fileName, ".csv").toFile();

    log.debug("File located at: {}", file.getPath());

    String[] headers = {
      I18n.get("Product_code"),
      I18n.get("Product_name"),
      I18n.get("Computed_cost"),
      I18n.get("Cost_to_apply")
    };

    CsvTool.csvWriter(file.getParent(), file.getName(), ';', '"', headers, list);

    try (InputStream is = new FileInputStream(file)) {
      DMSFile dmsFile = Beans.get(MetaFiles.class).attach(is, file.getName(), unitCostCalculation);
      return dmsFile.getMetaFile();
    }
  }

  @Override
  public void importUnitCostCalc(MetaFile dataFile, UnitCostCalculation unitCostCalculation)
      throws IOException {
    File tempDir = Files.createTempDir();
    File csvFile = new File(tempDir, "unitcostcalc.csv");
    Files.copy(MetaFiles.getPath(dataFile).toFile(), csvFile);
    File configXmlFile = this.getConfigXmlFile();
    CSVImporter csvImporter =
        new CSVImporter(configXmlFile.getAbsolutePath(), tempDir.getAbsolutePath());
    Map<String, Object> context = new HashMap<>();
    context.put("_unitCostCalculation", unitCostCalculation.getId());
    csvImporter.setContext(context);
    csvImporter.run();
  }

  protected File getConfigXmlFile() {
    File configFile = null;
    try {
      configFile = MetaFiles.createTempFile("input-config", ".xml").toFile();
      InputStream bindFileInputStream =
          this.getClass().getResourceAsStream("/import-configs/" + "csv-config.xml");
      if (bindFileInputStream == null) {
        throw new ValidationException(
            ProductionExceptionMessage.UNIT_COST_CALCULATION_IMPORT_FAIL_ERROR);
      }
      FileOutputStream outputStream = new FileOutputStream(configFile);
      IOUtils.copy(bindFileInputStream, outputStream);
    } catch (Exception e) {
      TraceBackService.trace(e);
    }

    return configFile;
  }

  @Override
  public void runUnitCostCalc(UnitCostCalculation unitCostCalculation) throws AxelorException {

    if (!unitCostCalculation.getUnitCostCalcLineList().isEmpty()) {
      clear(unitCostCalculation);
    }

    unitCostCalculation = unitCostCalculationRepository.find(unitCostCalculation.getId());
    this.assignProductAndLevel(
        this.getProductList(unitCostCalculation), this.getSingleCompany(unitCostCalculation));

    calculationProcess(unitCostCalculation);

    updateStatusToComputed(unitCostCalculationRepository.find(unitCostCalculation.getId()));
  }

  @Transactional
  protected void clear(UnitCostCalculation unitCostCalculation) {

    unitCostCalculation.clearUnitCostCalcLineList();

    unitCostCalculationRepository.save(unitCostCalculation);
  }

  @Transactional
  protected void updateStatusToComputed(UnitCostCalculation unitCostCalculation) {

    unitCostCalculation.setCalculationDateTime(
        appProductionService
            .getTodayDateTime(
                Optional.ofNullable(AuthUtils.getUser()).map(User::getActiveCompany).orElse(null))
            .toLocalDateTime());

    unitCostCalculation.setStatusSelect(UnitCostCalculationRepository.STATUS_COSTS_COMPUTED);

    unitCostCalculationRepository.save(unitCostCalculation);
  }

  protected void calculationProcess(UnitCostCalculation unitCostCalculation)
      throws AxelorException {

    for (int level = this.getMaxLevel(); level >= 0; level--) {

      for (Product product : this.getProductList(level)) {

        this.calculationProductProcess(
            unitCostCalculationRepository.find(unitCostCalculation.getId()),
            productRepository.find(product.getId()));

        JPA.clear();
      }
    }
  }

  @Transactional(rollbackOn = {Exception.class})
  protected void calculationProductProcess(UnitCostCalculation unitCostCalculation, Product product)
      throws AxelorException {

    int level = this.productMap.get(product.getId()).intValue();
    Company company = this.getSingleCompany(unitCostCalculation);

    log.debug("Unit cost price calculation for product : {}, level : {}", product.getCode(), level);

    int origin =
        unitCostCalculation.getAllBomLevels()
            ? CostSheetService.ORIGIN_BULK_UNIT_COST_CALCULATION
            : CostSheetService.ORIGIN_BILL_OF_MATERIAL;

    BillOfMaterial billOfMaterial = billOfMaterialService.getBOM(product, company);

    if (billOfMaterial == null) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
          I18n.get(ProductionExceptionMessage.NO_APPLICABLE_BILL_OF_MATERIALS),
          product.getFullName());
    }

    CostSheet costSheet =
        costSheetService.computeCostPrice(billOfMaterial, origin, unitCostCalculation);

    UnitCostCalcLine unitCostCalcLine =
        unitCostCalcLineService.createUnitCostCalcLine(
            product, billOfMaterial.getCompany(), level, costSheet);
    unitCostCalculation.addUnitCostCalcLineListItem(unitCostCalcLine);
    unitCostCalculationRepository.save(unitCostCalculation);
  }

  protected Set<Product> getProductList(UnitCostCalculation unitCostCalculation)
      throws AxelorException {

    Set<Product> productSet = Sets.newHashSet();

    Set<Product> selectedProductSet = unitCostCalculation.getProductSet();
    Set<ProductCategory> productCategorySet = unitCostCalculation.getProductCategorySet();
    Set<ProductFamily> productFamilySet = unitCostCalculation.getProductFamilySet();
    if (ObjectUtils.isEmpty(selectedProductSet)
        && ObjectUtils.isEmpty(productCategorySet)
        && ObjectUtils.isEmpty(productFamilySet)) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
          I18n.get(ProductionExceptionMessage.UNIT_COST_CALCULATION_CHOOSE_FILTERS));
    }

    if (!selectedProductSet.isEmpty()) {

      productSet.addAll(selectedProductSet);
    }

    List<Integer> productSubTypeSelects =
        StringTool.getIntegerList(unitCostCalculation.getProductSubTypeSelect());

    if (!productCategorySet.isEmpty()) {

      productSet.addAll(
          productRepository
              .all()
              .filter(
                  "self.productCategory in (?1) AND self.productTypeSelect = ?2 AND self.productSubTypeSelect in (?3)"
                      + " AND self.defaultBillOfMaterial.company in (?4) AND self.procurementMethodSelect in (?5, ?6)"
                      + " AND self.dtype = 'Product'",
                  productCategorySet,
                  ProductRepository.PRODUCT_TYPE_STORABLE,
                  productSubTypeSelects,
                  unitCostCalculation.getCompanySet(),
                  ProductRepository.PROCUREMENT_METHOD_PRODUCE,
                  ProductRepository.PROCUREMENT_METHOD_BUYANDPRODUCE)
              .fetch());
    }

    if (!productFamilySet.isEmpty()) {

      productSet.addAll(
          productRepository
              .all()
              .filter(
                  "self.productFamily in (?1) AND self.productTypeSelect = ?2 AND self.productSubTypeSelect in (?3)"
                      + " AND self.defaultBillOfMaterial.company in (?4) AND self.procurementMethodSelect in (?5, ?6)"
                      + " AND self.dtype = 'Product'",
                  productFamilySet,
                  ProductRepository.PRODUCT_TYPE_STORABLE,
                  productSubTypeSelects,
                  unitCostCalculation.getCompanySet(),
                  ProductRepository.PROCUREMENT_METHOD_PRODUCE,
                  ProductRepository.PROCUREMENT_METHOD_BUYANDPRODUCE)
              .fetch());
    }

    if (productSet.isEmpty()) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
          I18n.get(ProductionExceptionMessage.UNIT_COST_CALCULATION_NO_PRODUCT_FOUND));
    }

    Company company = this.getSingleCompany(unitCostCalculation);

    Map<Product, Integer> productLevelMap = new HashMap<>();
    for (Product product : productSet) {
      productLevelMap.put(
          product, calculateHierarchyDepth(product, company, unitCostCalculation, new HashSet<>()));
    }

    return productSet.stream()
        .sorted(
            Comparator.comparing(Product::getProductSubTypeSelect)
                .reversed()
                .thenComparing(productLevelMap::get))
        .collect(Collectors.toCollection(LinkedHashSet::new));
  }

  public int calculateHierarchyDepth(
      Product product,
      Company company,
      UnitCostCalculation unitCostCalculation,
      Set<Product> visited)
      throws AxelorException {
    if (visited.contains(product)) {
      return 0;
    }
    visited.add(product);

    BillOfMaterial bom = billOfMaterialService.getBOM(product, company);
    if (bom == null || ObjectUtils.isEmpty(bom.getBillOfMaterialSet())) {
      return 0;
    }

    int maxDepth = 0;
    for (BillOfMaterial subBom : bom.getBillOfMaterialSet()) {
      Product subProduct = subBom.getProduct();
      int subDepth = calculateHierarchyDepth(subProduct, company, unitCostCalculation, visited);
      maxDepth = Math.max(maxDepth, subDepth);
    }
    return maxDepth + 1;
  }

  /**
   * Get the list of product for a level
   *
   * @param level
   * @return
   */
  protected List<Product> getProductList(int level) {

    List<Product> productList = Lists.newArrayList();

    for (Long productId : this.productMap.keySet()) {

      if (this.productMap.get(productId) == level) {
        productList.add(productRepository.find(productId));
      }
    }

    return productList;
  }

  protected void assignProductAndLevel(Set<Product> productList, Company company)
      throws AxelorException {

    productMap = Maps.newHashMap();

    for (Product product : productList) {

      this.assignProductAndLevel(product, company);
    }
  }

  protected boolean hasValidBillOfMaterial(Product product, Company company)
      throws AxelorException {

    BillOfMaterial defaultBillOfMaterial = billOfMaterialService.getDefaultBOM(product, company);

    if (defaultBillOfMaterial != null
        && (defaultBillOfMaterial.getStatusSelect() == BillOfMaterialRepository.STATUS_VALIDATED
            || defaultBillOfMaterial.getStatusSelect()
                == BillOfMaterialRepository.STATUS_APPLICABLE)
        && (product
                .getProcurementMethodSelect()
                .equals(ProductRepository.PROCUREMENT_METHOD_BUYANDPRODUCE)
            || product
                .getProcurementMethodSelect()
                .equals(ProductRepository.PROCUREMENT_METHOD_PRODUCE))) {
      return true;
    }
    return false;
  }

  protected void assignProductAndLevel(Product product, Company company) throws AxelorException {

    log.debug("Add of the product : {}", product.getFullName());
    this.productMap.put(product.getId(), this.getMaxLevel(product, 0));

    if (hasValidBillOfMaterial(product, company)) {
      this.assignProductLevel(billOfMaterialService.getDefaultBOM(product, company), 0, company);
    }
  }

  protected int getMaxLevel(Product product, int level) {

    if (this.productMap.containsKey(product.getId())) {
      return Math.max(level, this.productMap.get(product.getId()));
    }

    return level;
  }

  protected int getMaxLevel() {

    int maxLevel = 0;

    for (int level : this.productMap.values()) {

      if (level > maxLevel) {
        maxLevel = level;
      }
    }

    return maxLevel;
  }

  /**
   * Update the level of Bill of materials. The highest for each product (0: product with parent, 1:
   * product with a parent, 2: product with a parent that have a parent, ...)
   *
   * @param billOfMaterial
   * @param level
   * @param company
   * @throws AxelorException
   */
  protected void assignProductLevel(BillOfMaterial billOfMaterial, int level, Company company)
      throws AxelorException {

    if (level > 50) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
          I18n.get(ProductionExceptionMessage.LOOP_IN_BILL_OF_MATERIALS));
    }

    Product product = billOfMaterial.getProduct();

    log.debug("Add of the sub product : {} for the level : {} ", product.getFullName(), level);
    this.productMap.put(product.getId(), this.getMaxLevel(product, level));

    if (CollectionUtils.isNotEmpty(billOfMaterial.getBillOfMaterialSet())) {

      level = level + 1;

      for (BillOfMaterial subBillOfMaterial : billOfMaterial.getBillOfMaterialSet()) {

        Product subProduct = subBillOfMaterial.getProduct();

        if (this.productMap.containsKey(subProduct.getId())) {
          this.assignProductLevel(subBillOfMaterial, level, company);

          if (hasValidBillOfMaterial(subProduct, company)) {
            this.assignProductLevel(
                billOfMaterialService.getDefaultBOM(subProduct, company), level, company);
          }
        }
      }
    }
  }

  public void updateUnitCosts(UnitCostCalculation unitCostCalculation) throws AxelorException {

    for (UnitCostCalcLine unitCostCalcLine : unitCostCalculation.getUnitCostCalcLineList()) {

      updateUnitCosts(unitCostCalcLineRepository.find(unitCostCalcLine.getId()));

      JPA.clear();
    }

    updateStatusProductCostPriceUpdated(
        unitCostCalculationRepository.find(unitCostCalculation.getId()));
  }

  @Transactional(rollbackOn = {Exception.class})
  protected void updateUnitCosts(UnitCostCalcLine unitCostCalcLine) throws AxelorException {

    Product product = unitCostCalcLine.getProduct();

    productCompanyService.set(
        product,
        "costPrice",
        unitCostCalcLine
            .getCostToApply()
            .setScale(
                appProductionService.getNbDecimalDigitForUnitPrice(), BigDecimal.ROUND_HALF_UP),
        unitCostCalcLine.getCompany());

    productService.updateSalePrice(product, unitCostCalcLine.getCompany());
  }

  @Transactional
  protected void updateStatusProductCostPriceUpdated(UnitCostCalculation unitCostCalculation) {

    unitCostCalculation.setUpdateCostDateTime(
        appProductionService
            .getTodayDateTime(
                Optional.ofNullable(AuthUtils.getUser()).map(User::getActiveCompany).orElse(null))
            .toLocalDateTime());

    unitCostCalculation.setStatusSelect(UnitCostCalculationRepository.STATUS_COSTS_UPDATED);

    unitCostCalculationRepository.save(unitCostCalculation);
  }

  @Override
  public String createProductSetDomain(UnitCostCalculation unitCostCalculation, Company company)
      throws AxelorException {
    String domain;
    String bomsProductsList = createBomProductList(unitCostCalculation, company);
    if (bomsProductsList.isEmpty()) {
      bomsProductsList = "0";
    }
    if (this.hasDefaultBOMSelected()) {
      if (company != null) {
        domain =
            "(self.id in ("
                + bomsProductsList
                + ")"
                + " OR self.defaultBillOfMaterial.company.id = "
                + company.getId().toString()
                + ")";
      } else {
        domain = "self.defaultBillOfMaterial IS NOT NULL";
      }

      if (unitCostCalculation.getProductCategorySet() != null
          && !unitCostCalculation.getProductCategorySet().isEmpty()) {
        domain +=
            " AND self.productCategory IN ("
                + StringTool.getIdListString(unitCostCalculation.getProductCategorySet())
                + ")";
      }

      if (unitCostCalculation.getProductFamilySet() != null
          && !unitCostCalculation.getProductFamilySet().isEmpty()) {
        domain +=
            " AND self.productFamily IN ("
                + StringTool.getIdListString(unitCostCalculation.getProductFamilySet())
                + ")";
      }

      domain +=
          " AND self.productTypeSelect = 'storable' AND self.productSubTypeSelect IN ("
              + unitCostCalculation.getProductSubTypeSelect()
              + ")";
    } else {
      domain =
          " self.productTypeSelect = 'storable' AND self.productSubTypeSelect IN ("
              + unitCostCalculation.getProductSubTypeSelect()
              + ") AND (self.defaultBillOfMaterial.company IN ("
              + StringTool.getIdListString(unitCostCalculation.getCompanySet())
              + ") OR self.id in ("
              + bomsProductsList
              + ")"
              + ") AND self.procurementMethodSelect IN ('produce', 'buyAndProduce') AND self.dtype = 'Product'";
    }
    log.debug("Product Domain: {}", domain);
    return domain;
  }

  protected String createBomProductList(UnitCostCalculation unitCostCalculation, Company company)
      throws AxelorException {

    Set<Company> companySet = new HashSet<>();

    if (unitCostCalculation.getCompanySet() != null) {
      companySet.addAll(unitCostCalculation.getCompanySet());
    }
    if (company != null) {
      companySet.add(company);
    }

    return billOfMaterialService.getBillOfMaterialProductsId(companySet).stream()
        .map(Object::toString)
        .collect(Collectors.joining(","));
  }

  @Override
  @Transactional
  public void fillCompanySet(UnitCostCalculation unitCostCalculation, Company company) {
    if (company != null) {
      unitCostCalculation.getCompanySet().clear();
      unitCostCalculation.addCompanySetItem(company);
      unitCostCalculationRepository.save(unitCostCalculation);
    }
  }

  @Override
  public Boolean hasDefaultBOMSelected() {
    Boolean containsDefaultBOMField = false;
    Set<MetaField> companySpecificFields =
        appBaseService.getAppBase().getCompanySpecificProductFieldsSet();
    for (MetaField field : companySpecificFields) {
      if (field.getName().equals("defaultBillOfMaterial")) {
        containsDefaultBOMField = true;
        break;
      }
    }
    return containsDefaultBOMField;
  }

  @Override
  public Company getSingleCompany(UnitCostCalculation unitCostCalculation) {
    Company company = null;
    if (unitCostCalculation.getCompanySet().size() == 1) {
      Iterator<Company> companyIterator = unitCostCalculation.getCompanySet().iterator();
      company = companyIterator.next();
    }
    return company;
  }
}
