package com.axelor.apps.purchase.service;

import com.axelor.apps.base.db.ABCAnalysis;
import com.axelor.apps.base.db.ABCAnalysisLine;
import com.axelor.apps.base.db.Product;
import com.axelor.apps.base.db.repo.ABCAnalysisLineRepository;
import com.axelor.apps.base.service.ABCAnalysisServiceImpl;
import com.axelor.apps.purchase.db.PurchaseOrderLine;
import com.axelor.apps.purchase.db.repo.PurchaseOrderLineRepository;
import com.axelor.apps.purchase.db.repo.PurchaseOrderRepository;
import com.axelor.db.JPA;
import com.axelor.db.Query;
import com.axelor.exception.AxelorException;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;

import java.math.BigDecimal;
import java.util.List;

import static com.axelor.apps.base.service.administration.AbstractBatch.FETCH_LIMIT;
import static com.axelor.apps.tool.date.DateTool.toDate;
import static com.axelor.apps.tool.date.DateTool.toLocalDateT;

public class ABCAnalysisServicePurchaseImpl extends ABCAnalysisServiceImpl {

    @Inject
    PurchaseOrderLineRepository purchaseOrderLineRepository;
    @Inject
    ABCAnalysisLineRepository abcAnalysisLineRepository;

    private final static String PURCHASABLE_TRUE = " AND self.purchasable = TRUE";

    @Override
    @Transactional(rollbackOn = {AxelorException.class, Exception.class})
    protected ABCAnalysisLine createABCAnalysisLine(ABCAnalysis abcAnalysis, Product product) {
        ABCAnalysisLine abcAnalysisLine = super.createABCAnalysisLine(abcAnalysis, product);
        BigDecimal productQty = BigDecimal.ZERO;
        BigDecimal productWorth = BigDecimal.ZERO;
        List<PurchaseOrderLine> purchaseOrderLineList;
        int offset = 0;

        Query<PurchaseOrderLine> purchaseOrderLineQuery = purchaseOrderLineRepository.all()
                .filter("self.purchaseOrder.statusSelect = :status AND self.purchaseOrder.validationDate >= :startDate AND self.purchaseOrder.validationDate <= :endDate AND self.product.id = :productId")
                .bind("status", PurchaseOrderRepository.STATUS_VALIDATED)
                .bind("startDate", abcAnalysis.getStartDate())
                .bind("endDate", abcAnalysis.getEndDate())
                .bind("productId", product.getId())
                .order("id");

        while(!(purchaseOrderLineList = purchaseOrderLineQuery.fetch(FETCH_LIMIT, offset)).isEmpty()){
            offset += purchaseOrderLineList.size();

            for(PurchaseOrderLine purchaseOrderLine: purchaseOrderLineList){
                productQty = productQty.add(purchaseOrderLine.getQty());
                productQty = productQty.add(purchaseOrderLine.getExTaxTotal());
            }

            super.incTotalQty(productQty);
            super.incTotalWorth(productWorth);

            JPA.clear();
        }

        return super.setQtyWorth(abcAnalysisLineRepository.find(abcAnalysisLine.getId()), productQty, productWorth);
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
