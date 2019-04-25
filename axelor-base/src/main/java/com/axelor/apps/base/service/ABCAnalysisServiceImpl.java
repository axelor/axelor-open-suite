package com.axelor.apps.base.service;

import com.axelor.apps.base.db.ABCAnalysis;
import com.axelor.apps.base.db.ABCAnalysisClass;
import com.axelor.apps.base.db.ABCAnalysisLine;
import com.axelor.apps.base.db.Product;
import com.axelor.apps.base.db.repo.ABCAnalysisClassRepository;
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
    protected ABCAnalysisLineRepository abcAnalysisLineRepository;
    protected UnitConversionService unitConversionService;
    private ABCAnalysisRepository abcAnalysisRepository;
    private ProductRepository productRepository;
    private ABCAnalysisClassRepository abcAnalysisClassRepository;

    private BigDecimal totalQty = BigDecimal.ZERO;
    private BigDecimal totalWorth = BigDecimal.ZERO;

    private BigDecimal cumulatedQty = BigDecimal.valueOf(0, 3);
    private BigDecimal cumulatedWorth = BigDecimal.valueOf(0, 3);

    private List<ABCAnalysisClass> abcAnalysisClassList;

    @Inject
    public ABCAnalysisServiceImpl(ABCAnalysisLineRepository abcAnalysisLineRepository, UnitConversionService unitConversionService, ABCAnalysisRepository abcAnalysisRepository, ProductRepository productRepository, ABCAnalysisClassRepository abcAnalysisClassRepository) {
        this.abcAnalysisLineRepository = abcAnalysisLineRepository;
        this.unitConversionService = unitConversionService;
        this.abcAnalysisRepository = abcAnalysisRepository;
        this.productRepository = productRepository;
        this.abcAnalysisClassRepository = abcAnalysisClassRepository;
    }

    @Override
    public List<ABCAnalysisClass> initABCClasses() {
        List<ABCAnalysisClass> abcAnalysisClassList = new ArrayList<>();

        abcAnalysisClassList.add(createAbcClass("A", 0, BigDecimal.valueOf(80), BigDecimal.valueOf(20)));
        abcAnalysisClassList.add(createAbcClass("B", 1, BigDecimal.valueOf(15), BigDecimal.valueOf(30)));
        abcAnalysisClassList.add(createAbcClass("C", 2, BigDecimal.valueOf(5), BigDecimal.valueOf(50)));

        return abcAnalysisClassList;
    }

    private ABCAnalysisClass createAbcClass(String name, Integer sequence, BigDecimal worth, BigDecimal qty){
        ABCAnalysisClass abcAnalysisClass = new ABCAnalysisClass();

        abcAnalysisClass.setName(name);
        abcAnalysisClass.setSequence(sequence);
        abcAnalysisClass.setWorth(worth);
        abcAnalysisClass.setQty(qty);

        return abcAnalysisClass;
    }

    @Override
    @Transactional
    public void runAnalysis(ABCAnalysis abcAnalysis) throws AxelorException {

        start(abcAnalysis);
        getAbcAnalysisClassList(abcAnalysis);
        createABCAnalysisLineForEachProduct(abcAnalysis);
        doAnalysis(abcAnalysisRepository.find(abcAnalysis.getId()));
        finish(abcAnalysisRepository.find(abcAnalysis.getId()));

    }

    @Transactional(rollbackOn = {AxelorException.class, Exception.class})
    private void start(ABCAnalysis abcAnalysis){
        abcAnalysis.setStatusSelect(ABCAnalysisRepository.STATUS_ANALYZING);
        abcAnalysisRepository.save(abcAnalysis);
    }


    private void getAbcAnalysisClassList(ABCAnalysis abcAnalysis){
        Query<ABCAnalysisClass> abcAnalysisClassQuery = abcAnalysisClassRepository.all()
                .filter("self.abcAnalysis.id = :abcAnalysisId")
                .bind("abcAnalysisId", abcAnalysis.getId())
                .order("sequence");
        this.abcAnalysisClassList =  abcAnalysisClassQuery.fetch();
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
    private void createABCAnalysisLineForEachProduct(ABCAnalysis abcAnalysis) throws AxelorException {
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
    protected ABCAnalysisLine createABCAnalysisLine(ABCAnalysis abcAnalysis, Product product) throws AxelorException {
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
            abcAnalysisLineList.forEach(this::analyzeLine);
            JPA.clear();
        }
    }

    @Transactional(rollbackOn = {AxelorException.class, Exception.class})
    private void analyzeLine(ABCAnalysisLine abcAnalysisLine){
        computePercentage(abcAnalysisLine);
        setABCAnalysisClass(abcAnalysisLine);

        abcAnalysisLineRepository.save(abcAnalysisLine);
    }

    private void computePercentage(ABCAnalysisLine abcAnalysisLine){
        BigDecimal qty = abcAnalysisLine.getDecimalQty().multiply(BigDecimal.valueOf(100)).divide(totalQty, 3, RoundingMode.HALF_EVEN);
        BigDecimal worth = abcAnalysisLine.getDecimalWorth().multiply(BigDecimal.valueOf(100)).divide(totalWorth, 3, RoundingMode.HALF_EVEN);

        incCumulatedQty(qty);
        incCumulatedWorth(worth);

        abcAnalysisLine.setQty(qty);
        abcAnalysisLine.setCumulatedQty(cumulatedQty);
        abcAnalysisLine.setWorth(worth);
        abcAnalysisLine.setCumulatedWorth(cumulatedWorth);

    }

    private void setABCAnalysisClass(ABCAnalysisLine abcAnalysisLine){
        BigDecimal maxQty = BigDecimal.ZERO;
        BigDecimal maxWorth = BigDecimal.ZERO;
        BigDecimal lineCumulatedQty = abcAnalysisLine.getCumulatedQty().setScale(2, RoundingMode.HALF_EVEN);
        BigDecimal lineCumulatedWorth = abcAnalysisLine.getCumulatedWorth().setScale(2, RoundingMode.HALF_EVEN);

        for(ABCAnalysisClass abcAnalysisClass: abcAnalysisClassList){
            maxQty = maxQty.add(abcAnalysisClass.getQty());
            maxWorth = maxWorth.add(abcAnalysisClass.getWorth());
            if(lineCumulatedQty.compareTo(maxQty) <= 0 && lineCumulatedWorth.compareTo(maxWorth) <= 0){
                abcAnalysisLine.setAbcAnalysisClass(abcAnalysisClassRepository.find(abcAnalysisClass.getId()));
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
