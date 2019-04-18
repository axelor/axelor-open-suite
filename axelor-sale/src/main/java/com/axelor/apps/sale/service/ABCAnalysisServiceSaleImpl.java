package com.axelor.apps.sale.service;

import com.axelor.apps.base.db.ABCAnalysis;
import com.axelor.apps.base.db.ABCAnalysisLine;
import com.axelor.apps.base.db.Product;
import com.axelor.apps.base.db.repo.ABCAnalysisClassRepository;
import com.axelor.apps.base.db.repo.ABCAnalysisLineRepository;
import com.axelor.apps.base.db.repo.ABCAnalysisRepository;
import com.axelor.apps.base.db.repo.ProductRepository;
import com.axelor.apps.base.service.ABCAnalysisServiceImpl;
import com.axelor.apps.base.service.UnitConversionService;
import com.axelor.apps.sale.db.SaleOrderLine;
import com.axelor.apps.sale.db.repo.SaleOrderLineRepository;
import com.axelor.apps.sale.db.repo.SaleOrderRepository;
import com.axelor.db.JPA;
import com.axelor.db.Query;
import com.axelor.exception.AxelorException;
import com.google.inject.Inject;
import java.math.BigDecimal;
import java.util.List;

import static com.axelor.apps.base.service.administration.AbstractBatch.FETCH_LIMIT;
import static com.axelor.apps.tool.date.DateTool.toDate;
import static com.axelor.apps.tool.date.DateTool.toLocalDateT;

public class ABCAnalysisServiceSaleImpl extends ABCAnalysisServiceImpl {
    private SaleOrderLineRepository saleOrderLineRepository;

    private final static String SELLABLE_TRUE = " AND self.sellable = TRUE";

    @Inject
    public ABCAnalysisServiceSaleImpl(ABCAnalysisLineRepository abcAnalysisLineRepository, UnitConversionService unitConversionService, ABCAnalysisRepository abcAnalysisRepository, ProductRepository productRepository, SaleOrderLineRepository saleOrderLineRepository, ABCAnalysisClassRepository abcAnalysisClassRepository) {
        super(abcAnalysisLineRepository, unitConversionService, abcAnalysisRepository, productRepository, abcAnalysisClassRepository);
        this.saleOrderLineRepository = saleOrderLineRepository;
    }

    @Override
    protected ABCAnalysisLine createABCAnalysisLine(ABCAnalysis abcAnalysis, Product product) throws AxelorException {
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
                BigDecimal convertedQty = unitConversionService.convert(saleOrderLine.getUnit(), product.getUnit(), saleOrderLine.getQty(), 5, product);
                productQty = productQty.add(convertedQty);
                productWorth = productWorth.add(saleOrderLine.getCompanyExTaxTotal());
            }

            super.incTotalQty(productQty);
            super.incTotalWorth(productWorth);

            JPA.clear();
        }

        return setQtyWorth(abcAnalysisLineRepository.find(abcAnalysisLine.getId()), productQty, productWorth);
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
