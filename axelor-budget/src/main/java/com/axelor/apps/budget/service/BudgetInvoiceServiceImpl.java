package com.axelor.apps.budget.service;

import com.axelor.apps.budget.db.Budget;
import com.axelor.apps.budget.db.BudgetDistribution;
import com.axelor.apps.account.db.Invoice;
import com.axelor.apps.account.db.InvoiceLine;
import com.axelor.apps.budget.db.repo.BudgetDistributionRepository;
import com.axelor.apps.budget.db.repo.BudgetRepository;
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
import com.axelor.apps.base.service.PartnerService;
import com.axelor.apps.base.service.alarm.AlarmEngineService;
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.apps.base.service.tax.TaxService;
import com.axelor.apps.cash.management.service.InvoiceEstimatedPaymentService;
import com.axelor.apps.cash.management.service.InvoiceServiceManagementImpl;
import com.axelor.apps.stock.db.repo.StockMoveRepository;
import com.axelor.apps.supplychain.service.IntercoService;
import com.axelor.message.service.TemplateMessageService;
import com.axelor.meta.CallMethod;
import com.google.common.base.Strings;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import com.google.inject.servlet.RequestScoped;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.collections.CollectionUtils;

@RequestScoped
public class BudgetInvoiceServiceImpl extends InvoiceServiceManagementImpl
    implements BudgetInvoiceService {

  protected BudgetDistributionRepository budgetDistributionRepo;
  protected BudgetRepository budgetRepository;
  protected BudgetInvoiceLineService budgetInvoiceLineService;
  protected BudgetBudgetDistributionService budgetDistributionService;

  @Inject
  public BudgetInvoiceServiceImpl(
      ValidateFactory validateFactory,
      VentilateFactory ventilateFactory,
      CancelFactory cancelFactory,
      AlarmEngineService<Invoice> alarmEngineService,
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
      InvoiceEstimatedPaymentService invoiceEstimatedPaymentService,
      BudgetDistributionRepository budgetDistributionRepo,
      BudgetRepository budgetRepository,
      BudgetInvoiceLineService budgetInvoiceLineService,
      BudgetBudgetDistributionService budgetDistributionService) {
    super(
        validateFactory,
        ventilateFactory,
        cancelFactory,
        alarmEngineService,
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
        stockMoveRepository,
        invoiceEstimatedPaymentService);
    this.budgetDistributionRepo = budgetDistributionRepo;
    this.budgetRepository = budgetRepository;
    this.budgetInvoiceLineService = budgetInvoiceLineService;
    this.budgetDistributionService = budgetDistributionService;
  }

  @Override
  @Transactional
  public String computeBudgetDistribution(Invoice invoice) {
    List<String> alertMessageTokenList = new ArrayList<>();
    if (!CollectionUtils.isEmpty(invoice.getInvoiceLineList())) {
      for (InvoiceLine invoiceLine : invoice.getInvoiceLineList()) {
        String alertMessage = budgetInvoiceLineService.computeBudgetDistribution(invoiceLine);
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

    if (appAccountService.isApp("budget")
        && appAccountService.getAppBudget().getCheckAvailableBudget()
        && invoice.getId() != null
        && CollectionUtils.isNotEmpty(invoiceLineList)
        && invoice.getOperationTypeSelect() < InvoiceRepository.OPERATION_TYPE_CLIENT_SALE) {

      Map<Budget, BigDecimal> amountPerBudgetMap = new HashMap<>();

      for (InvoiceLine invoiceLine : invoiceLineList) {
        if (appAccountService.getAppBudget().getManageMultiBudget()
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
}
