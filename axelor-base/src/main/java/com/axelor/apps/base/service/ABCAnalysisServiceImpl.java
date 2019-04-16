package com.axelor.apps.base.service;

import com.axelor.apps.base.db.ABCAnalysis;
import com.axelor.apps.base.db.ABCAnalysisClass;
import com.axelor.apps.base.db.ABCAnalysisLine;
import com.axelor.apps.base.db.Product;
import com.axelor.apps.base.db.repo.ABCAnalysisLineRepository;
import com.axelor.apps.base.db.repo.ABCAnalysisRepository;
import com.axelor.apps.base.db.repo.ProductRepository;
import com.axelor.db.JPA;
import com.axelor.db.Query;
import com.axelor.exception.AxelorException;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;

import static com.axelor.apps.base.service.administration.AbstractBatch.FETCH_LIMIT;

public class ABCAnalysisServiceImpl implements ABCAnalysisService {
    @Inject ABCAnalysisLineRepository abcAnalysisLineRepository;
    @Inject ABCAnalysisRepository abcAnalysisRepository;
    @Inject ProductRepository productRepository;

    private BigDecimal totalQty = BigDecimal.ZERO;
    private BigDecimal totalWorth = BigDecimal.ZERO;

    private BigDecimal cumulatedQty = BigDecimal.ZERO;
    private BigDecimal cumulatedWorth = BigDecimal.ZERO;

    @Override
    @Transactional
    public void runAnalysis(ABCAnalysis abcAnalysis) {

        start(abcAnalysis);
        createABCAnalysisLineForEachProduct(abcAnalysis);
        doAnalysis(abcAnalysisRepository.find(abcAnalysis.getId()));
        finish(abcAnalysisRepository.find(abcAnalysis.getId()));

    }

    @Transactional(rollbackOn = {AxelorException.class, Exception.class})
    private void start(ABCAnalysis abcAnalysis){
        abcAnalysis.setStatusSelect(ABCAnalysisRepository.STATUS_ANALYZING);
        abcAnalysisRepository.save(abcAnalysis);
    }

    private Set<Product> getProductSet(ABCAnalysis abcAnalysis) {
        Set<Product> productList = new HashSet<>();
        String productCategoryQuery = getProductCategoryQuery();
        String productFamilyQuery = getProductFamilyQuery();

        if (!abcAnalysis.getProductSet().isEmpty()) {
            productList.addAll(abcAnalysis.getProductSet());
        }

        if (!abcAnalysis.getProductCategorySet().isEmpty()) {
            productList.addAll(
                    productRepository
                            .all()
                            .filter(
                                    productCategoryQuery,
                                    abcAnalysis.getProductCategorySet(),
                                    ProductRepository.PRODUCT_TYPE_STORABLE)
                            .fetch());
        }

        if (!abcAnalysis.getProductFamilySet().isEmpty()) {
            productList.addAll(
                    productRepository
                            .all()
                            .filter(
                                    productFamilyQuery,
                                    abcAnalysis.getProductFamilySet(),
                                    ProductRepository.PRODUCT_TYPE_STORABLE)
                            .fetch());
        }

        return productList;
    }

    protected String getProductCategoryQuery() {
        return "self.productCategory in (?1) AND self.productTypeSelect = ?2";
    }

    protected String getProductFamilyQuery(){
        return "self.productFamily in (?1) AND self.productTypeSelect = ?2";
    }

    @Transactional(rollbackOn = {AxelorException.class, Exception.class})
    private void createABCAnalysisLineForEachProduct(ABCAnalysis abcAnalysis){
        Set<Product> productSet = getProductSet(abcAnalysis);
        for (Product product : productSet) {
            ABCAnalysisLine abcAnalysisLine = createABCAnalysisLine(abcAnalysis, product);
            if(abcAnalysisLine.getDecimalWorth().compareTo(BigDecimal.ZERO) == 0 ||
                    abcAnalysisLine.getDecimalQty().compareTo(BigDecimal.ZERO) == 0){
                abcAnalysisLineRepository.remove(abcAnalysisLineRepository.find(abcAnalysisLine.getId()));
            }
        }
    }

