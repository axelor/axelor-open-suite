/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2005-2023 Axelor (<http://axelor.com>).
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
package com.axelor.apps.sale.service.saleorder;

import com.axelor.apps.base.db.Product;
import com.axelor.apps.base.db.repo.ProductRepository;
import com.axelor.apps.sale.db.SaleOrderLineTree;
import com.axelor.apps.sale.db.repo.SaleOrderLineRepository;
import com.axelor.apps.sale.db.repo.SaleOrderLineTreeRepository;
import com.axelor.common.ObjectUtils;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Objects;

public class SaleOrderLineTreeServiceImpl implements SaleOrderLineTreeService {
  protected SaleOrderLineTreeRepository saleOrderLineTreeRepository;
  protected ProductRepository productRepository;

  protected SaleOrderLineRepository saleOrderLineRepository;

  @Inject
  public SaleOrderLineTreeServiceImpl(
      SaleOrderLineTreeRepository saleOrderLineTreeRepository,
      ProductRepository productRepository,
      SaleOrderLineRepository saleorderLineRepository) {
    this.saleOrderLineTreeRepository = saleOrderLineTreeRepository;
    this.productRepository = productRepository;
    this.saleOrderLineRepository = saleorderLineRepository;
  }

  protected SaleOrderLineTree emptyFields(SaleOrderLineTree saleOrderLineTree) {
    saleOrderLineTree.setTitle(null);
    saleOrderLineTree.setUnit(null);
    saleOrderLineTree.setDescription(null);
    saleOrderLineTree.setUnitCost(null);
    saleOrderLineTree.setUnitPrice(null);
    saleOrderLineTree.setTotalCost(null);
    saleOrderLineTree.setTotalPrice(null);
    saleOrderLineTree.setQuantity(null);
    saleOrderLineTree.setMarginRate(null);

    return saleOrderLineTree;
  }

  @Override
  public SaleOrderLineTree fillFields(SaleOrderLineTree saleOrderLineTree) {
    Product product = saleOrderLineTree.getProduct();
    if (Objects.isNull(product)) {
      return emptyFields(saleOrderLineTree);
    }
    BigDecimal costPrice = product.getCostPrice();
    BigDecimal salePrice = product.getSalePrice();
    BigDecimal qty = saleOrderLineTree.getQuantity();

    saleOrderLineTree.setTitle(product.getName());
    saleOrderLineTree.setUnit(product.getUnit());
    saleOrderLineTree.setDescription(product.getDescription());
    saleOrderLineTree.setUnitCost(costPrice);
    saleOrderLineTree.setUnitPrice(salePrice);
    saleOrderLineTree.setTotalCost(costPrice.multiply(qty));
    saleOrderLineTree.setTotalPrice(salePrice.multiply(qty));

    BigDecimal unitPrice = saleOrderLineTree.getUnitPrice();
    if (unitPrice.signum() != 0) {
      BigDecimal unitCost = saleOrderLineTree.getUnitCost();
      saleOrderLineTree.setMarginRate(
          unitPrice.subtract(unitCost).divide(unitCost, 2, RoundingMode.HALF_UP));
    }
    return saleOrderLineTree;
  }

  @Override
  public SaleOrderLineTree updateFields(SaleOrderLineTree saleOrderLineTree) {
    BigDecimal unitCost = saleOrderLineTree.getUnitCost();
    BigDecimal unitPrice = saleOrderLineTree.getUnitPrice();

    BigDecimal qty = saleOrderLineTree.getQuantity();
    saleOrderLineTree.setUnit(saleOrderLineTree.getUnit());

    BigDecimal totalCost = unitCost != null ? unitCost.multiply(qty) : null;
    saleOrderLineTree.setTotalCost(totalCost);

    BigDecimal totalPrice = unitPrice != null ? unitPrice.multiply(qty) : null;
    saleOrderLineTree.setTotalPrice(totalPrice);

    BigDecimal marginRate =
        unitPrice != null && unitCost != null && unitCost.signum() != 0
            ? unitPrice.subtract(unitCost).divide(unitCost, 2, RoundingMode.HALF_UP)
            : null;
    saleOrderLineTree.setMarginRate(marginRate);

    return saleOrderLineTree;
  }

  @Override
  public SaleOrderLineTree updateUnitPrice(SaleOrderLineTree saleOrderLineTree) {
    BigDecimal marginRate = saleOrderLineTree.getMarginRate();
    if (marginRate == null) {
      saleOrderLineTree.setUnitPrice(null);
    }

    if (BigDecimal.ONE.subtract(marginRate).signum() != 0) {
      BigDecimal unitCost = saleOrderLineTree.getUnitCost();
      BigDecimal unitPrice =
          unitCost != null ? unitCost.multiply(BigDecimal.ONE.add(marginRate)) : null;
      saleOrderLineTree.setUnitPrice(unitPrice);
    }

    updateFields(saleOrderLineTree);

    return saleOrderLineTree;
  }

  @Override
  @Transactional
  public void removeElement(SaleOrderLineTree saleOrderLineTree) {

    List<SaleOrderLineTree> saleOrderLineTrees = saleOrderLineTree.getChildSaleOrderLineTreeList();

    if (ObjectUtils.notEmpty(saleOrderLineTrees)) {
      for (SaleOrderLineTree subElement : saleOrderLineTrees) {
        removeElement(subElement);
      }
    }

    saleOrderLineTreeRepository.remove(saleOrderLineTree);
  }
}
