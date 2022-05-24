/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2022 Axelor (<http://axelor.com>).
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
package com.axelor.apps.sale.service;

import com.axelor.apps.base.db.Product;
import com.axelor.apps.base.db.Unit;
import com.axelor.apps.base.db.repo.ProductRepository;
import com.axelor.apps.base.service.ProductCompanyService;
import com.axelor.apps.sale.db.Pack;
import com.axelor.apps.sale.db.PackLine;
import com.axelor.exception.AxelorException;
import com.google.inject.Inject;
import java.math.BigDecimal;

public class PackLineServiceImpl implements PackLineService {

  protected ProductRepository productRepository;
  protected ProductCompanyService productCompanyService;

  @Inject
  public PackLineServiceImpl(
      ProductRepository productRepository, ProductCompanyService productCompanyService) {
    this.productRepository = productRepository;
    this.productCompanyService = productCompanyService;
  }

  @Override
  public PackLine resetProductInformation(PackLine packLine) {

    packLine.setProductName(null);
    packLine.setPrice(null);
    packLine.setUnit(null);
    packLine.setQuantity(BigDecimal.ZERO);
    return packLine;
  }

  @Override
  public PackLine computeProductInformation(Pack pack, PackLine packLine) throws AxelorException {
    Product product = packLine.getProduct();
    product = productRepository.find(product.getId());

    packLine.setProductName(product.getName());
    packLine.setUnit(this.getSaleUnit(product));
    packLine.setPrice(this.getUnitPrice(packLine, pack));
    return packLine;
  }

  private Unit getSaleUnit(Product product) {
    Unit unit = product.getSalesUnit();
    if (unit == null) {
      unit = product.getUnit();
    }
    return unit;
  }

  private BigDecimal getUnitPrice(PackLine packLine, Pack pack) throws AxelorException {
    return (BigDecimal)
        productCompanyService.get(packLine.getProduct(), "salePrice", pack.getCompany());
  }
}
