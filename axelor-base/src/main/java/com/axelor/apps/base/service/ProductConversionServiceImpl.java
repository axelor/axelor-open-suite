package com.axelor.apps.base.service;

import com.axelor.apps.base.db.Product;
import com.axelor.apps.base.db.Unit;
import com.axelor.apps.base.exceptions.BaseExceptionMessage;
import com.axelor.exception.AxelorException;
import com.axelor.exception.db.repo.TraceBackRepository;
import com.axelor.i18n.I18n;
import com.google.inject.Inject;
import java.math.BigDecimal;
import java.util.Optional;

public class ProductConversionServiceImpl implements ProductConversionService {

  protected ProductCompanyService productCompanyService;
  protected UnitConversionService unitConversionService;

  @Inject
  public ProductConversionServiceImpl(
      ProductCompanyService productCompanyService, UnitConversionService unitConversionService) {

    this.productCompanyService = productCompanyService;
    this.unitConversionService = unitConversionService;
  }

  @Override
  public BigDecimal convertFromPurchaseToStockUnitPrice(
      Product product, BigDecimal lastPurchasePrice) throws AxelorException {

    Unit purchaseUnit =
        Optional.ofNullable(productCompanyService.get(product, "purchasesUnit", null))
            .map(o -> (Unit) o)
            .orElse(null);
    Unit stockUnit =
        Optional.ofNullable(productCompanyService.get(product, "unit", null))
            .map(o -> (Unit) o)
            .orElse(null);

    if (purchaseUnit == null || stockUnit == null) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
          I18n.get(
              String.format(
                  BaseExceptionMessage.PRODUCT_MISSING_UNITS_TO_CONVERT, product.getName())));
    }

    return unitConversionService.convert(
        stockUnit, purchaseUnit, lastPurchasePrice, lastPurchasePrice.scale(), null);
  }
}
