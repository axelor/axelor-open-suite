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
package com.axelor.apps.production.service.configurator;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Product;
import com.axelor.apps.base.db.Unit;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.apps.production.db.ConfiguratorProdProduct;
import com.axelor.apps.production.db.ProdProduct;
import com.axelor.apps.production.exceptions.ProductionExceptionMessage;
import com.axelor.apps.production.service.ProdProductService;
import com.axelor.apps.sale.service.configurator.ConfiguratorService;
import com.axelor.i18n.I18n;
import com.axelor.rpc.JsonContext;
import com.google.inject.Inject;
import java.math.BigDecimal;

public class ConfiguratorProdProductServiceImpl implements ConfiguratorProdProductService {

  protected ProdProductService prodProductService;
  protected ConfiguratorService configuratorService;

  @Inject
  public ConfiguratorProdProductServiceImpl(
      ProdProductService prodProductService, ConfiguratorService configuratorService) {
    this.prodProductService = prodProductService;
    this.configuratorService = configuratorService;
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
                      ProductionExceptionMessage
                          .CONFIGURATOR_PROD_PRODUCT_INCONSISTENT_PRODUCT_FORMULA),
                  confProdProduct.getId()));
        } else {
          product = (Product) computedProduct;
        }
      } else {
        product = confProdProduct.getProduct();
        if (product == null) {
          throw new AxelorException(
              confProdProduct,
              TraceBackRepository.CATEGORY_INCONSISTENCY,
              String.format(
                  I18n.get(
                      ProductionExceptionMessage
                          .CONFIGURATOR_PROD_PRODUCT_INCONSISTENT_NULL_PRODUCT),
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
                  I18n.get(
                      ProductionExceptionMessage
                          .CONFIGURATOR_PROD_PRODUCT_INCONSISTENT_QTY_FORMULA),
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
                  I18n.get(
                      ProductionExceptionMessage.CONFIGURATOR_PROD_PRODUCT_INCONSISTENT_NULL_QTY),
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
                  I18n.get(
                      ProductionExceptionMessage
                          .CONFIGURATOR_PROD_PRODUCT_INCONSISTENT_UNIT_FORMULA),
                  confProdProduct.getId()));
        } else {
          unit = (Unit) computedUnit;
        }
      } else {
        unit = confProdProduct.getUnit();
        if (unit == null) {
          throw new AxelorException(
              confProdProduct,
              TraceBackRepository.CATEGORY_INCONSISTENCY,
              String.format(
                  I18n.get(
                      ProductionExceptionMessage.CONFIGURATOR_PROD_PRODUCT_INCONSISTENT_NULL_UNIT),
                  confProdProduct.getId()));
        }
      }
      ProdProduct generatedProdProduct = new ProdProduct(product, qty, unit);
      configuratorService.fixRelationalFields(generatedProdProduct);
      return generatedProdProduct;
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
              I18n.get(ProductionExceptionMessage.CONFIGURATOR_PROD_PRODUCT_INCONSISTENT_CONDITION),
              confProdProduct.getId()));
    }

    return (boolean) computedConditions;
  }
}
