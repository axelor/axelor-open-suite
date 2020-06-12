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
package com.axelor.apps.base.service;

import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.Product;
import com.axelor.apps.base.db.ProductVariant;
import com.axelor.apps.base.db.ProductVariantAttr;
import com.axelor.apps.base.db.ProductVariantConfig;
import com.axelor.apps.base.db.ProductVariantValue;
import com.axelor.apps.base.db.repo.ProductRepository;
import com.axelor.apps.base.db.repo.ProductVariantRepository;
import com.axelor.apps.base.db.repo.ProductVariantValueRepository;
import com.axelor.apps.base.db.repo.SequenceRepository;
import com.axelor.apps.base.exceptions.IExceptionMessage;
import com.axelor.apps.base.service.administration.SequenceService;
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.exception.AxelorException;
import com.axelor.exception.db.repo.TraceBackRepository;
import com.axelor.i18n.I18n;
import com.axelor.meta.MetaFiles;
import com.google.common.collect.Lists;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;

public class ProductServiceImpl implements ProductService {

  protected ProductVariantService productVariantService;
  protected ProductVariantRepository productVariantRepo;
  protected SequenceService sequenceService;
  protected AppBaseService appBaseService;
  protected ProductRepository productRepo;
  protected ProductCompanyService productCompanyService;

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
  @Transactional
  public void updateProductPrice(Product product) throws AxelorException {

    this.updateSalePrice(product, null);

    productRepo.save(product);
  }

  public String getSequence() throws AxelorException {
    String seq = sequenceService.getSequenceNumber(SequenceRepository.PRODUCT);

    if (seq == null) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
          I18n.get(IExceptionMessage.PRODUCT_NO_SEQUENCE));
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

  private void updateSalePriceOfVariant(Product product) throws AxelorException {

    List<? extends Product> productVariantList =
        productRepo.all().filter("self.parentProduct = ?1 AND dtype = 'Product'", product).fetch();

    for (Product productVariant : productVariantList) {

      productVariant.setCostPrice(product.getCostPrice());
      if (product.getAutoUpdateSalePrice()) {
        productVariant.setSalePrice(product.getSalePrice());
      }
      productVariant.setManagPriceCoef(product.getManagPriceCoef());

      this.updateSalePrice(productVariant, null);
    }
  }

  @Override
  @Transactional
  public void generateProductVariants(Product productModel) throws AxelorException {

    List<ProductVariant> productVariantList =
        this.getProductVariantList(productModel.getProductVariantConfig());

    int seq = 1;

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

    this.updateSalePrice(product, null);

    return product;
  }

  /**
   * @param productVariant
   * @param applicationPriceSelect - 1 : Sale price - 2 : Cost price
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

    if (productVariantValue1 != null
        && productVariantValue1.getApplicationPriceSelect() == applicationPriceSelect) {

      extraPrice = extraPrice.add(productVariantValue1.getPriceExtra());
    }

    if (productVariantValue2 != null) {

      extraPrice = extraPrice.add(productVariantValue2.getPriceExtra());
    }

    if (productVariantValue3 != null) {

      extraPrice = extraPrice.add(productVariantValue3.getPriceExtra());
    }

    if (productVariantValue4 != null) {

      extraPrice = extraPrice.add(productVariantValue4.getPriceExtra());
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
          this.createProductVariant(productVariantConfig, productVariantValue1, null, null, null));
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
              productVariantConfig, productVariantValue1, productVariantValue2, null, null));
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

        productVariantList.add(
            this.createProductVariant(
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
      ProductVariantValue productVariantValue4) {

    ProductVariantAttr productVariantAttr1 = null,
        productVariantAttr2 = null,
        productVariantAttr3 = null,
        productVariantAttr4 = null;
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

    return productVariantService.createProductVariant(
        productVariantAttr1,
        productVariantAttr2,
        productVariantAttr3,
        productVariantAttr4,
        productVariantValue1,
        productVariantValue2,
        productVariantValue3,
        productVariantValue4,
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
  }
}
