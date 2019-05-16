package com.axelor.apps.purchase.service;

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
import com.axelor.exception.AxelorException;
import com.google.inject.Inject;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static com.axelor.apps.base.service.administration.AbstractBatch.FETCH_LIMIT;

public class ABCAnalysisServicePurchaseImpl extends ABCAnalysisServiceImpl {

    protected PurchaseOrderLineRepository purchaseOrderLineRepository;

    private final static String PURCHASABLE_TRUE = " AND self.purchasable = TRUE";

    @Inject
    public ABCAnalysisServicePurchaseImpl(ABCAnalysisLineRepository abcAnalysisLineRepository, UnitConversionService unitConversionService, ABCAnalysisRepository abcAnalysisRepository, ProductRepository productRepository, PurchaseOrderLineRepository purchaseOrderLineRepository, ABCAnalysisClassRepository abcAnalysisClassRepository, SequenceService sequenceService) {
        super(abcAnalysisLineRepository, unitConversionService, abcAnalysisRepository, productRepository, abcAnalysisClassRepository, sequenceService);
        this.purchaseOrderLineRepository = purchaseOrderLineRepository;
    }

    @Override
    protected Optional<ABCAnalysisLine> createABCAnalysisLine(ABCAnalysis abcAnalysis, Product product) throws AxelorException {
        ABCAnalysisLine abcAnalysisLine = null;
        BigDecimal productQty = BigDecimal.ZERO;
        BigDecimal productWorth = BigDecimal.ZERO;
        List<PurchaseOrderLine> purchaseOrderLineList;
        int offset = 0;

        Query<PurchaseOrderLine> purchaseOrderLineQuery = purchaseOrderLineRepository.all()
                .filter("(self.purchaseOrder.statusSelect = :statusValidated OR self.purchaseOrder.statusSelect = :statusFinished) AND self.purchaseOrder.validationDate >= :startDate AND self.purchaseOrder.validationDate <= :endDate AND self.product.id = :productId")
                .bind("statusValidated", PurchaseOrderRepository.STATUS_VALIDATED)
                .bind("statusFinished", PurchaseOrderRepository.STATUS_FINISHED)
                .bind("startDate", abcAnalysis.getStartDate())
                .bind("endDate", abcAnalysis.getEndDate())
                .bind("productId", product.getId())
                .order("id");

        while(!(purchaseOrderLineList = purchaseOrderLineQuery.fetch(FETCH_LIMIT, offset)).isEmpty()){
            offset += purchaseOrderLineList.size();
            abcAnalysis = abcAnalysisRepository.find(abcAnalysis.getId());

            if (abcAnalysisLine == null) {
                abcAnalysisLine = super.createABCAnalysisLine(abcAnalysis, product).get();
            }

            for(PurchaseOrderLine purchaseOrderLine: purchaseOrderLineList){
                BigDecimal convertedQty = unitConversionService.convert(purchaseOrderLine.getUnit(), product.getUnit(), purchaseOrderLine.getQty(), 2, product);
                productQty = productQty.add(convertedQty);
                productWorth = productWorth.add(purchaseOrderLine.getCompanyExTaxTotal());
            }

            super.incTotalQty(productQty);
            super.incTotalWorth(productWorth);

            JPA.clear();
        }

        if(abcAnalysisLine != null){
            setQtyWorth(abcAnalysisLineRepository.find(abcAnalysisLine.getId()), productQty, productWorth);
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
