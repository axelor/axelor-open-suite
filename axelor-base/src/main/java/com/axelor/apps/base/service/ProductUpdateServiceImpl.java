package com.axelor.apps.base.service;

import com.axelor.apps.base.db.Product;
import com.axelor.apps.base.db.ProductCompany;
import com.axelor.apps.base.db.repo.ProductRepository;
import com.axelor.exception.AxelorException;
import com.google.inject.Inject;
import java.math.BigDecimal;
import java.util.Optional;

public class ProductUpdateServiceImpl implements ProductUpdateService {

  protected ProductCompanyService productCompanyService;
  protected ProductConversionService productConversionService;

  @Inject
  public ProductUpdateServiceImpl(
      ProductCompanyService productCompanyService,
      ProductConversionService productConversionService) {
    this.productCompanyService = productCompanyService;
    this.productConversionService = productConversionService;
  }

  @Override
  public void updateCostPriceFromView(Product product) throws AxelorException {

    BigDecimal costPrice = BigDecimal.ZERO;

    switch (product.getCostTypeSelect()) {
      case ProductRepository.COST_TYPE_LAST_PURCHASE_PRICE:
        costPrice = computeWithLastPurchasePrice(product);
        break;
      case ProductRepository.COST_TYPE_AVERAGE_PRICE:
        costPrice = getBigDecimalFieldValue(product, "avgPrice");
        break;
      case ProductRepository.COST_TYPE_LAST_PRODUCTION_PRICE:
        costPrice = getBigDecimalFieldValue(product, "lastProductionPrice");
        break;
    }

    productCompanyService.set(product, "costPrice", costPrice, null);

    // Products company share the same cost price
    if (!productCompanyService.isCompanySpecificProductFields("costTypeSelect")) {
      for (ProductCompany productCompany : product.getProductCompanyList()) {
        productCompany.setCostPrice(costPrice);
      }
    }
  }

  protected BigDecimal getBigDecimalFieldValue(Product product, String fieldName)
      throws AxelorException {
    return Optional.ofNullable(productCompanyService.get(product, fieldName, null))
        .map(o -> (BigDecimal) o)
        .orElse(null);
  }

  protected BigDecimal computeWithLastPurchasePrice(Product product) throws AxelorException {

    BigDecimal fieldShippingCoef = getBigDecimalFieldValue(product, "shippingCoef");

    BigDecimal shippingCoef =
        fieldShippingCoef != null && fieldShippingCoef.signum() > 0
            ? fieldShippingCoef
            : BigDecimal.ONE;

    BigDecimal lastPurchasePrice = getBigDecimalFieldValue(product, "lastPurchasePrice");

    if (lastPurchasePrice == null || lastPurchasePrice.compareTo(BigDecimal.ZERO) <= 0) {
      lastPurchasePrice =
          Optional.ofNullable(getBigDecimalFieldValue(product, "purchasePrice"))
              .orElse(BigDecimal.ZERO);
    }

    lastPurchasePrice =
        productConversionService.convertFromPurchaseToStockUnitPrice(product, lastPurchasePrice);

    return lastPurchasePrice.multiply(shippingCoef);
  }
}
