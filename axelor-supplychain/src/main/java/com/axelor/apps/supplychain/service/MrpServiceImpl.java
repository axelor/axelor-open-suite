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
package com.axelor.apps.supplychain.service;

import static java.time.temporal.ChronoUnit.DAYS;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.Partner;
import com.axelor.apps.base.db.Product;
import com.axelor.apps.base.db.ProductCategory;
import com.axelor.apps.base.db.ProductMultipleQty;
import com.axelor.apps.base.db.Unit;
import com.axelor.apps.base.db.repo.ProductRepository;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.apps.base.service.ProductCategoryService;
import com.axelor.apps.base.service.UnitConversionService;
import com.axelor.apps.base.service.administration.AbstractBatch;
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.apps.base.service.exception.TraceBackService;
import com.axelor.apps.purchase.db.PurchaseOrder;
import com.axelor.apps.purchase.db.PurchaseOrderLine;
import com.axelor.apps.purchase.db.SupplierCatalog;
import com.axelor.apps.purchase.db.repo.PurchaseOrderLineRepository;
import com.axelor.apps.purchase.db.repo.PurchaseOrderRepository;
import com.axelor.apps.purchase.service.app.AppPurchaseService;
import com.axelor.apps.sale.db.SaleOrder;
import com.axelor.apps.sale.db.SaleOrderLine;
import com.axelor.apps.sale.db.repo.SaleOrderLineRepository;
import com.axelor.apps.sale.service.app.AppSaleService;
import com.axelor.apps.stock.db.StockHistoryLine;
import com.axelor.apps.stock.db.StockLocation;
import com.axelor.apps.stock.db.StockLocationLine;
import com.axelor.apps.stock.db.StockRules;
import com.axelor.apps.stock.db.repo.StockHistoryLineRepository;
import com.axelor.apps.stock.db.repo.StockLocationLineRepository;
import com.axelor.apps.stock.db.repo.StockLocationRepository;
import com.axelor.apps.stock.db.repo.StockRulesRepository;
import com.axelor.apps.stock.service.StockLocationService;
import com.axelor.apps.stock.service.StockRulesService;
import com.axelor.apps.supplychain.db.Mrp;
import com.axelor.apps.supplychain.db.MrpFamily;
import com.axelor.apps.supplychain.db.MrpForecast;
import com.axelor.apps.supplychain.db.MrpLine;
import com.axelor.apps.supplychain.db.MrpLineOrigin;
import com.axelor.apps.supplychain.db.MrpLineType;
import com.axelor.apps.supplychain.db.repo.MrpForecastRepository;
import com.axelor.apps.supplychain.db.repo.MrpLineRepository;
import com.axelor.apps.supplychain.db.repo.MrpLineTypeRepository;
import com.axelor.apps.supplychain.db.repo.MrpRepository;
import com.axelor.apps.supplychain.exception.SupplychainExceptionMessage;
import com.axelor.auth.AuthUtils;
import com.axelor.db.JPA;
import com.axelor.db.Model;
import com.axelor.db.Query;
import com.axelor.db.mapper.Mapper;
import com.axelor.i18n.I18n;
import com.axelor.message.service.MailMessageService;
import com.axelor.utils.StringTool;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import com.google.inject.servlet.RequestScoper;
import com.google.inject.servlet.ServletScopes;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MrpServiceImpl implements MrpService {

  private final Logger log = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
  private static final Integer ITERATIONS = 100;

  protected MrpRepository mrpRepository;
  protected StockLocationRepository stockLocationRepository;
  protected ProductRepository productRepository;
  protected StockLocationLineRepository stockLocationLineRepository;
  protected MrpLineTypeRepository mrpLineTypeRepository;
  protected PurchaseOrderLineRepository purchaseOrderLineRepository;
  protected SaleOrderLineRepository saleOrderLineRepository;
  protected MrpLineRepository mrpLineRepository;
  protected StockRulesService stockRulesService;
  protected MrpLineService mrpLineService;
  protected MrpForecastRepository mrpForecastRepository;
  protected StockLocationService stockLocationService;
  protected MailMessageService mailMessageService;
  protected UnitConversionService unitConversionService;
  protected ProductCategoryService productCategoryService;
  protected StockHistoryLineRepository stockHistoryLineRepository;
  protected MrpLineTypeService mrpLineTypeService;
  protected MrpSaleOrderCheckLateSaleService mrpSaleOrderCheckLateSaleService;

  protected AppBaseService appBaseService;
  protected AppSaleService appSaleService;
  protected AppPurchaseService appPurchaseService;

  protected List<StockLocation> stockLocationList;
  protected Map<Long, Integer> productMap;
  protected Mrp mrp;
  protected LocalDate today;

  @Inject
  public MrpServiceImpl(
      MrpRepository mrpRepository,
      StockLocationRepository stockLocationRepository,
      ProductRepository productRepository,
      StockLocationLineRepository stockLocationLineRepository,
      MrpLineTypeRepository mrpLineTypeRepository,
      PurchaseOrderLineRepository purchaseOrderLineRepository,
      SaleOrderLineRepository saleOrderLineRepository,
      MrpLineRepository mrpLineRepository,
      StockRulesService stockRulesService,
      MrpLineService mrpLineService,
      MrpForecastRepository mrpForecastRepository,
      ProductCategoryService productCategoryService,
      StockLocationService stockLocationService,
      MailMessageService mailMessageService,
      UnitConversionService unitConversionService,
      AppBaseService appBaseService,
      AppSaleService appSaleService,
      AppPurchaseService appPurchaseService,
      StockHistoryLineRepository stockHistoryLineRepository,
      MrpSaleOrderCheckLateSaleService mrpSaleOrderCheckLateSaleService,
      MrpLineTypeService mrpLineTypeService) {

    this.mrpRepository = mrpRepository;
    this.stockLocationRepository = stockLocationRepository;
    this.productRepository = productRepository;
    this.stockLocationLineRepository = stockLocationLineRepository;
    this.mrpLineTypeRepository = mrpLineTypeRepository;
    this.purchaseOrderLineRepository = purchaseOrderLineRepository;
    this.saleOrderLineRepository = saleOrderLineRepository;
    this.mrpLineRepository = mrpLineRepository;
    this.stockRulesService = stockRulesService;
    this.mrpLineService = mrpLineService;
    this.mrpForecastRepository = mrpForecastRepository;
    this.productCategoryService = productCategoryService;
    this.stockLocationService = stockLocationService;
    this.mailMessageService = mailMessageService;
    this.unitConversionService = unitConversionService;
    this.appBaseService = appBaseService;
    this.appSaleService = appSaleService;
    this.appPurchaseService = appPurchaseService;
    this.stockHistoryLineRepository = stockHistoryLineRepository;
    this.mrpLineTypeService = mrpLineTypeService;
    this.mrpSaleOrderCheckLateSaleService = mrpSaleOrderCheckLateSaleService;
  }

  @Override
  public void setMrp(Mrp mrp) {
    this.mrp = mrp;
  }

  @Override
  public void runCalculation(Mrp mrp) throws AxelorException {

    this.reset(mrpRepository.find(mrp.getId()));

    this.startMrp(mrpRepository.find(mrp.getId()));
    this.completeMrp(mrpRepository.find(mrp.getId()));
    this.doCalculation(mrpRepository.find(mrp.getId()));
    this.finish(mrpRepository.find(mrp.getId()));
  }

  @Override
  public boolean isOnGoing(Mrp mrp) {

    return mrp.getStatusSelect() == MrpRepository.STATUS_CALCULATION_STARTED;
  }

  @Transactional
  protected void startMrp(Mrp mrp) {

    mrp.setStartDateTime(appBaseService.getTodayDateTime().toLocalDateTime());
    log.debug("Start MRP");

    mrp.setStatusSelect(MrpRepository.STATUS_CALCULATION_STARTED);

    // TODO check that the different types used for purchase/manufOrder proposal are in stock type
    // TODO check that all types exist + override the method on production module

    mrpRepository.save(mrp);
  }

  @Override
  @Transactional
  public void reset(Mrp mrp) {
    today = appBaseService.getTodayDate(mrp.getStockLocation().getCompany());

    mrpLineRepository
        .all()
        .filter("self.mrp.id = ?1 AND self.isEditedByUser = false", mrp.getId())
        .remove();
    mrpLineRepository
        .all()
        .filter(
            "self.mrp.id = ?1 AND self.isEditedByUser = true AND self.maturityDate < ?2",
            mrp.getId(),
            today)
        .update("maturityDate", today);

    mrp.setStatusSelect(MrpRepository.STATUS_DRAFT);
    mrp.setErrorLog(null);

    mrpRepository.save(mrp);
  }

  protected void completeMrp(Mrp mrp) throws AxelorException {

    log.debug("Complete MRP");

    // Initialize
    this.mrp = mrp;
    List<StockLocation> slList =
        stockLocationService.getAllLocationAndSubLocation(mrp.getStockLocation(), false).stream()
            .filter(x -> !x.getIsNotInMrp())
            .collect(Collectors.toList());
    this.stockLocationList = slList;

    this.assignProductAndLevel(this.getProductList());
    if (stockLocationList.isEmpty()) {
      throw new AxelorException(
          Mrp.class,
          TraceBackRepository.CATEGORY_NO_VALUE,
          I18n.get(SupplychainExceptionMessage.MRP_MISSING_STOCK_LOCATION_VALID));
    }
    // Get the stock for each product on each stock location
    this.createAvailableStockMrpLines();

    this.createPurchaseMrpLines();

    this.createSaleOrderMrpLines();

    this.createSaleForecastMrpLines();

    this.createStockHistoryMrpLines();
  }

  protected void doCalculation(Mrp mrp) throws AxelorException {

    log.debug("Do calculation");

    this.mrp = mrp;

    this.checkInsufficientCumulativeQty();

    //		this.consolidateMrp(mrp);

  }

  @Transactional
  protected void finish(Mrp mrp) {

    log.debug("Finish MRP");

    mrp.setStatusSelect(MrpRepository.STATUS_CALCULATION_ENDED);
    mrp.setEndDateTime(appBaseService.getTodayDateTime().toLocalDateTime());
    mrpRepository.save(mrp);
  }

  protected void checkInsufficientCumulativeQty() throws AxelorException {

    for (int level = 0; level <= this.getMaxLevel(); level++) {

      for (Product product : this.getProductList(level)) {

        this.checkInsufficientCumulativeQty(product);
      }
    }
  }

  /**
   * Get the list of product for a level
   *
   * @param level
   * @return
   */
  protected List<Product> getProductList(int level) {

    List<Product> productList = Lists.newArrayList();

    for (Long productId : this.productMap.keySet()) {

      if (this.productMap.get(productId) == level) {
        productList.add(productRepository.find(productId));
      }
    }

    return productList;
  }

  protected int getMaxLevel() {

    int maxLevel = 0;

    for (int level : this.productMap.values()) {

      if (level > maxLevel) {
        maxLevel = level;
      }
    }

    return maxLevel;
  }

  protected void checkInsufficientCumulativeQty(Product product) throws AxelorException {
    checkInsufficientCumulativeQty(product, 0);
  }

  protected void checkInsufficientCumulativeQty(Product product, int counter)
      throws AxelorException {

    final int MAX_ITERATION = 1000;

    if (counter > MAX_ITERATION) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_INCONSISTENCY,
          I18n.get(SupplychainExceptionMessage.MRP_TOO_MANY_ITERATIONS));
    }

    boolean doASecondPass = false;

    this.computeCumulativeQty(productRepository.find(product.getId()));

    JPA.clear();

    List<MrpLine> mrpLineList =
        mrpLineRepository
            .all()
            .filter("self.mrp.id = ?1 AND self.product.id = ?2", mrp.getId(), product.getId())
            .order("maturityDate")
            .order("mrpLineType.typeSelect")
            .order("mrpLineType.sequence")
            .order("id")
            .fetch();
    for (MrpLine mrpLine : mrpLineList) {

      doASecondPass =
          this.checkInsufficientCumulativeQty(
              mrpLineRepository.find(mrpLine.getId()),
              productRepository.find(product.getId()),
              counter == 0);
      JPA.clear();
      if (doASecondPass) {
        break;
      }
    }

    if (doASecondPass) {

      this.checkInsufficientCumulativeQty(product, counter + 1);
    }
  }

  @Transactional(rollbackOn = {Exception.class})
  protected boolean checkInsufficientCumulativeQty(
      MrpLine mrpLine, Product product, boolean firstPass) throws AxelorException {

    BigDecimal cumulativeQty = mrpLine.getCumulativeQty();

    MrpLineType mrpLineType = mrpLine.getMrpLineType();
    if (mrpLineType.getElementSelect() == MrpLineTypeRepository.ELEMENT_PURCHASE_PROPOSAL
        && mrpLine.getEstimatedDeliveryMrpLine() != null) {
      return false;
    }

    if ((mrpLineType.getElementSelect() == MrpLineTypeRepository.ELEMENT_PURCHASE_ORDER
            || mrpLineType.getElementSelect() == MrpLineTypeRepository.ELEMENT_MANUFACTURING_ORDER)
        && !firstPass) {
      return false;
    }

    boolean isProposalElement = this.isProposalElement(mrpLineType);

    BigDecimal minQty = mrpLine.getMinQty();

    if ((((mrpLine.getMrpLineType().getElementSelect()
                    != MrpLineTypeRepository.ELEMENT_AVAILABLE_STOCK)
                && (!isProposalElement
                    || mrpLineType.getTypeSelect() == MrpLineTypeRepository.TYPE_OUT))
            || (mrpLine.getMrpLineType().getElementSelect()
                    == MrpLineTypeRepository.ELEMENT_AVAILABLE_STOCK
                && firstPass))
        && cumulativeQty.compareTo(mrpLine.getMinQty()) < 0) {

      Company company = null;
      StockLocation stockLocation = mrpLine.getStockLocation();

      log.debug(
          "Cumulative qty ({} < {}) is insufficient for product ({}) at the maturity date ({})",
          cumulativeQty,
          minQty,
          product.getFullName(),
          mrpLine.getMaturityDate());

      BigDecimal reorderQty = minQty.subtract(cumulativeQty);

      StockRules stockRules =
          stockRulesService.getStockRules(
              product,
              stockLocation,
              StockRulesRepository.TYPE_FUTURE,
              StockRulesRepository.USE_CASE_USED_FOR_MRP);

      if (stockRules != null) {
        reorderQty = reorderQty.max(stockRules.getReOrderQty());
      }

      if (stockLocation.getCompany() != null) {
        company = stockLocation.getCompany();
      }

      MrpLineType mrpLineTypeProposal =
          this.getMrpLineTypeForProposal(stockRules, product, company);

      if (mrpLineTypeProposal == null) {
        return false;
      }

      long duplicateCount =
          mrpLineRepository
              .all()
              .filter(
                  "self.mrp.id = ?1  AND self.isEditedByUser = ?2 AND self.product = ?3 AND self.relatedToSelectName = ?4",
                  mrp.getId(),
                  true,
                  product,
                  mrpLine.getRelatedToSelectName())
              .count();

      if (duplicateCount != 0) {
        return false;
      }

      this.createProposalMrpLine(
          mrpLine.getMrp(),
          product,
          mrpLineTypeProposal,
          reorderQty,
          stockLocation,
          mrpLine.getMaturityDate(),
          mrpLine.getMrpLineOriginList(),
          mrpLine.getRelatedToSelectName());

      return true;
    }

    return false;
  }

  public MrpLine getPreviousProposalMrpLine(
      Product product,
      MrpLineType mrpLineType,
      StockLocation stockLocation,
      LocalDate maturityDate) {

    LocalDate startPeriodDate = maturityDate;

    MrpFamily mrpFamily = product.getMrpFamily();

    if (mrpFamily != null) {

      if (mrpFamily.getDayNb() == 0) {
        return null;
      }

      startPeriodDate = maturityDate.minusDays(mrpFamily.getDayNb());
    }

    return mrpLineRepository
        .all()
        .filter(
            "self.mrp.id = ?1 AND self.product = ?2 AND self.mrpLineType = ?3 AND self.stockLocation = ?4 AND self.maturityDate > ?5 AND self.maturityDate <= ?6",
            mrp.getId(),
            product,
            mrpLineType,
            stockLocation,
            startPeriodDate,
            maturityDate)
        .fetchOne();
  }

  @Transactional(rollbackOn = {Exception.class})
  protected void createProposalMrpLine(
      Mrp mrp,
      Product product,
      MrpLineType mrpLineType,
      BigDecimal reorderQty,
      StockLocation stockLocation,
      LocalDate maturityDate,
      List<MrpLineOrigin> mrpLineOriginList,
      String relatedToSelectName)
      throws AxelorException {

    LocalDate initialMaturityDate = maturityDate;

    if (mrpLineType.getElementSelect() == MrpLineTypeRepository.ELEMENT_PURCHASE_PROPOSAL) {
      maturityDate = maturityDate.minusDays(product.getSupplierDeliveryTime());
      reorderQty = reorderQty.max(this.getSupplierCatalogMinQty(product));
      if (appPurchaseService.getAppPurchase() != null
          && appPurchaseService.getAppPurchase().getManageMultiplePurchaseQuantity()) {
        reorderQty = computeMultipleProductsPurchaseReorderQty(product, reorderQty);
      }
    }

    if (maturityDate.isBefore(today)) {
      maturityDate = today;
    }

    MrpLine mrpLine =
        this.getPreviousProposalMrpLine(product, mrpLineType, stockLocation, maturityDate);

    if (mrpLine != null) {
      if (mrpLineType.getTypeSelect() == MrpLineTypeRepository.TYPE_OUT) {
        reorderQty = reorderQty.negate();
      }
      mrpLine.setQty(mrpLine.getQty().add(reorderQty));
      mrpLine.setRelatedToSelectName(null);
      MrpLine estimatedDeliveryMrpLine = mrpLine.getEstimatedDeliveryMrpLine();
      if (estimatedDeliveryMrpLine != null) {
        estimatedDeliveryMrpLine.setQty(estimatedDeliveryMrpLine.getQty().add(reorderQty));
        estimatedDeliveryMrpLine.setRelatedToSelectName(relatedToSelectName);
        this.copyMrpLineOrigins(estimatedDeliveryMrpLine, mrpLineOriginList);
      }
    } else {
      MrpLine createdmrpLine =
          this.createMrpLine(
              mrp,
              product,
              mrpLineType,
              reorderQty,
              maturityDate,
              BigDecimal.ZERO,
              stockLocation,
              null);

      mrpLine = mrpLineRepository.save(createdmrpLine);

      if (createdmrpLine != null) {
        createdmrpLine.setWarnDelayFromSupplier(
            getWarnDelayFromSupplier(createdmrpLine, initialMaturityDate));

        MrpLineType mrpLineTypeEstimatedDelivery =
            mrpLineTypeService.getMrpLineType(
                MrpLineTypeRepository.ELEMENT_PURCHASE_PROPOSAL_ESTIMATED_DELIVERY,
                mrp.getMrpTypeSelect());
        if (mrpLineType.getElementSelect() == MrpLineTypeRepository.ELEMENT_PURCHASE_PROPOSAL
            && mrpLineTypeEstimatedDelivery != null) {
          MrpLine createdEstimatedDeliveryMrpLine =
              this.createMrpLine(
                  mrp,
                  product,
                  mrpLineTypeEstimatedDelivery,
                  reorderQty,
                  initialMaturityDate,
                  BigDecimal.ZERO,
                  stockLocation,
                  null);
          createdEstimatedDeliveryMrpLine.setRelatedToSelectName(relatedToSelectName);
          createdEstimatedDeliveryMrpLine.setEstimatedDeliveryDate(
              maturityDate.plusDays(product.getSupplierDeliveryTime()));
          createdEstimatedDeliveryMrpLine.setWarnDelayFromSupplier(
              createdmrpLine.getWarnDelayFromSupplier());
          this.copyMrpLineOrigins(createdEstimatedDeliveryMrpLine, mrpLineOriginList);
          createdmrpLine.setEstimatedDeliveryMrpLine(createdEstimatedDeliveryMrpLine);
          createdmrpLine.setDeliveryDelayDate(
              initialMaturityDate.minusDays(product.getSupplierDeliveryTime()));
        }
      }

      mrpLine.setRelatedToSelectName(relatedToSelectName);
    }

    this.copyMrpLineOrigins(mrpLine, mrpLineOriginList);
  }

  protected boolean getWarnDelayFromSupplier(MrpLine mrpLine, LocalDate initialMaturityDate) {
    return mrpLine.getMrpLineType().getElementSelect()
            == MrpLineTypeRepository.ELEMENT_PURCHASE_PROPOSAL
        && DAYS.between(mrpLine.getMaturityDate(), initialMaturityDate)
            < mrpLine.getProduct().getSupplierDeliveryTime();
  }

  protected BigDecimal getSupplierCatalogMinQty(Product product) {

    Partner supplierPartner = product.getDefaultSupplierPartner();

    if (supplierPartner != null && appPurchaseService.getAppPurchase().getManageSupplierCatalog()) {

      for (SupplierCatalog supplierCatalog : product.getSupplierCatalogList()) {

        if (supplierCatalog.getSupplierPartner().equals(supplierPartner)) {
          return supplierCatalog.getMinQty();
        }
      }
    }
    return BigDecimal.ZERO;
  }

  protected BigDecimal computeMultipleProductsPurchaseReorderQty(
      Product product, BigDecimal reorderQty) {
    List<ProductMultipleQty> productMultipleQtyList = product.getPurchaseProductMultipleQtyList();
    if (productMultipleQtyList == null || reorderQty == null || reorderQty.signum() == 0) {
      return reorderQty;
    }
    BigDecimal diff =
        productMultipleQtyList.stream()
            .map(ProductMultipleQty::getMultipleQty)
            .filter(bigDecimal -> bigDecimal.signum() != 0)
            // compute what needs to be added to reorder quantity to have a multiple
            .map(
                bigDecimal -> {
                  BigDecimal remainder = reorderQty.remainder(bigDecimal);
                  return remainder.signum() == 0 ? BigDecimal.ZERO : bigDecimal.subtract(remainder);
                })
            .min(Comparator.naturalOrder())
            .orElse(BigDecimal.ZERO);
    return reorderQty.add(diff);
  }

  protected MrpLineType getMrpLineTypeForProposal(
      StockRules stockRules, Product product, Company company) throws AxelorException {

    return mrpLineTypeService.getMrpLineType(
        MrpLineTypeRepository.ELEMENT_PURCHASE_PROPOSAL, mrp.getMrpTypeSelect());
  }

  protected void consolidateMrp() {

    List<MrpLine> mrpLineList =
        mrpLineRepository
            .all()
            .filter("self.mrp.id = ?1", mrp.getId())
            .order("product.code")
            .order("maturityDate")
            .order("mrpLineType.typeSelect")
            .order("mrpLineType.sequence")
            .order("id")
            .fetch();

    Map<List<Object>, MrpLine> map = Maps.newHashMap();
    MrpLine consolidateMrpLine = null;
    List<Object> keys = new ArrayList<>();

    for (MrpLine mrpLine : mrpLineList) {

      MrpLineType mrpLineType = mrpLine.getMrpLineType();

      keys.clear();
      keys.add(mrpLineType);
      keys.add(mrpLine.getProduct());
      keys.add(mrpLine.getMaturityDate());
      keys.add(mrpLine.getStockLocation());

      if (map.containsKey(keys)) {

        consolidateMrpLine = map.get(keys);
        consolidateMrpLine.setQty(consolidateMrpLine.getQty().add(mrpLine.getQty()));
        consolidateMrpLine.setCumulativeQty(
            consolidateMrpLine.getCumulativeQty().add(mrpLine.getCumulativeQty()));
        mrpLineRepository.remove(mrpLine);

      } else {
        map.put(keys, mrpLine);
      }
    }
  }

  protected boolean isProposalElement(MrpLineType mrpLineType) {
    return mrpLineType.getElementSelect() == MrpLineTypeRepository.ELEMENT_PURCHASE_PROPOSAL;
  }

  protected void computeCumulativeQty() {

    for (Long productId : this.productMap.keySet()) {

      this.computeCumulativeQty(productRepository.find(productId));
    }
  }

  @Transactional
  protected void computeCumulativeQty(Product product) {

    List<MrpLine> mrpLineList =
        mrpLineRepository
            .all()
            .filter("self.mrp.id = ?1 AND self.product.id = ?2", mrp.getId(), product.getId())
            .order("maturityDate")
            .order("mrpLineType.typeSelect")
            .order("mrpLineType.sequence")
            .order("id")
            .fetch();

    BigDecimal previousCumulativeQty = BigDecimal.ZERO;
    for (MrpLine mrpLine : mrpLineList) {

      if (mrpLine.getMrpLineType() != null
          && mrpLine.getMrpLineType().getElementSelect()
              == MrpLineTypeRepository.ELEMENT_PURCHASE_PROPOSAL
          && mrpLine.getEstimatedDeliveryMrpLine() != null) {
        mrpLine.setCumulativeQty(previousCumulativeQty);
      } else {
        mrpLine.setCumulativeQty(previousCumulativeQty.add(mrpLine.getQty()));
      }
      previousCumulativeQty = mrpLine.getCumulativeQty();

      log.debug(
          "Cumulative qty is ({}) for product ({}) and move ({}) at the maturity date ({})",
          previousCumulativeQty,
          mrpLine.getProduct().getFullName(),
          mrpLine.getMrpLineType().getName(),
          mrpLine.getMaturityDate());
    }
  }

  protected void createPurchaseMrpLines() throws AxelorException {
    MrpLineType purchaseOrderMrpLineType =
        mrpLineTypeService.getMrpLineType(
            MrpLineTypeRepository.ELEMENT_PURCHASE_ORDER, mrp.getMrpTypeSelect());

    if (purchaseOrderMrpLineType == null) {
      return;
    }

    String statusSelect = purchaseOrderMrpLineType.getStatusSelect();
    List<Integer> statusList = StringTool.getIntegerList(statusSelect);

    if (statusList.isEmpty()) {
      statusList.add(PurchaseOrderRepository.STATUS_VALIDATED);
    }

    // TODO : Manage the case where order is partially delivered
    List<PurchaseOrderLine> purchaseOrderLineList =
        purchaseOrderLineRepository
            .all()
            .filter(
                "self.product.id in (?1) AND self.purchaseOrder.stockLocation in (?2) AND self.receiptState != ?3 "
                    + "AND self.purchaseOrder.statusSelect IN (?4)",
                this.productMap.keySet(),
                this.stockLocationList,
                PurchaseOrderRepository.STATE_RECEIVED,
                statusList)
            .fetch();

    for (PurchaseOrderLine purchaseOrderLine : purchaseOrderLineList) {

      this.createPurchaseMrpLines(
          mrpRepository.find(mrp.getId()),
          purchaseOrderLineRepository.find(purchaseOrderLine.getId()),
          mrpLineTypeRepository.find(purchaseOrderMrpLineType.getId()));
      JPA.clear();
    }
  }

  @Transactional(rollbackOn = {Exception.class})
  protected void createPurchaseMrpLines(
      Mrp mrp, PurchaseOrderLine purchaseOrderLine, MrpLineType purchaseOrderMrpLineType)
      throws AxelorException {

    PurchaseOrder purchaseOrder = purchaseOrderLine.getPurchaseOrder();

    LocalDate maturityDate = purchaseOrderLine.getEstimatedReceiptDate();

    if (maturityDate == null) {
      maturityDate = purchaseOrder.getEstimatedReceiptDate();
    }
    if (maturityDate == null) {
      maturityDate = purchaseOrderLine.getDesiredReceiptDate();
    }
    maturityDate = this.computeMaturityDate(maturityDate, purchaseOrderMrpLineType);

    if (this.isBeforeEndDate(maturityDate) || purchaseOrderMrpLineType.getIgnoreEndDate()) {

      Unit unit = purchaseOrderLine.getProduct().getUnit();
      BigDecimal qty = purchaseOrderLine.getQty().subtract(purchaseOrderLine.getReceivedQty());
      if (!unit.equals(purchaseOrderLine.getUnit())) {
        qty =
            unitConversionService.convert(
                purchaseOrderLine.getUnit(),
                unit,
                qty,
                qty.scale(),
                purchaseOrderLine.getProduct());
      }
      MrpLine mrpLine =
          this.createMrpLine(
              mrp,
              purchaseOrderLine.getProduct(),
              purchaseOrderMrpLineType,
              qty,
              maturityDate,
              BigDecimal.ZERO,
              purchaseOrder.getStockLocation(),
              purchaseOrderLine);
      if (mrpLine != null) {
        mrpLine.setSupplierPartner(purchaseOrder.getSupplierPartner());
        mrpLineRepository.save(mrpLine);
      }
    }
  }

  protected void createSaleOrderMrpLines() throws AxelorException {
    List<MrpLineType> saleOrderMrpLineTypeList =
        mrpLineTypeService.getMrpLineTypeList(
            MrpLineTypeRepository.ELEMENT_SALE_ORDER, mrp.getMrpTypeSelect());
    if (saleOrderMrpLineTypeList == null || saleOrderMrpLineTypeList.isEmpty()) {
      return;
    }

    mrp = mrpRepository.find(mrp.getId());

    List<SaleOrderLine> saleOrderLineList = new ArrayList<>();

    // If the MRP's list of sale order lines is empty, treat the lines concerned by the mrpLineTypes
    if (mrp.getSaleOrderLineSet().isEmpty()) {

      // For each MrpLineType found, fetch saleOrderLines and generate mrpLines
      for (MrpLineType saleOrderMrpLineType : saleOrderMrpLineTypeList) {
        saleOrderLineList = new ArrayList<>();

        List<Integer> statusList =
            StringTool.getIntegerList(saleOrderMrpLineType.getStatusSelect());

        String filter =
            "self.product.id in (?1) AND self.saleOrder.stockLocation in (?2) AND self.deliveryState != ?3 "
                + "AND self.saleOrder.statusSelect IN (?4) ";

        // Checking the one off sales parameter
        if (saleOrderMrpLineType.getIncludeOneOffSalesSelect()
            == MrpLineTypeRepository.ONE_OFF_SALES_EXCLUDED) {
          filter += "AND (self.saleOrder.oneoffSale IS NULL OR self.saleOrder.oneoffSale IS FALSE)";
        } else if (saleOrderMrpLineType.getIncludeOneOffSalesSelect()
            == MrpLineTypeRepository.ONE_OFF_SALES_ONLY) {
          filter += "AND self.saleOrder.oneoffSale IS TRUE";
        }

        saleOrderLineList.addAll(
            saleOrderLineRepository
                .all()
                .filter(
                    filter,
                    this.productMap.keySet(),
                    this.stockLocationList,
                    SaleOrderLineRepository.DELIVERY_STATE_DELIVERED,
                    statusList)
                .fetch());

        for (SaleOrderLine saleOrderLine : saleOrderLineList) {

          if (saleOrderLine.getSaleOrder() != null
              && mrpSaleOrderCheckLateSaleService.checkLateSalesParameter(
                  saleOrderLine, saleOrderMrpLineType)) {
            this.createSaleOrderMrpLines(
                mrpRepository.find(mrp.getId()),
                saleOrderLineRepository.find(saleOrderLine.getId()),
                mrpLineTypeRepository.find(saleOrderMrpLineType.getId()),
                statusList);
            JPA.clear();
          }
        }
      }
      // If the MRP's list of saleOrderLines is not empty, treat all the selected lines instead
    } else {
      saleOrderLineList.addAll(mrp.getSaleOrderLineSet());

      for (MrpLineType saleOrderMrpLineType : saleOrderMrpLineTypeList) {
        List<Integer> statusList =
            StringTool.getIntegerList(saleOrderMrpLineType.getStatusSelect());

        for (SaleOrderLine saleOrderLine : saleOrderLineList) {
          if (saleOrderLine.getSaleOrder() != null) {
            saleOrderLine = saleOrderLineRepository.find(saleOrderLine.getId());
            if (this.productMap.keySet().contains(saleOrderLine.getProduct().getId())
                && this.stockLocationList.contains(saleOrderLine.getSaleOrder().getStockLocation())
                && saleOrderLine.getDeliveryState()
                    != SaleOrderLineRepository.DELIVERY_STATE_DELIVERED
                && statusList.contains(saleOrderLine.getSaleOrder().getStatusSelect())) {

              // Checking the one off sales parameter
              if (saleOrderMrpLineType.getIncludeOneOffSalesSelect()
                      == MrpLineTypeRepository.ONE_OFF_SALES_INCLUDED
                  || (saleOrderMrpLineType.getIncludeOneOffSalesSelect()
                          == MrpLineTypeRepository.ONE_OFF_SALES_EXCLUDED
                      && (!Boolean.TRUE.equals(saleOrderLine.getSaleOrder().getOneoffSale())))
                  || (saleOrderMrpLineType.getIncludeOneOffSalesSelect()
                          == MrpLineTypeRepository.ONE_OFF_SALES_ONLY
                      && Boolean.TRUE.equals(saleOrderLine.getSaleOrder().getOneoffSale()))) {

                // Checking the late sales parameter
                if (mrpSaleOrderCheckLateSaleService.checkLateSalesParameter(
                    saleOrderLine, saleOrderMrpLineType)) {
                  this.createSaleOrderMrpLines(
                      mrpRepository.find(mrp.getId()),
                      saleOrderLine,
                      mrpLineTypeRepository.find(saleOrderMrpLineType.getId()),
                      statusList);
                  JPA.clear();
                }
              }
            }
          }
        }
      }
    }
  }

  @Transactional(rollbackOn = {Exception.class})
  protected void createSaleOrderMrpLines(
      Mrp mrp,
      SaleOrderLine saleOrderLine,
      MrpLineType saleOrderMrpLineType,
      List<Integer> statusList)
      throws AxelorException {

    SaleOrder saleOrder = saleOrderLine.getSaleOrder();
    if (!this.stockLocationList.contains(saleOrder.getStockLocation())) {
      return;
    }
    if (!statusList.contains(saleOrder.getStatusSelect())) {
      return;
    }
    if (saleOrderLine.getDeliveryState() == SaleOrderLineRepository.DELIVERY_STATE_DELIVERED) {
      return;
    }

    LocalDate maturityDate = saleOrderLine.getEstimatedShippingDate();

    if (maturityDate == null) {
      maturityDate = saleOrder.getEstimatedShippingDate();
    }
    if (maturityDate == null) {
      maturityDate = saleOrderLine.getDesiredDeliveryDate();
    }
    maturityDate = this.computeMaturityDate(maturityDate, saleOrderMrpLineType);

    if (this.isBeforeEndDate(maturityDate)) {
      Unit unit = saleOrderLine.getProduct().getUnit();
      BigDecimal qty = saleOrderLine.getQty().subtract(saleOrderLine.getDeliveredQty());
      if (!unit.equals(saleOrderLine.getUnit())) {
        qty =
            unitConversionService.convert(
                saleOrderLine.getUnit(),
                unit,
                qty,
                saleOrderLine.getQty().scale(),
                saleOrderLine.getProduct());
      }

      MrpLine mrpLine =
          this.createMrpLine(
              mrp,
              saleOrderLine.getProduct(),
              saleOrderMrpLineType,
              qty,
              maturityDate,
              BigDecimal.ZERO,
              saleOrder.getStockLocation(),
              saleOrderLine);
      if (mrpLine != null) {
        mrpLineRepository.save(mrpLine);
      }
    }
  }

  protected void createSaleForecastMrpLines() throws AxelorException {

    MrpLineType saleForecastMrpLineType =
        mrpLineTypeService.getMrpLineType(
            MrpLineTypeRepository.ELEMENT_SALE_FORECAST, mrp.getMrpTypeSelect());

    if (saleForecastMrpLineType == null) {
      return;
    }

    List<MrpForecast> mrpForecastList = new ArrayList<>();

    mrp = mrpRepository.find(mrp.getId());

    if (mrp.getMrpForecastSet().isEmpty()) {

      mrpForecastList.addAll(
          mrpForecastRepository
              .all()
              .filter(
                  "self.product.id in (?1) AND self.stockLocation in (?2) AND self.forecastDate >= ?3 AND self.statusSelect = ?4",
                  this.productMap.keySet(),
                  this.stockLocationList,
                  today,
                  MrpForecastRepository.STATUS_CONFIRMED)
              .fetch());

    } else {
      mrpForecastList.addAll(mrp.getMrpForecastSet());
    }

    for (MrpForecast mrpForecast : mrpForecastList) {

      this.createSaleForecastMrpLines(
          mrpRepository.find(mrp.getId()),
          mrpForecastRepository.find(mrpForecast.getId()),
          mrpLineTypeRepository.find(saleForecastMrpLineType.getId()));
      JPA.clear();
    }
  }

  protected void createStockHistoryMrpLines() throws AxelorException {
    MrpLineType stockHistoryMrpLineType =
        mrpLineTypeService.getMrpLineType(
            MrpLineTypeRepository.ELEMENT_STOCK_HISTORY, mrp.getMrpTypeSelect());

    if (stockHistoryMrpLineType == null) {
      return;
    }

    this.mrp = mrpRepository.find(mrp.getId());

    for (Long productId : this.productMap.keySet()) {
      Product product = productRepository.find(productId);
      Mrp mrp = mrpRepository.find(this.mrp.getId());
      this.createStockHistoryWeigthedLine(
          product,
          mrp,
          mrpLineTypeRepository.find(stockHistoryMrpLineType.getId()),
          stockLocationRepository.find(mrp.getStockLocation().getId()));
      JPA.clear();
    }
  }

  @Transactional(rollbackOn = {Exception.class})
  protected void createStockHistoryWeigthedLine(
      Product product, Mrp mrp, MrpLineType mrpLineType, StockLocation stockLocation)
      throws AxelorException {

    Query<StockHistoryLine> query = buildStockHistoryLineQuery(product, mrp, mrpLineType);
    List<StockHistoryLine> stockHistoryLineList = query.fetch();
    BigDecimal growthCoef =
        productCategoryService
            .getGrowthCoeff(product.getProductCategory())
            .multiply(mrpLineType.getGrowthCoef());

    if (mrpLineType.getIsProjectedForNextMonths()) {
      if (stockHistoryLineList != null && !stockHistoryLineList.isEmpty()) {
        // List should be ordered by date
        StockHistoryLine stockHistoryLineToProject = stockHistoryLineList.get(0);
        LocalDate firstDate =
            LocalDate.parse(stockHistoryLineToProject.getLabel())
                .plusMonths(mrpLineType.getOffsetInMonths());
        MrpLine mrpLine =
            createMrpLine(
                product,
                mrp,
                mrpLineType,
                stockLocation,
                stockHistoryLineToProject,
                firstDate,
                growthCoef);
        if (mrpLine != null) {
          mrpLineRepository.save(mrpLine);
          for (int i = 0; i < mrpLineType.getNbrOfMonthProjection(); i++) {
            LocalDate datePlusMonths = firstDate.plusMonths(i + 1);
            if (mrp.getEndDate() != null && datePlusMonths.isAfter(mrp.getEndDate())) {
              break;
            }
            MrpLine projectedMrpLine =
                createMrpLine(
                    product,
                    mrp,
                    mrpLineType,
                    stockLocation,
                    stockHistoryLineToProject,
                    datePlusMonths,
                    growthCoef);
            mrpLineRepository.save(projectedMrpLine);
          }
        }
      }
    } else {
      for (StockHistoryLine stockHistoryLine : stockHistoryLineList) {
        LocalDate date =
            LocalDate.parse(stockHistoryLine.getLabel())
                .plusMonths(mrpLineType.getOffsetInMonths());
        MrpLine mrpLine =
            createMrpLine(
                product, mrp, mrpLineType, stockLocation, stockHistoryLine, date, growthCoef);
        mrpLineRepository.save(mrpLine);
      }
    }
  }

  protected MrpLine createMrpLine(
      Product product,
      Mrp mrp,
      MrpLineType mrpLineType,
      StockLocation stockLocation,
      StockHistoryLine stockHistoryLine,
      LocalDate date,
      BigDecimal growthCoef)
      throws AxelorException {
    log.debug("Creating mrp line for stock history line {}", stockHistoryLine);

    try {
      BigDecimal fieldValue = computeFieldValue(mrpLineType, stockHistoryLine, growthCoef, date);
      date = computeStockHistoryMrpLineDate(date);
      MrpLine mrpLine =
          this.createMrpLine(
              mrp, product, mrpLineType, fieldValue, date, fieldValue, stockLocation, null);
      return mrpLine;
    } catch (Exception e) {
      throw new AxelorException(e, TraceBackRepository.CATEGORY_CONFIGURATION_ERROR);
    }
  }

  protected LocalDate computeStockHistoryMrpLineDate(LocalDate date) {
    if (!date.isBefore(today)) {
      date = date.withDayOfMonth(1);
    } else {
      date = today;
    }
    return date;
  }

  protected BigDecimal computeFieldValue(
      MrpLineType mrpLineType,
      StockHistoryLine stockHistoryLine,
      BigDecimal growthCoef,
      LocalDate date)
      throws IllegalAccessException, InvocationTargetException {
    Mapper mapper = Mapper.of(StockHistoryLine.class);
    Method getter = mapper.getGetter(mrpLineType.getMetaField().getName());
    BigDecimal fieldValue = (BigDecimal) getter.invoke(stockHistoryLine);
    if (date.isBefore(today)) {
      fieldValue = computeProrata(fieldValue, today);
    }
    fieldValue = fieldValue.multiply(growthCoef);
    return fieldValue.setScale(getStockHistoryMrpLineNbDecimal(), RoundingMode.HALF_UP);
  }

  protected BigDecimal computeProrata(BigDecimal amount, LocalDate dateOfTheDay) {
    // Prorata = (amount * remainingDay) / number of days in the month

    BigDecimal result = null;
    log.debug("Computing prorata of value {} at {}", amount, dateOfTheDay);
    if (amount != null && dateOfTheDay != null) {
      int lengthOfMonth =
          YearMonth.of(dateOfTheDay.getYear(), dateOfTheDay.getMonthValue()).lengthOfMonth();
      result = amount.multiply(BigDecimal.valueOf(lengthOfMonth - dateOfTheDay.getDayOfMonth()));
      result =
          result.divide(
              BigDecimal.valueOf(lengthOfMonth),
              getStockHistoryMrpLineNbDecimal(),
              RoundingMode.HALF_UP);
    }

    return result;
  }

  protected int getStockHistoryMrpLineNbDecimal() {
    return appBaseService.getNbDecimalDigitForQty();
  }

  protected Query<StockHistoryLine> buildStockHistoryLineQuery(
      Product product, Mrp mrp, MrpLineType mrpLineType) throws AxelorException {
    Map<String, Object> bindings = new HashMap<>();
    StringBuilder querySb =
        new StringBuilder("self.product.id = :productId AND DATE(self.label) >= :startDate ");
    bindings.put("productId", product.getId());
    bindings.put("startDate", today.minusMonths(mrpLineType.getOffsetInMonths()).withDayOfMonth(1));
    String fieldName = mrpLineType.getMetaField().getName();

    // Field name can not be null
    if (fieldName == null) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
          SupplychainExceptionMessage.MRP_STOCK_HISTORY_FIELD_SELECT_MISSING,
          mrpLineType.getName());
    }

    // We will filter lines that don't have minimun value
    querySb.append("AND self.").append(fieldName).append(" > :minValue");
    bindings.put("minValue", BigDecimal.ZERO);

    if (mrp.getEndDate() != null) {
      querySb.append(" AND DATE(self.label) <= :endDate");
      bindings.put("endDate", mrp.getEndDate().minusMonths(mrpLineType.getOffsetInMonths()));
    }

    Query<StockHistoryLine> query =
        stockHistoryLineRepository.all().filter(querySb.toString()).bind(bindings).order("label");
    return query;
  }

  @Transactional(rollbackOn = {Exception.class})
  protected void createSaleForecastMrpLines(
      Mrp mrp, MrpForecast mrpForecast, MrpLineType saleForecastMrpLineType)
      throws AxelorException {

    LocalDate maturityDate = mrpForecast.getForecastDate();

    if (maturityDate != null
        && !maturityDate.isBefore(today)
        && this.isBeforeEndDate(maturityDate)) {
      Unit unit = mrpForecast.getProduct().getUnit();
      BigDecimal qty = mrpForecast.getQty();
      if (!unit.equals(mrpForecast.getUnit())) {
        qty =
            unitConversionService.convert(
                mrpForecast.getUnit(), unit, qty, qty.scale(), mrpForecast.getProduct());
      }
      MrpLine mrpLine =
          this.createMrpLine(
              mrp,
              mrpForecast.getProduct(),
              saleForecastMrpLineType,
              qty,
              maturityDate,
              BigDecimal.ZERO,
              mrpForecast.getStockLocation(),
              mrpForecast);
      if (mrpLine != null) {
        mrpLineRepository.save(mrpLine);
      }
    }
  }

  protected LocalDate computeMaturityDate(LocalDate maturityDate, MrpLineType mrpLineType) {
    if ((maturityDate != null && maturityDate.isBefore(today))
        || (maturityDate == null && mrpLineType.getIncludeElementWithoutDate())) {
      maturityDate = today;
    }
    return maturityDate;
  }

  public boolean isBeforeEndDate(LocalDate maturityDate) {
    return maturityDate != null
        && (mrp.getEndDate() == null || !maturityDate.isAfter(mrp.getEndDate()));
  }

  protected void createAvailableStockMrpLines() throws AxelorException {

    MrpLineType availableStockMrpLineType =
        mrpLineTypeService.getMrpLineType(
            MrpLineTypeRepository.ELEMENT_AVAILABLE_STOCK, mrp.getMrpTypeSelect());

    if (availableStockMrpLineType == null) {
      return;
    }

    for (Long productId : this.productMap.keySet()) {
      Mrp mrp = mrpRepository.find(this.mrp.getId());
      if (mrp.getComputeWithSubStockLocation()) {
        for (StockLocation stockLocation : this.stockLocationList) {
          this.createAvailableStockMrpLine(
              mrp,
              productRepository.find(productId),
              stockLocationRepository.find(stockLocation.getId()),
              mrpLineTypeRepository.find(availableStockMrpLineType.getId()));
        }
      } else {
        Product product = productRepository.find(productId);
        StockLocation stockLocation = mrp.getStockLocation();
        BigDecimal qty = computeTotalQuantityFromSubStockLocations(product);
        this.createAvailableStockMrpLine(
            mrp,
            product,
            qty,
            stockLocationRepository.find(stockLocation.getId()),
            mrpLineTypeRepository.find(availableStockMrpLineType.getId()));
      }
      JPA.clear();
    }
  }

  protected BigDecimal computeTotalQuantityFromSubStockLocations(Product product) {
    return Optional.ofNullable(
            JPA.em()
                .createQuery(
                    "SELECT SUM(self.currentQty) "
                        + "FROM StockLocationLine self "
                        + "WHERE self.stockLocation in (:stockLocationLineList) AND self.product = :product",
                    BigDecimal.class)
                .setParameter("stockLocationLineList", this.stockLocationList)
                .setParameter("product", product)
                .getSingleResult())
        .orElse(BigDecimal.ZERO);
  }

  @Transactional(rollbackOn = {Exception.class})
  protected MrpLine createAvailableStockMrpLine(
      Mrp mrp, Product product, StockLocation stockLocation, MrpLineType availableStockMrpLineType)
      throws AxelorException {

    BigDecimal qty = BigDecimal.ZERO;

    StockLocationLine stockLocationLine = this.getStockLocationLine(product, stockLocation);

    if (stockLocationLine != null) {

      qty = stockLocationLine.getCurrentQty();
    }

    return createAvailableStockMrpLine(mrp, product, qty, stockLocation, availableStockMrpLineType);
  }

  @Transactional(rollbackOn = {Exception.class})
  protected MrpLine createAvailableStockMrpLine(
      Mrp mrp,
      Product product,
      BigDecimal qty,
      StockLocation stockLocation,
      MrpLineType availableStockMrpLineType)
      throws AxelorException {

    return mrpLineRepository.save(
        this.createMrpLine(
            mrp, product, availableStockMrpLineType, qty, today, qty, stockLocation, null));
  }

  protected StockLocationLine getStockLocationLine(Product product, StockLocation stockLocation) {

    return stockLocationLineRepository
        .all()
        .filter(
            "self.stockLocation.id = ?1 AND self.product.id = ?2",
            stockLocation.getId(),
            product.getId())
        .fetchOne();
  }

  protected Set<Product> getProductList() throws AxelorException {

    Set<Product> productSet = Sets.newHashSet();

    if (mrp.getProductSet() != null && !mrp.getProductSet().isEmpty()) {

      productSet.addAll(mrp.getProductSet());
    }

    if (mrp.getProductCategorySet() != null && !mrp.getProductCategorySet().isEmpty()) {

      Set<ProductCategory> productCategorySet = new HashSet<>(mrp.getProductCategorySet());

      if (mrp.getTakeInAccountSubCategories()) {
        for (ProductCategory productCategory : productCategorySet) {
          productCategorySet.addAll(
              productCategoryService.fetchChildrenCategoryList(productCategory));
        }
      }

      productSet.addAll(
          productRepository
              .all()
              .filter(
                  "self.productCategory in (?1) "
                      + "AND self.productTypeSelect = ?2 "
                      + "AND self.excludeFromMrp = false "
                      + "AND self.stockManaged = true "
                      + "AND (?3 is true OR self.productSubTypeSelect = ?4) "
                      + "AND self.dtype = 'Product'",
                  productCategorySet,
                  ProductRepository.PRODUCT_TYPE_STORABLE,
                  mrp.getMrpTypeSelect() == MrpRepository.MRP_TYPE_MRP,
                  ProductRepository.PRODUCT_SUB_TYPE_FINISHED_PRODUCT)
              .fetch());
    }

    if (mrp.getProductFamilySet() != null && !mrp.getProductFamilySet().isEmpty()) {

      productSet.addAll(
          productRepository
              .all()
              .filter(
                  "self.productFamily in (?1) "
                      + "AND self.productTypeSelect = ?2 AND "
                      + "self.excludeFromMrp = false "
                      + "AND self.stockManaged = true "
                      + "AND (?3 is true OR self.productSubTypeSelect = ?4) "
                      + "AND self.dtype = 'Product'",
                  mrp.getProductFamilySet(),
                  ProductRepository.PRODUCT_TYPE_STORABLE,
                  mrp.getMrpTypeSelect() == MrpRepository.MRP_TYPE_MRP,
                  ProductRepository.PRODUCT_SUB_TYPE_FINISHED_PRODUCT)
              .fetch());
    }
    if (mrp.getSaleOrderLineSet() != null) {
      for (SaleOrderLine saleOrderLine : mrp.getSaleOrderLineSet()) {
        productSet.add(saleOrderLine.getProduct());
      }
    }

    if (mrp.getMrpForecastSet() != null) {
      for (MrpForecast mrpForecast : mrp.getMrpForecastSet()) {

        productSet.add(mrpForecast.getProduct());
      }
    }

    if (productSet.isEmpty()) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
          I18n.get(SupplychainExceptionMessage.MRP_NO_PRODUCT));
    }

    return productSet;
  }

  public boolean isMrpProduct(Product product) {
    return product != null
        && !product.getExcludeFromMrp()
        && product.getProductTypeSelect().equals(ProductRepository.PRODUCT_TYPE_STORABLE);
  }

  protected void assignProductAndLevel(Set<Product> productList) throws AxelorException {

    productMap = Maps.newHashMap();

    for (Product product : productList) {

      this.assignProductAndLevel(product);
    }
  }

  protected void assignProductAndLevel(Product product) throws AxelorException {

    log.debug("Add of the product : {}", product.getFullName());
    this.productMap.put(product.getId(), 0);
  }

  protected MrpLine createMrpLine(
      Mrp mrp,
      Product product,
      MrpLineType mrpLineType,
      BigDecimal qty,
      LocalDate maturityDate,
      BigDecimal cumulativeQty,
      StockLocation stockLocation,
      Model model)
      throws AxelorException {

    if (productMap != null && product != null) {
      if (product.getUnit() == null) {
        throw new AxelorException(
            TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
            I18n.get(SupplychainExceptionMessage.MRP_NO_PRODUCT_UNIT),
            product.getFullName());
      }
      if (!this.productMap.containsKey(product.getId())) {
        throw new AxelorException(
            TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
            I18n.get(SupplychainExceptionMessage.MRP_NO_PRODUCT_ID),
            product.getCode(),
            product.getName());
      }
      return mrpLineService.createMrpLine(
          mrp,
          product,
          this.productMap.get(product.getId()),
          mrpLineType,
          qty,
          maturityDate,
          cumulativeQty,
          mrp.getComputeWithSubStockLocation() ? stockLocation : mrp.getStockLocation(),
          model);
    }
    return null;
  }

  protected void copyMrpLineOrigins(MrpLine mrpLine, List<MrpLineOrigin> mrpLineOriginList) {

    if (mrpLineOriginList != null) {

      for (MrpLineOrigin mrpLineOrigin : mrpLineOriginList) {

        mrpLine.addMrpLineOriginListItem(mrpLineService.copyMrpLineOrigin(mrpLineOrigin));
      }
    }
  }

  @Override
  public void generateSelectedProposals(Mrp mrp, boolean isProposalPerSupplier)
      throws AxelorException {

    Map<Pair<Partner, LocalDate>, PurchaseOrder> purchaseOrders = new HashMap<>();
    Map<Partner, PurchaseOrder> purchaseOrdersPerSupplier = new HashMap<>();
    List<MrpLine> mrpLineList;

    if (getSelectedMrpLines(mrp).count() <= 0) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_INCONSISTENCY,
          I18n.get(SupplychainExceptionMessage.MRP_GENERATE_PROPOSAL_NO_LINE_SELECTED));
    }

    while (!(mrpLineList = getSelectedMrpLines(mrp).fetch(1)).isEmpty()) {
      mrp = mrpRepository.find(mrp.getId());
      generateProposals(
          isProposalPerSupplier, purchaseOrders, purchaseOrdersPerSupplier, mrpLineList);
      JPA.clear();
    }
  }

  protected Query<MrpLine> getSelectedMrpLines(Mrp mrp) {
    return mrpLineRepository
        .all()
        .filter(
            "self.mrp.id = ?1 AND self.proposalToProcess = true AND self.proposalGenerated = false",
            mrp.getId())
        .order("maturityDate");
  }

  @Override
  public void generateAllProposals(Mrp mrp, boolean isProposalsPerSupplier) throws AxelorException {
    Map<Pair<Partner, LocalDate>, PurchaseOrder> purchaseOrders = new HashMap<>();
    Map<Partner, PurchaseOrder> purchaseOrdersPerSupplier = new HashMap<>();
    List<MrpLine> mrpLineList;

    if (getAllMrpLines(mrp).count() <= 0) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_INCONSISTENCY,
          I18n.get(SupplychainExceptionMessage.MRP_GENERATE_PROPOSAL_NO_POSSIBLE_LINE));
    }

    while (!(mrpLineList = getAllMrpLines(mrp).fetch(1)).isEmpty()) {
      mrp = mrpRepository.find(mrp.getId());
      generateProposals(
          isProposalsPerSupplier, purchaseOrders, purchaseOrdersPerSupplier, mrpLineList);
      JPA.clear();
    }
  }

  protected Query<MrpLine> getAllMrpLines(Mrp mrp) {
    return mrpLineRepository
        .all()
        .filter(
            "self.mrp.id = :mrpId AND self.proposalGenerated = false AND self.mrpLineType.elementSelect in (:purchaseProposal, :manufProposal)")
        .bind("mrpId", mrp.getId())
        .bind("purchaseProposal", MrpLineTypeRepository.ELEMENT_PURCHASE_PROPOSAL)
        .bind("manufProposal", MrpLineTypeRepository.ELEMENT_MANUFACTURING_PROPOSAL)
        .order("maturityDate");
  }

  @Transactional(rollbackOn = {Exception.class})
  protected void generateProposals(
      boolean isProposalPerSupplier,
      Map<Pair<Partner, LocalDate>, PurchaseOrder> purchaseOrders,
      Map<Partner, PurchaseOrder> purchaseOrdersPerSupplier,
      List<MrpLine> mrpLineList)
      throws AxelorException {
    for (MrpLine mrpLine : mrpLineList) {
      if (!mrpLine.getProposalGenerated()) {
        generateProposal(isProposalPerSupplier, purchaseOrders, purchaseOrdersPerSupplier, mrpLine);
      }
    }
  }

  protected void generateProposal(
      boolean isProposalPerSupplier,
      Map<Pair<Partner, LocalDate>, PurchaseOrder> purchaseOrders,
      Map<Partner, PurchaseOrder> purchaseOrdersPerSupplier,
      MrpLine mrpLine)
      throws AxelorException {
    mrpLineService.generateProposal(
        mrpLine, purchaseOrders, purchaseOrdersPerSupplier, isProposalPerSupplier);
    mrpLine.setProposalToProcess(false);
    mrpLineRepository.save(mrpLine);
  }

  @Override
  public LocalDate findMrpEndDate(Mrp mrp) {
    if (mrp.getEndDate() != null) {
      return mrp.getEndDate();
    }

    MrpLine mrpLine =
        mrpLineRepository
            .all()
            .filter("self.mrp.id = ?1", mrp.getId())
            .order("-maturityDate")
            .fetchOne();

    if (mrpLine != null && mrpLine.getMaturityDate() != null) {
      return mrpLine.getMaturityDate();
    }

    return today;
  }

  @Override
  public Mrp createProjectedStock(
      Mrp mrp, Product product, Company company, StockLocation stockLocation)
      throws AxelorException {
    this.completeProjectedStock(mrp, product, company, stockLocation);
    this.computeCumulativeQty(product);
    return mrp;
  }

  protected Mrp completeProjectedStock(
      Mrp mrp, Product product, Company company, StockLocation stockLocation)
      throws AxelorException {
    this.mrp = mrp;
    mrp.addProductSetItem(product);
    if (stockLocation != null) {
      this.stockLocationList =
          stockLocationService.getAllLocationAndSubLocation(mrp.getStockLocation(), false);
    } else if (company != null) {
      this.stockLocationList =
          stockLocationRepository
              .all()
              .filter(
                  "self.company.id = ?1 AND self.typeSelect != ?2",
                  company.getId(),
                  StockLocationRepository.TYPE_VIRTUAL)
              .fetch();
    } else {
      this.stockLocationList =
          stockLocationRepository
              .all()
              .filter("self.typeSelect != ?1", StockLocationRepository.TYPE_VIRTUAL)
              .fetch();
    }
    reset(mrpRepository.find(mrp.getId()));
    this.startMrp(mrpRepository.find(mrp.getId()));
    this.assignProductAndLevel(this.getProductList());

    // Get the stock for each product on each stock location
    this.createAvailableStockMrpLines();

    this.createPurchaseMrpLines();

    this.createSaleOrderMrpLines();

    return mrp;
  }

  @Override
  @Transactional(rollbackOn = {Exception.class})
  public void undoManualChanges(Mrp mrp) {
    mrpLineRepository.all().filter("self.mrp.id = ?1", mrp.getId()).update("isEditedByUser", false);
  }

  @Override
  public Mrp call() throws AxelorException {
    final RequestScoper scope = ServletScopes.scopeRequest(Collections.emptyMap());
    try (RequestScoper.CloseableScope ignored = scope.open()) {
      this.runCalculation(mrp);
      mailMessageService.sendNotification(
          AuthUtils.getUser(),
          String.format(
              I18n.get(SupplychainExceptionMessage.MRP_FINISHED_MESSAGE_SUBJECT), mrp.getMrpSeq()),
          String.format(
              I18n.get(SupplychainExceptionMessage.MRP_FINISHED_MESSAGE_BODY), mrp.getMrpSeq()),
          mrp.getId(),
          mrp.getClass());
    } catch (Exception e) {
      onRunnerException(e);
      throw e;
    }
    return mrp;
  }

  @Transactional
  protected void onRunnerException(Exception e) {
    TraceBackService.trace(e);
    mailMessageService.sendNotification(
        AuthUtils.getUser(),
        String.format(
            I18n.get(SupplychainExceptionMessage.MRP_ERROR_WHILE_COMPUTATION), mrp.getMrpSeq()),
        e.getMessage(),
        mrp.getId(),
        mrp.getClass());
    this.reset(mrpRepository.find(mrp.getId()));
    this.saveErrorInMrp(mrpRepository.find(mrp.getId()), e);
  }

  @Override
  @Transactional
  public void saveErrorInMrp(Mrp mrp, Exception e) {
    mrp.setErrorLog(e.getMessage());
  }

  @Override
  @Transactional(rollbackOn = {Exception.class})
  public void massUpdateProposalToProcess(Mrp mrp, boolean proposalToProcess) {
    Query<MrpLine> mrpLineQuery =
        mrpLineRepository
            .all()
            .filter(
                "self.mrp.id = :mrpId AND self.mrpLineType.elementSelect in (:purchaseProposal, :manufProposal)")
            .bind("mrpId", mrp.getId())
            .bind("purchaseProposal", MrpLineTypeRepository.ELEMENT_PURCHASE_PROPOSAL)
            .bind("manufProposal", MrpLineTypeRepository.ELEMENT_MANUFACTURING_PROPOSAL)
            .order("id");

    int offset = 0;
    List<MrpLine> mrpLineList;

    while (!(mrpLineList = mrpLineQuery.fetch(AbstractBatch.FETCH_LIMIT, offset)).isEmpty()) {
      for (MrpLine mrpLine : mrpLineList) {
        offset++;

        mrpLineService.updateProposalToProcess(mrpLine, true);
      }

      JPA.clear();
    }
  }
}
