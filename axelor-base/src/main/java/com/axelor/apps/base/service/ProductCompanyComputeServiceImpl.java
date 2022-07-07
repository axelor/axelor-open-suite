package com.axelor.apps.base.service;

import com.axelor.apps.base.db.Product;
import com.axelor.apps.base.db.ProductCompany;
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.exception.AxelorException;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.math.BigDecimal;

public class ProductCompanyComputeServiceImpl implements ProductCompanyComputeService {

  protected AppBaseService appBaseService;
  protected ProductService productService;
  protected ProductCompanyService productCompanyService;

  @Inject
  public ProductCompanyComputeServiceImpl(
      AppBaseService appBaseService,
      ProductService productService,
      ProductCompanyService productCompanyService) {
    this.appBaseService = appBaseService;
    this.productService = productService;
    this.productCompanyService = productCompanyService;
  }

  @Transactional(rollbackOn = {Exception.class})
  public void updateProductCompanySalePriceWithSalesUnit(Product product) throws AxelorException {
    if (appBaseService.isFieldSpecificToCompany("autoUpdateSalePrice")) {
      for (ProductCompany productCompany : product.getProductCompanyList()) {
        if (productCompany.getAutoUpdateSalePrice()) {
          BigDecimal salePrice =
              productService.computeSalePrice(
                  productCompany.getManagPriceCoef(),
                  productCompany.getCostPrice(),
                  product,
                  productCompany.getCompany());
          productCompanyService.set(product, "salePrice", salePrice, productCompany.getCompany());
        }
      }
    }
  }
}
