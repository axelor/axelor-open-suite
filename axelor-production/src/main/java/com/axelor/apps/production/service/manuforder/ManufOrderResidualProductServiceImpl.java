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
package com.axelor.apps.production.service.manuforder;

import com.axelor.apps.base.db.Product;
import com.axelor.apps.base.service.ProductVariantService;
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.apps.production.db.BillOfMaterial;
import com.axelor.apps.production.db.ManufOrder;
import com.axelor.apps.production.db.ProdProduct;
import com.axelor.apps.production.db.ProdResidualProduct;
import com.axelor.apps.production.service.app.AppProductionService;
import com.axelor.inject.Beans;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

public class ManufOrderResidualProductServiceImpl implements ManufOrderResidualProductService {

  @Override
  public boolean hasResidualProduct(ManufOrder manufOrder) {

    return !getResidualProdProductList(manufOrder).isEmpty();
  }

  @Override
  public List<ProdProduct> getResidualProdProductList(ManufOrder manufOrder) {

    List<ProdProduct> residualProdProductList = new ArrayList<>();

    BillOfMaterial billOfMaterial = manufOrder.getBillOfMaterial();

    if (billOfMaterial == null
        || billOfMaterial.getProdResidualProductList() == null
        || !Beans.get(AppProductionService.class)
            .getAppProduction()
            .getManageResidualProductOnBom()) {
      return residualProdProductList;
    }

    BigDecimal manufOrderQty = manufOrder.getQty();
    BigDecimal bomQty = billOfMaterial.getQty();
    int scale = Beans.get(AppBaseService.class).getNbDecimalDigitForQty();
    ProductVariantService productVariantService = Beans.get(ProductVariantService.class);

    for (ProdResidualProduct prodResidualProduct : billOfMaterial.getProdResidualProductList()) {

      Product product =
          productVariantService.getProductVariant(
              manufOrder.getProduct(), prodResidualProduct.getProduct());

      BigDecimal qty =
          bomQty != null && bomQty.signum() != 0
              ? prodResidualProduct
                  .getQty()
                  .multiply(manufOrderQty)
                  .divide(bomQty, scale, RoundingMode.HALF_UP)
              : BigDecimal.ZERO;

      residualProdProductList.add(new ProdProduct(product, qty, prodResidualProduct.getUnit()));
    }

    return residualProdProductList;
  }

  @Override
  public boolean isResidualProduct(ProdProduct prodProduct, ManufOrder manufOrder) {
    if (manufOrder.getBillOfMaterial() != null
        && manufOrder.getBillOfMaterial().getProdResidualProductList() != null) {
      return manufOrder.getBillOfMaterial().getProdResidualProductList().stream()
          .map(ProdResidualProduct::getProduct)
          .anyMatch(product -> product.equals(prodProduct.getProduct()));
    }
    return false;
  }
}
