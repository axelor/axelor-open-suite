package com.axelor.apps.base.service;

import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.Product;
import com.axelor.apps.base.db.Unit;
import com.axelor.apps.base.db.repo.UnitConversionRepository;
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.exception.AxelorException;
import com.axelor.exception.db.repo.TraceBackRepository;
import com.google.inject.Inject;
import java.io.IOException;
import java.math.BigDecimal;

public class ProductComputePriceServiceImpl implements ProductComputePriceService {

  protected AppBaseService appBaseService;
  protected ProductCompanyService productCompanyService;
  protected UnitConversionService unitConversionService;
  protected UnitConversionRepository unitConversionRepository;

  @Inject
  public ProductComputePriceServiceImpl(
      AppBaseService appBaseService,
      ProductCompanyService productCompanyService,
      UnitConversionService unitConversionService,
      UnitConversionRepository unitConversionRepository) {

    this.appBaseService = appBaseService;
    this.productCompanyService = productCompanyService;
    this.unitConversionService = unitConversionService;
    this.unitConversionRepository = unitConversionRepository;
  }

  @Override
  public BigDecimal computeSalePrice(
      BigDecimal managePriceCoef, BigDecimal costPrice, Product product, Company company)
      throws AxelorException {
    BigDecimal salePrice;
    try {
      salePrice = costPrice.multiply(managePriceCoef);
      if (productCompanyService.get(product, "unit", company) != null
          && productCompanyService.get(product, "salesUnit", company) != null
          && !productCompanyService
              .get(product, "unit", company)
              .equals(productCompanyService.get(product, "salesUnit", company))) {
        salePrice =
            salePrice.multiply(
                unitConversionService.getCoefficient(
                    unitConversionRepository.all().fetch(),
                    (Unit) productCompanyService.get(product, "salesUnit", company),
                    (Unit) productCompanyService.get(product, "unit", company),
                    product));
      }
    } catch (ClassNotFoundException | IOException e) {
      throw new AxelorException(TraceBackRepository.CATEGORY_CONFIGURATION_ERROR, e.getMessage());
    }
    return salePrice.setScale(
        appBaseService.getNbDecimalDigitForUnitPrice(), BigDecimal.ROUND_HALF_UP);
  }
}
