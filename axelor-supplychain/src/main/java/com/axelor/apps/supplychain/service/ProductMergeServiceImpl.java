/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2005-2026 Axelor (<http://axelor.com>).
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
package com.axelor.apps.supplychain.service;

import com.axelor.apps.account.db.InvoiceLine;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.ABCAnalysis;
import com.axelor.apps.base.db.ABCAnalysisLine;
import com.axelor.apps.base.db.Product;
import com.axelor.apps.base.db.ProductCompany;
import com.axelor.apps.base.db.repo.ProductRepository;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.apps.purchase.db.SupplierCatalog;
import com.axelor.apps.purchase.db.repo.SupplierCatalogRepository;
import com.axelor.apps.stock.db.StockHistoryLine;
import com.axelor.apps.stock.db.StockLocationLine;
import com.axelor.apps.stock.db.StockRules;
import com.axelor.apps.stock.db.TrackingNumber;
import com.axelor.apps.supplychain.db.ProductMergeLog;
import com.axelor.apps.supplychain.db.UnitCostCalcLine;
import com.axelor.apps.supplychain.db.UnitCostCalculation;
import com.axelor.apps.supplychain.db.repo.MrpRepository;
import com.axelor.apps.supplychain.db.repo.ProductMergeLogRepository;
import com.axelor.apps.supplychain.exception.SupplychainExceptionMessage;
import com.axelor.apps.supplychain.service.app.AppSupplychainService;
import com.axelor.auth.AuthUtils;
import com.axelor.auth.db.User;
import com.axelor.db.JPA;
import com.axelor.db.internal.DBHelper;
import com.axelor.db.mapper.Mapper;
import com.axelor.db.mapper.Property;
import com.axelor.dms.db.DMSFile;
import com.axelor.dms.db.repo.DMSFileRepository;
import com.axelor.i18n.I18n;
import com.axelor.meta.db.MetaField;
import com.axelor.meta.db.MetaJsonField;
import com.axelor.meta.db.repo.MetaFieldRepository;
import com.axelor.meta.db.repo.MetaJsonFieldRepository;
import com.google.inject.persist.Transactional;
import jakarta.inject.Inject;
import jakarta.persistence.Query;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ProductMergeServiceImpl implements ProductMergeService {

  protected final AppSupplychainService appSupplychainService;
  protected final ProductRepository productRepository;
  protected final MrpRepository mrpRepository;
  protected final MetaFieldRepository metaFieldRepository;
  protected final MetaJsonFieldRepository metaJsonFieldRepository;
  protected final DMSFileRepository dmsFileRepository;
  protected final SupplierCatalogRepository supplierCatalogRepository;
  protected final ProductMergeStockService productMergeStockService;
  protected final ProductMergeLogRepository productMergeLogRepository;

  @Inject
  public ProductMergeServiceImpl(
      AppSupplychainService appSupplychainService,
      ProductRepository productRepository,
      MrpRepository mrpRepository,
      MetaFieldRepository metaFieldRepository,
      MetaJsonFieldRepository metaJsonFieldRepository,
      DMSFileRepository dmsFileRepository,
      SupplierCatalogRepository supplierCatalogRepository,
      ProductMergeStockService productMergeStockService,
      ProductMergeLogRepository productMergeLogRepository) {
    this.appSupplychainService = appSupplychainService;
    this.productRepository = productRepository;
    this.mrpRepository = mrpRepository;
    this.metaFieldRepository = metaFieldRepository;
    this.metaJsonFieldRepository = metaJsonFieldRepository;
    this.dmsFileRepository = dmsFileRepository;
    this.supplierCatalogRepository = supplierCatalogRepository;
    this.productMergeStockService = productMergeStockService;
    this.productMergeLogRepository = productMergeLogRepository;
  }

  @Override
  public void checkAuthorization(User user) throws AxelorException {
    if (!appSupplychainService.isApp("supplychain")) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
          I18n.get(SupplychainExceptionMessage.PRODUCT_MERGE_APP_NOT_INSTALLED));
    }

    Set<User> authorizedUserSet =
        appSupplychainService.getAppSupplychain().getProductMergeAuthorizedUserSet();
    if (authorizedUserSet == null || authorizedUserSet.isEmpty()) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
          I18n.get(SupplychainExceptionMessage.PRODUCT_MERGE_NO_AUTHORIZED_USER));
    }
    if (!authorizedUserSet.contains(user)) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
          I18n.get(SupplychainExceptionMessage.PRODUCT_MERGE_USER_NOT_AUTHORIZED),
          getNames(authorizedUserSet.stream().map(User::getFullName)));
    }
  }

  @Override
  public void checkMerge(Product absorbedProduct, Product keptProduct) throws AxelorException {
    List<String> blockingChecks = getMergeBlockingChecks(absorbedProduct, keptProduct);
    if (blockingChecks.isEmpty()) {
      return;
    }

    // the reasons are listed under a heading: run together in a single paragraph, several of
    // them cannot be read
    StringBuilder message =
        new StringBuilder(
            String.format(
                I18n.get(SupplychainExceptionMessage.PRODUCT_MERGE_BLOCKED),
                absorbedProduct.getCode(),
                keptProduct.getCode()));
    for (String blockingCheck : blockingChecks) {
      message.append("<br/>\u2022 ").append(blockingCheck);
    }

    throw new AxelorException(TraceBackRepository.CATEGORY_INCONSISTENCY, message.toString());
  }

  @Override
  @Transactional(rollbackOn = {Exception.class})
  public ProductMergeResult merge(Product absorbedProduct, Product keptProduct)
      throws AxelorException {
    User user = AuthUtils.getUser();
    checkAuthorization(user);
    checkMerge(absorbedProduct, keptProduct);

    ProductMergeResult result = new ProductMergeResult();
    transferSupplierCatalogs(absorbedProduct, keptProduct, result);
    transferReferences(absorbedProduct, keptProduct, result);
    transferDmsFiles(absorbedProduct, keptProduct, result);
    transferCustomFields(absorbedProduct, keptProduct, result);
    postReferenceTransfer(absorbedProduct, keptProduct, result);
    productMergeStockService.transferStock(absorbedProduct, keptProduct, result);
    postMerge(absorbedProduct, keptProduct, result);
    archiveAbsorbedProduct(absorbedProduct, keptProduct);
    createProductMergeLog(absorbedProduct, keptProduct, user, result);
    return result;
  }

  /**
   * Returns the message of every condition blocking the merge of the absorbed product into the kept
   * product. An empty list means the merge can be run.
   *
   * <p>Modules extending the product merge add their own conditions by overriding this method.
   */
  protected List<String> getMergeBlockingChecks(Product absorbedProduct, Product keptProduct) {
    List<String> blockingChecks = new ArrayList<>();

    if (absorbedProduct.equals(keptProduct)) {
      blockingChecks.add(I18n.get(SupplychainExceptionMessage.PRODUCT_MERGE_SAME_PRODUCT));
      return blockingChecks;
    }

    blockingChecks.addAll(getStatusChecks(absorbedProduct, keptProduct));
    blockingChecks.addAll(getUnitChecks(absorbedProduct, keptProduct));
    blockingChecks.addAll(getTypeChecks(absorbedProduct, keptProduct));
    blockingChecks.addAll(getVariantChecks(absorbedProduct, keptProduct));
    blockingChecks.addAll(getMrpChecks());
    blockingChecks.addAll(
        productMergeStockService.getStockBlockingChecks(absorbedProduct, keptProduct));

    return blockingChecks;
  }

  protected List<String> getStatusChecks(Product absorbedProduct, Product keptProduct) {
    List<String> checks = new ArrayList<>();
    for (Product product : Arrays.asList(absorbedProduct, keptProduct)) {
      if (Boolean.TRUE.equals(product.getArchived())) {
        checks.add(
            String.format(
                I18n.get(SupplychainExceptionMessage.PRODUCT_MERGE_ARCHIVED_PRODUCT),
                product.getCode()));
      }
      if (product.getMergedIntoProduct() != null) {
        checks.add(
            String.format(
                I18n.get(SupplychainExceptionMessage.PRODUCT_MERGE_ALREADY_MERGED),
                product.getCode(),
                product.getMergedIntoProduct().getCode()));
      }
    }
    return checks;
  }

  protected List<String> getUnitChecks(Product absorbedProduct, Product keptProduct) {
    List<String> checks = new ArrayList<>();
    if (!Objects.equals(absorbedProduct.getUnit(), keptProduct.getUnit())) {
      checks.add(I18n.get(SupplychainExceptionMessage.PRODUCT_MERGE_DIFFERENT_UNIT));
    }
    if (!Objects.equals(absorbedProduct.getSalesUnit(), keptProduct.getSalesUnit())) {
      checks.add(I18n.get(SupplychainExceptionMessage.PRODUCT_MERGE_DIFFERENT_SALES_UNIT));
    }
    if (!Objects.equals(absorbedProduct.getPurchasesUnit(), keptProduct.getPurchasesUnit())) {
      checks.add(I18n.get(SupplychainExceptionMessage.PRODUCT_MERGE_DIFFERENT_PURCHASES_UNIT));
    }
    return checks;
  }

  protected List<String> getTypeChecks(Product absorbedProduct, Product keptProduct) {
    List<String> checks = new ArrayList<>();
    if (!Objects.equals(
        absorbedProduct.getProductTypeSelect(), keptProduct.getProductTypeSelect())) {
      checks.add(I18n.get(SupplychainExceptionMessage.PRODUCT_MERGE_DIFFERENT_TYPE));
    }
    if (!Objects.equals(
        absorbedProduct.getProductSubTypeSelect(), keptProduct.getProductSubTypeSelect())) {
      checks.add(I18n.get(SupplychainExceptionMessage.PRODUCT_MERGE_DIFFERENT_SUB_TYPE));
    }
    return checks;
  }

  protected List<String> getVariantChecks(Product absorbedProduct, Product keptProduct) {
    List<String> checks = new ArrayList<>();
    for (Product product : Arrays.asList(absorbedProduct, keptProduct)) {
      String variantCheck = getVariantCheck(product);
      if (variantCheck != null) {
        checks.add(variantCheck);
      }
    }
    return checks;
  }

  /**
   * A product variant and a product model are not merged: their stock, their variant values and the
   * link with their model would become inconsistent.
   *
   * @return the reason why the product cannot be merged, null when nothing prevents its merge
   */
  protected String getVariantCheck(Product product) {
    if (Boolean.TRUE.equals(product.getIsModel())) {
      return String.format(
          I18n.get(SupplychainExceptionMessage.PRODUCT_MERGE_PRODUCT_MODEL), product.getCode());
    }
    if (product.getParentProduct() != null) {
      return String.format(
          I18n.get(SupplychainExceptionMessage.PRODUCT_MERGE_PRODUCT_VARIANT),
          product.getCode(),
          product.getParentProduct().getCode());
    }
    if (product.getProductVariant() != null) {
      return String.format(
          I18n.get(SupplychainExceptionMessage.PRODUCT_MERGE_PRODUCT_VARIANT_NO_MODEL),
          product.getCode());
    }
    if (productRepository
            .all()
            .filter("self.parentProduct = :product")
            .bind("product", product)
            .count()
        > 0) {
      return String.format(
          I18n.get(SupplychainExceptionMessage.PRODUCT_MERGE_PRODUCT_WITH_VARIANTS),
          product.getCode());
    }
    return null;
  }

  /** A merge is not run while a MRP calculation is in progress. */
  protected List<String> getMrpChecks() {
    List<String> checks = new ArrayList<>();
    if (mrpRepository
            .all()
            .filter("self.statusSelect = :statusSelect")
            .bind("statusSelect", MrpRepository.STATUS_CALCULATION_STARTED)
            .count()
        > 0) {
      checks.add(I18n.get(SupplychainExceptionMessage.PRODUCT_MERGE_MRP_IN_PROGRESS));
    }
    return checks;
  }

  /**
   * Transfers the supplier catalogs of the absorbed product to the kept product, except the ones of
   * a supplier the kept product already has a catalog for: those stay on the absorbed product, so
   * that the kept product keeps a single catalog per supplier.
   */
  protected void transferSupplierCatalogs(
      Product absorbedProduct, Product keptProduct, ProductMergeResult result) {
    List<SupplierCatalog> absorbedSupplierCatalogList =
        supplierCatalogRepository
            .all()
            .filter("self.product = :product")
            .bind("product", absorbedProduct)
            .fetch();
    if (absorbedSupplierCatalogList.isEmpty()) {
      return;
    }

    List<Long> keptSupplierIdList =
        supplierCatalogRepository
            .all()
            .filter("self.product = :product")
            .bind("product", keptProduct)
            .fetch()
            .stream()
            .map(supplierCatalog -> supplierCatalog.getSupplierPartner().getId())
            .collect(Collectors.toList());

    int count = 0;
    for (SupplierCatalog supplierCatalog : absorbedSupplierCatalogList) {
      if (keptSupplierIdList.contains(supplierCatalog.getSupplierPartner().getId())) {
        result.addWarning(
            String.format(
                I18n.get(SupplychainExceptionMessage.PRODUCT_MERGE_LOG_SUPPLIER_CATALOG_KEPT),
                supplierCatalog.getSupplierPartner().getFullName(),
                ProductMergeResult.format(supplierCatalog.getPrice())));
        continue;
      }
      supplierCatalog.setProduct(keptProduct);
      count++;
    }
    result.addTransferredReferences(SupplierCatalog.class.getSimpleName() + ".product", count);
  }

  /**
   * Transfers every reference to the absorbed product to the kept product, except the references
   * which must stay on the absorbed product and the ones transferred by a dedicated treatment.
   */
  protected void transferReferences(
      Product absorbedProduct, Product keptProduct, ProductMergeResult result) {
    Set<String> excludedModelSet = getExcludedModels();
    Set<String> excludedFieldSet = getExcludedFields();
    Set<String> specificallyHandledModelSet = getSpecificallyHandledModels();
    Set<String> productAttributeModelNameSet = getProductAttributeModelNames();

    for (MetaField metaField : getProductReferenceFields()) {
      String modelName = metaField.getMetaModel().getFullName();
      if (excludedModelSet.contains(modelName)
          || specificallyHandledModelSet.contains(modelName)
          || excludedFieldSet.contains(modelName + "." + metaField.getName())
          || !isTransferableField(metaField)) {
        continue;
      }

      switch (metaField.getRelationship()) {
        case "ManyToOne":
          transferManyToOneReferences(metaField, absorbedProduct, keptProduct, result);
          break;
        case "OneToOne":
          transferOneToOneReference(metaField, absorbedProduct, keptProduct, result);
          break;
        case "ManyToMany":
          // a many-to-many mirroring one of the product own attributes is not transferred: the kept
          // product keeps its own values
          if (!productAttributeModelNameSet.contains(metaField.getMetaModel().getName())) {
            transferManyToManyReferences(metaField, absorbedProduct, keptProduct, result);
          }
          break;
        default:
          break;
      }
    }
  }

  /**
   * A field is transferred only when it exists on the object and holds a value of its own: a
   * computed field is read from another field and has nothing to update, and the metadata can
   * reference an object which is not deployed.
   */
  protected boolean isTransferableField(MetaField metaField) {
    Class<?> modelClass;
    try {
      modelClass = Class.forName(metaField.getMetaModel().getFullName());
    } catch (ClassNotFoundException e) {
      return false;
    }
    Property property = Mapper.of(modelClass).getProperty(metaField.getName());
    return property != null && !property.isTransient() && !property.isVirtual();
  }

  /**
   * Returns every field referencing a product. Objects without a table of their own are left out:
   * they are stored in the table of the object they extend, which is processed on its own.
   */
  protected List<MetaField> getProductReferenceFields() {
    return metaFieldRepository
        .all()
        .filter(
            "self.relationship IN ('ManyToOne', 'OneToOne', 'ManyToMany') "
                + "AND self.typeName = :typeName "
                + "AND self.metaModel.tableName IS NOT NULL")
        .bind("typeName", Product.class.getSimpleName())
        .order("metaModel.name")
        .order("name")
        .fetch();
  }

  /**
   * Returns the name of the objects the product itself has a many-to-many with (hazard phrases,
   * characteristics...). Those many-to-many describe the product: they are not transferred.
   */
  protected Set<String> getProductAttributeModelNames() {
    return metaFieldRepository
        .all()
        .filter("self.relationship = 'ManyToMany' AND self.metaModel.name = :modelName")
        .bind("modelName", Product.class.getSimpleName())
        .fetch()
        .stream()
        .map(MetaField::getTypeName)
        .collect(Collectors.toSet());
  }

  protected void transferManyToOneReferences(
      MetaField metaField,
      Product absorbedProduct,
      Product keptProduct,
      ProductMergeResult result) {
    int count =
        JPA.em()
            .createQuery(
                String.format(
                    "UPDATE %s self SET self.%s = :keptProduct WHERE self.%s = :absorbedProduct",
                    metaField.getMetaModel().getFullName(),
                    metaField.getName(),
                    metaField.getName()))
            .setParameter("keptProduct", keptProduct)
            .setParameter("absorbedProduct", absorbedProduct)
            .executeUpdate();
    result.addTransferredReferences(getReferenceName(metaField), count);
  }

  /**
   * A one-to-one reference is unique: it is transferred only when the kept product does not have
   * one already, otherwise it stays on the absorbed product and a warning is written in the log.
   */
  protected void transferOneToOneReference(
      MetaField metaField,
      Product absorbedProduct,
      Product keptProduct,
      ProductMergeResult result) {
    if (countReferences(metaField, absorbedProduct) == 0) {
      return;
    }
    if (countReferences(metaField, keptProduct) > 0) {
      result.addWarning(
          String.format(
              I18n.get(SupplychainExceptionMessage.PRODUCT_MERGE_LOG_REFERENCE_NOT_TRANSFERRED),
              getReferenceName(metaField)));
      return;
    }
    transferManyToOneReferences(metaField, absorbedProduct, keptProduct, result);
  }

  protected long countReferences(MetaField metaField, Product product) {
    return JPA.em()
        .createQuery(
            String.format(
                "SELECT COUNT(self) FROM %s self WHERE self.%s = :product",
                metaField.getMetaModel().getFullName(), metaField.getName()),
            Long.class)
        .setParameter("product", product)
        .getSingleResult();
  }

  @SuppressWarnings("unchecked")
  protected void transferManyToManyReferences(
      MetaField metaField,
      Product absorbedProduct,
      Product keptProduct,
      ProductMergeResult result) {
    String fieldName = metaField.getName();
    List<?> holderList =
        JPA.em()
            .createQuery(
                String.format(
                    "SELECT self FROM %s self LEFT JOIN self.%s AS product "
                        + "WHERE product = :absorbedProduct",
                    metaField.getMetaModel().getFullName(), fieldName))
            .setParameter("absorbedProduct", absorbedProduct)
            .getResultList();

    for (Object holder : holderList) {
      Set<Object> productSet = (Set<Object>) Mapper.of(holder.getClass()).get(holder, fieldName);
      if (productSet == null) {
        continue;
      }
      // the kept product may already be in the set: it is a set, it stays there only once
      productSet.remove(absorbedProduct);
      productSet.add(keptProduct);
    }
    result.addTransferredReferences(getReferenceName(metaField), holderList.size());
  }

  /**
   * Moves the files of the absorbed product to the kept product. The messages and the followers are
   * not moved: they are the history of the absorbed product.
   */
  protected void transferDmsFiles(
      Product absorbedProduct, Product keptProduct, ProductMergeResult result) {
    DMSFile absorbedProductHome = dmsFileRepository.findHomeByRelated(absorbedProduct);
    DMSFile keptProductHome = dmsFileRepository.findHomeByRelated(keptProduct);
    Long absorbedProductHomeId = -1L;

    if (absorbedProductHome != null) {
      if (keptProductHome == null) {
        // the kept product has no folder yet: the folder of the absorbed product becomes its own
        absorbedProductHome.setFileName(keptProduct.getFullName());
      } else {
        // both products have a folder: the content is moved, the emptied folder of the absorbed
        // product is left on the absorbed product
        dmsFileRepository
            .all()
            .filter("self.parent = :parent")
            .bind("parent", absorbedProductHome)
            .fetch()
            .forEach(dmsFile -> dmsFile.setParent(keptProductHome));
        absorbedProductHomeId = absorbedProductHome.getId();
      }
      JPA.em().flush();
    }

    int count =
        JPA.em()
            .createQuery(
                "UPDATE com.axelor.dms.db.DMSFile self SET self.relatedId = :keptProductId "
                    + "WHERE self.relatedModel = :relatedModel "
                    + "AND self.relatedId = :absorbedProductId "
                    + "AND self.id != :absorbedProductHomeId")
            .setParameter("keptProductId", keptProduct.getId())
            .setParameter("relatedModel", Product.class.getName())
            .setParameter("absorbedProductId", absorbedProduct.getId())
            .setParameter("absorbedProductHomeId", absorbedProductHomeId)
            .executeUpdate();
    result.addTransferredReferences(DMSFile.class.getSimpleName(), count);
  }

  /**
   * Re-points the custom fields referencing the absorbed product to the kept product, by updating
   * the JSON attributes of the objects holding them.
   */
  protected void transferCustomFields(
      Product absorbedProduct, Product keptProduct, ProductMergeResult result) {
    // the JSON functions used here are not available on this database
    if (DBHelper.isHSQL()) {
      return;
    }

    Property nameField = Mapper.of(Product.class).getNameField();
    if (nameField == null) {
      return;
    }

    for (MetaJsonField metaJsonField : getProductCustomFields()) {
      if (metaJsonField.getModel() == null || metaJsonField.getModelField() == null) {
        continue;
      }
      if (!"many-to-one".equals(metaJsonField.getType())) {
        result.addWarning(
            String.format(
                I18n.get(
                    SupplychainExceptionMessage.PRODUCT_MERGE_LOG_CUSTOM_FIELD_NOT_TRANSFERRED),
                getCustomFieldName(metaJsonField)));
        continue;
      }

      // the displayed name is updated first: the identifier is the one selecting the records
      updateCustomFieldReference(
          metaJsonField,
          metaJsonField.getName() + "." + nameField.getName(),
          keptProduct.getFullName(),
          absorbedProduct.getId());
      int count =
          updateCustomFieldReference(
              metaJsonField,
              metaJsonField.getName() + ".id",
              keptProduct.getId(),
              absorbedProduct.getId());
      result.addTransferredReferences(getCustomFieldName(metaJsonField), count);
    }
  }

  /**
   * A custom field is named as the other references, by the object holding it and the field name.
   * The package is left out, an object name is unique.
   */
  protected String getCustomFieldName(MetaJsonField metaJsonField) {
    String model = metaJsonField.getModel();
    return model.substring(model.lastIndexOf('.') + 1) + "." + metaJsonField.getName();
  }

  protected List<MetaJsonField> getProductCustomFields() {
    return metaJsonFieldRepository
        .all()
        .filter("self.targetModel = :targetModel")
        .bind("targetModel", Product.class.getName())
        .order("model")
        .order("name")
        .fetch();
  }

  protected int updateCustomFieldReference(
      MetaJsonField metaJsonField, String path, Object value, Long absorbedProductId) {
    boolean isCustomModel = metaJsonField.getJsonModel() != null;
    Query query =
        JPA.em()
            .createQuery(
                String.format(
                    "UPDATE %s self SET self.%s = json_set(self.%s, '%s', :value) "
                        + "WHERE json_extract(self.%s, '%s', 'id') = :absorbedProductId%s",
                    metaJsonField.getModel(),
                    metaJsonField.getModelField(),
                    metaJsonField.getModelField(),
                    path,
                    metaJsonField.getModelField(),
                    metaJsonField.getName(),
                    isCustomModel ? " AND self.jsonModel = :jsonModel" : ""))
            .setParameter("value", value)
            .setParameter("absorbedProductId", absorbedProductId.toString());
    if (isCustomModel) {
      query.setParameter("jsonModel", metaJsonField.getJsonModel().getName());
    }
    return query.executeUpdate();
  }

  /**
   * Returns the name of the objects whose references stay on the absorbed product, for a legal or a
   * technical reason.
   *
   * <p>The names are returned as text so that a module can exclude an object which is not available
   * on the supplychain classpath, as the timesheet lines below.
   */
  protected Set<String> getExcludedModels() {
    return new HashSet<>(
        Arrays.asList(
            // an issued accounting document keeps the product it was issued with
            InvoiceLine.class.getName(),
            // a timesheet line keeps the product it was entered with, and has a unique constraint
            // on it: the human resource module is not on the supplychain classpath, hence the text
            "com.axelor.apps.hr.db.TimesheetLine",
            // unique constraint on (product, company, label)
            StockHistoryLine.class.getName(),
            // the values of the absorbed product for a company, unique constraint on (product,
            // company)
            ProductCompany.class.getName(),
            // the stock rules configured for the absorbed product
            StockRules.class.getName(),
            // a dated analysis keeps the product it was run on
            ABCAnalysis.class.getName(),
            ABCAnalysisLine.class.getName(),
            // a dated cost calculation keeps the product it was computed for
            UnitCostCalculation.class.getName(),
            UnitCostCalcLine.class.getName(),
            // the previous merges are a history and are not rewritten
            ProductMergeLog.class.getName()));
  }

  /** Returns the "Object.field" references which stay on the absorbed product. */
  protected Set<String> getExcludedFields() {
    // a product merged into the absorbed product keeps pointing to it: the merge log and the
    // archived products give the whole history
    return new HashSet<>(Collections.singletonList(Product.class.getName() + ".mergedIntoProduct"));
  }

  /**
   * Returns the name of the objects transferred by a dedicated treatment, because moving their
   * references is not enough: quantities have to be added up, unique sequences have to be renamed.
   */
  protected Set<String> getSpecificallyHandledModels() {
    return new HashSet<>(
        Arrays.asList(
            SupplierCatalog.class.getName(),
            StockLocationLine.class.getName(),
            TrackingNumber.class.getName()));
  }

  /**
   * Called once every reference of the absorbed product has been transferred to the kept product.
   *
   * <p>Modules extending the product merge add their own treatments by overriding this method.
   */
  protected void postReferenceTransfer(
      Product absorbedProduct, Product keptProduct, ProductMergeResult result)
      throws AxelorException {}

  /**
   * The absorbed product is archived and keeps a link to the product it was merged into: its code
   * stays consultable and it is not deleted.
   */
  protected void archiveAbsorbedProduct(Product absorbedProduct, Product keptProduct) {
    absorbedProduct.setMergedIntoProduct(keptProduct);
    absorbedProduct.setArchived(true);
    productRepository.save(absorbedProduct);
  }

  /** Records the merge, so that it stays consultable from the product merge log menu. */
  protected ProductMergeLog createProductMergeLog(
      Product absorbedProduct, Product keptProduct, User user, ProductMergeResult result) {
    ProductMergeLog productMergeLog = new ProductMergeLog();
    productMergeLog.setAbsorbedProduct(absorbedProduct);
    productMergeLog.setKeptProduct(keptProduct);
    productMergeLog.setUser(user);
    productMergeLog.setMergeDateTime(appSupplychainService.getTodayDateTime().toLocalDateTime());
    productMergeLog.setLog(String.join("\n", result.getLogLines()));
    return productMergeLogRepository.save(productMergeLog);
  }

  /**
   * Called once the merge is done, before the absorbed product is archived.
   *
   * <p>Modules extending the product merge add their own treatments by overriding this method.
   */
  protected void postMerge(Product absorbedProduct, Product keptProduct, ProductMergeResult result)
      throws AxelorException {}

  protected String getReferenceName(MetaField metaField) {
    return metaField.getMetaModel().getName() + "." + metaField.getName();
  }

  protected String getNames(Stream<String> nameStream) {
    return nameStream.filter(Objects::nonNull).sorted().collect(Collectors.joining(", "));
  }
}
