package com.axelor.apps.sale.service;

import com.axelor.apps.base.db.ABCAnalysis;
import com.axelor.apps.base.db.ABCAnalysisLine;
import com.axelor.apps.base.db.Product;
import com.axelor.apps.base.db.repo.ABCAnalysisLineRepository;
import com.axelor.apps.base.service.ABCAnalysisServiceImpl;
import com.axelor.apps.sale.db.SaleOrderLine;
import com.axelor.apps.sale.db.repo.SaleOrderLineRepository;
import com.axelor.apps.sale.db.repo.SaleOrderRepository;
import com.axelor.db.JPA;
import com.axelor.db.Query;
import com.axelor.exception.AxelorException;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import static com.axelor.apps.base.service.administration.AbstractBatch.FETCH_LIMIT;
import static com.axelor.apps.tool.date.DateTool.toDate;
import static com.axelor.apps.tool.date.DateTool.toLocalDateT;

public class ABCAnalysisServiceSaleImpl extends ABCAnalysisServiceImpl {
    @Inject SaleOrderLineRepository saleOrderLineRepository;
    @Inject ABCAnalysisLineRepository abcAnalysisLineRepository;

    private final static String SELLABLE_TRUE = " AND self.sellable = TRUE";

    @Override
    protected ABCAnalysisLine createABCAnalysisLine(ABCAnalysis abcAnalysis, Product product) {
        ABCAnalysisLine abcAnalysisLine = super.createABCAnalysisLine(abcAnalysis, product);
        BigDecimal productQty = BigDecimal.ZERO;
        BigDecimal productWorth = BigDecimal.ZERO;
        List<SaleOrderLine> saleOrderLineList;
        int offset = 0;

        Query<SaleOrderLine> saleOrderLineQuery = saleOrderLineRepository.all()
                .filter("self.saleOrder.statusSelect = :status AND self.saleOrder.confirmationDateTime >= :startDate AND self.saleOrder.confirmationDateTime <= :endDate AND self.product.id = :productId")
                .bind("status", SaleOrderRepository.STATUS_ORDER_CONFIRMED)
                .bind("startDate", toLocalDateT(toDate(abcAnalysis.getStartDate())))
                .bind("endDate", toLocalDateT(toDate(abcAnalysis.getEndDate())))
                .bind("productId", product.getId())
                .order("id");

        while(!(saleOrderLineList = saleOrderLineQuery.fetch(FETCH_LIMIT, offset)).isEmpty()){
            offset += saleOrderLineList.size();

            for(SaleOrderLine saleOrderLine: saleOrderLineList){
                productQty = productQty.add(saleOrderLine.getQty());
                productQty = productQty.add(saleOrderLine.getExTaxTotal());
            }

            super.incTotalQty(productQty);
            super.incTotalWorth(productWorth);

            JPA.clear();
        }

        return super.setQtyWorth(abcAnalysisLineRepository.find(abcAnalysisLine.getId()), productQty, productWorth);
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
