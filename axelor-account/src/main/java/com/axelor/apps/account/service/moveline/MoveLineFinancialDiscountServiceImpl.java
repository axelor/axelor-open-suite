package com.axelor.apps.account.service.moveline;

import com.axelor.apps.account.db.FinancialDiscount;
import com.axelor.apps.account.db.Move;
import com.axelor.apps.account.db.MoveLine;
import com.axelor.apps.account.db.repo.AccountTypeRepository;
import com.axelor.apps.account.service.FinancialDiscountService;
import com.axelor.apps.account.service.app.AppAccountService;
import com.axelor.apps.account.service.invoice.InvoiceTermFinancialDiscountService;
import com.axelor.apps.account.service.invoice.InvoiceTermService;
import com.google.inject.Inject;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collection;
import java.util.Optional;
import org.apache.commons.collections.CollectionUtils;

public class MoveLineFinancialDiscountServiceImpl implements MoveLineFinancialDiscountService {
  protected AppAccountService appAccountService;
  protected InvoiceTermService invoiceTermService;
  protected InvoiceTermFinancialDiscountService invoiceTermFinancialDiscountService;
  protected FinancialDiscountService financialDiscountService;

  @Inject
  public MoveLineFinancialDiscountServiceImpl(
      AppAccountService appAccountService,
      InvoiceTermService invoiceTermService,
      InvoiceTermFinancialDiscountService invoiceTermFinancialDiscountService,
      FinancialDiscountService financialDiscountService) {
    this.appAccountService = appAccountService;
    this.invoiceTermService = invoiceTermService;
    this.invoiceTermFinancialDiscountService = invoiceTermFinancialDiscountService;
    this.financialDiscountService = financialDiscountService;
  }

  @Override
  public LocalDate getFinancialDiscountDeadlineDate(MoveLine moveLine) {
    if (moveLine == null) {
      return null;
    }

    int discountDelay =
        Optional.of(moveLine)
            .map(MoveLine::getFinancialDiscount)
            .map(FinancialDiscount::getDiscountDelay)
            .orElse(0);

    LocalDate deadlineDate = moveLine.getDueDate().minusDays(discountDelay);

    return deadlineDate.isBefore(moveLine.getDate()) ? moveLine.getDate() : deadlineDate;
  }

  @Override
  public void computeFinancialDiscount(MoveLine moveLine) {
    if (!appAccountService.getAppAccount().getManageFinancialDiscount()) {
      return;
    }

    if (moveLine.getAccount() != null
        && moveLine.getAccount().getUseForPartnerBalance()
        && moveLine.getFinancialDiscount() != null) {
      FinancialDiscount financialDiscount = moveLine.getFinancialDiscount();
      BigDecimal amount = moveLine.getCurrencyAmount().abs();

      moveLine.setFinancialDiscountRate(financialDiscount.getDiscountRate());
      moveLine.setFinancialDiscountTotalAmount(
          this.computeFinancialDiscountTotalAmount(financialDiscount, moveLine, amount));
      moveLine.setRemainingAmountAfterFinDiscount(
          amount.subtract(moveLine.getFinancialDiscountTotalAmount()));
    } else {
      moveLine.setFinancialDiscount(null);
      moveLine.setFinancialDiscountRate(BigDecimal.ZERO);
      moveLine.setFinancialDiscountTotalAmount(BigDecimal.ZERO);
      moveLine.setRemainingAmountAfterFinDiscount(BigDecimal.ZERO);
    }

    this.computeInvoiceTermsFinancialDiscount(moveLine);
  }

  protected BigDecimal computeFinancialDiscountTotalAmount(
      FinancialDiscount financialDiscount, MoveLine moveLine, BigDecimal amount) {
    BigDecimal taxAmount =
        Optional.of(moveLine).map(MoveLine::getMove).map(Move::getMoveLineList).stream()
            .flatMap(Collection::stream)
            .filter(
                it ->
                    it.getAccount()
                        .getAccountType()
                        .getTechnicalTypeSelect()
                        .equals(AccountTypeRepository.TYPE_TAX))
            .map(MoveLine::getCurrencyAmount)
            .map(BigDecimal::abs)
            .findFirst()
            .orElse(BigDecimal.ZERO);

    return financialDiscountService.computeFinancialDiscountTotalAmount(
        financialDiscount, amount, taxAmount);
  }

  protected void computeInvoiceTermsFinancialDiscount(MoveLine moveLine) {
    if (CollectionUtils.isNotEmpty(moveLine.getInvoiceTermList())) {
      moveLine.getInvoiceTermList().stream()
          .filter(it -> !it.getIsPaid() && it.getAmountRemaining().compareTo(it.getAmount()) == 0)
          .forEach(
              it ->
                  invoiceTermFinancialDiscountService.computeFinancialDiscount(
                      it,
                      moveLine.getCredit().max(moveLine.getDebit()),
                      moveLine.getFinancialDiscount(),
                      moveLine.getFinancialDiscountTotalAmount(),
                      moveLine.getRemainingAmountAfterFinDiscount()));
    }
  }
}
