package com.axelor.apps.stock.service;

import com.axelor.apps.base.db.ABCAnalysis;
import com.axelor.apps.base.db.ABCAnalysisLine;
import com.axelor.apps.base.db.Product;
import com.axelor.apps.base.db.repo.ABCAnalysisLineRepository;
import com.axelor.apps.base.service.ABCAnalysisServiceImpl;
import com.axelor.apps.stock.db.StockLocation;
import com.axelor.apps.stock.db.StockLocationLine;
import com.axelor.apps.stock.db.repo.StockLocationLineRepository;
import com.axelor.db.JPA;
import com.axelor.db.Query;
import com.google.inject.Inject;

import java.math.BigDecimal;
import java.util.List;

import static com.axelor.apps.base.service.administration.AbstractBatch.FETCH_LIMIT;
import static com.axelor.apps.tool.StringTool.getIdListString;

public class ABCAnalysisServiceStockImpl extends ABCAnalysisServiceImpl {

    @Inject StockLocationService stockLocationService;
    @Inject StockLocationLineRepository stockLocationLineRepository;
    @Inject ABCAnalysisLineRepository abcAnalysisLineRepository;

    private final static String STOCK_MANAGED_TRUE = " AND self.stockManaged = TRUE";

    @Override
    protected ABCAnalysisLine createABCAnalysisLine(ABCAnalysis abcAnalysis, Product product) {
        ABCAnalysisLine abcAnalysisLine =  super.createABCAnalysisLine(abcAnalysis, product);
        List<StockLocation> stockLocationList = stockLocationService.getAllLocationAndSubLocation(abcAnalysis.getStockLocation(), false);
        BigDecimal productQty = BigDecimal.ZERO;
        BigDecimal productWorth = BigDecimal.ZERO;
        List<StockLocationLine> stockLocationLineList;
        int offset = 0;

        Query<StockLocationLine> stockLocationLineQuery = stockLocationLineRepository.all()
                .filter("self.stockLocation IN :stockLocationList AND self.product.id = :productId AND self.currentQty != 0 ")
                .bind("stockLocationList", stockLocationList)
                .bind("productId", product.getId());

        while(!(stockLocationLineList = stockLocationLineQuery.fetch(FETCH_LIMIT, offset)).isEmpty()){
            offset += stockLocationLineList.size();

            for(StockLocationLine purchaseOrderLine: stockLocationLineList){
                productQty = productQty.add(purchaseOrderLine.getCurrentQty());
                productWorth = productQty.add(purchaseOrderLine.getAvgPrice());
            }

            super.incTotalQty(productQty);
            super.incTotalWorth(productWorth);

            JPA.clear();
        }

        return super.setQtyWorth(abcAnalysisLineRepository.find(abcAnalysisLine.getId()), productQty, productWorth);
    }

    @Override
    protected String getProductCategoryQuery() {
        return super.getProductCategoryQuery() + STOCK_MANAGED_TRUE;
    }

    @Override
    protected String getProductFamilyQuery() {
        return super.getProductFamilyQuery() + STOCK_MANAGED_TRUE;
    }
}