    @Transactional(rollbackOn = {AxelorException.class, Exception.class})
    protected ABCAnalysisLine createABCAnalysisLine(ABCAnalysis abcAnalysis, Product product) {
        ABCAnalysisLine abcAnalysisLine = new ABCAnalysisLine();

        abcAnalysisLine.setAbcAnalysis(abcAnalysisRepository.find(abcAnalysis.getId()));
        abcAnalysisLine.setProduct(productRepository.find(product.getId()));

        abcAnalysisLineRepository.save(abcAnalysisLine);

        return abcAnalysisLine;
    }

    @Transactional(rollbackOn = {AxelorException.class, Exception.class})
    protected ABCAnalysisLine setQtyWorth(ABCAnalysisLine abcAnalysisLine, BigDecimal decimalQty, BigDecimal decimalWorth){
        abcAnalysisLine.setDecimalQty(decimalQty);
        abcAnalysisLine.setDecimalWorth(decimalWorth);
        abcAnalysisLineRepository.save(abcAnalysisLine);

        return abcAnalysisLine;
    }

    protected void doAnalysis(ABCAnalysis abcAnalysis) {
        List<ABCAnalysisLine> abcAnalysisLineList;
        int offset = 0;
        Query<ABCAnalysisLine> query = abcAnalysisLineRepository.all()
                .filter("self.abcAnalysis.id = :abcAnalysisId")
                .bind("abcAnalysisId", abcAnalysis.getId())
                .order("-decimalWorth")
                .order("id");

        while(!(abcAnalysisLineList = query.fetch(FETCH_LIMIT, offset)).isEmpty()){
            offset += abcAnalysisLineList.size();
            abcAnalysisLineList.forEach(line -> analyzeLine(line, abcAnalysisRepository.find(abcAnalysis.getId())));
            JPA.clear();
        }
    }

    @Transactional(rollbackOn = {AxelorException.class, Exception.class})
    private void analyzeLine(ABCAnalysisLine abcAnalysisLine, ABCAnalysis abcAnalysis){
        computePercentage(abcAnalysisLine);
        setABCAnalysisClass(abcAnalysisLine, abcAnalysis);

        abcAnalysisLineRepository.save(abcAnalysisLine);
    }

    private void computePercentage(ABCAnalysisLine abcAnalysisLine){
        BigDecimal qty = abcAnalysisLine.getDecimalQty().divide(totalQty, 10, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100));
        BigDecimal worth = abcAnalysisLine.getDecimalWorth().divide(totalWorth, totalWorth.scale(), RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100));

        incCumulatedQty(qty);
        incCumulatedWorth(worth);

        abcAnalysisLine.setQty(qty);
        abcAnalysisLine.setCumulatedQty(cumulatedQty);
        abcAnalysisLine.setWorth(worth);
        abcAnalysisLine.setCumulatedWorth(cumulatedWorth);

    }

    private void setABCAnalysisClass(ABCAnalysisLine abcAnalysisLine, ABCAnalysis abcAnalysis){
        List<ABCAnalysisClass> abcAnalysisClassList = abcAnalysis.getAbcAnalysisClassList();
        abcAnalysisClassList.sort(Comparator.comparing(ABCAnalysisClass::getName));
        for(ABCAnalysisClass abcAnalysisClass: abcAnalysisClassList){
            if(abcAnalysisLine.getCumulatedWorth().compareTo(abcAnalysisClass.getWorth()) <= 0
            && abcAnalysisLine.getCumulatedQty().compareTo(abcAnalysisClass.getQty()) <= 0){
                abcAnalysisLine.setAbcAnalysisClass(abcAnalysisClass);
                break;
            }
        }
    }

    protected void incTotalQty(BigDecimal totalQty) {
        this.totalQty = this.totalQty.add(totalQty);
    }

    protected void incTotalWorth(BigDecimal totalWorth) {
        this.totalWorth = this.totalWorth.add(totalWorth);
    }

    private void incCumulatedQty(BigDecimal cumulatedQty) {
        this.cumulatedQty = this.cumulatedQty.add(cumulatedQty);
    }

    private void incCumulatedWorth(BigDecimal cumulatedWorth) {
        this.cumulatedWorth = this.cumulatedWorth.add(cumulatedWorth);
    }

    @Transactional(rollbackOn = {AxelorException.class, Exception.class})
    private void finish(ABCAnalysis abcAnalysis){
        abcAnalysis.setStatusSelect(ABCAnalysisRepository.STATUS_FINISHED);
        abcAnalysisRepository.save(abcAnalysis);
    }
}
