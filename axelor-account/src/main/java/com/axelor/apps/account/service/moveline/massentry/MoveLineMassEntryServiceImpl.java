package com.axelor.apps.account.service.moveline.massentry;

import com.axelor.apps.account.db.Account;
import com.axelor.apps.account.db.Move;
import com.axelor.apps.account.db.MoveLineMassEntry;
import com.axelor.apps.account.db.repo.MoveRepository;
import com.axelor.apps.account.service.invoice.InvoiceTermService;
import com.axelor.apps.account.service.move.MoveCounterPartService;
import com.axelor.apps.account.service.move.massentry.MassEntryToolService;
import com.axelor.apps.account.service.moveline.MoveLineTaxService;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.Currency;
import com.axelor.apps.base.db.Partner;
import com.axelor.apps.base.service.CurrencyService;
import com.axelor.auth.db.User;
import com.axelor.common.ObjectUtils;
import com.google.inject.Inject;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Objects;

public class MoveLineMassEntryServiceImpl implements MoveLineMassEntryService {

  protected MoveLineTaxService moveLineTaxService;
  protected MoveCounterPartService moveCounterPartService;
  protected MassEntryToolService massEntryToolService;
  protected CurrencyService currencyService;
  protected InvoiceTermService invoiceTermService;

  @Inject
  public MoveLineMassEntryServiceImpl(
      MoveLineTaxService moveLineTaxService,
      MoveCounterPartService moveCounterPartService,
      MassEntryToolService massEntryToolService,
      CurrencyService currencyService,
      InvoiceTermService invoiceTermService) {
    this.moveLineTaxService = moveLineTaxService;
    this.moveCounterPartService = moveCounterPartService;
    this.massEntryToolService = massEntryToolService;
    this.currencyService = currencyService;
    this.invoiceTermService = invoiceTermService;
  }

  @Override
  public void generateTaxLineAndCounterpart(
      Move parentMove, Move childMove, LocalDate dueDate, Integer temporaryMoveNumber)
      throws AxelorException {
    if (ObjectUtils.notEmpty(childMove.getMoveLineList())) {
      if (childMove.getStatusSelect().equals(MoveRepository.STATUS_NEW)
          || childMove.getStatusSelect().equals(MoveRepository.STATUS_SIMULATED)) {
        moveLineTaxService.autoTaxLineGenerateNoSave(childMove, null);
        moveCounterPartService.generateCounterpartMoveLine(childMove, dueDate);
      }
      massEntryToolService.clearMoveLineMassEntryListAndAddNewLines(
          parentMove, childMove, temporaryMoveNumber);
    }
  }

  @Override
  public BigDecimal computeCurrentRate(
      BigDecimal currencyRate,
      List<MoveLineMassEntry> moveLineList,
      Currency currency,
      Currency companyCurrency,
      Integer temporaryMoveNumber,
      LocalDate originDate)
      throws AxelorException {
    if (currency != null && companyCurrency != null && !currency.equals(companyCurrency)) {
      if (moveLineList.size() == 0) {
        if (originDate != null) {
          currencyRate =
              currencyService.getCurrencyConversionRate(currency, companyCurrency, originDate);
        } else {
          currencyRate = currencyService.getCurrencyConversionRate(currency, companyCurrency);
        }
      } else {
        if (moveLineList.stream()
            .anyMatch(it -> Objects.equals(it.getTemporaryMoveNumber(), temporaryMoveNumber))) {
          currencyRate =
              moveLineList.stream()
                  .filter(it -> Objects.equals(it.getTemporaryMoveNumber(), temporaryMoveNumber))
                  .findFirst()
                  .get()
                  .getCurrencyRate();
        }
      }
    }
    return currencyRate;
  }

  @Override
  public User getPfpValidatorUserForInTaxAccount(
      Account account, Company company, Partner partner) {
    if (ObjectUtils.notEmpty(account) && account.getUseForPartnerBalance()) {
      return invoiceTermService.getPfpValidatorUser(partner, company);
    }
    return null;
  }

  @Override
  public void setPfpValidatorUserForInTaxAccount(
      List<MoveLineMassEntry> moveLineMassEntryList, Company company, int temporaryMoveNumber) {
    for (MoveLineMassEntry moveLine : moveLineMassEntryList) {
      if (moveLine.getTemporaryMoveNumber() == temporaryMoveNumber
          && ObjectUtils.isEmpty(moveLine.getMovePfpValidatorUser())) {
        moveLine.setMovePfpValidatorUser(
            getPfpValidatorUserForInTaxAccount(
                moveLine.getAccount(), company, moveLine.getPartner()));
      }
    }
  }
}
