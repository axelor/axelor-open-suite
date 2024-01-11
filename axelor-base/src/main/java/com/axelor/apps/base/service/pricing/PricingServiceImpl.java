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
package com.axelor.apps.base.service.pricing;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.Pricing;
import com.axelor.apps.base.db.PricingLine;
import com.axelor.apps.base.db.Product;
import com.axelor.apps.base.db.ProductCategory;
import com.axelor.apps.base.db.repo.PricingLineRepository;
import com.axelor.apps.base.db.repo.PricingRepository;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.apps.base.exceptions.BaseExceptionMessage;
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.auth.AuthUtils;
import com.axelor.common.ObjectUtils;
import com.axelor.i18n.I18n;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.lang.invoke.MethodHandles;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PricingServiceImpl implements PricingService {

  protected PricingRepository pricingRepo;
  protected AppBaseService appBaseService;
  protected PricingLineRepository pricingLineRepository;
  private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  @Inject
  public PricingServiceImpl(
      PricingRepository pricingRepo,
      AppBaseService appBaseService,
      PricingLineRepository pricingLineRepository) {
    this.pricingRepo = pricingRepo;
    this.appBaseService = appBaseService;
    this.pricingLineRepository = pricingLineRepository;
  }

  @Override
  public Optional<Pricing> getRandomPricing(
      Company company,
      Product product,
      ProductCategory productCategory,
      String modelName,
      Pricing previousPricing) {

    return getPricings(company, product, productCategory, modelName, previousPricing).stream()
        .findAny();
  }

  @Override
  public List<Pricing> getPricings(
      Company company,
      Product product,
      ProductCategory productCategory,
      String modelName,
      Pricing previousPricing) {

    LOG.debug("Fetching pricings");
    StringBuilder filter = new StringBuilder();
    Map<String, Object> bindings = new HashMap<>();

    filter.append("self.startDate <= :todayDate ");
    filter.append("AND (self.endDate > :todayDate OR self.endDate = NULL) ");
    bindings.put("todayDate", appBaseService.getTodayDate(company));

    if (company != null) {
      filter.append("AND self.company = :company ");
      bindings.put("company", company);
    }

    if (modelName != null) {
      filter.append("AND self.concernedModel.name = :modelName ");
      bindings.put("modelName", modelName);
    }

    if (previousPricing != null) {
      filter.append("AND self.previousPricing = :previousPricing ");
      bindings.put("previousPricing", previousPricing);
    } else {
      filter.append("AND self.previousPricing is NULL ");
    }
    filter.append("AND (self.archived = false OR self.archived is null) ");

    appendProductFilter(product, productCategory, filter, bindings);
    LOG.debug("Filtering pricing with {}", filter.toString());
    return pricingRepo.all().filter(filter.toString()).bind(bindings).fetch();
  }

  protected void appendProductFilter(
      Product product,
      ProductCategory productCategory,
      StringBuilder filter,
      Map<String, Object> bindings) {
    StringBuilder productFilter = new StringBuilder();
    productFilter.append("(");
    if (product != null) {
      productFilter.append("self.product = :product ");
      bindings.put("product", product);

      if (product.getParentProduct() != null) {
        productFilter.append("OR self.product = :parentProduct ");
        bindings.put("parentProduct", product.getParentProduct());
      }
      if (productCategory != null) {
        productFilter.append("OR self.productCategory = :productCategory ");
        bindings.put("productCategory", productCategory);
      }
    } else {
      if (productCategory != null) {
        productFilter.append("self.productCategory = :productCategory ");
        bindings.put("productCategory", productCategory);
      }
    }
    productFilter.append(")");

    // if productFilter is more than just "()"
    if (productFilter.length() > 2) {
      filter.append("AND ");
      filter.append(productFilter);
    }
  }

  @Override
  @Transactional(rollbackOn = Exception.class)
  public void historizePricing(Pricing pricing) throws AxelorException {
    historizeCurrentPricing(pricing);

    pricing.setStartDate(null);
    pricing.setEndDate(null);
    pricingRepo.save(pricing);
  }

  @Override
  public void checkDates(Pricing pricing) throws AxelorException {
    LocalDate startDate = pricing.getStartDate();
    LocalDate endDate = pricing.getEndDate();
    if (startDate == null || endDate == null) {
      return;
    }
    if (startDate.isAfter(endDate)) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
          I18n.get(BaseExceptionMessage.PRICING_INVALID_DATES));
    }
  }

  @Override
  @Transactional(rollbackOn = Exception.class)
  public Pricing recoverPricing(Pricing pricing, Boolean isHistorizeCurrentPricing)
      throws AxelorException {
    Pricing currentPricing = pricingRepo.find(pricing.getCurrentPricing().getId());
    if (ObjectUtils.notEmpty(isHistorizeCurrentPricing)) {
      historizeCurrentPricing(currentPricing);
    }

    currentPricing.setStartDate(pricing.getStartDate());
    currentPricing.setEndDate(pricing.getEndDate());
    currentPricing.setName(pricing.getName());
    currentPricing.setClass1PricingRule(pricing.getClass1PricingRule());
    currentPricing.setClass2PricingRule(pricing.getClass2PricingRule());
    currentPricing.setClass3PricingRule(pricing.getClass3PricingRule());
    currentPricing.setClass4PricingRule(pricing.getClass4PricingRule());
    currentPricing.setCompany(pricing.getCompany());
    currentPricing.setConcernedModel(pricing.getConcernedModel());
    currentPricing.setPreviousPricing(pricing.getPreviousPricing());
    currentPricing.setResult1PricingRule(pricing.getResult1PricingRule());
    currentPricing.setResult2PricingRule(pricing.getResult2PricingRule());
    currentPricing.setResult3PricingRule(pricing.getResult3PricingRule());
    currentPricing.setResult4PricingRule(pricing.getResult4PricingRule());
    currentPricing.setImportId(pricing.getImportId());
    currentPricing.setImportOrigin(pricing.getImportOrigin());
    currentPricing.setProduct(pricing.getProduct());
    currentPricing.setProductCategory(pricing.getProductCategory());
    currentPricing.setVersion(pricing.getVersion());
    currentPricing.setAttrs(pricing.getAttrs());
    currentPricing.clearPricingLineList();
    pricing
        .getPricingLineList()
        .forEach(
            pricingLine -> {
              PricingLine copy = pricingLineRepository.copy(pricingLine, false);
              currentPricing.addPricingLineListItem(copy);
            });
    return pricingRepo.save(currentPricing);
  }

  @Override
  @Transactional(rollbackOn = Exception.class)
  public void historizeCurrentPricing(Pricing pricing) throws AxelorException {
    Pricing historizedPricing = pricingRepo.copy(pricing, false);
    historizedPricing.setCurrentPricing(pricing);
    historizedPricing.setHistorizedBy(AuthUtils.getUser());

    LocalDate todayDate = appBaseService.getTodayDate(historizedPricing.getCompany());

    if (ObjectUtils.isEmpty(historizedPricing.getStartDate())) {
      historizedPricing.setStartDate(todayDate);
    }

    if (ObjectUtils.isEmpty(historizedPricing.getEndDate())) {
      historizedPricing.setEndDate(todayDate);
    }

    List<PricingLine> pricingLineList = pricing.getPricingLineList();
    for (PricingLine pricingLine : pricingLineList) {
      PricingLine newPricingLine = pricingLineRepository.copy(pricingLine, false);
      historizedPricing.addPricingLineListItem(newPricingLine);
    }

    historizedPricing.setArchived(true);
    pricingRepo.save(historizedPricing);
  }
}
