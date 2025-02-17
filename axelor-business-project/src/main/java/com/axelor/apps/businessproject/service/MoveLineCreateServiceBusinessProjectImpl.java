package com.axelor.apps.businessproject.service;

import com.axelor.apps.account.db.Account;
import com.axelor.apps.account.db.Invoice;
import com.axelor.apps.account.db.InvoiceLine;
import com.axelor.apps.account.db.InvoiceLineTax;
import com.axelor.apps.account.db.InvoiceTerm;
import com.axelor.apps.account.db.Move;
import com.axelor.apps.account.db.MoveLine;
import com.axelor.apps.account.db.Tax;
import com.axelor.apps.account.db.repo.AccountingSituationRepository;
import com.axelor.apps.account.db.repo.InvoiceLineRepository;
import com.axelor.apps.account.exception.AccountExceptionMessage;
import com.axelor.apps.account.service.FiscalPositionAccountService;
import com.axelor.apps.account.service.TaxAccountService;
import com.axelor.apps.account.service.accountingsituation.AccountingSituationService;
import com.axelor.apps.account.service.analytic.AnalyticLineService;
import com.axelor.apps.account.service.analytic.AnalyticMoveLineGenerateRealService;
import com.axelor.apps.account.service.invoice.InvoiceTermService;
import com.axelor.apps.account.service.invoice.InvoiceToolService;
import com.axelor.apps.account.service.moveline.MoveLineComputeAnalyticService;
import com.axelor.apps.account.service.moveline.MoveLineConsolidateService;
import com.axelor.apps.account.service.moveline.MoveLineCreateServiceImpl;
import com.axelor.apps.account.service.moveline.MoveLineTaxService;
import com.axelor.apps.account.service.moveline.MoveLineToolService;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.Partner;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.apps.base.service.CurrencyScaleService;
import com.axelor.apps.base.service.CurrencyService;
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.apps.base.service.config.CompanyConfigService;
import com.axelor.apps.base.service.tax.FiscalPositionService;
import com.axelor.apps.base.service.tax.TaxService;
import com.axelor.apps.businessproject.db.ProjectHoldBackATI;
import com.axelor.i18n.I18n;
import com.google.common.collect.Sets;
import com.google.inject.Inject;
import java.lang.invoke.MethodHandles;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MoveLineCreateServiceBusinessProjectImpl extends MoveLineCreateServiceImpl {

  private final Logger log = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  @Inject
  public MoveLineCreateServiceBusinessProjectImpl(
      CompanyConfigService companyConfigService,
      CurrencyService currencyService,
      FiscalPositionAccountService fiscalPositionAccountService,
      AnalyticMoveLineGenerateRealService analyticMoveLineGenerateRealService,
      TaxAccountService taxAccountService,
      MoveLineToolService moveLineToolService,
      MoveLineComputeAnalyticService moveLineComputeAnalyticService,
      MoveLineConsolidateService moveLineConsolidateService,
      InvoiceTermService invoiceTermService,
      MoveLineTaxService moveLineTaxService,
      AccountingSituationRepository accountingSituationRepository,
      AccountingSituationService accountingSituationService,
      FiscalPositionService fiscalPositionService,
      TaxService taxService,
      AppBaseService appBaseService,
      AnalyticLineService analyticLineService,
      CurrencyScaleService currencyScaleService) {
    super(
        companyConfigService,
        currencyService,
        fiscalPositionAccountService,
        analyticMoveLineGenerateRealService,
        taxAccountService,
        moveLineToolService,
        moveLineComputeAnalyticService,
        moveLineConsolidateService,
        invoiceTermService,
        moveLineTaxService,
        accountingSituationRepository,
        accountingSituationService,
        fiscalPositionService,
        taxService,
        appBaseService,
        analyticLineService,
        currencyScaleService);
  }

  @Override
  public List<MoveLine> createMoveLines(
      Invoice invoice,
      Move move,
      Company company,
      Partner partner,
      Account partnerAccount,
      boolean consolidate,
      boolean isPurchase,
      boolean isDebitCustomer)
      throws AxelorException {
    log.debug("Creation of move lines of the invoice : {}", invoice.getInvoiceId());

    List<MoveLine> moveLines = new ArrayList<>();

    if (partner == null) {
      throw new AxelorException(
          invoice,
          TraceBackRepository.CATEGORY_MISSING_FIELD,
          I18n.get(AccountExceptionMessage.MOVE_LINE_1),
          invoice.getInvoiceId());
    }

    if (partnerAccount == null) {
      throw new AxelorException(
          invoice,
          TraceBackRepository.CATEGORY_MISSING_FIELD,
          I18n.get(AccountExceptionMessage.MOVE_LINE_2),
          invoice.getInvoiceId());
    }

    String origin =
        InvoiceToolService.isPurchase(invoice)
            ? invoice.getSupplierInvoiceNb()
            : invoice.getInvoiceId();

    if (partnerAccount.getUseForPartnerBalance()) {
      moveLines.addAll(
          addInvoiceTermMoveLines(invoice, partnerAccount, move, partner, isDebitCustomer, origin));
    } else {
      MoveLine moveLine =
          this.createMoveLine(
              move,
              partner,
              partnerAccount,
              invoice.getInTaxTotal().subtract(invoice.getHoldBacksTotal()),
              invoice.getCompanyInTaxTotal().subtract(invoice.getCompanyHoldBacksTotal()),
              null,
              isDebitCustomer,
              invoice.getInvoiceDate(),
              invoice.getDueDate(),
              invoice.getOriginDate(),
              1,
              origin,
              null);
      moveLines.add(moveLine);
    }

    int moveLineId = moveLines.size() + 1;

    // Creation of product move lines for each invoice line
    for (InvoiceLine invoiceLine :
        invoice.getInvoiceLineList().stream()
            .filter(invoiceLine -> invoiceLine.getTypeSelect() == InvoiceLineRepository.TYPE_NORMAL)
            .collect(Collectors.toList())) {
      BigDecimal companyExTaxTotal = invoiceLine.getCompanyExTaxTotal();

      if (companyExTaxTotal.compareTo(BigDecimal.ZERO) != 0) {
        Account account = invoiceLine.getAccount();

        if (account == null) {
          throw new AxelorException(
              move,
              TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
              I18n.get(AccountExceptionMessage.MOVE_LINE_4),
              invoiceLine.getName(),
              company.getName());
        }

        companyExTaxTotal = invoiceLine.getCompanyExTaxTotal();

        log.debug(
            "Processing of the invoice line : account = {}, amount = {}",
            new Object[] {account.getName(), companyExTaxTotal});

        if (invoiceLine.getAnalyticDistributionTemplate() == null
            && (invoiceLine.getAnalyticMoveLineList() == null
                || invoiceLine.getAnalyticMoveLineList().isEmpty())
            && account.getAnalyticDistributionAuthorized()
            && account.getAnalyticDistributionRequiredOnInvoiceLines()) {
          throw new AxelorException(
              move,
              TraceBackRepository.CATEGORY_MISSING_FIELD,
              I18n.get(AccountExceptionMessage.ANALYTIC_DISTRIBUTION_MISSING),
              invoiceLine.getName(),
              company.getName());
        }

        MoveLine moveLine =
            this.createMoveLine(
                move,
                partner,
                account,
                invoiceLine.getExTaxTotal(),
                companyExTaxTotal,
                null,
                !isDebitCustomer,
                invoice.getInvoiceDate(),
                null,
                invoice.getOriginDate(),
                moveLineId++,
                origin,
                invoiceLine.getProductName());

        moveLine = fillMoveLineWithInvoiceLine(moveLine, invoiceLine, move.getCompany());
        moveLines.add(moveLine);
      }
    }

    // Creation of tax move lines for each invoice line tax
    for (InvoiceLineTax invoiceLineTax : invoice.getInvoiceLineTaxList()) {
      if (invoiceLineTax.getCompanyTaxTotal().compareTo(BigDecimal.ZERO) != 0) {
        Account account = invoiceLineTax.getImputedAccount();
        Tax tax = invoiceLineTax.getTaxLine().getTax();

        if (account == null) {
          throw new AxelorException(
              move,
              TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
              I18n.get(AccountExceptionMessage.MOVE_LINE_6),
              tax.getName(),
              company.getName());
        }

        MoveLine moveLine =
            this.createMoveLine(
                move,
                partner,
                account,
                invoiceLineTax.getTaxTotal(),
                invoiceLineTax.getCompanyTaxTotal(),
                null,
                !isDebitCustomer,
                invoice.getInvoiceDate(),
                null,
                invoice.getOriginDate(),
                moveLineId++,
                origin,
                null);

        moveLine.setTaxLineSet(Sets.newHashSet(invoiceLineTax.getTaxLine()));
        moveLine.setTaxRate(invoiceLineTax.getTaxLine().getValue());
        moveLine.setTaxCode(tax.getCode());
        moveLine.setVatSystemSelect(invoiceLineTax.getVatSystemSelect());
        moveLineToolService.setIsNonDeductibleTax(moveLine, tax);
        moveLines.add(moveLine);
      }
    }

    // Creation of hold backs move lines
    for (ProjectHoldBackATI projectHoldBackATI : invoice.getProjectHoldBackATIList()) {
      if (projectHoldBackATI.getAmount().compareTo(BigDecimal.ZERO) != 0) {
        Account account = accountingSituationService.getPartnerAccount(invoice, true);

        MoveLine moveLine =
            this.createMoveLine(
                move,
                partner,
                account,
                projectHoldBackATI.getAmount().abs(),
                projectHoldBackATI.getCompanyAmount().abs(),
                null,
                isDebitCustomer,
                invoice.getInvoiceDate(),
                null,
                invoice.getOriginDate(),
                moveLineId++,
                origin,
                null);
        moveLines.add(moveLine);
      }
    }

    if (consolidate) {
      moveLineConsolidateService.consolidateMoveLines(moveLines);
    }

    return moveLines;
  }

  @Override
  protected List<MoveLine> addInvoiceTermMoveLines(
      Invoice invoice,
      Account partnerAccount,
      Move move,
      Partner partner,
      boolean isDebitCustomer,
      String origin)
      throws AxelorException {
    int moveLineId = 1;
    BigDecimal totalCompanyAmount = BigDecimal.ZERO;
    BigDecimal totalAmount = BigDecimal.ZERO;
    List<MoveLine> moveLines = new ArrayList<>();
    MoveLine moveLine = null;
    MoveLine holdBackMoveLine;
    LocalDate latestDueDate = invoiceTermService.getLatestInvoiceTermDueDate(invoice);
    BigDecimal companyAmount;
    BigDecimal amount;

    for (InvoiceTerm invoiceTerm : invoice.getInvoiceTermList()) {
      companyAmount =
          invoiceTerm.equals(
                  invoice.getInvoiceTermList().get(invoice.getInvoiceTermList().size() - 1))
              ? (invoice
                  .getCompanyInTaxTotal()
                  .subtract(totalCompanyAmount.add(invoice.getCompanyHoldBacksTotal().abs())))
              : invoiceTerm.getCompanyAmount();
      totalCompanyAmount = totalCompanyAmount.add(invoiceTerm.getCompanyAmount());

      amount =
          invoiceTerm.equals(
                  invoice.getInvoiceTermList().get(invoice.getInvoiceTermList().size() - 1))
              ? (invoice
                  .getInTaxTotal()
                  .subtract(totalAmount.add(invoice.getHoldBacksTotal().abs())))
              : invoiceTerm.getAmount();
      totalAmount = totalAmount.add(invoiceTerm.getAmount());

      Account account = partnerAccount;
      if (invoiceTerm.getIsHoldBack()) {
        account = accountingSituationService.getPartnerAccount(invoice, true);
        holdBackMoveLine =
            this.createMoveLine(
                move,
                partner,
                account,
                amount,
                companyAmount,
                null,
                isDebitCustomer,
                invoice.getInvoiceDate(),
                invoiceTerm.getDueDate(),
                invoice.getOriginDate(),
                moveLineId++,
                origin,
                null);
        holdBackMoveLine.addInvoiceTermListItem(invoiceTerm);
        moveLines.add(holdBackMoveLine);
      } else {
        if (moveLine == null) {
          moveLine =
              this.createMoveLine(
                  move,
                  partner,
                  account,
                  amount,
                  companyAmount,
                  null,
                  isDebitCustomer,
                  invoice.getInvoiceDate(),
                  latestDueDate,
                  invoice.getOriginDate(),
                  moveLineId++,
                  origin,
                  null);
        } else {
          if (moveLine.getDebit().compareTo(BigDecimal.ZERO) != 0) {
            // Debit
            BigDecimal currencyAmount = moveLine.getCurrencyAmount().add(amount);
            moveLine.setDebit(moveLine.getDebit().add(companyAmount));
            moveLine.setCurrencyAmount(currencyAmount);
          } else {
            // Credit
            BigDecimal currencyAmount = moveLine.getCurrencyAmount().subtract(amount);
            moveLine.setCredit(moveLine.getCredit().add(companyAmount));
            moveLine.setCurrencyAmount(currencyAmount);
          }
        }
      }
    }

    if (moveLine != null) {
      for (InvoiceTerm invoiceTerm : invoice.getInvoiceTermList()) {
        if (!invoiceTerm.getIsHoldBack()) {
          moveLine.addInvoiceTermListItem(invoiceTerm);
        }
      }

      moveLine.setDebit(currencyScaleService.getCompanyScaledValue(move, moveLine.getDebit()));
      moveLine.setCredit(currencyScaleService.getCompanyScaledValue(move, moveLine.getCredit()));

      moveLines.add(moveLine);
    }

    return moveLines;
  }
}
