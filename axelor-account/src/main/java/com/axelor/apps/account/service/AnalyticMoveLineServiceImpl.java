/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2019 Axelor (<http://axelor.com>).
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
package com.axelor.apps.account.service;

import com.axelor.apps.account.db.AccountManagement;
import com.axelor.apps.account.db.AnalyticAxis;
import com.axelor.apps.account.db.AnalyticDistributionLine;
import com.axelor.apps.account.db.AnalyticDistributionTemplate;
import com.axelor.apps.account.db.AnalyticMoveLine;
import com.axelor.apps.account.db.InvoiceLine;
import com.axelor.apps.account.db.MoveLine;
import com.axelor.apps.account.service.app.AppAccountService;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.Partner;
import com.axelor.apps.base.db.Product;
import com.axelor.apps.base.db.repo.AppAccountRepository;
import com.axelor.apps.base.service.tax.AccountManagementService;
import com.axelor.exception.AxelorException;
import com.axelor.rpc.Context;
import com.google.inject.Inject;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AnalyticMoveLineServiceImpl implements AnalyticMoveLineService {

  protected AppAccountService appAccountService;
  protected AccountManagementService accountManagementService;

  @Inject
  public AnalyticMoveLineServiceImpl(
      AppAccountService appAccountService, AccountManagementService accountManagementService) {

    this.appAccountService = appAccountService;
    this.accountManagementService = accountManagementService;
  }

  @Override
  public BigDecimal computeAmount(AnalyticMoveLine analyticMoveLine) {
    BigDecimal amount = BigDecimal.ZERO;
    if (analyticMoveLine.getInvoiceLine() != null) {
      amount =
          analyticMoveLine
              .getPercentage()
              .multiply(analyticMoveLine.getInvoiceLine().getExTaxTotal())
              .divide(new BigDecimal(100), 2, RoundingMode.HALF_UP);
    }
    if (analyticMoveLine.getMoveLine() != null) {
      if (analyticMoveLine.getMoveLine().getCredit().compareTo(BigDecimal.ZERO) != 0) {
        amount =
            analyticMoveLine
                .getPercentage()
                .multiply(analyticMoveLine.getMoveLine().getCredit())
                .divide(new BigDecimal(100), 2, RoundingMode.HALF_UP);
      } else {
        amount =
            analyticMoveLine
                .getPercentage()
                .multiply(analyticMoveLine.getMoveLine().getDebit())
                .divide(new BigDecimal(100), 2, RoundingMode.HALF_UP);
      }
    }
    return amount;
  }

  @Override
  public BigDecimal chooseComputeWay(Context context, AnalyticMoveLine analyticMoveLine) {
    if (analyticMoveLine.getInvoiceLine() == null && analyticMoveLine.getMoveLine() == null) {
      if (context.getParent().getContextClass() == InvoiceLine.class) {
        analyticMoveLine.setInvoiceLine(context.getParent().asType(InvoiceLine.class));
      } else {
        analyticMoveLine.setMoveLine(context.getParent().asType(MoveLine.class));
      }
    }
    return this.computeAmount(analyticMoveLine);
  }

  @Override
  public List<AnalyticMoveLine> generateLines(
      Partner partner, Product product, Company company, BigDecimal total) throws AxelorException {
    List<AnalyticMoveLine> analyticDistributionLineList = new ArrayList<AnalyticMoveLine>();
    if (appAccountService.getAppAccount().getAnalyticDistributionTypeSelect()
        == AppAccountRepository.DISTRIBUTION_TYPE_PARTNER) {
      analyticDistributionLineList = this.generateLinesFromPartner(partner, total);
    } else if (appAccountService.getAppAccount().getAnalyticDistributionTypeSelect()
        == AppAccountRepository.DISTRIBUTION_TYPE_PRODUCT) {
      analyticDistributionLineList = this.generateLinesFromProduct(product, company, total);
    }
    return analyticDistributionLineList;
  }

  public AnalyticMoveLine createAnalyticMoveLine(
      AnalyticDistributionLine analyticDistributionLine, BigDecimal total) {

    AnalyticMoveLine analyticMoveLine = new AnalyticMoveLine();
    analyticMoveLine.setAmount(
        analyticDistributionLine
            .getPercentage()
            .multiply(total)
            .divide(new BigDecimal(100), 2, RoundingMode.HALF_UP));
    analyticMoveLine.setAnalyticAccount(analyticDistributionLine.getAnalyticAccount());
    analyticMoveLine.setAnalyticAxis(analyticDistributionLine.getAnalyticAxis());
    analyticMoveLine.setAnalyticJournal(analyticDistributionLine.getAnalyticJournal());
    analyticMoveLine.setDate(appAccountService.getTodayDate());
    analyticMoveLine.setPercentage(analyticDistributionLine.getPercentage());

    return analyticMoveLine;
  }

  @Override
  public List<AnalyticMoveLine> generateLinesFromPartner(Partner partner, BigDecimal total) {

    return this.generateLinesWithTemplate(partner.getAnalyticDistributionTemplate(), total);
  }

  @Override
  public List<AnalyticMoveLine> generateLinesFromProduct(
      Product product, Company company, BigDecimal total) throws AxelorException {
    AnalyticDistributionTemplate analyticDistributionTemplate = null;
    AccountManagement accountManagement =
        accountManagementService.getAccountManagement(product, company);
    if (accountManagement != null) {
      analyticDistributionTemplate = accountManagement.getAnalyticDistributionTemplate();
    }
    return this.generateLinesWithTemplate(analyticDistributionTemplate, total);
  }

  @Override
  public List<AnalyticMoveLine> generateLinesWithTemplate(
      AnalyticDistributionTemplate template, BigDecimal total) {
    List<AnalyticMoveLine> analyticMoveLineList = new ArrayList<AnalyticMoveLine>();
    if (template != null) {
      for (AnalyticDistributionLine analyticDistributionLine :
          template.getAnalyticDistributionLineList()) {
        analyticMoveLineList.add(this.createAnalyticMoveLine(analyticDistributionLine, total));
      }
    }
    return analyticMoveLineList;
  }

  @Override
  public boolean validateLines(List<AnalyticDistributionLine> analyticDistributionLineList) {
    if (analyticDistributionLineList != null) {
      Map<AnalyticAxis, BigDecimal> map = new HashMap<AnalyticAxis, BigDecimal>();
      for (AnalyticDistributionLine analyticDistributionLine : analyticDistributionLineList) {
        if (map.containsKey(analyticDistributionLine.getAnalyticAxis())) {
          map.put(
              analyticDistributionLine.getAnalyticAxis(),
              map.get(analyticDistributionLine.getAnalyticAxis())
                  .add(analyticDistributionLine.getPercentage()));
        } else {
          map.put(
              analyticDistributionLine.getAnalyticAxis(), analyticDistributionLine.getPercentage());
        }
      }
      for (AnalyticAxis analyticAxis : map.keySet()) {
        if (map.get(analyticAxis).compareTo(new BigDecimal(100)) > 0) {
          return false;
        }
      }
    }
    return true;
  }
}
