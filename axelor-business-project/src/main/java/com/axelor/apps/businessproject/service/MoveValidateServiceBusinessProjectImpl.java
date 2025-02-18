package com.axelor.apps.businessproject.service;

import com.axelor.apps.account.db.InvoiceTerm;
import com.axelor.apps.account.db.Move;
import com.axelor.apps.account.db.MoveLine;
import com.axelor.apps.account.db.repo.AccountRepository;
import com.axelor.apps.account.db.repo.MoveRepository;
import com.axelor.apps.account.exception.AccountExceptionMessage;
import com.axelor.apps.account.service.TaxAccountService;
import com.axelor.apps.account.service.app.AppAccountService;
import com.axelor.apps.account.service.config.AccountConfigService;
import com.axelor.apps.account.service.fixedasset.FixedAssetGenerationService;
import com.axelor.apps.account.service.move.MoveControlService;
import com.axelor.apps.account.service.move.MoveCustAccountService;
import com.axelor.apps.account.service.move.MoveCutOffService;
import com.axelor.apps.account.service.move.MoveInvoiceTermService;
import com.axelor.apps.account.service.move.MoveLineControlService;
import com.axelor.apps.account.service.move.MoveSequenceService;
import com.axelor.apps.account.service.move.MoveToolService;
import com.axelor.apps.account.service.moveline.MoveLineCheckService;
import com.axelor.apps.account.service.moveline.MoveLineFinancialDiscountService;
import com.axelor.apps.account.service.moveline.MoveLineTaxService;
import com.axelor.apps.account.service.moveline.MoveLineToolService;
import com.axelor.apps.account.service.period.PeriodCheckService;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.repo.PartnerRepository;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.apps.base.service.CurrencyScaleService;
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.apps.base.service.config.CompanyConfigService;
import com.axelor.apps.base.service.user.UserService;
import com.axelor.apps.hr.db.repo.ExpenseRepository;
import com.axelor.apps.hr.service.move.MoveValidateHRServiceImpl;
import com.axelor.i18n.I18n;
import com.google.inject.Inject;
import java.lang.invoke.MethodHandles;
import java.math.BigDecimal;
import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MoveValidateServiceBusinessProjectImpl extends MoveValidateHRServiceImpl {

  private final Logger log = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  @Inject
  public MoveValidateServiceBusinessProjectImpl(
      MoveLineControlService moveLineControlService,
      MoveLineToolService moveLineToolService,
      AccountConfigService accountConfigService,
      MoveSequenceService moveSequenceService,
      MoveCustAccountService moveCustAccountService,
      MoveToolService moveToolService,
      MoveInvoiceTermService moveInvoiceTermService,
      MoveRepository moveRepository,
      AccountRepository accountRepository,
      PartnerRepository partnerRepository,
      AppBaseService appBaseService,
      AppAccountService appAccountService,
      FixedAssetGenerationService fixedAssetGenerationService,
      MoveLineTaxService moveLineTaxService,
      PeriodCheckService periodCheckService,
      MoveControlService moveControlService,
      MoveCutOffService moveCutOffService,
      MoveLineCheckService moveLineCheckService,
      CompanyConfigService companyConfigService,
      CurrencyScaleService currencyScaleService,
      MoveLineFinancialDiscountService moveLineFinancialDiscountService,
      ExpenseRepository expenseRepository,
      TaxAccountService taxAccountService,
      UserService userService) {
    super(
        moveLineControlService,
        moveLineToolService,
        accountConfigService,
        moveSequenceService,
        moveCustAccountService,
        moveToolService,
        moveInvoiceTermService,
        moveRepository,
        accountRepository,
        partnerRepository,
        appBaseService,
        appAccountService,
        fixedAssetGenerationService,
        moveLineTaxService,
        periodCheckService,
        moveControlService,
        moveCutOffService,
        moveLineCheckService,
        companyConfigService,
        currencyScaleService,
        moveLineFinancialDiscountService,
        expenseRepository,
        taxAccountService,
        userService);
  }

  @Override
  protected void checkMoveLineInvoiceTermBalance(Move move) throws AxelorException {

    log.debug(
        "Well-balanced move line invoice terms validation on account move {}", move.getReference());

    for (MoveLine moveLine : move.getMoveLineList()) {
      if (CollectionUtils.isEmpty(moveLine.getInvoiceTermList())
          || !moveLine.getAccount().getUseForPartnerBalance()) {
        return;
      }
      BigDecimal totalMoveLineInvoiceTerm =
          moveLine.getInvoiceTermList().stream()
              .map(InvoiceTerm::getCompanyAmount)
              .reduce(BigDecimal.ZERO, BigDecimal::add);
      totalMoveLineInvoiceTerm =
          totalMoveLineInvoiceTerm.add(move.getInvoice().getCompanyHoldBacksTotal());
      if (totalMoveLineInvoiceTerm.compareTo(moveLine.getDebit().max(moveLine.getCredit())) != 0) {
        throw new AxelorException(
            move,
            TraceBackRepository.CATEGORY_INCONSISTENCY,
            I18n.get(AccountExceptionMessage.MOVE_LINE_INVOICE_TERM_SUM_COMPANY_AMOUNT),
            moveLine.getName());
      }
    }
  }
}
