/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2020 Axelor (<http://axelor.com>).
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
package com.axelor.apps.production.service.costsheet;

import com.axelor.app.AppSettings;
import com.axelor.apps.base.db.Product;
import com.axelor.apps.base.db.repo.ProductRepository;
import com.axelor.apps.base.service.ProductCompanyService;
import com.axelor.apps.base.service.ProductService;
import com.axelor.apps.production.db.BillOfMaterial;
import com.axelor.apps.production.db.CostSheet;
import com.axelor.apps.production.db.UnitCostCalcLine;
import com.axelor.apps.production.db.UnitCostCalculation;
import com.axelor.apps.production.db.repo.BillOfMaterialRepository;
import com.axelor.apps.production.db.repo.UnitCostCalcLineRepository;
import com.axelor.apps.production.db.repo.UnitCostCalculationRepository;
import com.axelor.apps.production.exceptions.IExceptionMessage;
import com.axelor.apps.production.service.app.AppProductionService;
import com.axelor.apps.tool.StringTool;
import com.axelor.apps.tool.file.CsvTool;
import com.axelor.data.csv.CSVImporter;
import com.axelor.db.JPA;
import com.axelor.dms.db.DMSFile;
import com.axelor.exception.AxelorException;
import com.axelor.exception.db.repo.TraceBackRepository;
import com.axelor.exception.service.TraceBackService;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.axelor.meta.MetaFiles;
import com.axelor.meta.db.MetaFile;
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
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.validation.ValidationException;
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
      ProductCompanyService productCompanyService) {
    this.productRepository = productRepository;
    this.unitCostCalculationRepository = unitCostCalculationRepository;
    this.unitCostCalcLineService = unitCostCalcLineService;
    this.costSheetService = costSheetService;
    this.unitCostCalcLineRepository = unitCostCalcLineRepository;
    this.appProductionService = appProductionService;
    this.productService = productService;
    this.productCompanyService = productCompanyService;
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

    String filePath = AppSettings.get().get("file.upload.dir");
    Path path = Paths.get(filePath, fileName);
    File file = path.toFile();

    log.debug("File located at: {}", path);

    String[] headers = {
      I18n.get("Product_code"),
      I18n.get("Product_name"),
      I18n.get("Product_currency"),
      I18n.get("Computed_cost"),
      I18n.get("Cost_to_apply")
    };

    CsvTool.csvWriter(filePath, fileName, ';', '"', headers, list);

    try (InputStream is = new FileInputStream(file)) {
      DMSFile dmsFile = Beans.get(MetaFiles.class).attach(is, fileName, unitCostCalculation);
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
      configFile = File.createTempFile("input-config", ".xml");
      InputStream bindFileInputStream =
          this.getClass().getResourceAsStream("/import-configs/" + "csv-config.xml");
      if (bindFileInputStream == null) {
        throw new ValidationException(IExceptionMessage.UNIT_COST_CALCULATION_IMPORT_FAIL_ERROR);
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
    this.assignProductAndLevel(this.getProductList(unitCostCalculation));

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

    unitCostCalculation.setCalculationDate(appProductionService.getTodayDate());

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

    log.debug("Unit cost price calculation for product : {}, level : {}", product.getCode(), level);

    int origin =
        unitCostCalculation.getAllBomLevels()
            ? CostSheetService.ORIGIN_BULK_UNIT_COST_CALCULATION
            : CostSheetService.ORIGIN_BILL_OF_MATERIAL;

    BillOfMaterial billOfMaterial = product.getDefaultBillOfMaterial();

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

    if (!unitCostCalculation.getProductSet().isEmpty()) {

      productSet.addAll(unitCostCalculation.getProductSet());
    }

    List<Integer> productSubTypeSelects =
        StringTool.getIntegerList(unitCostCalculation.getProductSubTypeSelect());

    if (!unitCostCalculation.getProductCategorySet().isEmpty()) {

      productSet.addAll(
          productRepository
              .all()
              .filter(
                  "self.productCategory in (?1) AND self.productTypeSelect = ?2 AND self.productSubTypeSelect in (?3)"
                      + " AND self.defaultBillOfMaterial.company in (?4) AND self.procurementMethodSelect in (?5, ?6)"
                      + " AND dtype = 'Product'",
                  unitCostCalculation.getProductCategorySet(),
                  ProductRepository.PRODUCT_TYPE_STORABLE,
                  productSubTypeSelects,
                  unitCostCalculation.getCompanySet(),
                  ProductRepository.PROCUREMENT_METHOD_PRODUCE,
                  ProductRepository.PROCUREMENT_METHOD_BUYANDPRODUCE)
              .fetch());
    }

    if (!unitCostCalculation.getProductFamilySet().isEmpty()) {

      productSet.addAll(
          productRepository
              .all()
              .filter(
                  "self.productFamily in (?1) AND self.productTypeSelect = ?2 AND self.productSubTypeSelect in (?3)"
                      + " AND self.defaultBillOfMaterial.company in (?4) AND self.procurementMethodSelect in (?5, ?6)" 
                      + " AND dtype = 'Product'",
                  unitCostCalculation.getProductFamilySet(),
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
          I18n.get(IExceptionMessage.UNIT_COST_CALCULATION_NO_PRODUCT));
    }

    return productSet;
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

  protected void assignProductAndLevel(Set<Product> productList) {

    productMap = Maps.newHashMap();

    for (Product product : productList) {

      this.assignProductAndLevel(product);
    }
  }

  protected boolean hasValidBillOfMaterial(Product product) {

    BillOfMaterial defaultBillOfMaterial = product.getDefaultBillOfMaterial();

    if (defaultBillOfMaterial != null
        && (defaultBillOfMaterial.getStatusSelect() == BillOfMaterialRepository.STATUS_VALIDATED
            || defaultBillOfMaterial.getStatusSelect()
                == BillOfMaterialRepository.STATUS_APPLICABLE)
        && (product.getProcurementMethodSelect()
                == ProductRepository.PROCUREMENT_METHOD_BUYANDPRODUCE
            || product.getProcurementMethodSelect()
                == ProductRepository.PROCUREMENT_METHOD_PRODUCE)) {
      return true;
    }
    return false;
  }

  protected void assignProductAndLevel(Product product) {

    log.debug("Add of the product : {}", product.getFullName());
    this.productMap.put(product.getId(), this.getMaxLevel(product, 0));

    if (hasValidBillOfMaterial(product)) {
      this.assignProductLevel(product.getDefaultBillOfMaterial(), 0);
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
   */
  protected void assignProductLevel(BillOfMaterial billOfMaterial, int level) {

    if (billOfMaterial.getBillOfMaterialSet() == null
        || billOfMaterial.getBillOfMaterialSet().isEmpty()
        || level > 100) {

      Product subProduct = billOfMaterial.getProduct();

      log.debug("Add of the sub product : {} for the level : {} ", subProduct.getFullName(), level);
      this.productMap.put(subProduct.getId(), this.getMaxLevel(subProduct, level));

    } else {

      level = level + 1;

      for (BillOfMaterial subBillOfMaterial : billOfMaterial.getBillOfMaterialSet()) {

        Product subProduct = subBillOfMaterial.getProduct();

        if (this.productMap.containsKey(subProduct.getId())) {
          this.assignProductLevel(subBillOfMaterial, level);

          if (hasValidBillOfMaterial(subProduct)) {
            this.assignProductLevel(subProduct.getDefaultBillOfMaterial(), level);
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

  @Transactional
  protected void updateUnitCosts(UnitCostCalcLine unitCostCalcLine) throws AxelorException {

    Product product = unitCostCalcLine.getProduct();

    productCompanyService.set(product, "costPrice", unitCostCalcLine
            .getCostToApply()
            .setScale(
                appProductionService.getNbDecimalDigitForUnitPrice(), BigDecimal.ROUND_HALF_UP),
            unitCostCalcLine.getCompany());

    productService.updateSalePrice(product, unitCostCalcLine.getCompany());
  }

  @Transactional
  protected void updateStatusProductCostPriceUpdated(UnitCostCalculation unitCostCalculation) {

    unitCostCalculation.setUpdateCostDate(appProductionService.getTodayDate());

    unitCostCalculation.setStatusSelect(UnitCostCalculationRepository.STATUS_COSTS_UPDATED);

    unitCostCalculationRepository.save(unitCostCalculation);
  }
}
