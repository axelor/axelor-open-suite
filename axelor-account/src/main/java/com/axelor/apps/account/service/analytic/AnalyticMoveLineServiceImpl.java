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
package com.axelor.apps.account.service.analytic;

import com.axelor.apps.account.db.AccountConfig;
import com.axelor.apps.account.db.AnalyticAccount;
import com.axelor.apps.account.db.AnalyticAxis;
import com.axelor.apps.account.db.AnalyticAxisByCompany;
import com.axelor.apps.account.db.AnalyticDistributionLine;
import com.axelor.apps.account.db.AnalyticDistributionTemplate;
import com.axelor.apps.account.db.AnalyticJournal;
import com.axelor.apps.account.db.AnalyticMoveLine;
import com.axelor.apps.account.db.Invoice;
import com.axelor.apps.account.db.InvoiceLine;
import com.axelor.apps.account.db.MoveLine;
import com.axelor.apps.account.db.repo.AccountConfigRepository;
import com.axelor.apps.account.db.repo.AccountRepository;
import com.axelor.apps.account.db.repo.AnalyticMoveLineRepository;
import com.axelor.apps.account.service.AccountManagementServiceAccountImpl;
import com.axelor.apps.account.service.app.AppAccountService;
import com.axelor.apps.account.service.config.AccountConfigService;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.Partner;
import com.axelor.apps.base.db.Product;
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.auth.AuthUtils;
import com.axelor.auth.db.User;
import com.axelor.utils.StringTool;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import org.apache.commons.collections.CollectionUtils;

public class AnalyticMoveLineServiceImpl implements AnalyticMoveLineService {

  protected AnalyticMoveLineRepository analyticMoveLineRepository;
  protected AppAccountService appAccountService;
  protected AccountManagementServiceAccountImpl accountManagementServiceAccountImpl;
  protected AccountConfigService accountConfigService;
  protected AccountConfigRepository accountConfigRepository;
  protected AccountRepository accountRepository;
  protected AppBaseService appBaseService;
  private final int RETURN_SCALE = 2;
  private final int CALCULATION_SCALE = 10;

  @Inject
  public AnalyticMoveLineServiceImpl(
      AnalyticMoveLineRepository analyticMoveLineRepository,
      AppAccountService appAccountService,
      AccountManagementServiceAccountImpl accountManagementServiceAccountImpl,
      AccountConfigService accountConfigService,
      AccountConfigRepository accountConfigRepository,
      AccountRepository accountRepository,
      AppBaseService appBaseService) {

    this.analyticMoveLineRepository = analyticMoveLineRepository;
    this.appAccountService = appAccountService;
    this.accountManagementServiceAccountImpl = accountManagementServiceAccountImpl;
    this.accountConfigService = accountConfigService;
    this.accountConfigRepository = accountConfigRepository;
    this.accountRepository = accountRepository;
    this.appBaseService = appBaseService;
  }

