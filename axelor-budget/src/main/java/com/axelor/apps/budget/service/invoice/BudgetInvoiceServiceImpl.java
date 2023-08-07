/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2005-2023 Axelor (<http://axelor.com>).
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
package com.axelor.apps.budget.service.invoice;

import com.axelor.apps.account.db.Invoice;
import com.axelor.apps.account.db.InvoiceLine;
import com.axelor.apps.account.db.repo.InvoiceLineRepository;
import com.axelor.apps.account.db.repo.InvoiceRepository;
import com.axelor.apps.account.service.app.AppAccountService;
import com.axelor.apps.account.service.config.AccountConfigService;
import com.axelor.apps.account.service.invoice.InvoiceLineService;
import com.axelor.apps.account.service.invoice.InvoiceTermPfpService;
import com.axelor.apps.account.service.invoice.InvoiceTermService;
import com.axelor.apps.account.service.invoice.factory.CancelFactory;
import com.axelor.apps.account.service.invoice.factory.ValidateFactory;
import com.axelor.apps.account.service.invoice.factory.VentilateFactory;
import com.axelor.apps.account.service.invoice.generator.invoice.RefundInvoice;
import com.axelor.apps.account.service.invoice.print.InvoiceProductStatementService;
import com.axelor.apps.account.service.move.MoveToolService;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Product;
import com.axelor.apps.base.service.PartnerService;
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.apps.base.service.tax.TaxService;
import com.axelor.apps.budget.db.Budget;
import com.axelor.apps.budget.db.BudgetDistribution;
import com.axelor.apps.budget.db.BudgetLine;
import com.axelor.apps.budget.db.repo.BudgetDistributionRepository;
import com.axelor.apps.budget.db.repo.BudgetRepository;
import com.axelor.apps.budget.service.AppBudgetService;
import com.axelor.apps.budget.service.BudgetDistributionService;
import com.axelor.apps.budget.service.BudgetLineService;
import com.axelor.apps.budget.service.BudgetService;
import com.axelor.apps.businessproject.service.InvoiceServiceProjectImpl;
import com.axelor.apps.stock.db.repo.StockMoveRepository;
import com.axelor.apps.supplychain.service.IntercoService;
import com.axelor.common.ObjectUtils;
import com.axelor.message.service.TemplateMessageService;
import com.axelor.meta.CallMethod;
import com.google.common.base.Strings;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.apache.commons.collections.CollectionUtils;

public class BudgetInvoiceServiceImpl extends InvoiceServiceProjectImpl
    implements BudgetInvoiceService {

  protected BudgetDistributionRepository budgetDistributionRepo;
  protected BudgetRepository budgetRepository;
  protected BudgetInvoiceLineService budgetInvoiceLineService;
  protected BudgetDistributionService budgetDistributionService;

  protected BudgetService budgetService;
  protected BudgetLineService budgetLineService;
  protected AppBudgetService appBudgetService;

  @Inject
  public BudgetInvoiceServiceImpl(
      ValidateFactory validateFactory,
      VentilateFactory ventilateFactory,
      CancelFactory cancelFactory,
      InvoiceRepository invoiceRepo,
      AppAccountService appAccountService,
      PartnerService partnerService,
      InvoiceLineService invoiceLineService,
      AccountConfigService accountConfigService,
      MoveToolService moveToolService,
      InvoiceLineRepository invoiceLineRepo,
      InvoiceTermService invoiceTermService,
      InvoiceTermPfpService invoiceTermPfpService,
      AppBaseService appBaseService,
      TemplateMessageService templateMessageService,
      IntercoService intercoService,
      TaxService taxService,
      InvoiceProductStatementService invoiceProductStatementService,
      StockMoveRepository stockMoveRepository,
      BudgetDistributionRepository budgetDistributionRepo,
      BudgetRepository budgetRepository,
      BudgetInvoiceLineService budgetInvoiceLineService,
      BudgetDistributionService budgetDistributionService,
      BudgetService budgetService,
      BudgetLineService budgetLineService,
      AppBudgetService appBudgetService) {
    super(
        validateFactory,
        ventilateFactory,
        cancelFactory,
        invoiceRepo,
        appAccountService,
        partnerService,
        invoiceLineService,
        accountConfigService,
        moveToolService,
        invoiceTermService,
        invoiceTermPfpService,
        appBaseService,
        taxService,
        invoiceProductStatementService,
        templateMessageService,
        invoiceLineRepo,
        intercoService,
        stockMoveRepository);
    this.budgetDistributionRepo = budgetDistributionRepo;
    this.budgetRepository = budgetRepository;
    this.budgetInvoiceLineService = budgetInvoiceLineService;
    this.budgetDistributionService = budgetDistributionService;
    this.budgetService = budgetService;
    this.budgetLineService = budgetLineService;
    this.appBudgetService = appBudgetService;
  }

  @Override
  @Transactional
  public String computeBudgetDistribution(Invoice invoice) {
    List<String> alertMessageTokenList = new ArrayList<>();
    if (!CollectionUtils.isEmpty(invoice.getInvoiceLineList())) {
      for (InvoiceLine invoiceLine : invoice.getInvoiceLineList()) {
        String alertMessage =
            budgetInvoiceLineService.computeBudgetDistribution(invoice, invoiceLine);
        if (Strings.isNullOrEmpty(alertMessage)) {
          invoice.setBudgetDistributionGenerated(true);
        } else {
          alertMessageTokenList.add(alertMessage);
        }
      }
      invoiceRepo.save(invoice);
    }
    return String.join(", ", alertMessageTokenList);
  }

  @Override
  @Transactional(rollbackOn = {Exception.class})
  public Invoice createRefund(Invoice invoice) throws AxelorException {

    Invoice refund = new RefundInvoice(invoice).generate();
    invoice.addRefundInvoiceListItem(refund);
    updateRefundBudgetDistribution(refund);
    invoiceRepo.save(invoice);

    return refund;
  }

  public void updateRefundBudgetDistribution(Invoice refund) {

    if (!CollectionUtils.isEmpty(refund.getInvoiceLineList())) {
      for (InvoiceLine invoiceLine : refund.getInvoiceLineList()) {
        if (CollectionUtils.isNotEmpty(invoiceLine.getBudgetDistributionList())) {
          invoiceLine.getBudgetDistributionList().stream()
              .forEach(
                  budgetDistribution -> {
                    budgetDistribution.setAmount(budgetDistribution.getAmount().negate());
                  });
        }
      }
    }
  }

  @Override
  @CallMethod
  public String getBudgetExceedAlert(Invoice invoice) {
    String budgetExceedAlert = "";

    List<InvoiceLine> invoiceLineList = invoice.getInvoiceLineList();

    if (appBudgetService.getAppBudget() != null
        && appBudgetService.getAppBudget().getCheckAvailableBudget()
        && invoice.getId() != null
        && CollectionUtils.isNotEmpty(invoiceLineList)) {

      Map<Budget, BigDecimal> amountPerBudgetMap = new HashMap<>();

      for (InvoiceLine invoiceLine : invoiceLineList) {
        if (appBudgetService.getAppBudget().getManageMultiBudget()
            && CollectionUtils.isNotEmpty(invoiceLine.getBudgetDistributionList())) {

          for (BudgetDistribution budgetDistribution : invoiceLine.getBudgetDistributionList()) {
            Budget budget = budgetDistribution.getBudget();

            if (!amountPerBudgetMap.containsKey(budget)) {
              amountPerBudgetMap.put(budget, budgetDistribution.getAmount());
            } else {
              BigDecimal oldAmount = amountPerBudgetMap.get(budget);

              amountPerBudgetMap.remove(budget);
              amountPerBudgetMap.put(budget, oldAmount.add(budgetDistribution.getAmount()));
            }
          }
          for (Map.Entry<Budget, BigDecimal> budgetEntry : amountPerBudgetMap.entrySet()) {
            budgetExceedAlert +=
                budgetDistributionService.getBudgetExceedAlert(
                    budgetEntry.getKey(),
                    budgetEntry.getValue(),
                    invoice.getInvoiceDate() != null
                        ? invoice.getInvoiceDate()
                        : invoice.getCreatedOn().toLocalDate());
          }
        } else {
          Budget budget = invoiceLine.getBudget();
          if (budget != null) {
            if (!amountPerBudgetMap.containsKey(budget)) {
              amountPerBudgetMap.put(budget, invoiceLine.getExTaxTotal());
            } else {
              BigDecimal oldAmount = amountPerBudgetMap.get(budget);
              amountPerBudgetMap.put(budget, oldAmount.add(invoiceLine.getExTaxTotal()));
            }

            budgetExceedAlert +=
                budgetDistributionService.getBudgetExceedAlert(
                    budget,
                    amountPerBudgetMap.get(budget),
                    invoice.getInvoiceDate() != null
                        ? invoice.getInvoiceDate()
                        : invoice.getCreatedOn().toLocalDate());
          }
        }
      }
    }
    return budgetExceedAlert;
  }

  @Override
  public boolean isBudgetInLines(Invoice invoice) {
    if (!CollectionUtils.isEmpty(invoice.getInvoiceLineList())) {
      for (InvoiceLine invoiceLine : invoice.getInvoiceLineList()) {
        if (invoiceLine.getBudget() != null
            || !CollectionUtils.isEmpty(invoiceLine.getBudgetDistributionList())) {
          return true;
        }
      }
    }
    return false;
  }

  @Override
  public void updateBudgetLinesFromInvoice(Invoice invoice) {
    List<InvoiceLine> invoiceLineList = invoice.getInvoiceLineList();

    if (CollectionUtils.isEmpty(invoiceLineList)) {
      return;
    }

    invoiceLineList.stream()
        .filter(invoiceLine -> !CollectionUtils.isEmpty(invoiceLine.getBudgetDistributionList()))
        .forEach(
            invoiceLine -> {
              updateLinesFromInvoice(invoiceLine.getBudgetDistributionList(), invoice, invoiceLine);
            });
  }

  @Transactional
  protected void updateLinesFromInvoice(
      List<BudgetDistribution> budgetDistributionList, Invoice invoice, InvoiceLine invoiceLine) {
    if (budgetDistributionList != null) {
      for (BudgetDistribution budgetDistribution : budgetDistributionList) {
        if (invoiceLine.getInvoice().getPurchaseOrder() != null
            || invoiceLine.getInvoice().getSaleOrder() != null) {
          updateLineWithPO(budgetDistribution, invoice, invoiceLine);
        } else {
          updateLineWithNoPO(budgetDistribution, invoice);
        }
        Budget budget = budgetDistribution.getBudget();
        if (budget != null) {
          budgetService.computeTotalAmountRealized(budget);
          budgetService.computeTotalFirmGap(budget);
          budgetService.computeTotalAmountCommitted(budget);
          budgetRepository.save(budget);
        }
      }
    }
  }

  @Override
  @Transactional
  public void updateLineWithNoPO(BudgetDistribution budgetDistribution, Invoice invoice) {
    if (budgetDistribution != null && budgetDistribution.getBudget() != null) {
      LocalDate date =
          invoice.getInvoiceDate() != null
              ? invoice.getInvoiceDate()
              : invoice.getCreatedOn().toLocalDate();
      Budget budget = budgetDistribution.getBudget();
      Optional<BudgetLine> optBudgetLine =
          budgetLineService.findBudgetLineAtDate(budget.getBudgetLineList(), date);
      if (optBudgetLine.isPresent()) {
        BudgetLine budgetLine = optBudgetLine.get();
        budgetLine.setRealizedWithNoPo(
            budgetLine.getRealizedWithNoPo().add(budgetDistribution.getAmount()));
        budgetLine.setAmountRealized(
            budgetLine.getAmountRealized().add(budgetDistribution.getAmount()));
        budgetLine.setToBeCommittedAmount(
            budgetLine.getToBeCommittedAmount().subtract(budgetDistribution.getAmount()));
        BigDecimal firmGap =
            budgetLine
                .getAmountExpected()
                .subtract(budgetLine.getRealizedWithPo().add(budgetLine.getRealizedWithNoPo()));
        budgetLine.setFirmGap(firmGap.signum() >= 0 ? BigDecimal.ZERO : firmGap.abs());

        budgetLine.setAvailableAmount(
            budgetLine
                        .getAvailableAmount()
                        .subtract(budgetDistribution.getAmount())
                        .compareTo(BigDecimal.ZERO)
                    > 0
                ? budgetLine.getAvailableAmount().subtract(budgetDistribution.getAmount())
                : BigDecimal.ZERO);
      }
    }
  }

  @Override
  @Transactional
  public void updateLineWithPO(
      BudgetDistribution budgetDistribution, Invoice invoice, InvoiceLine invoiceLine) {

    if (budgetDistribution != null && budgetDistribution.getBudget() != null) {
      LocalDate date = null;
      if (invoiceLine.getInvoice().getPurchaseOrder() != null
          && invoiceLine.getInvoice().getPurchaseOrder().getOrderDate() != null) {
        date = invoiceLine.getInvoice().getPurchaseOrder().getOrderDate();
      } else if (invoiceLine.getInvoice().getSaleOrder() != null) {
        date =
            invoiceLine.getInvoice().getSaleOrder().getOrderDate() != null
                ? invoiceLine.getInvoice().getSaleOrder().getOrderDate()
                : invoiceLine.getInvoice().getSaleOrder().getCreationDate();
      }
      Budget budget = budgetDistribution.getBudget();
      Optional<BudgetLine> optBudgetLine =
          budgetLineService.findBudgetLineAtDate(budget.getBudgetLineList(), date);
      if (optBudgetLine.isPresent()) {
        BudgetLine budgetLine = optBudgetLine.get();
        budgetLine.setRealizedWithPo(
            budgetLine.getRealizedWithPo().add(budgetDistribution.getAmount()));
        budgetLine.setAmountRealized(
            budgetLine.getAmountRealized().add(budgetDistribution.getAmount()));
        budgetLine.setAmountCommitted(
            budgetLine.getAmountCommitted().subtract(budgetDistribution.getAmount()));
        BigDecimal firmGap =
            budgetLine
                .getAmountExpected()
                .subtract(budgetLine.getRealizedWithPo().add(budgetLine.getRealizedWithNoPo()));
        budgetLine.setFirmGap(firmGap.signum() >= 0 ? BigDecimal.ZERO : firmGap.abs());
        budgetLine.setAvailableAmount(
            budgetLine
                        .getAvailableAmount()
                        .subtract(budgetDistribution.getAmount())
                        .compareTo(BigDecimal.ZERO)
                    > 0
                ? budgetLine.getAvailableAmount().subtract(budgetDistribution.getAmount())
                : BigDecimal.ZERO);
      }
    }
  }

  @Override
  public void generateBudgetDistribution(Invoice invoice) {
    if (invoice.getInvoiceLineList() != null) {
      for (InvoiceLine invoiceLine : invoice.getInvoiceLineList()) {
        if (invoiceLine.getBudget() != null
            && (ObjectUtils.isEmpty(invoiceLine.getBudgetDistributionList()))) {
          BudgetDistribution budgetDistribution = new BudgetDistribution();
          budgetDistribution.setBudget(invoiceLine.getBudget());
          budgetDistribution.setAmount(invoiceLine.getCompanyExTaxTotal());
          invoiceLine.addBudgetDistributionListItem(budgetDistribution);
        }
      }
    }
  }

  @Override
  public void setComputedBudgetLinesAmount(List<InvoiceLine> invoiceLineList) {
    invoiceLineList.forEach(invoiceLine -> computeBudgetLineAmount(invoiceLineList, invoiceLine));
  }

  protected void computeBudgetLineAmount(
      List<InvoiceLine> invoiceLineList, InvoiceLine invoiceLine) {

    Product product = invoiceLine.getProduct();

    if (invoiceLine != null && !CollectionUtils.isEmpty(invoiceLine.getBudgetDistributionList())) {
      invoiceLine
          .getBudgetDistributionList()
          .forEach(
              budgetDistribution ->
                  budgetDistribution.setAmount(
                      divideBudgetDistributionAmount(
                          budgetDistribution, product, invoiceLineList)));
    }
  }

  protected BigDecimal divideBudgetDistributionAmount(
      BudgetDistribution budgetDistribution, Product product, List<InvoiceLine> invoiceLineList) {
    return budgetDistribution
        .getAmount()
        .divide(
            new BigDecimal(
                countInvoiceLineWithSameProductAndBudget(
                    product, invoiceLineList, budgetDistribution.getBudget())),
            RoundingMode.HALF_UP);
  }

  protected long countInvoiceLineWithSameProductAndBudget(
      Product product, List<InvoiceLine> invoiceLineList, Budget budget) {
    return invoiceLineList.stream()
        .filter(
            invoiceLine ->
                product.equals(invoiceLine.getProduct()) && useSameBudget(budget, invoiceLine))
        .count();
  }

  protected boolean useSameBudget(Budget budget, InvoiceLine invoiceLine) {
    List<BudgetDistribution> budgetDistributionList = invoiceLine.getBudgetDistributionList();
    if (budgetDistributionList == null) {
      return false;
    }
    return budgetDistributionList.stream()
        .anyMatch(budgetDistribution -> budget.equals(budgetDistribution.getBudget()));
  }
}
