/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2021 Axelor (<http://axelor.com>).
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

import com.axelor.apps.account.db.AnalyticAccount;
import com.axelor.apps.account.db.AnalyticAxis;
import com.axelor.apps.account.db.AnalyticDistributionLine;
import com.axelor.apps.account.db.AnalyticDistributionTemplate;
import com.axelor.apps.account.db.AnalyticJournal;
import com.axelor.apps.account.db.AnalyticMoveLine;
import com.axelor.apps.account.db.MoveLine;
import com.axelor.apps.account.db.repo.AccountConfigRepository;
import com.axelor.apps.account.db.repo.AccountRepository;
import com.axelor.apps.account.db.repo.AnalyticMoveLineRepository;
import com.axelor.apps.account.service.app.AppAccountService;
import com.axelor.apps.account.service.config.AccountConfigService;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.Partner;
import com.axelor.apps.base.db.Product;
import com.axelor.exception.AxelorException;
import com.google.inject.Inject;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AnalyticMoveLineServiceImpl implements AnalyticMoveLineService {

  protected AppAccountService appAccountService;
  protected AccountManagementServiceAccountImpl accountManagementServiceAccountImpl;
  protected AccountConfigService accountConfigService;
  protected AccountRepository accountRepository;

  @Inject
  public AnalyticMoveLineServiceImpl(
      AppAccountService appAccountService,
      AccountManagementServiceAccountImpl accountManagementServiceAccountImpl,
      AccountConfigService accountConfigService,
      AccountRepository accountRepository) {

    this.appAccountService = appAccountService;
    this.accountManagementServiceAccountImpl = accountManagementServiceAccountImpl;
    this.accountConfigService = accountConfigService;
    this.accountRepository = accountRepository;
  }

  @Override
  public BigDecimal computeAmount(AnalyticMoveLine analyticMoveLine) {

    return analyticMoveLine
        .getPercentage()
        .multiply(analyticMoveLine.getOriginalPieceAmount())
        .divide(new BigDecimal(100), 2, RoundingMode.HALF_UP);
  }

  @Override
  public List<AnalyticMoveLine> generateLines(
      AnalyticDistributionTemplate analyticDistributionTemplate,
      BigDecimal total,
      int typeSelect,
      LocalDate date) {

    List<AnalyticMoveLine> analyticMoveLineList = new ArrayList<AnalyticMoveLine>();
    if (analyticDistributionTemplate != null) {
      for (AnalyticDistributionLine analyticDistributionLine :
          analyticDistributionTemplate.getAnalyticDistributionLineList()) {
        analyticMoveLineList.add(
            this.createAnalyticMoveLine(analyticDistributionLine, total, typeSelect, date));
      }
    }

    return analyticMoveLineList;
  }

  @Override
  public AnalyticDistributionTemplate getAnalyticDistributionTemplate(
      Partner partner, Product product, Company company) throws AxelorException {

    if (accountConfigService.getAccountConfig(company).getAnalyticDistributionTypeSelect()
            == AccountConfigRepository.DISTRIBUTION_TYPE_PARTNER
        && partner != null
        && company != null) {
      return partner.getAnalyticDistributionTemplate();
    } else if (accountConfigService.getAccountConfig(company).getAnalyticDistributionTypeSelect()
            == AccountConfigRepository.DISTRIBUTION_TYPE_PRODUCT
        && company != null) {
      return accountManagementServiceAccountImpl.getAnalyticDistributionTemplate(product, company);
    }
    return null;
  }

  public AnalyticMoveLine createAnalyticMoveLine(
      AnalyticDistributionLine analyticDistributionLine,
      BigDecimal total,
      int typeSelect,
      LocalDate date) {

    AnalyticMoveLine analyticMoveLine = new AnalyticMoveLine();
    analyticMoveLine.setOriginalPieceAmount(total);
    analyticMoveLine.setAnalyticAccount(analyticDistributionLine.getAnalyticAccount());
    analyticMoveLine.setAnalyticAxis(analyticDistributionLine.getAnalyticAxis());
    analyticMoveLine.setAnalyticJournal(analyticDistributionLine.getAnalyticJournal());

    AnalyticJournal analyticJournal = analyticDistributionLine.getAnalyticJournal();
    Company company = analyticJournal == null ? null : analyticJournal.getCompany();
    if (company != null) {
      analyticMoveLine.setCurrency(company.getCurrency());
    }
    analyticMoveLine.setDate(date);
    analyticMoveLine.setPercentage(analyticDistributionLine.getPercentage());
    analyticMoveLine.setAmount(computeAmount(analyticMoveLine));
    analyticMoveLine.setTypeSelect(typeSelect);

    return analyticMoveLine;
  }

  @Override
  public void updateAnalyticMoveLine(
      AnalyticMoveLine analyticMoveLine, BigDecimal total, LocalDate date) {

    analyticMoveLine.setOriginalPieceAmount(total);
    analyticMoveLine.setAmount(computeAmount(analyticMoveLine));
    analyticMoveLine.setDate(date);
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

  @Override
  public boolean validateAnalyticMoveLines(List<AnalyticMoveLine> analyticMoveLineList) {

    if (analyticMoveLineList != null) {
      Map<AnalyticAxis, BigDecimal> map = new HashMap<AnalyticAxis, BigDecimal>();
      for (AnalyticMoveLine analyticMoveLine : analyticMoveLineList) {
        if (map.containsKey(analyticMoveLine.getAnalyticAxis())) {
          map.put(
              analyticMoveLine.getAnalyticAxis(),
              map.get(analyticMoveLine.getAnalyticAxis()).add(analyticMoveLine.getPercentage()));
        } else {
          map.put(analyticMoveLine.getAnalyticAxis(), analyticMoveLine.getPercentage());
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

  public AnalyticMoveLine computeAnalyticMoveLine(
      MoveLine moveLine, AnalyticAccount analyticAccount) throws AxelorException {
    AnalyticMoveLine analyticMoveLine = new AnalyticMoveLine();
    if (moveLine.getMove() != null
        && accountConfigService
                .getAccountConfig(moveLine.getMove().getCompany())
                .getAnalyticJournal()
            != null) {

      analyticMoveLine.setAnalyticJournal(
          accountConfigService
              .getAccountConfig(analyticAccount.getAnalyticAxis().getCompany())
              .getAnalyticJournal());
    }

    analyticMoveLine.setDate(moveLine.getDate());
    analyticMoveLine.setPercentage(new BigDecimal(100));
    analyticMoveLine.setTypeSelect(AnalyticMoveLineRepository.STATUS_REAL_ACCOUNTING);
    if (moveLine.getAccount() != null) {
      analyticMoveLine.setAccount(moveLine.getAccount());
      if (moveLine.getAccount().getAccountType() != null) {
        analyticMoveLine.setAccountType(moveLine.getAccount().getAccountType());
      }
    } else {
      analyticMoveLine.setAccount(accountRepository.find((long) 1));
    }

    if (analyticAccount != null) {
      analyticMoveLine.setAnalyticAxis(analyticAccount.getAnalyticAxis());
      analyticMoveLine.setAnalyticAccount(analyticAccount);
    }
    if (moveLine.getCredit().signum() > 0) {
      analyticMoveLine.setAmount(moveLine.getCredit());
    } else if (moveLine.getDebit().signum() > 0) {
      analyticMoveLine.setAmount(moveLine.getDebit());
    }
    return analyticMoveLine;
  }
}
