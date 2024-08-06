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
package com.axelor.apps.purchase.service;

import static com.axelor.apps.base.service.administration.AbstractBatch.FETCH_LIMIT;

import com.axelor.apps.base.AxelorException;
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
import com.axelor.apps.purchase.db.PurchaseOrderLine;
import com.axelor.apps.purchase.db.repo.PurchaseOrderLineRepository;
import com.axelor.apps.purchase.db.repo.PurchaseOrderRepository;
import com.axelor.db.JPA;
import com.axelor.db.Query;
import com.google.inject.Inject;
import java.math.BigDecimal;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

public class ABCAnalysisServicePurchaseImpl extends ABCAnalysisServiceImpl {

  protected PurchaseOrderLineRepository purchaseOrderLineRepository;

  private static final String PURCHASABLE_TRUE = " AND self.purchasable = TRUE";

  @Inject
  public ABCAnalysisServicePurchaseImpl(
      ABCAnalysisLineRepository abcAnalysisLineRepository,
      UnitConversionService unitConversionService,
      ABCAnalysisRepository abcAnalysisRepository,
      ProductRepository productRepository,
      PurchaseOrderLineRepository purchaseOrderLineRepository,
      ABCAnalysisClassRepository abcAnalysisClassRepository,
      SequenceService sequenceService) {
    super(
        abcAnalysisLineRepository,
        unitConversionService,
        abcAnalysisRepository,
        productRepository,
        abcAnalysisClassRepository,
        sequenceService);
    this.purchaseOrderLineRepository = purchaseOrderLineRepository;
  }

  @Override
  protected Optional<ABCAnalysisLine> createABCAnalysisLine(
      ABCAnalysis abcAnalysis, Product product) throws AxelorException {
    ABCAnalysisLine abcAnalysisLine = null;
    BigDecimal productQty = BigDecimal.ZERO;
    BigDecimal productWorth = BigDecimal.ZERO;
    List<PurchaseOrderLine> purchaseOrderLineList;
    int offset = 0;

    Query<PurchaseOrderLine> purchaseOrderLineQuery =
        purchaseOrderLineRepository
            .all()
            .filter(
                "(self.purchaseOrder.statusSelect = :statusValidated OR self.purchaseOrder.statusSelect = :statusFinished) AND self.purchaseOrder.validationDateTime >= :startDate AND self.purchaseOrder.validationDateTime <= :endDate AND self.product.id = :productId")
            .bind("statusValidated", PurchaseOrderRepository.STATUS_VALIDATED)
            .bind("statusFinished", PurchaseOrderRepository.STATUS_FINISHED)
            .bind("startDate", abcAnalysis.getStartDate().atStartOfDay())
            .bind("endDate", abcAnalysis.getEndDate().atTime(LocalTime.MAX))
            .bind("productId", product.getId())
            .order("id");

    while (!(purchaseOrderLineList = purchaseOrderLineQuery.fetch(FETCH_LIMIT, offset)).isEmpty()) {
      offset += purchaseOrderLineList.size();
      abcAnalysis = abcAnalysisRepository.find(abcAnalysis.getId());

      if (abcAnalysisLine == null) {
        abcAnalysisLine = super.createABCAnalysisLine(abcAnalysis, product).get();
      }

      for (PurchaseOrderLine purchaseOrderLine : purchaseOrderLineList) {
        BigDecimal convertedQty =
            unitConversionService.convert(
                purchaseOrderLine.getUnit(),
                product.getUnit(),
                purchaseOrderLine.getQty(),
                2,
                product);
        productQty = productQty.add(convertedQty);
        productWorth = productWorth.add(purchaseOrderLine.getCompanyExTaxTotal());
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
    return super.getProductCategoryQuery() + PURCHASABLE_TRUE;
  }

  @Override
  protected String getProductFamilyQuery() {
    return super.getProductFamilyQuery() + PURCHASABLE_TRUE;
  }
}
