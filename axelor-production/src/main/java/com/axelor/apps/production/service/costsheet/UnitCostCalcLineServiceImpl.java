/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2020 Axelor (<http://axelor.com>).
 *
 * This program is free software: you can redistribute it and/or  modify
 * it under the terms of the GNU Affero General Public License, version 3,
 * as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.axelor.apps.production.service.costsheet;

import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.Product;
import com.axelor.apps.base.db.repo.ProductRepository;
import com.axelor.apps.base.service.ProductCompanyService;
import com.axelor.apps.production.db.CostSheet;
import com.axelor.apps.production.db.UnitCostCalcLine;
import com.axelor.apps.production.db.UnitCostCalculation;
import com.axelor.apps.production.db.repo.UnitCostCalcLineRepository;
import com.google.inject.Inject;
import java.lang.invoke.MethodHandles;
import java.math.BigDecimal;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UnitCostCalcLineServiceImpl implements UnitCostCalcLineService {

  private final Logger log = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  protected ProductRepository productRepository;
  protected UnitCostCalcLineRepository unitCostCalcLineRepository;
  protected ProductCompanyService productCompanyService;

  @Inject
  public UnitCostCalcLineServiceImpl(
		  UnitCostCalcLineRepository unitCostCalcLineRepository,
		  ProductCompanyService productCompanyService) {
    this.unitCostCalcLineRepository = unitCostCalcLineRepository;
    this.productCompanyService = productCompanyService;
  }

  public UnitCostCalcLine createUnitCostCalcLine(
      Product product, Company company, int maxLevel, CostSheet costSheet) {

    UnitCostCalcLine unitCostCalcLine = new UnitCostCalcLine();
    unitCostCalcLine.setProduct(product);
    unitCostCalcLine.setCompany(company);
    unitCostCalcLine.setPreviousCost((BigDecimal) productCompanyService.get(product, "costPrice", company));
    unitCostCalcLine.setCostSheet(costSheet);
    unitCostCalcLine.setComputedCost(costSheet.getCostPrice());
    unitCostCalcLine.setCostToApply(costSheet.getCostPrice());
    unitCostCalcLine.setMaxLevel(maxLevel);

    return unitCostCalcLine;
  }

  public UnitCostCalcLine getUnitCostCalcLine(
      UnitCostCalculation unitCostCalculation, Product product) {

    return unitCostCalcLineRepository
        .all()
        .filter("self.unitCostCalculation = ?1 AND self.product = ?2", unitCostCalculation, product)
        .fetchOne();
  }
}
