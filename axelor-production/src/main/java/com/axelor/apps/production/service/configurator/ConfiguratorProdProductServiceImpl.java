package com.axelor.apps.production.service.configurator;

import com.axelor.apps.base.db.Product;
import com.axelor.apps.base.db.Unit;
import com.axelor.apps.base.db.repo.ProductRepository;
import com.axelor.apps.base.db.repo.UnitRepository;
import com.axelor.apps.production.db.ConfiguratorProdProduct;
import com.axelor.apps.production.db.ProdProduct;
import com.axelor.apps.production.exceptions.IExceptionMessage;
import com.axelor.apps.production.service.ProdProductService;
import com.axelor.apps.sale.service.configurator.ConfiguratorService;
import com.axelor.exception.AxelorException;
import com.axelor.exception.db.repo.TraceBackRepository;
import com.axelor.i18n.I18n;
import com.axelor.rpc.JsonContext;
import com.google.inject.Inject;
import java.math.BigDecimal;

public class ConfiguratorProdProductServiceImpl implements ConfiguratorProdProductService {

  protected ProdProductService prodProductService;
  protected ConfiguratorService configuratorService;
  protected ProductRepository productRepository;
  protected UnitRepository unitRepository;

  @Inject
  public ConfiguratorProdProductServiceImpl(
      ProdProductService prodProductService,
      ConfiguratorService configuratorService,
      ProductRepository productRepository,
      UnitRepository unitRepository) {
    this.prodProductService = prodProductService;
    this.configuratorService = configuratorService;
    this.productRepository = productRepository;
    this.unitRepository = unitRepository;
  }

  @Override
  public ProdProduct generateProdProduct(
      ConfiguratorProdProduct confProdProduct, JsonContext attributes) throws AxelorException {
    if (confProdProduct != null && checkConditions(confProdProduct, attributes)) {
      Product product;
      BigDecimal qty;
      Unit unit;

      if (confProdProduct.getDefProductAsFormula()) {
        Object computedProduct =
            configuratorService.computeFormula(confProdProduct.getProductFormula(), attributes);
        if (computedProduct == null) {
          throw new AxelorException(
              confProdProduct,
              TraceBackRepository.CATEGORY_INCONSISTENCY,
              String.format(
                  I18n.get(
                      IExceptionMessage.CONFIGURATOR_PROD_PRODUCT_INCONSISTENT_PRODUCT_FORMULA),
                  confProdProduct.getId()));
        } else {
          // M2O field define by script
          // Explicit repo call needed in order to prevent case where formula is referring to
          // JSON context attribute defined on configurator creator
          // In this case object is not managed and it causes hibernate issues
          product = productRepository.find(((Product) computedProduct).getId());
        }
      } else {
        product = confProdProduct.getProduct();
        if (product == null) {
          throw new AxelorException(
              confProdProduct,
              TraceBackRepository.CATEGORY_INCONSISTENCY,
              String.format(
                  I18n.get(IExceptionMessage.CONFIGURATOR_PROD_PRODUCT_INCONSISTENT_NULL_PRODUCT),
                  confProdProduct.getId()));
        }
      }

      if (confProdProduct.getDefQtyAsFormula()) {
        Object computedQty =
            configuratorService.computeFormula(confProdProduct.getQtyFormula(), attributes);
        if (computedQty == null) {
          throw new AxelorException(
              confProdProduct,
              TraceBackRepository.CATEGORY_INCONSISTENCY,
              String.format(
                  I18n.get(IExceptionMessage.CONFIGURATOR_PROD_PRODUCT_INCONSISTENT_QTY_FORMULA),
                  confProdProduct.getId()));
        } else {
          qty = new BigDecimal(computedQty.toString());
        }
      } else {
        qty = confProdProduct.getQty();
        if (qty == null) {
          throw new AxelorException(
              confProdProduct,
              TraceBackRepository.CATEGORY_INCONSISTENCY,
              String.format(
                  I18n.get(IExceptionMessage.CONFIGURATOR_PROD_PRODUCT_INCONSISTENT_NULL_QTY),
                  confProdProduct.getId()));
        }
      }

      if (confProdProduct.getDefUnitAsFormula()) {
        Object computedUnit =
            configuratorService.computeFormula(confProdProduct.getUnitFormula(), attributes);
        if (computedUnit == null) {
          throw new AxelorException(
              confProdProduct,
              TraceBackRepository.CATEGORY_INCONSISTENCY,
              String.format(
                  I18n.get(IExceptionMessage.CONFIGURATOR_PROD_PRODUCT_INCONSISTENT_UNIT_FORMULA),
                  confProdProduct.getId()));
        } else {
          // M2O field define by script
          // Explicit repo call needed in order to prevent case where formula is referring to
          // JSON context attribute defined on configurator creator
          // In this case object is not managed and it causes hibernate issues
          unit = unitRepository.find(((Unit) computedUnit).getId());
        }
      } else {
        unit = confProdProduct.getUnit();
        if (unit == null) {
          throw new AxelorException(
              confProdProduct,
              TraceBackRepository.CATEGORY_INCONSISTENCY,
              String.format(
                  I18n.get(IExceptionMessage.CONFIGURATOR_PROD_PRODUCT_INCONSISTENT_NULL_UNIT),
                  confProdProduct.getId()));
        }
      }

      return new ProdProduct(product, qty, unit);
    }
    return null;
  }

  protected boolean checkConditions(
      ConfiguratorProdProduct confProdProduct, JsonContext jsonAttributes) throws AxelorException {
    String condition = confProdProduct.getUseCondition();
    // no condition = we always generate the prod product
    if (condition == null || condition.trim().isEmpty()) {
      return true;
    }

    Object computedConditions = configuratorService.computeFormula(condition, jsonAttributes);
    if (computedConditions == null) {
      throw new AxelorException(
          confProdProduct,
          TraceBackRepository.CATEGORY_INCONSISTENCY,
          String.format(
              I18n.get(IExceptionMessage.CONFIGURATOR_PROD_PRODUCT_INCONSISTENT_CONDITION),
              confProdProduct.getId()));
    }

    return (boolean) computedConditions;
  }
}
