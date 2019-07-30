/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2019 Axelor (<http://axelor.com>).
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

import static com.axelor.apps.base.service.administration.AbstractBatch.FETCH_LIMIT;
import static com.axelor.apps.tool.date.DateTool.toDate;
import static com.axelor.apps.tool.date.DateTool.toLocalDateT;

import com.axelor.apps.base.db.ABCAnalysis;
import com.axelor.apps.base.db.ABCAnalysisLine;
import com.axelor.apps.base.db.Product;
import com.axelor.apps.base.db.repo.ABCAnalysisClassRepository;
import com.axelor.apps.base.db.repo.ABCAnalysisLineRepository;
import com.axelor.apps.base.db.repo.ABCAnalysisRepository;
import com.axelor.apps.base.db.repo.ProductRepository;
import com.axelor.apps.base.service.ABCAnalysisServiceImpl;
import com.axelor.apps.base.service.UnitConversionService;
import com.axelor.apps.base.service.administration.SequenceService;
import com.axelor.apps.sale.db.SaleOrderLine;
import com.axelor.apps.sale.db.repo.SaleOrderLineRepository;
import com.axelor.apps.sale.db.repo.SaleOrderRepository;
import com.axelor.db.JPA;
import com.axelor.db.Query;
import com.axelor.exception.AxelorException;
import com.google.inject.Inject;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public class ABCAnalysisServiceSaleImpl extends ABCAnalysisServiceImpl {
  protected SaleOrderLineRepository saleOrderLineRepository;

  private static final String SELLABLE_TRUE = " AND self.sellable = TRUE";

  @Inject
  public ABCAnalysisServiceSaleImpl(
      ABCAnalysisLineRepository abcAnalysisLineRepository,
      UnitConversionService unitConversionService,
      ABCAnalysisRepository abcAnalysisRepository,
      ProductRepository productRepository,
      SaleOrderLineRepository saleOrderLineRepository,
      ABCAnalysisClassRepository abcAnalysisClassRepository,
      SequenceService sequenceService) {
    super(
        abcAnalysisLineRepository,
        unitConversionService,
        abcAnalysisRepository,
        productRepository,
        abcAnalysisClassRepository,
        sequenceService);
    this.saleOrderLineRepository = saleOrderLineRepository;
  }

  @Override
  protected Optional<ABCAnalysisLine> createABCAnalysisLine(
      ABCAnalysis abcAnalysis, Product product) throws AxelorException {
    ABCAnalysisLine abcAnalysisLine = null;
    BigDecimal productQty = BigDecimal.ZERO;
    BigDecimal productWorth = BigDecimal.ZERO;
    List<SaleOrderLine> saleOrderLineList;
    int offset = 0;

    Query<SaleOrderLine> saleOrderLineQuery =
        saleOrderLineRepository
            .all()
            .filter(
                "(self.saleOrder.statusSelect = :statusConfirmed OR self.saleOrder.statusSelect = :statusCompleted) AND self.saleOrder.confirmationDateTime >= :startDate AND self.saleOrder.confirmationDateTime <= :endDate AND self.product.id = :productId")
            .bind("statusConfirmed", SaleOrderRepository.STATUS_ORDER_CONFIRMED)
            .bind("statusCompleted", SaleOrderRepository.STATUS_ORDER_COMPLETED)
            .bind("startDate", toLocalDateT(toDate(abcAnalysis.getStartDate())))
            .bind(
                "endDate",
                toLocalDateT(toDate(abcAnalysis.getEndDate()))
                    .withHour(23)
                    .withMinute(59)
                    .withSecond(59))
            .bind("productId", product.getId())
            .order("id");

    while (!(saleOrderLineList = saleOrderLineQuery.fetch(FETCH_LIMIT, offset)).isEmpty()) {
      offset += saleOrderLineList.size();
      abcAnalysis = abcAnalysisRepository.find(abcAnalysis.getId());

      if (abcAnalysisLine == null) {
        abcAnalysisLine = super.createABCAnalysisLine(abcAnalysis, product).get();
      }

      for (SaleOrderLine saleOrderLine : saleOrderLineList) {
        BigDecimal convertedQty =
            unitConversionService.convert(
                saleOrderLine.getUnit(), product.getUnit(), saleOrderLine.getQty(), 5, product);
        productQty = productQty.add(convertedQty);
        productWorth = productWorth.add(saleOrderLine.getCompanyExTaxTotal());
      }

      super.incTotalQty(productQty);
      super.incTotalWorth(productWorth);

      JPA.clear();
    }

    if (abcAnalysisLine != null) {
      setQtyWorth(
          abcAnalysisLineRepository.find(abcAnalysisLine.getId()), productQty, productWorth);
    }

    return Optional.ofNullable(abcAnalysisLine);
  }

  @Override
  protected String getProductCategoryQuery() {
    return super.getProductCategoryQuery() + SELLABLE_TRUE;
  }

  @Override
  protected String getProductFamilyQuery() {
    return super.getProductFamilyQuery() + SELLABLE_TRUE;
  }
}
