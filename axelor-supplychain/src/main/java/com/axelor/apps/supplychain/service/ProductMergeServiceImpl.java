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

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Product;
import com.axelor.apps.base.db.repo.ProductRepository;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.apps.supplychain.db.repo.MrpRepository;
import com.axelor.apps.supplychain.exception.SupplychainExceptionMessage;
import com.axelor.apps.supplychain.service.app.AppSupplychainService;
import com.axelor.auth.db.User;
import com.axelor.i18n.I18n;
import jakarta.inject.Inject;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ProductMergeServiceImpl implements ProductMergeService {

  protected final AppSupplychainService appSupplychainService;
  protected final ProductRepository productRepository;
  protected final MrpRepository mrpRepository;

  @Inject
  public ProductMergeServiceImpl(
      AppSupplychainService appSupplychainService,
      ProductRepository productRepository,
      MrpRepository mrpRepository) {
    this.appSupplychainService = appSupplychainService;
    this.productRepository = productRepository;
    this.mrpRepository = mrpRepository;
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
    if (!blockingChecks.isEmpty()) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_INCONSISTENCY, String.join("\n", blockingChecks));
    }
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

    return blockingChecks;
  }

  protected List<String> getStatusChecks(Product absorbedProduct, Product keptProduct) {
    List<String> checks = new ArrayList<>();
    if (Boolean.TRUE.equals(absorbedProduct.getArchived())
        || Boolean.TRUE.equals(keptProduct.getArchived())) {
      checks.add(I18n.get(SupplychainExceptionMessage.PRODUCT_MERGE_ARCHIVED_PRODUCT));
    }
    for (Product product : Arrays.asList(absorbedProduct, keptProduct)) {
      if (product.getMergedIntoProduct() != null) {
        checks.add(
            String.format(
                I18n.get(SupplychainExceptionMessage.PRODUCT_MERGE_ALREADY_MERGED),
                product.getFullName(),
                product.getMergedIntoProduct().getFullName()));
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
      if (isVariantOrModel(product)) {
        checks.add(
            String.format(
                I18n.get(SupplychainExceptionMessage.PRODUCT_MERGE_VARIANT_OR_MODEL),
                product.getFullName()));
      }
    }
    return checks;
  }

  /**
   * A product variant and a product model are not merged: their stock, their variant values and the
   * link with their model would become inconsistent.
   */
  protected boolean isVariantOrModel(Product product) {
    return Boolean.TRUE.equals(product.getIsModel())
        || product.getProductVariant() != null
        || product.getParentProduct() != null
        || productRepository
                .all()
                .filter("self.parentProduct = :product")
                .bind("product", product)
                .count()
            > 0;
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

  protected String getNames(Stream<String> nameStream) {
    return nameStream.filter(Objects::nonNull).sorted().collect(Collectors.joining(", "));
  }
}
