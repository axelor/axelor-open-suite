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

import com.axelor.apps.account.db.TaxLine;
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.apps.base.service.tax.TaxService;
import com.axelor.apps.sale.db.SaleOrderLine;
import com.axelor.apps.sale.db.SaleOrderLineTree;
import com.axelor.apps.sale.db.repo.SaleOrderLineTreeRepository;
import com.axelor.common.ObjectUtils;
import com.axelor.inject.Beans;
import com.google.inject.persist.Transactional;
import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class SaleOrderLineTreeComputationServiceImpl
    implements SaleOrderLineTreeComputationService {

  @Override
  @Transactional
  public SaleOrderLine computePrices(SaleOrderLine saleOrderLine) {

    List<SaleOrderLineTree> saleOrderLineTrees = saleOrderLine.getSaleOrderLineTreeList();

    saleOrderLineTrees.forEach(this::setPriceAndCostOfWholeTree);

    BigDecimal totalPrice =
        saleOrderLineTrees.stream()
            .map(SaleOrderLineTree::getTotalPrice)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
    saleOrderLine.setPrice(totalPrice);

    BigDecimal totalCost =
        saleOrderLineTrees.stream()
            .map(SaleOrderLineTree::getTotalCost)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
    saleOrderLine.setSubTotalCostPrice(totalCost);

    setInTaxPrice(saleOrderLine);

    //    if (Beans.get(AppAccountService.class).getAppAccount().getManageAnalyticAccounting()) {
    //      saleOrderLine =
    //          Beans.get(SaleOrderLineServiceSupplyChain.class)
    //              .computeAnalyticDistribution(saleOrderLine);
    //      response.setValue(
    //          "analyticDistributionTemplate", saleOrderLine.getAnalyticDistributionTemplate());
    //      response.setValue("analyticMoveLineList", saleOrderLine.getAnalyticMoveLineList());
    //    }

    return saleOrderLine;
  }

  private void setInTaxPrice(SaleOrderLine saleOrderLine) {
    BigDecimal exTaxPrice = saleOrderLine.getPrice();
    TaxLine taxLine = saleOrderLine.getTaxLine();
    BigDecimal intaxPrice =
        Beans.get(TaxService.class)
            .convertUnitPrice(
                false,
                taxLine,
                exTaxPrice,
                Beans.get(AppBaseService.class).getNbDecimalDigitForUnitPrice());
    saleOrderLine.setInTaxPrice(intaxPrice);
  }

  protected void setPriceAndCostOfWholeTree(SaleOrderLineTree tree) {
    if (tree.getTypeSelect() == SaleOrderLineTreeRepository.PRODUCT_TYPE) {
      return;
    }

    List<SaleOrderLineTree> childSaleOrderLineTrees = tree.getChildSaleOrderLineTreeList();
    if (ObjectUtils.isEmpty(childSaleOrderLineTrees)) {
      tree.setTotalCost(BigDecimal.ZERO);
      tree.setTotalPrice(BigDecimal.ZERO);
    }

    childSaleOrderLineTrees.forEach(this::setPriceAndCostOfWholeTree);

    BigDecimal totalCost = computeTotalCost(childSaleOrderLineTrees);
    tree.setTotalCost(totalCost);

    BigDecimal totalPrice = computeTotalPrice(childSaleOrderLineTrees);

    tree.setTotalPrice(totalPrice);
  }

  private BigDecimal computeTotalCost(List<SaleOrderLineTree> childSaleOrderLineTrees) {
    List<BigDecimal> totalCosts =
        childSaleOrderLineTrees.stream()
            .map(SaleOrderLineTree::getTotalCost)
            .collect(Collectors.toList());

    return sumDecimal(totalCosts);
  }

  private BigDecimal computeTotalPrice(List<SaleOrderLineTree> childSaleOrderLineTrees) {
    List<BigDecimal> totalPrices =
        childSaleOrderLineTrees.stream()
            .map(SaleOrderLineTree::getTotalPrice)
            .collect(Collectors.toList());

    return sumDecimal(totalPrices);
  }

  private BigDecimal sumDecimal(List<BigDecimal> decimals) {
    return decimals.stream().anyMatch(Objects::isNull)
        ? null
        : decimals.stream().filter(Objects::nonNull).reduce(BigDecimal.ZERO, BigDecimal::add);
  }
}