  public AnalyticMoveLineRepository getAnalyticMoveLineRepository() {
    return this.analyticMoveLineRepository;
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
      Partner partner, Product product, Company company, boolean isPurchase)
      throws AxelorException {

    if (accountConfigService.getAccountConfig(company).getAnalyticDistributionTypeSelect()
            == AccountConfigRepository.DISTRIBUTION_TYPE_PARTNER
        && partner != null
        && company != null) {
      return partner.getAnalyticDistributionTemplate();
    } else if (accountConfigService.getAccountConfig(company).getAnalyticDistributionTypeSelect()
            == AccountConfigRepository.DISTRIBUTION_TYPE_PRODUCT
        && company != null) {
      return accountManagementServiceAccountImpl.getAnalyticDistributionTemplate(
          product, company, isPurchase);
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
        if (map.get(analyticAxis).compareTo(new BigDecimal(100)) != 0) {
          return false;
        }
      }
    }
    return true;
  }

  @Override
  public boolean validateAnalyticMoveLines(List<AnalyticMoveLine> analyticMoveLineList) {
    return CollectionUtils.isEmpty(analyticMoveLineList)
        || analyticMoveLineList.stream()
            .collect(
                Collectors.groupingBy(
                    AnalyticMoveLine::getAnalyticAxis,
                    Collectors.reducing(
                        BigDecimal.ZERO, AnalyticMoveLine::getPercentage, BigDecimal::add)))
            .values()
            .stream()
            .allMatch(percentage -> percentage.compareTo(BigDecimal.valueOf(100)) == 0);
  }

  @Override
  public AnalyticMoveLine computeAnalyticMoveLine(
      MoveLine moveLine, Company company, AnalyticAccount analyticAccount) throws AxelorException {
    AnalyticMoveLine analyticMoveLine = computeAnalytic(company, analyticAccount);

    analyticMoveLine.setDate(moveLine.getDate());
    if (moveLine.getAccount() != null) {
      analyticMoveLine.setAccount(moveLine.getAccount());
      analyticMoveLine.setAccountType(moveLine.getAccount().getAccountType());
    }
    if (moveLine.getCredit().signum() > 0) {
      analyticMoveLine.setAmount(moveLine.getCredit());
    } else if (moveLine.getDebit().signum() > 0) {
      analyticMoveLine.setAmount(moveLine.getDebit());
    }
    return analyticMoveLine;
  }

  @Override
  public AnalyticMoveLine computeAnalyticMoveLine(
      InvoiceLine invoiceLine, Invoice invoice, Company company, AnalyticAccount analyticAccount)
      throws AxelorException {
    AnalyticMoveLine analyticMoveLine = computeAnalytic(company, analyticAccount);

    analyticMoveLine.setDate(this.getAnalyticMoveLineDate(invoice));
    if (invoiceLine.getAccount() != null) {
      analyticMoveLine.setAccount(invoiceLine.getAccount());
      analyticMoveLine.setAccountType(invoiceLine.getAccount().getAccountType());
    }
    analyticMoveLine.setAmount(invoiceLine.getCompanyExTaxTotal());
    return analyticMoveLine;
  }

  protected LocalDate getAnalyticMoveLineDate(Invoice invoice) {
    if (invoice.getOriginDate() != null) {
      return invoice.getOriginDate();
    } else if (invoice.getDueDate() != null) {
      return invoice.getDueDate();
    } else if (invoice.getInvoiceDate() != null) {
      return invoice.getInvoiceDate();
    } else {
      Company company = invoice.getCompany();

      if (company == null) {
        company = Optional.ofNullable(AuthUtils.getUser()).map(User::getActiveCompany).orElse(null);
      }

      return appBaseService.getTodayDate(company);
    }
  }

  public AnalyticMoveLine computeAnalytic(Company company, AnalyticAccount analyticAccount)
      throws AxelorException {
    AnalyticMoveLine analyticMoveLine = new AnalyticMoveLine();

    if (company != null) {
      AnalyticJournal analyticJournal =
          accountConfigService.getAccountConfig(company).getAnalyticJournal();
      if (analyticJournal != null) {
        analyticMoveLine.setAnalyticJournal(analyticJournal);
      }
    }
    analyticMoveLine.setPercentage(new BigDecimal(100));
    analyticMoveLine.setTypeSelect(AnalyticMoveLineRepository.STATUS_REAL_ACCOUNTING);

    if (analyticAccount != null) {
      analyticMoveLine.setAnalyticAxis(analyticAccount.getAnalyticAxis());
      analyticMoveLine.setAnalyticAccount(analyticAccount);
    }
    return analyticMoveLine;
  }

  protected BigDecimal getAnalyticAmount(MoveLine moveLine, AnalyticMoveLine analyticMoveLine) {
    if (moveLine.getCredit().compareTo(BigDecimal.ZERO) > 0) {
      return analyticMoveLine
          .getPercentage()
          .multiply(moveLine.getCredit())
          .divide(new BigDecimal(100), RETURN_SCALE, RoundingMode.HALF_UP);
    } else if (moveLine.getDebit().compareTo(BigDecimal.ZERO) > 0) {
      return analyticMoveLine
          .getPercentage()
          .multiply(moveLine.getDebit())
          .divide(new BigDecimal(100), RETURN_SCALE, RoundingMode.HALF_UP);
    }
    return BigDecimal.ZERO;
  }

  protected BigDecimal getAnalyticAmount(
      InvoiceLine invoiceLine, AnalyticMoveLine analyticMoveLine) {
    if (invoiceLine.getCompanyExTaxTotal().compareTo(BigDecimal.ZERO) > 0) {
      return analyticMoveLine
          .getPercentage()
          .multiply(invoiceLine.getCompanyExTaxTotal())
          .divide(new BigDecimal(100), RETURN_SCALE, RoundingMode.HALF_UP);
    }
    return BigDecimal.ZERO;
  }

  @Override
  public AnalyticMoveLine reverse(
      AnalyticMoveLine analyticMoveLine, AnalyticAccount analyticAccount) {

    MoveLine moveLine = analyticMoveLine.getMoveLine();
    AnalyticMoveLine reverse = analyticMoveLineRepository.copy(analyticMoveLine, false);
    reverse.setOriginAnalyticMoveLine(analyticMoveLine);
    moveLine.addAnalyticMoveLineListItem(reverse);
    reverse.setAmount(analyticMoveLine.getAmount().negate());
    reverse.setSubTypeSelect(AnalyticMoveLineRepository.SUB_TYPE_REVERSE);

    return reverse;
  }

  @Override
  @Transactional
  public AnalyticMoveLine reverseAndPersist(
      AnalyticMoveLine analyticMoveLine, AnalyticAccount analyticAccount) {
    return analyticMoveLineRepository.save(reverse(analyticMoveLine, analyticAccount));
  }

  @Override
  @Transactional
  public AnalyticMoveLine generateAnalyticMoveLine(
      AnalyticMoveLine analyticMoveLine, AnalyticAccount analyticAccount) {

    AnalyticMoveLine newAnalyticmoveLine = analyticMoveLineRepository.copy(analyticMoveLine, false);

    MoveLine moveLine = analyticMoveLine.getMoveLine();
    newAnalyticmoveLine.setOriginAnalyticMoveLine(analyticMoveLine);
    moveLine.addAnalyticMoveLineListItem(newAnalyticmoveLine);
    newAnalyticmoveLine.setSubTypeSelect(AnalyticMoveLineRepository.SUB_TYPE_REVISION);
    if (Objects.nonNull(analyticAccount)) {
      newAnalyticmoveLine.setAnalyticAccount(analyticAccount);
    }

    return analyticMoveLineRepository.save(newAnalyticmoveLine);
  }

  @Override
  public String getAnalyticAxisDomain(Company company) throws AxelorException {
    if (company == null) {
      return "self.id IN (0)";
    }
    StringBuilder domain = new StringBuilder();
    domain
        .append("(self.company is null OR self.company.id = ")
        .append(company.getId())
        .append(")");
    AccountConfig accountConfig = accountConfigService.getAccountConfig(company);
    List<AnalyticAxis> analyticAxisList =
        accountConfig.getAnalyticAxisByCompanyList().stream()
            .map(AnalyticAxisByCompany::getAnalyticAxis)
            .collect(Collectors.toList());
    String idList = StringTool.getIdListString(analyticAxisList);
    domain.append(" AND self.id IN (").append(idList).append(")");
    return domain.toString();
  }
}
