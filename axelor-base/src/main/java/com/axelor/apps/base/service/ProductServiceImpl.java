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
package com.axelor.apps.base.service;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.PriceList;
import com.axelor.apps.base.db.PriceListLine;
import com.axelor.apps.base.db.Product;
import com.axelor.apps.base.db.ProductCategory;
import com.axelor.apps.base.db.ProductVariant;
import com.axelor.apps.base.db.ProductVariantAttr;
import com.axelor.apps.base.db.ProductVariantConfig;
import com.axelor.apps.base.db.ProductVariantValue;
import com.axelor.apps.base.db.Sequence;
import com.axelor.apps.base.db.repo.CompanyRepository;
import com.axelor.apps.base.db.repo.ProductRepository;
import com.axelor.apps.base.db.repo.ProductVariantRepository;
import com.axelor.apps.base.db.repo.ProductVariantValueRepository;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.apps.base.exceptions.BaseExceptionMessage;
import com.axelor.apps.base.service.administration.SequenceService;
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.i18n.I18n;
import com.axelor.meta.MetaFiles;
import com.axelor.studio.db.repo.AppBaseRepository;
import com.google.common.collect.Lists;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;
import org.apache.commons.lang3.StringUtils;

public class ProductServiceImpl implements ProductService {

  protected ProductVariantService productVariantService;
  protected ProductVariantRepository productVariantRepo;
  protected SequenceService sequenceService;
  protected AppBaseService appBaseService;
  protected ProductRepository productRepo;
  protected ProductCompanyService productCompanyService;
  protected CompanyRepository companyRepo;

  @Inject
  public ProductServiceImpl(
      ProductVariantService productVariantService,
      ProductVariantRepository productVariantRepo,
      SequenceService sequenceService,
      AppBaseService appBaseService,
      ProductRepository productRepo,
      ProductCompanyService productCompanyService) {
    this.productVariantService = productVariantService;
    this.productVariantRepo = productVariantRepo;
    this.sequenceService = sequenceService;
    this.appBaseService = appBaseService;
    this.productRepo = productRepo;
    this.productCompanyService = productCompanyService;
  }

  @Inject private MetaFiles metaFiles;

  @Override
  @Transactional(rollbackOn = {Exception.class})
  public void updateProductPrice(Product product) throws AxelorException {

    this.updateSalePrice(product, null);

    productRepo.save(product);
  }

