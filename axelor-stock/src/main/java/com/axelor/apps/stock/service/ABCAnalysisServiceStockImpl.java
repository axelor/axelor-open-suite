package com.axelor.apps.stock.service;

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
import com.axelor.apps.stock.db.StockLocation;
import com.axelor.apps.stock.db.StockLocationLine;
import com.axelor.apps.stock.db.repo.StockLocationLineRepository;
import com.axelor.db.JPA;
import com.axelor.db.Query;
import com.axelor.exception.AxelorException;
import com.google.inject.Inject;

import java.math.BigDecimal;
import java.util.List;

import static com.axelor.apps.base.service.administration.AbstractBatch.FETCH_LIMIT;

public class ABCAnalysisServiceStockImpl extends ABCAnalysisServiceImpl {

    private StockLocationService stockLocationService;
    private StockLocationLineRepository stockLocationLineRepository;

    private final static String STOCK_MANAGED_TRUE = " AND self.stockManaged = TRUE";

    @Inject
    public ABCAnalysisServiceStockImpl(ABCAnalysisLineRepository abcAnalysisLineRepository, UnitConversionService unitConversionService, ABCAnalysisRepository abcAnalysisRepository, ProductRepository productRepository, StockLocationService stockLocationService, StockLocationLineRepository stockLocationLineRepository, ABCAnalysisClassRepository abcAnalysisClassRepository, SequenceService sequenceService) {
        super(abcAnalysisLineRepository, unitConversionService, abcAnalysisRepository, productRepository, abcAnalysisClassRepository, sequenceService);
        this.stockLocationService = stockLocationService;
        this.stockLocationLineRepository = stockLocationLineRepository;
    }

    @Override
    protected ABCAnalysisLine createABCAnalysisLine(ABCAnalysis abcAnalysis, Product product) throws AxelorException {
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

            for(StockLocationLine stockLocationLine: stockLocationLineList){
                BigDecimal convertedQty = unitConversionService.convert(stockLocationLine.getUnit(), product.getUnit(), stockLocationLine.getCurrentQty(), 5, product);
                productQty = productQty.add(convertedQty);
                productWorth = productQty.add(stockLocationLine.getAvgPrice());

            }

            super.incTotalQty(productQty);
            super.incTotalWorth(productWorth);

            JPA.clear();
        }

        return setQtyWorth(abcAnalysisLineRepository.find(abcAnalysisLine.getId()), productQty, productWorth);
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