  public String getSequence(Product product) throws AxelorException {
    String seq = null;
    if (appBaseService
        .getAppBase()
        .getProductSequenceTypeSelect()
        .equals(AppBaseRepository.SEQUENCE_PER_PRODUCT_CATEGORY)) {
      ProductCategory productCategory = product.getProductCategory();
      if (productCategory.getSequence() != null) {
        seq =
            sequenceService.getSequenceNumber(productCategory.getSequence(), Product.class, "code");
      }
      if (seq == null) {
        throw new AxelorException(
            TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
            I18n.get(BaseExceptionMessage.CATEGORY_NO_SEQUENCE));
      }
    } else if (appBaseService
        .getAppBase()
        .getProductSequenceTypeSelect()
        .equals(AppBaseRepository.SEQUENCE_PER_PRODUCT)) {
      Sequence productSequence = appBaseService.getAppBase().getProductSequence();
      if (productSequence != null) {
        seq = sequenceService.getSequenceNumber(productSequence, Product.class, "code");
      }
      if (seq == null) {
        throw new AxelorException(
            TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
            I18n.get(BaseExceptionMessage.APP_BASE_NO_SEQUENCE));
      }
    }

    if (seq == null) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
          I18n.get(BaseExceptionMessage.PRODUCT_NO_SEQUENCE));
    }

    return seq;
  }

  @Override
  public void updateSalePrice(Product product, Company company) throws AxelorException {
    BigDecimal managePriceCoef =
        (BigDecimal) productCompanyService.get(product, "managPriceCoef", company);

    if ((BigDecimal) productCompanyService.get(product, "costPrice", company) != null) {

      if (product.getProductVariant() != null) {

        product.setCostPrice(
            product
                .getCostPrice()
                .add(
                    this.getProductExtraPrice(
                        product.getProductVariant(),
                        ProductVariantValueRepository.APPLICATION_COST_PRICE)));
      }
    }

    if ((BigDecimal) productCompanyService.get(product, "purchasePrice", company) != null) {

      if (product.getProductVariant() != null) {

        product.setPurchasePrice(
            product
                .getPurchasePrice()
                .add(
                    this.getProductExtraPrice(
                        product.getProductVariant(),
                        ProductVariantValueRepository.APPLICATION_PURCHASE_PRICE)));
      }
    }

    if ((BigDecimal) productCompanyService.get(product, "costPrice", company) != null
        && managePriceCoef != null
        && (Boolean) productCompanyService.get(product, "autoUpdateSalePrice", company)) {

      productCompanyService.set(
          product,
          "salePrice",
          (((BigDecimal) productCompanyService.get(product, "costPrice", company))
                  .multiply(managePriceCoef))
              .setScale(appBaseService.getNbDecimalDigitForUnitPrice(), BigDecimal.ROUND_HALF_UP),
          company);
    }

    if ((BigDecimal) productCompanyService.get(product, "salePrice", company) != null) {

      if (product.getProductVariant() != null) {

        product.setSalePrice(
            product
                .getSalePrice()
                .add(
                    this.getProductExtraPrice(
                        product.getProductVariant(),
                        ProductVariantValueRepository.APPLICATION_SALE_PRICE)));
      }
    }

    if (product.getProductVariantConfig() != null && product.getManageVariantPrice()) {

      this.updateSalePriceOfVariant(product);
    }
  }

  protected void updateSalePriceOfVariant(Product product) throws AxelorException {

    List<? extends Product> productVariantList =
        productRepo
            .all()
            .filter("self.parentProduct = ?1 AND self.dtype = 'Product'", product)
            .fetch();

    for (Product productVariant : productVariantList) {

      productVariant.setCostPrice(product.getCostPrice());
      productVariant.setPurchasePrice(product.getPurchasePrice());
      productVariant.setSalePrice(product.getSalePrice());
      productVariant.setManagPriceCoef(product.getManagPriceCoef());

      this.updateSalePrice(productVariant, null);
    }
  }

  public boolean hasActivePriceList(Product product) {
    return product.getPriceListLineList() != null
        && product.getPriceListLineList().stream()
            .map(PriceListLine::getPriceList)
            .filter(Objects::nonNull)
            .anyMatch(PriceList::getIsActive);
  }

  @Override
  @Transactional(rollbackOn = {Exception.class})
  public void generateProductVariants(Product productModel) throws AxelorException {

    List<ProductVariant> productVariantList =
        this.getProductVariantList(productModel.getProductVariantConfig());

    int seq = 1;

    List<Product> productVariantsList =
        productRepo.all().filter("self.parentProduct = ?1", productModel).fetch();

    if (productVariantsList != null && !productVariantsList.isEmpty()) {
      Integer lastSeq = 0;
      for (Product product : productVariantsList) {
        Integer productSeq =
            Integer.parseInt(StringUtils.substringAfterLast(product.getCode(), "-"));
        if (productSeq.compareTo(lastSeq) > 0) {
          lastSeq = productSeq;
        }
      }
      seq = lastSeq + 1;
    }

    for (ProductVariant productVariant : productVariantList) {

      productVariantRepo.save(productVariant);

      productRepo.save(this.createProduct(productModel, productVariant, seq++));
    }
  }

  @Override
  public Product createProduct(Product productModel, ProductVariant productVariant, int seq)
      throws AxelorException {

    String description = "";
    String internalDescription = "";

    if (productModel.getDescription() != null) {
      description = productModel.getDescription();
    }
    if (productModel.getInternalDescription() != null) {
      internalDescription = productModel.getInternalDescription();
    }
    description += "<br>" + productVariant.getName();
    internalDescription += "<br>" + productVariant.getName();

    Product product =
        new Product(
            productModel.getName() + " (" + productVariant.getName() + ")",
            productModel.getCode() + "-" + seq,
            description,
            internalDescription,
            productModel.getPicture(),
            productModel.getProductCategory(),
            productModel.getProductFamily(),
            productModel.getUnit(),
            productModel.getSaleSupplySelect(),
            productModel.getProductTypeSelect(),
            productModel.getProcurementMethodSelect(),
            productModel.getSaleCurrency(),
            productModel.getPurchaseCurrency(),
            productModel.getStartDate(),
            productModel.getEndDate());

    productModel.setIsModel(true);

    product.setIsModel(false);
    product.setParentProduct(productModel);
    product.setProductVariant(productVariant);

    product.setCostPrice(productModel.getCostPrice());
    product.setSalePrice(productModel.getSalePrice());
    product.setManagPriceCoef(productModel.getManagPriceCoef());

    product = productVariantService.copyAdditionalFields(product, productModel);

    this.updateSalePrice(product, null);

    return product;
  }

  /**
   * @param productVariant
   * @param applicationPriceSelect - 1 : Sale price - 2 : Cost price - 3 : Purchase price
   * @return
   */
  @Override
  public BigDecimal getProductExtraPrice(
      ProductVariant productVariant, int applicationPriceSelect) {

    BigDecimal extraPrice = BigDecimal.ZERO;

    ProductVariantValue productVariantValue1 = productVariant.getProductVariantValue1();
    ProductVariantValue productVariantValue2 = productVariant.getProductVariantValue2();
    ProductVariantValue productVariantValue3 = productVariant.getProductVariantValue3();
    ProductVariantValue productVariantValue4 = productVariant.getProductVariantValue4();
    ProductVariantValue productVariantValue5 = productVariant.getProductVariantValue5();

    if (productVariantValue1 != null
        && productVariantValue1.getApplicationPriceSelect() == applicationPriceSelect) {

      extraPrice = extraPrice.add(productVariantValue1.getPriceExtra());
    }

    if (productVariantValue2 != null
        && productVariantValue2.getApplicationPriceSelect() == applicationPriceSelect) {

      extraPrice = extraPrice.add(productVariantValue2.getPriceExtra());
    }

    if (productVariantValue3 != null
        && productVariantValue3.getApplicationPriceSelect() == applicationPriceSelect) {

      extraPrice = extraPrice.add(productVariantValue3.getPriceExtra());
    }

    if (productVariantValue4 != null
        && productVariantValue4.getApplicationPriceSelect() == applicationPriceSelect) {

      extraPrice = extraPrice.add(productVariantValue4.getPriceExtra());
    }

    if (productVariantValue5 != null
        && productVariantValue5.getApplicationPriceSelect() == applicationPriceSelect) {

      extraPrice = extraPrice.add(productVariantValue5.getPriceExtra());
    }

    return extraPrice;
  }

  private List<ProductVariant> getProductVariantList(ProductVariantConfig productVariantConfig) {

    List<ProductVariant> productVariantList = Lists.newArrayList();

    if (productVariantConfig.getProductVariantAttr1() != null
        && productVariantConfig.getProductVariantValue1Set() != null) {

      for (ProductVariantValue productVariantValue1 :
          productVariantConfig.getProductVariantValue1Set()) {

        productVariantList.addAll(
            this.getProductVariantList(productVariantConfig, productVariantValue1));
      }
    }

    return productVariantList;
  }

  private List<ProductVariant> getProductVariantList(
      ProductVariantConfig productVariantConfig, ProductVariantValue productVariantValue1) {

    List<ProductVariant> productVariantList = Lists.newArrayList();

    if (productVariantConfig.getProductVariantAttr2() != null
        && productVariantConfig.getProductVariantValue2Set() != null) {

      for (ProductVariantValue productVariantValue2 :
          productVariantConfig.getProductVariantValue2Set()) {

        productVariantList.addAll(
            this.getProductVariantList(
                productVariantConfig, productVariantValue1, productVariantValue2));
      }
    } else {

      productVariantList.add(
          this.createProductVariant(
              productVariantConfig, productVariantValue1, null, null, null, null));
    }

    return productVariantList;
  }

  private List<ProductVariant> getProductVariantList(
      ProductVariantConfig productVariantConfig,
      ProductVariantValue productVariantValue1,
      ProductVariantValue productVariantValue2) {

    List<ProductVariant> productVariantList = Lists.newArrayList();

    if (productVariantConfig.getProductVariantAttr3() != null
        && productVariantConfig.getProductVariantValue3Set() != null) {

      for (ProductVariantValue productVariantValue3 :
          productVariantConfig.getProductVariantValue3Set()) {

        productVariantList.addAll(
            this.getProductVariantList(
                productVariantConfig,
                productVariantValue1,
                productVariantValue2,
                productVariantValue3));
      }
    } else {

      productVariantList.add(
          this.createProductVariant(
              productVariantConfig, productVariantValue1, productVariantValue2, null, null, null));
    }

    return productVariantList;
  }

  private List<ProductVariant> getProductVariantList(
      ProductVariantConfig productVariantConfig,
      ProductVariantValue productVariantValue1,
      ProductVariantValue productVariantValue2,
      ProductVariantValue productVariantValue3) {

    List<ProductVariant> productVariantList = Lists.newArrayList();

    if (productVariantConfig.getProductVariantAttr4() != null
        && productVariantConfig.getProductVariantValue4Set() != null) {

      for (ProductVariantValue productVariantValue4 :
          productVariantConfig.getProductVariantValue4Set()) {

        productVariantList.addAll(
            this.getProductVariantList(
                productVariantConfig,
                productVariantValue1,
                productVariantValue2,
                productVariantValue3,
                productVariantValue4));
      }
    } else {

      productVariantList.add(
          this.createProductVariant(
              productVariantConfig,
              productVariantValue1,
              productVariantValue2,
              productVariantValue3,
              null,
              null));
    }

    return productVariantList;
  }

  private List<ProductVariant> getProductVariantList(
      ProductVariantConfig productVariantConfig,
      ProductVariantValue productVariantValue1,
      ProductVariantValue productVariantValue2,
      ProductVariantValue productVariantValue3,
      ProductVariantValue productVariantValue4) {

    List<ProductVariant> productVariantList = Lists.newArrayList();

    if (productVariantConfig.getProductVariantAttr5() != null
        && productVariantConfig.getProductVariantValue5Set() != null) {

      for (ProductVariantValue productVariantValue5 :
          productVariantConfig.getProductVariantValue5Set()) {

        productVariantList.add(
            this.createProductVariant(
                productVariantConfig,
                productVariantValue1,
                productVariantValue2,
                productVariantValue3,
                productVariantValue4,
                productVariantValue5));
      }
    } else {

      productVariantList.add(
          this.createProductVariant(
              productVariantConfig,
              productVariantValue1,
              productVariantValue2,
              productVariantValue3,
              productVariantValue4,
              null));
    }

    return productVariantList;
  }

  @Override
  public ProductVariant createProductVariant(
      ProductVariantConfig productVariantConfig,
      ProductVariantValue productVariantValue1,
      ProductVariantValue productVariantValue2,
      ProductVariantValue productVariantValue3,
      ProductVariantValue productVariantValue4,
      ProductVariantValue productVariantValue5) {

    ProductVariantAttr productVariantAttr1 = null,
        productVariantAttr2 = null,
        productVariantAttr3 = null,
        productVariantAttr4 = null,
        productVariantAttr5 = null;
    if (productVariantValue1 != null) {
      productVariantAttr1 = productVariantConfig.getProductVariantAttr1();
    }
    if (productVariantValue2 != null) {
      productVariantAttr2 = productVariantConfig.getProductVariantAttr2();
    }
    if (productVariantValue3 != null) {
      productVariantAttr3 = productVariantConfig.getProductVariantAttr3();
    }
    if (productVariantValue4 != null) {
      productVariantAttr4 = productVariantConfig.getProductVariantAttr4();
    }
    if (productVariantValue5 != null) {
      productVariantAttr5 = productVariantConfig.getProductVariantAttr5();
    }

    return productVariantService.createProductVariant(
        productVariantAttr1,
        productVariantAttr2,
        productVariantAttr3,
        productVariantAttr4,
        productVariantAttr5,
        productVariantValue1,
        productVariantValue2,
        productVariantValue3,
        productVariantValue4,
        productVariantValue5,
        false);
  }

  public void copyProduct(Product product, Product copy) {
    copy.setBarCode(null);
    try {
      if (product.getPicture() != null) {
        File file = MetaFiles.getPath(product.getPicture()).toFile();
        copy.setPicture(metaFiles.upload(file));
      }
    } catch (IOException e) {
      e.printStackTrace();
    }
    copy.setStartDate(null);
    copy.setEndDate(null);
    copy.setCostPrice(BigDecimal.ZERO);
    copy.setPurchasePrice(BigDecimal.ZERO);
    copy.setProductCompanyList(null);
    copy.setLastPurchaseDate(null);
    copy.setCode(null);
  }
}
